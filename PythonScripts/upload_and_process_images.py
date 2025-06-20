"""
This script requires the Pillow and pyexiftool Python packages to be installed.
For the pop-up windows to work as intended, you should also make sure you are
working with Python 3.8 or 3.10+, using Python 3.9 may cause a black window issue.
To check your python version you can go to the terminal and enter:
python3 --version

To install the Python 3.10, download installer for your machine here:
https://www.python.org/downloads/release/python-3100/

Make sure you have the correct Python version before installing the packages below
or you'll need to reinstall them.

You must also make sure the exiftool command line tool has been installed.
Install the dependencies using the following (replacing pip3 with pip if that's
the version your machine is using):
pip3 install Pillow
pip3 install git+http://github.com/smarnach/pyexiftool.git

For macOS users, install exiftool via Homebrew:
brew install exiftool

Some resources used to help build this script:
https://stackoverflow.com/questions/45322213/python-set-maximum-file-size-when-converting-pdf-to-jpeg-using-e-g-wand
https://smarnach.github.io/pyexiftool/
https://stackoverflow.com/questions/10075115/call-exiftool-from-a-python-script
https://stackoverflow.com/questions/27815719/editing-updating-the-data-of-photo-metadata-using-pyexiftool
"""

import argparse
import boto3
import json
import os
import shutil
import subprocess
import io
from datetime import datetime

import exiftool
from PIL import Image, ExifTags


class ExifTool(object):

    sentinel = "{ready}\n"

    def __init__(self, executable="/usr/bin/exiftool"):
        self.executable = executable

    def __enter__(self):
        self.process = subprocess.Popen(
            [self.executable, "-stay_open", "True", "-@", "-"],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
        )
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self.process.stdin.write("-stay_open\nFalse\n")
        self.process.stdin.flush()

    def execute(self, *args):
        args = args + ("-execute\n",)
        self.process.stdin.write(str.join("\n", args))
        self.process.stdin.flush()
        output = ""
        fd = self.process.stdout.fileno()
        while not output.endswith(self.sentinel):
            output += os.read(fd, 4096)
        return output[: -len(self.sentinel)]

    def get_metadata(self, *filenames):
        return json.loads(self.execute("-G", "-j", "-n", *filenames))


def resize_and_compress_image(filepath, max_size=2000, max_file_size_kb=900):
    """
    Resize and compress image using Pillow instead of Wand/ImageMagick
    """
    try:
        with Image.open(filepath) as img:
            # Store original format
            original_format = img.format
            
            # Preserve EXIF data if it exists
            exif_data = img.info.get('exif')
            
            # Resize image (equivalent to resize="2000>")
            # Only resize if image is larger than max_size
            if img.width > max_size or img.height > max_size:
                img.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
            
            # Convert to RGB if necessary (for JPEG saving)
            if img.mode in ('RGBA', 'P'):
                img = img.convert('RGB')
            
            # Determine output format - default to JPEG for compression
            output_format = 'JPEG' if original_format in ['JPEG', 'JPG'] or img.mode == 'RGB' else original_format
            
            # If it's a JPEG, compress to target file size
            if output_format == 'JPEG':
                quality = 95
                while quality > 10:
                    # Save to memory buffer to check file size
                    buffer = io.BytesIO()
                    
                    save_kwargs = {'format': 'JPEG', 'quality': quality, 'optimize': True}
                    if exif_data:
                        save_kwargs['exif'] = exif_data
                        
                    img.save(buffer, **save_kwargs)
                    
                    # Check if file size is within target
                    file_size_kb = len(buffer.getvalue()) / 1024
                    if file_size_kb <= max_file_size_kb or quality <= 10:
                        # Save the final image
                        with open(filepath, 'wb') as f:
                            f.write(buffer.getvalue())
                        break
                    
                    quality -= 5
            else:
                # For non-JPEG formats, just save with original format
                save_kwargs = {'format': output_format}
                if exif_data and output_format in ['JPEG', 'TIFF']:
                    save_kwargs['exif'] = exif_data
                img.save(filepath, **save_kwargs)
                
    except Exception as e:
        print(f"Error processing {filepath}: {e}")


