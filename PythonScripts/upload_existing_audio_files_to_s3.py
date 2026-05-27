import argparse
import boto3
from pathlib import Path
import os
import soundfile as sf


def upload_wav_files_to_s3(
    path_to_project_dir: Path | str,
    bucket_name: str,
    extra_args: dict[str, str] | None = None
) -> None:
    path_to_project_dir = Path(path_to_project_dir)

    session = boto3.Session(profile_name="default")
    s3_client = session.client("s3")

    # Check bucket for already-uploaded files instead of maintaining a local tracking file
    print(f"Listing existing files in s3 bucket {bucket_name}...")
    existing_s3_files = set()
    paginator = s3_client.get_paginator('list_objects_v2')
    for page in paginator.paginate(Bucket=bucket_name):
        for obj in page.get('Contents', []):
            existing_s3_files.add(obj['Key'])
    print(f"Found {len(existing_s3_files)} existing files in bucket.")

    # Find deployment dirs and corresponding search paths.
    # Case A: supplied dir contains AM subdirectories.
    # Case B: no AM subdirs — find the nearest AM-named ancestor and treat the
    #         supplied dir as the search root under that deployment.
    am_subdirs = sorted(
        item for item in os.listdir(path_to_project_dir)
        if os.path.isdir(path_to_project_dir / item) and item.startswith("AM")
    )

    if am_subdirs:
        deployments = [(name, path_to_project_dir / name) for name in am_subdirs]
    else:
        am_ancestor = None
        if path_to_project_dir.name.startswith("AM"):
            am_ancestor = path_to_project_dir
        else:
            for parent in path_to_project_dir.parents:
                if parent.name.startswith("AM"):
                    am_ancestor = parent
                    break

        if am_ancestor is None:
            print("Error: no AM directories found in the supplied path or any ancestor.")
            exit()

        print(f"No AM subdirectories found; using AM ancestor '{am_ancestor.name}' as deployment dir.")
        deployments = [(am_ancestor.name, path_to_project_dir)]

    # Collect WAV files to upload
    all_files_to_upload = []
    destination_filenames = []
    number_of_files_already_uploaded = 0

    for deployment_dir_name, search_path in deployments:
        direct_files = sorted(search_path.glob("*.[Ww][Aa][Vv]"))
        if direct_files:
            print(f"Found {len(direct_files)} files directly in {search_path.name}")
        for filepath in direct_files:
            dest = f"{deployment_dir_name}_{filepath.stem}.flac"
            if dest in existing_s3_files:
                number_of_files_already_uploaded += 1
            else:
                all_files_to_upload.append(filepath)
                destination_filenames.append(dest)

        for subdir in sorted(p for p in search_path.iterdir() if p.is_dir()):
            sub_files = sorted(subdir.glob("*.[Ww][Aa][Vv]"))
            if sub_files:
                print(f"Found {len(sub_files)} files in sub-directory {subdir.name}")
            for filepath in sub_files:
                dest = f"{deployment_dir_name}_{filepath.stem}.flac"
                if dest in existing_s3_files:
                    number_of_files_already_uploaded += 1
                else:
                    all_files_to_upload.append(filepath)
                    destination_filenames.append(dest)

    print(f"\n{number_of_files_already_uploaded} files already in s3 bucket.")
    print(f"Will upload {len(all_files_to_upload)} files to {bucket_name}.")

    if all_files_to_upload:
        print(f"\nExample uploads:")
        print(f"{all_files_to_upload[0]} -> {destination_filenames[0]}")
        if len(all_files_to_upload) > 1:
            print(f"{all_files_to_upload[1]} -> {destination_filenames[1]}")
        print("...etc")

    response = input(
        f"\nDo you want to convert WAV to FLAC and start upload of {len(all_files_to_upload)} files? (y/n): "
    ).strip().lower()

    if response == 'n':
        print("Exiting script.")
        exit()
    elif response != 'y':
        print("Invalid input. Exiting script.")
        exit()

    files_uploaded = []
    for wav_file, destination_filename in zip(all_files_to_upload, destination_filenames):
        temp_flac_path = f"{wav_file}.temp.flac"
        try:
            print(f"Converting {wav_file.name} to FLAC...")
            data, samplerate = sf.read(wav_file)
            sf.write(temp_flac_path, data, samplerate, format="FLAC")

            print(f"Uploading {destination_filename}...")
            s3_client.upload_file(temp_flac_path, bucket_name, destination_filename, ExtraArgs=extra_args or {})
            print(f"Upload successful: {destination_filename}")
            files_uploaded.append(destination_filename)

        except Exception as e:
            print(f"Error processing/uploading {destination_filename}:", e)
            raise
        finally:
            if os.path.exists(temp_flac_path):
                os.remove(temp_flac_path)

    print(f"\nSuccessfully uploaded {len(files_uploaded)} files.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Convert WAV files to FLAC and upload to S3. Accepts a project directory "
                    "containing AM deployment subdirectories, or a path inside an AM directory."
    )
    parser.add_argument(
        "--path-to-project-directory",
        required=True,
        help="path to folder containing AM deployment directories, or a subdirectory within an AM folder",
    )
    parser.add_argument(
        "--bucket-name",
        required=True,
        help="name of s3 bucket to upload files to",
    )
    extra_args = {"StorageClass": "DEEP_ARCHIVE"}
    args = parser.parse_args()
    upload_wav_files_to_s3(
        path_to_project_dir=args.path_to_project_directory,
        bucket_name=args.bucket_name,
        extra_args=extra_args
    )
