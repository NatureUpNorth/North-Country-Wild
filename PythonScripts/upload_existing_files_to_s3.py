import argparse
import boto3
import glob
from pathlib import Path
import os
from upload_and_process_images import get_timestamp_code_for_filename


def upload_files_to_s3(path_to_year_dir: Path | str, bucket_name: str, extra_args: dict[str, str] | None = None) -> None:
    # Create a session using the specified profile
    session = boto3.Session(profile_name="default")
    s3_client = session.client("s3")

    raw_files_dir = f"{path_to_year_dir}/Raw"
    # Get files in raw directory
    camera_sd_folders = [item for item in os.listdir(raw_files_dir) if os.path.isdir(os.path.join(raw_files_dir, item))]

    all_files_to_upload = []
    destination_filenames = []
    for camera_sd_folder in camera_sd_folders:
        camera_sd_path = os.path.join(raw_files_dir, camera_sd_folder)
        files = glob.glob(f"{camera_sd_path}/*.JPG")
        camera_code = camera_sd_folder[:4]
        sd_code = camera_sd_folder[5:10]
        for filepath in files:
            all_files_to_upload.append(filepath)
            timestamp_code = get_timestamp_code_for_filename(filepath)
            filename = os.path.basename(filepath)
            filename_with_prefix = f"{camera_code}_{sd_code}_{timestamp_code}_{filename}"
            destination_filenames.append(filename_with_prefix)
        print(f"Added {len(list(files))} from folder {camera_sd_folder}")

    print(f"Will upload {len(all_files_to_upload)} files to {bucket_name} s3 bucket.")
    print(f"Example uploads:\n{all_files_to_upload[0]} -> {destination_filenames[0]}\n{all_files_to_upload[1]} -> {destination_filenames[1]}\n...etc")
    response = input(f"Do you want to start upload of {len(all_files_to_upload)} files? (y/n): ").strip().lower()

    if response == 'n':
        print("Exiting script.")
        exit()  # or use sys.exit() if you prefer
    elif response != 'y':
        print("Invalid input. Exiting script.")
        exit()

    for file_to_upload, destination_filename in zip(all_files_to_upload, destination_filenames):
        # Upload the file to the specified bucket and key
        try:
            s3_client.upload_file(str(file_to_upload), bucket_name, destination_filename, ExtraArgs=extra_args or {})
            print("Upload successful!")
        except Exception as e:
            print("Error uploading file:", e)
            raise


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--path-to-year-directory",
        help="full path to academic year folder",  # noqa: E501
    )
    parser.add_argument(
        "--bucket-name",
        help="path to file to be uploaded",  # noqa: E501
    )
    #extra_args = {"StorageClass": "DEEP_ARCHIVE"}
    extra_args = None
    args = parser.parse_args()
    upload_files_to_s3(path_to_year_dir=args.path_to_year_directory, bucket_name=args.bucket_name, extra_args=extra_args)