def get_timestamp_code_for_filename(filename: str) -> str:
    # Get timestamp as unique identifier for when same camera and sd card are used for multiple
    # deployments
    with exiftool.ExifTool() as et:
        metadata = et.get_metadata(filename)
    if "EXIF:DateTimeOriginal" in metadata:
        datetime_string = metadata["EXIF:DateTimeOriginal"]
        datetime_obj = datetime.strptime(datetime_string, "%Y:%m:%d %H:%M:%S")
    else:
        datetime_obj = datetime.now()
    year = str(datetime_obj.year)
    month = str(datetime_obj.month).zfill(2)
    day = str(datetime_obj.day).zfill(2)
    hour = str(datetime_obj.hour).zfill(2)
    minute = str(datetime_obj.minute).zfill(2)
    second = str(datetime_obj.second).zfill(2)

    return f"{year}{month}{day}{hour}{minute}{second}"


def change_file_size_and_copyright(
    path_to_processed_images: str,
) -> None:
    for filename in os.listdir(path_to_processed_images):
        # Only process unhidden images
        if not filename.startswith("."):
            print(f"resizing and changing copyright info for {filename}")
            processed_filepath = os.path.join(path_to_processed_images, filename)

            # Use Pillow for resizing and compression
            resize_and_compress_image(processed_filepath)

            # Use exiftool for copyright metadata
            with exiftool.ExifTool() as et:
                et.execute(
                    b"-overwrite_original",
                    b"-rights=Copyright",
                    bytes(processed_filepath, encoding="ascii"),
                )
                et.execute(
                    b"-overwrite_original",
                    b"-CopyrightNotice=Bart Lab and Nature Up North",
                    bytes(processed_filepath, encoding="ascii"),
                )


def copy_raw_images_change_file_size_and_copyright(
    path_to_raw_images: str,
    path_to_processed_images: str,
    camera_number: str,
    sd_card_number: str,
    path_to_uploaded_file: str,
    bucket_name: str,
) -> None:
    # Make sure camera and sd numbers have leading zeros
    # TO DO: Should add check here that there are no more than three digits
    extra_args = {"StorageClass": "DEEP_ARCHIVE"}
    camera_number_with_leading_zeros = camera_number.zfill(3)
    sd_card_number_with_leading_zeros = sd_card_number.zfill(3)

    # Ask if files should be uploaded to s3
    response = input(f"Do you want to upload files to s3 files? (y/n): ").strip().lower()
    if response != 'y' and response != 'n':
        print("Invalid input. Exiting script.")
        exit()

    if response == 'y':
        # Create a session using the specified profile
        session = boto3.Session(profile_name="default")
        s3_client = session.client("s3")

        uploaded_files = []
        with open(path_to_uploaded_file, 'r') as file:
            for line in file:
                cleaned_line = line.strip()
                if cleaned_line:  # Avoid adding empty strings
                    uploaded_files.append(cleaned_line)

    # Copy and rename images from raw_images_path to processed_images_path
    files_uploaded = []
    for filename in os.listdir(path_to_raw_images):
        # Only process unhidden images
        if not filename.startswith("."):
            raw_filepath = os.path.join(path_to_raw_images, filename)
            timestamp_code = get_timestamp_code_for_filename(raw_filepath)
            filename_with_prefix = f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}_{timestamp_code}_{filename}"
            if filename != filename_with_prefix:
                renamed_filename = filename_with_prefix
            else:
                renamed_filename = filename
            processed_filepath = os.path.join(
                path_to_processed_images, renamed_filename
            )
            print(f"copying {raw_filepath} to {processed_filepath}")
            shutil.copy(raw_filepath, processed_filepath)

            if response == 'y' and renamed_filename not in uploaded_files:
                try:
                    s3_client.upload_file(str(raw_filepath), bucket_name, renamed_filename, ExtraArgs=extra_args)
                    print("Upload successful!")
                    files_uploaded.append(renamed_filename)
                except Exception as e:
                    print("Error uploading file:", e)
                    raise

    response2 = input(f"Do you want to overwrite uploaded files file? (y/n) If not, writes to uploaded_files.txt: ").strip().lower()

    if response2 == 'y':
        print("Overwriting uploaded files file.")
        with open(path_to_uploaded_file, "a") as f:
            for file in files_uploaded:
                f.write(f"{file}\n")
    else:
        print("Writing new uploaded files file.")
        with open("uploaded_files.txt", "w") as f:
            for file in files_uploaded:
                f.write(f"{file}\n")

    change_file_size_and_copyright(path_to_processed_images)


