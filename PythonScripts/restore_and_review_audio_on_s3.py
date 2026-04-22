#!/usr/bin/env python3
"""
Script to check S3 storage classes for audio files, restore from Glacier if needed,
and download files to a local directory, optionally converting FLAC back to WAV.

The --files argument expects a text file with one S3 key per line, matching the
format written by upload_existing_audio_files_to_s3.py.
"""

import argparse
import os
import sys
import boto3
from botocore.exceptions import ClientError
import soundfile as sf


def parse_arguments():
    parser = argparse.ArgumentParser(
        description="Check S3 storage classes for audio files, restore from Glacier if needed, and download to a local directory"
    )

    parser.add_argument(
        '--files',
        type=str,
        required=True,
        help='Text file with one S3 key per line (e.g. the uploaded files list from upload_existing_audio_files_to_s3.py)'
    )

    parser.add_argument(
        '--bucket',
        type=str,
        required=True,
        help='S3 bucket name'
    )

    parser.add_argument(
        '--output-dir',
        type=str,
        required=True,
        help='Local directory to download audio files into'
    )

    parser.add_argument(
        '--restore-days',
        type=int,
        default=7,
        help='Number of days to keep files accessible after Glacier restore (default: 7)'
    )

    parser.add_argument(
        '--restore-tier',
        type=str,
        choices=['Expedited', 'Standard', 'Bulk'],
        default='Standard',
        help='Restore tier for Glacier retrieval (default: Standard)'
    )

    parser.add_argument(
        '--convert-to-wav',
        action='store_true',
        default=False,
        help='Convert downloaded FLAC files back to WAV format after downloading'
    )

    return parser.parse_args()


def read_filenames(files_path):
    """Read S3 keys from a text file, one per line."""
    filenames = []
    try:
        with open(files_path, 'r') as f:
            for line in f:
                cleaned = line.strip()
                if cleaned:
                    filenames.append(cleaned)
    except Exception as e:
        print(f"Error reading files list: {e}")
        sys.exit(1)
    return filenames


def check_storage_class(s3_client, bucket, filename):
    """Check the storage class of an S3 object."""
    try:
        response = s3_client.head_object(Bucket=bucket, Key=filename)
        storage_class = response.get('StorageClass', 'STANDARD')

        restore_status = response.get('Restore')
        is_restoring = restore_status and 'ongoing-request="true"' in restore_status
        is_restored = restore_status and 'ongoing-request="false"' in restore_status

        return {
            'exists': True,
            'storage_class': storage_class,
            'is_restoring': is_restoring,
            'is_restored': is_restored,
        }
    except ClientError as e:
        if e.response['Error']['Code'] == '404':
            return {'exists': False, 'storage_class': None}
        else:
            raise


def restore_from_glacier(s3_client, bucket, filename, days, tier):
    """Initiate restore from Glacier."""
    try:
        s3_client.restore_object(
            Bucket=bucket,
            Key=filename,
            RestoreRequest={
                'Days': days,
                'GlacierJobParameters': {'Tier': tier}
            }
        )
        return True
    except ClientError as e:
        if e.response['Error']['Code'] == 'RestoreAlreadyInProgress':
            print(f"  Restore already in progress for {filename}")
            return True
        else:
            print(f"  Error restoring {filename}: {e}")
            return False


def download_and_optionally_convert(s3_client, bucket, s3_key, output_dir, convert_to_wav):
    """Download a file from S3, optionally converting FLAC to WAV."""
    local_filename = os.path.basename(s3_key)
    local_path = os.path.join(output_dir, local_filename)

    try:
        print(f"  Downloading {s3_key}...")
        s3_client.download_file(bucket, s3_key, local_path)

        if convert_to_wav and local_filename.lower().endswith('.flac'):
            wav_filename = os.path.splitext(local_filename)[0] + '.wav'
            wav_path = os.path.join(output_dir, wav_filename)
            print(f"  Converting {local_filename} to WAV...")
            data, samplerate = sf.read(local_path)
            sf.write(wav_path, data, samplerate, format='WAV')
            os.remove(local_path)
            print(f"  Saved {wav_filename}")
        else:
            print(f"  Saved {local_filename}")

        return True

    except ClientError as e:
        print(f"  Error downloading {s3_key}: {e}")
        return False
    except Exception as e:
        print(f"  Error processing {s3_key}: {e}")
        return False


def main():
    args = parse_arguments()

    filenames = read_filenames(args.files)
    if not filenames:
        print("No filenames found in files list.")
        sys.exit(1)

    os.makedirs(args.output_dir, exist_ok=True)

    print(f"Processing {len(filenames)} files from bucket '{args.bucket}'")

    s3_client = boto3.client('s3')

    print("\nChecking storage classes...")
    storage_info = {}
    glacier_files = []
    available_files = []
    missing_files = []

    for filename in filenames:
        info = check_storage_class(s3_client, args.bucket, filename)
        storage_info[filename] = info

        if not info['exists']:
            missing_files.append(filename)
            print(f"  {filename}: NOT FOUND")
        elif info['storage_class'] in ['GLACIER', 'DEEP_ARCHIVE']:
            if info['is_restored']:
                available_files.append(filename)
                print(f"  {filename}: {info['storage_class']} (restored)")
            else:
                glacier_files.append(filename)
                status = "RESTORING" if info['is_restoring'] else info['storage_class']
                print(f"  {filename}: {status}")
        else:
            available_files.append(filename)
            print(f"  {filename}: {info['storage_class']}")

    if missing_files:
        print(f"\nWarning: {len(missing_files)} files not found in bucket")

    if glacier_files:
        print(f"\n{len(glacier_files)} files are in Glacier storage.")
        response = input(f"Do you want to restore them? (Tier: {args.restore_tier}, Days: {args.restore_days}) [y/N]: ")

        if response.lower() in ['y', 'yes']:
            print("\nInitiating restore...")
            restored_count = 0
            for filename in glacier_files:
                if not storage_info[filename]['is_restoring'] and not storage_info[filename]['is_restored']:
                    if restore_from_glacier(s3_client, args.bucket, filename, args.restore_days, args.restore_tier):
                        restored_count += 1
                        print(f"  Restore initiated for {filename}")
                else:
                    print(f"  {filename} already restoring/restored")

            print(f"\nRestore initiated for {restored_count} files.")
            print(f"Files will be available for {args.restore_days} days once restored.")
        else:
            print("Restore cancelled.")

    if available_files:
        print(f"\n{len(available_files)} files available for download.")
        if glacier_files:
            print(f"{len(glacier_files)} files in Glacier will be skipped.")

        convert_note = " and convert to WAV" if args.convert_to_wav else ""
        response = input(f"\nDownload{convert_note} {len(available_files)} files to '{args.output_dir}'? [y/N]: ")

        if response.lower() not in ['y', 'yes']:
            print("Download cancelled.")
            sys.exit(0)

        downloaded = 0
        for filename in available_files:
            if download_and_optionally_convert(s3_client, args.bucket, filename, args.output_dir, args.convert_to_wav):
                downloaded += 1

        print(f"\nSuccessfully downloaded {downloaded} of {len(available_files)} files to '{args.output_dir}'.")
        sys.exit(0 if downloaded == len(available_files) else 1)
    else:
        print("\nNo files available for download.")
        sys.exit(1)


if __name__ == '__main__':
    main()
