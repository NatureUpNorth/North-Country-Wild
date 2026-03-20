import argparse
import boto3
import glob
from pathlib import Path
import os
import soundfile as sf


def upload_wav_files_to_s3(
    path_to_project_dir: Path | str,
    path_to_uploaded_file: str,
    bucket_name: str,
    extra_args: dict[str, str] | None = None
) -> None:
    # Get uploaded files
    uploaded_files = []
    with open(path_to_uploaded_file, 'r') as file:
        for line in file:
            cleaned_line = line.strip()
            if cleaned_line:  # Avoid adding empty strings
                uploaded_files.append(cleaned_line)

    # Create a session using the specified profile
    session = boto3.Session(profile_name="default")
    s3_client = session.client("s3")

    # Get deployment directories
    deployment_dirs = [
        item for item in os.listdir(path_to_project_dir)
        if os.path.isdir(os.path.join(path_to_project_dir, item))
        and item.startswith("AM")
    ]

    all_files_to_upload = []
    destination_filenames = []
    number_of_files_already_uploaded = 0
    
    for deployment_dir in deployment_dirs:
        deployment_path = os.path.join(path_to_project_dir, deployment_dir)

        sub_deployment_dirs = [
            item for item in os.listdir(deployment_path)
            if os.path.isdir(os.path.join(deployment_path, item))
            ]

        # Check for WAV files directly in the deployment directory
        direct_files = glob.glob(f"{deployment_path}/*.[Ww][Aa][Vv]")
        for filepath in direct_files:
            filename = os.path.basename(filepath)
            base_name = os.path.splitext(filename)[0]
            destination_filename = f"{deployment_dir}_{base_name}.flac"

            if destination_filename in uploaded_files:
                number_of_files_already_uploaded += 1
                continue

            all_files_to_upload.append(filepath)
            destination_filenames.append(destination_filename)

        if direct_files:
            print(f"Found {len(direct_files)} files directly in deployment dir {deployment_dir}")

        for sub_deployment_dir in sub_deployment_dirs:
            sub_deployment_path = os.path.join(deployment_path, sub_deployment_dir)
            files = glob.glob(f"{sub_deployment_path}/*.[Ww][Aa][Vv]")
            for filepath in files:
                filename = os.path.basename(filepath)
                base_name = os.path.splitext(filename)[0]
                destination_filename = f"{deployment_dir}_{base_name}.flac"

                if destination_filename in uploaded_files:
                    number_of_files_already_uploaded += 1
                    continue

                all_files_to_upload.append(filepath)
                destination_filenames.append(destination_filename)

            print(f"Found {len(files)} files in sub-deployment {sub_deployment_dir}")

    print(f"\n{number_of_files_already_uploaded} files already uploaded to s3.")
    print(f"Will upload {len(all_files_to_upload)} files to {bucket_name} s3 bucket.")
    
    if len(all_files_to_upload) > 0:
        print(f"\nExample uploads:")
        print(f"{all_files_to_upload[0]} -> {destination_filenames[0]}")
        if len(all_files_to_upload) > 1:
            print(f"{all_files_to_upload[1]} -> {destination_filenames[1]}")
        print("...etc")
    
    response = input(f"\nDo you want to convert WAV to FLAC and then start upload of {len(all_files_to_upload)} files? (y/n): ").strip().lower()

    if response == 'n':
        print("Exiting script.")
        exit()
    elif response != 'y':
        print("Invalid input. Exiting script.")
        exit()

    files_uploaded = []
    for wav_file, destination_filename in zip(all_files_to_upload, destination_filenames):
        # Convert WAV to FLAC
        temp_flac_path = f"{wav_file}.temp.flac"
        try:
            print(f"Converting {os.path.basename(wav_file)} to FLAC...")
            data, samplerate = sf.read(wav_file)
            sf.write(temp_flac_path, data, samplerate, format="FLAC")
            
            # Upload the FLAC file to S3
            print(f"Uploading {destination_filename}...")
            s3_client.upload_file(temp_flac_path, bucket_name, destination_filename, ExtraArgs=extra_args or {})
            print(f"Upload successful: {destination_filename}")
            files_uploaded.append(destination_filename)
            
        except Exception as e:
            print(f"Error processing/uploading file {destination_filename}:", e)
            raise
        finally:
            # Clean up temporary FLAC file
            if os.path.exists(temp_flac_path):
                os.remove(temp_flac_path)
                print(f"Cleaned up temporary FLAC file")

    print(f"\nSuccessfully uploaded {len(files_uploaded)} files.")
    response2 = input(f"Do you want to append to uploaded files file? (y/n) If not, writes to uploaded_wav_files.txt: ").strip().lower()

    if response2 == 'y':
        print("Appending to uploaded files file.")
        with open(path_to_uploaded_file, "a") as f:
            for file in files_uploaded:
                f.write(f"{file}\n")
    else:
        print("Writing to uploaded_wav_files.txt")
        with open("uploaded_wav_files.txt", "w") as f:
            for file in files_uploaded:
                f.write(f"{file}\n")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--path-to-project-directory",
        help="full path to project folder containing deployment directories",
    )
    parser.add_argument(
        "--path-to-uploaded-files-file",
        help="file that has record of files that have been uploaded to s3",
        default="../misc_files/wav_files_uploaded_to_s3.txt",
    )
    parser.add_argument(
        "--bucket-name",
        help="name of s3 bucket where file will be uploaded to",
    )
    extra_args = {"StorageClass": "DEEP_ARCHIVE"}
    args = parser.parse_args()
    upload_wav_files_to_s3(
        path_to_project_dir=args.path_to_project_directory,
        path_to_uploaded_file=args.path_to_uploaded_files_file,
        bucket_name=args.bucket_name,
        extra_args=extra_args
    )