def completely_process_images_from_sd_card(
    memory_card_path: str,
    path_to_raw_images: str,
    path_to_processed_images: str,
    camera_number: int,
    sd_card_number: int,
    path_to_uploaded_file: str,
    bucket_name: str,
):
    extra_args = {"StorageClass": "DEEP_ARCHIVE"}
    extra_args = {}
    camera_number_with_leading_zeros = str(camera_number).zfill(3)
    sd_card_number_with_leading_zeros = str(sd_card_number).zfill(3)

    # TO DO:
    # Should print message to command line to confirm you want to download images to full path
    # Should also prevent overwriting where possible
    # https://stackoverflow.com/questions/82831/how-do-i-check-whether-a-file-exists-without-exceptions

    # Copy images from memory_card_path to raw_images_path
    for filename in os.listdir(memory_card_path):
        # Only process unhidden images
        if not filename.startswith("."):
            sd_filepath = os.path.join(memory_card_path, filename)
            timestamp_code = get_timestamp_code_for_filename(sd_filepath)
            filename_with_prefix = f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}_{timestamp_code}_{filename}"
            raw_images_filepath = os.path.join(path_to_raw_images, filename_with_prefix)
            print(f"copying {sd_filepath} to {raw_images_filepath}")
            shutil.copy(sd_filepath, raw_images_filepath)
    
    # Ask if files should be uploaded to s3
    response = input(f"Do you want to upload files to s3 files? (y/n): ").strip().lower()
    if response != 'y' and response != 'n':
        print("Invalid input. Exiting script.")
        exit()

    if response == 'y':
        # Create a session using the specified profile
        session = boto3.Session(profile_name="no_co_wild")
        s3_client = session.client("s3")

        uploaded_files = []
        with open(path_to_uploaded_file, 'r') as file:
            for line in file:
                cleaned_line = line.strip()
                if cleaned_line:  # Avoid adding empty strings
                    uploaded_files.append(cleaned_line)

    # Copy and rename images from raw_images_path to processed_images_path
    files_uploaded = []
    for filename in os.listdir(path_to_raw_images):
        # Only process unhidden images
        if not filename.startswith("."):
            raw_filepath = os.path.join(path_to_raw_images, filename)
            processed_filepath = os.path.join(
                path_to_processed_images, filename
            )
            print(f"copying {raw_filepath} to {processed_filepath}")
            shutil.copy(raw_filepath, processed_filepath)
            if response == 'y' and filename not in uploaded_files:
                try:
                    s3_client.upload_file(str(raw_filepath), bucket_name, filename, ExtraArgs=extra_args)
                    print("Upload successful!")
                    files_uploaded.append(filename)
                except Exception as e:
                    print("Error uploading file:", e)
                    raise

    response2 = input(f"Do you want to overwrite uploaded files file? (y/n) If not, writes to uploaded_files.txt: ").strip().lower()

    if response2 == 'y':
        print("Overwriting uploaded files file.")
        with open(path_to_uploaded_file, "a") as f:
            for file in files_uploaded:
                f.write(f"{file}\n")
    else:
        print("Writing new uploaded files file.")
        with open("uploaded_files.txt", "w") as f:
            for file in files_uploaded:
                f.write(f"{file}\n")

    change_file_size_and_copyright(path_to_processed_images)


