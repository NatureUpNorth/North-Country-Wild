import argparse
import boto3
import glob
from pathlib import Path
import os
from upload_and_process_images import get_timestamp_code_for_filename


def rename_and_upload_files_to_s3(path_to_raw_directory: Path | str, bucket_name: str, path_to_uploaded_file:str, camera_number: str | None = None, sd_card_number: str | None = None, extra_args: dict[str, str] | None = None) -> None:
    # Create a session using the specified profile
    session = boto3.Session(profile_name="default")
    s3_client = session.client("s3")

    # Get list of camera-sd folders to process
    if camera_number is None and sd_card_number is None:
        print("No camera or sd card number provided. Will check for all folders in raw directory.")
        camera_sd_folders = [item for item in os.listdir(path_to_raw_directory) if os.path.isdir(os.path.join(path_to_raw_directory, item))]
    elif camera_number is None or sd_card_number is None:
        print("Camera OR sd card number not provided. Exiting script.")
        exit()
    else:
        camera_number_with_leading_zeros = camera_number.zfill(3)
        sd_card_number_with_leading_zeros = sd_card_number.zfill(3)
        camera_sd_folders = [f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}"]

    # Get uploaded files
    uploaded_files = []
    with open(path_to_uploaded_file, 'r') as file:
        for line in file:
            cleaned_line = line.strip()
            if cleaned_line:  # Avoid adding empty strings
                uploaded_files.append(cleaned_line)
   
    raw_files_dir = f"{path_to_raw_directory}"
    all_files_to_upload = []
    destination_filenames = []
    number_of_files_already_uploaded = 0
    for camera_sd_folder in camera_sd_folders:
        camera_sd_path = os.path.join(raw_files_dir, camera_sd_folder)
        files = glob.glob(f"{camera_sd_path}/*.jpg") + glob.glob(f"{camera_sd_path}/*.JPG")
        print(f"Found {len(files)} files in {camera_sd_path}")

        # We redefine here in case the number wasn't supplied as argument
        camera_code = camera_sd_folder[:4]
        sd_code = camera_sd_folder[5:10]

        # Rename first
        new_filepaths = []
        for filepath in files:
            filename = os.path.basename(filepath)
            timestamp_code = get_timestamp_code_for_filename(filepath)
            filename_with_prefix = f"{camera_code}_{sd_code}_{timestamp_code}_{filename}"
            new_filepath = os.path.join(camera_sd_path, filename_with_prefix)
            new_filepaths.append(new_filepath)
        
        # Get subset of old file names and new file names to print in response
        old_filepaths_to_print = files[:3]
        new_filepaths_to_print = new_filepaths[:3]
        print(f"Example old file names:\n{old_filepaths_to_print}\nExample new file names:\n{new_filepaths_to_print}")

        rename_response = input(f"Rename raw files to new naming convention? (y/n): ").strip().lower()

        if rename_response == 'n':
            print("Ok. Leaving file names as-is")
            new_filepaths = files
            continue
        elif rename_response != 'y':
            print("Invalid input. Exiting script.")
            exit()
        else:
            for filepath, new_filepath in zip(files, new_filepaths):
                os.rename(filepath, new_filepath)

        # Upload to s3
        # Now let's check if files have been uploaded already
        # We get list of files again, because we might have renamed them or left them the same
        all_files = glob.glob(f"{camera_sd_path}/*.jpg") + glob.glob(f"{camera_sd_path}/*.JPG")
        for filepath in all_files:
            filename = os.path.basename(filepath)
            # Check if file has been uploaded 
            if filename in uploaded_files:
                number_of_files_already_uploaded += 1
                continue
            all_files_to_upload.append(filepath)
            timestamp_code = get_timestamp_code_for_filename(filepath)
            if rename_response == 'y':
                destination_filenames.append(filename)
            else:
                destination_filenames.append(f"{camera_code}_{sd_code}_{timestamp_code}_{filename}")
        print(f"Added {len(list(files))} from folder {camera_sd_folder}")

    print(f"{number_of_files_already_uploaded} files in year directory already uploaded to s3.")
    print(f"Will upload {len(all_files_to_upload)} files to {bucket_name} s3 bucket.")
    print(f"Example uploads:\n{all_files_to_upload[0]} -> {destination_filenames[0]}\n{all_files_to_upload[1]} -> {destination_filenames[1]}\n...etc")
    response = input(f"Do you want to start upload of {len(all_files_to_upload)} files? (y/n): ").strip().lower()

    if response == 'n':
        print("Exiting script.")
        exit()  # or use sys.exit() if you prefer
    elif response != 'y':
        print("Invalid input. Exiting script.")
        exit()

    files_uploaded = []
    for file_to_upload, destination_filename in zip(all_files_to_upload, destination_filenames):
        # Upload the file to the specified bucket and key
        try:
            s3_client.upload_file(str(file_to_upload), bucket_name, destination_filename, ExtraArgs=extra_args or {})
            print("Upload successful!")
            files_uploaded.append(destination_filename)
        except Exception as e:
            print("Error uploading file:", e)
            raise

    response2 = input(f"Do you want to overwrite uploaded files file? (y/n) If not, writes to uploaded_files.txt: ").strip().lower()

    if response2 == 'y':
        print(f"Overwriting uploaded files file ({path_to_uploaded_file}).")
        with open(path_to_uploaded_file, "a") as f:
            for file in files_uploaded:
                f.write(f"{file}\n")
    else:
        print("Exiting script.")
        with open("uploaded_files.txt", "w") as f:
            for file in files_uploaded:
                f.write(f"{file}\n")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--path-to-raw-directory",
        help="full path to folder with raw files with old naming convention (e.g., IMG_0001.JPG)",
    )
    parser.add_argument(
        "--camera-number",
        type=str,
        required=False,
        help="camera number, as an integer, not exceeding three digits",
    )
    parser.add_argument(
        "--sd-card-number",
        type=str,
        required=False,
        help="sd card number, as an integer, not exceeding three digits",
    )
    parser.add_argument(
        "--path-to-uploaded-files-file",
        help="file that has record of files that have been uploaded to s3",
        default="../misc_files/files_uploaded_to_s3.txt",
    )
    parser.add_argument(
        "--bucket-name",
        help="name of s3 bucket where file will be uploaded to",
    )
    extra_args = {"StorageClass": "DEEP_ARCHIVE"}
    #extra_args = None
    args = parser.parse_args()
    rename_and_upload_files_to_s3(
        path_to_raw_directory=args.path_to_raw_directory,
        path_to_uploaded_file=args.path_to_uploaded_files_file,
        bucket_name=args.bucket_name,
        camera_number=args.camera_number,
        sd_card_number=args.sd_card_number,
        extra_args=extra_args
    )