def get_args():
    parser = argparse.ArgumentParser(
        description="This script takes images from one of three entry points (see subcommands) and performs the specified processing steps (any of: copying, renaming, optionally backing-up, resizing, changing copyright information)"
    )
    subparsers = parser.add_subparsers(title="subcommands")

    file_size_and_copyright_parser = subparsers.add_parser(
        "change_file_size_and_copyright",
        help="this subcommand takes a path to the processed images, which have already been renamed, and resizes them and adds copyright information",
    )
    file_size_and_copyright_parser.set_defaults(func=change_file_size_and_copyright)
    file_size_and_copyright_parser.add_argument(
        "--path-to-processed-images",
        type=str,
        required=True,
        help="full path to where processed images are held; assumes you want to overwrite the files at this path",
    )

    copy_raw_image_file_size_and_copyright_parser = subparsers.add_parser(
        "copy_raw_images_change_file_size_and_copyright",
        help="""this subcommand takes the camera number and sd number, as three-digit integers, and copies, resizes, and adds copyright information to the images;\n
        if a three digit integer is not provided, the script will automatically add leading zeros.\n
        This subcommand also allows you to upload files to s3.""",
    )
    copy_raw_image_file_size_and_copyright_parser.set_defaults(
        func=copy_raw_images_change_file_size_and_copyright
    )
    copy_raw_image_file_size_and_copyright_parser.add_argument(
        "--path-to-raw-images",
        type=str,
        required=True,
        help="full path to raw images, before renaming, resizing, or changing copyright",
    )
    copy_raw_image_file_size_and_copyright_parser.add_argument(
        "--path-to-processed-images",
        type=str,
        required=True,
        help="full path to where processed images will be placed",
    )
    copy_raw_image_file_size_and_copyright_parser.add_argument(
        "--camera-number",
        type=str,
        required=True,
        help="camera number, as an integer, not exceeding three digits",
    )
    copy_raw_image_file_size_and_copyright_parser.add_argument(
        "--sd-card-number",
        type=str,
        required=True,
        help="sd card number, as an integer, not exceeding three digits",
    )
    copy_raw_image_file_size_and_copyright_parser.add_argument(
        "--path-to-uploaded-file",
        type=str,
        required=True,
        help="full path to txt file that records what files have been uploaded",
    )
    copy_raw_image_file_size_and_copyright_parser.add_argument(
        "--bucket-name",
        type=str,
        required=True,
        help="name of s3 bucket to upload files to",
    )

    from_sd_card_parser = subparsers.add_parser(
        "completely_process_images_from_sd_card",
        help="this subcommand takes a number of arguments and completely processes images from the sd card to separate directories for the raw and processed images",
    )
    from_sd_card_parser.set_defaults(func=completely_process_images_from_sd_card)
    from_sd_card_parser.add_argument(
        "--memory-card-path",
        type=str,
        required=True,
        help="full path to memory card holding the images, as a string",
    )
    from_sd_card_parser.add_argument(
        "--path-to-raw-images",
        type=str,
        required=True,
        help="full path to raw images, before renaming, resizing, or changing copyright",
    )
    from_sd_card_parser.add_argument(
        "--path-to-processed-images",
        type=str,
        required=True,
        help="full path to where processed images will be placed",
    )
    from_sd_card_parser.add_argument(
        "--camera-number",
        type=int,
        required=True,
        help="camera number, as an integer, not exceeding three digits",
    )
    from_sd_card_parser.add_argument(
        "--sd-card-number",
        type=int,
        required=True,
        help="sd card number, as an integer, not exceeding three digits",
    )
    from_sd_card_parser.add_argument(
        "--path-to-uploaded-file",
        type=str,
        required=True,
        help="full path to txt file that records what files have been uploaded",
    )
    from_sd_card_parser.add_argument(
        "--bucket-name",
        type=str,
        required=True,
        help="name of s3 bucket to upload files to",
    )

    namespace_args = parser.parse_args()
    subcommand = namespace_args.func
    return subcommand, {k: v for k, v in vars(namespace_args).items() if k != "func"}


# argparse these variables
if __name__ == "__main__":
    func, args = get_args()
    func(**args)