"""
This script requires the Wand and pyexiftool Python packages to be installed.
For the pop-up windows to work as intended, you should also make sure you are
working with Python 3.8 or 3.10+, using Python 3.9 may cause a black window issue.
To check your python version you can go to the terminal and enter:
python3 --version

To install the Python 3.10, download installer for your machine here:
https://www.python.org/downloads/release/python-3100/

Make sure you have the correct Python version before installing the packages below
or you'll need to reinstall them.

You must also make sure the exiftool and ImageMagick command line tools have been installed.
Install those packages and the dependencies using the following (replacing pip3 with pip if that's
the version your machine is using):
pip3 install Wand
pip3 install git+http://github.com/smarnach/pyexiftool.git
brew install imagemagick@6

Export the path to where your imagemagick executable is stored.
This should be in a versioned folder within the imagemagick@6 directory.
You can view your version by cd into: /opt/homebrew/Cellar/imagemagick@6
and ls to view the version number:
export MAGICK_HOME=/opt/homebrew/Cellar/imagemagick@6/6.9.12-63
Also see: https://github.com/ImageMagick/ImageMagick/issues/953
**Important Note** This export will only last as long as your bash session.
If you'd like your bash profile to always search for this directory when
importing imagemagick, you should include the export in your bash profile.
To do so, do the following:
# Change to your home directory
cd ~
# List all files and directories, including the hidden ones
ls -a
# You should see a hidden file called .zshrc, this is your bash profile
# Use your favorite bash text editor to edit .zshrc
nano .zshrc
# Scroll to the bottom of the file and add the export MAGICK_HOME line you ran above
# Close out of the file, making sure you save your changes
# For nano, this is ctrl + x, then you will be asked if you want to save
# and what name to save the file under. You want to keep the same file name, .zshrc
# so you overwrite the file

Some resources used to help build this script:
https://stackoverflow.com/questions/45322213/python-set-maximum-file-size-when-converting-pdf-to-jpeg-using-e-g-wand
https://smarnach.github.io/pyexiftool/
https://stackoverflow.com/questions/10075115/call-exiftool-from-a-python-script
https://stackoverflow.com/questions/27815719/editing-updating-the-data-of-photo-metadata-using-pyexiftool
"""

import argparse
import json
import os
import shutil
import subprocess
from datetime import datetime
from PIL import Image
#import exiftool
import exifread
import piexif
"""
from wand.api import library
from wand.compat import binary
from wand.image import Image
"""

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


#class wimage(Image):
#   def myDefine(self, key, value):
#        """Skip over wand.image.Image.option"""
#        return library.MagickSetOption(self.wand, binary(key), binary(value))

"""
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
"""
def get_timestamp_code_for_filename(filename: str) -> str:
    """Extract timestamp from EXIF metadata, or use current time if unavailable."""
    # using exifread to extract EXIF metadata
    with open(filename, "rb") as img_file:
        tags = exifread.process_file(img_file)
    # getting timestamp and formatting it in string form same as
    # commented-out code above
    datetime_string = tags.get("EXIF DateTimeOriginal") # gets timestamp
    # convert into datetime object
    if datetime_string:
        datetime_obj = datetime.strptime(str(datetime_string), "%Y:%m:%d %H:%M:%S")
    else:
        datetime_obj = datetime.now() # if no timestamp, use current time

    return datetime_obj.strftime("%Y%m%d%H%M%S")
"""
def change_file_size_and_copyright(
    path_to_processed_images: str,
) -> None:
    for filename in os.listdir(path_to_processed_images):
        # Only process unhidden images
        if not filename.startswith("."):
            print(f"resizing and changing copyright info for {filename}")
            processed_filepath = os.path.join(path_to_processed_images, filename)

          #  with wimage(filename=processed_filepath) as img_to_save:
          #      img_to_save.transform(resize="2000>")
          #      img_to_save.myDefine("jpeg:extent", "900kb")
                # img_to_save.format = "jpeg"
          #      img_to_save.save(filename=processed_filepath)
            with Image.open(processed_filepath) as img:
                img = img.resize((2000, int(img.height * (2000 / img.width))))
                img.save(processed_filepath, format = "JPEG", quality = 85, optimize = True)
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
"""

def change_file_size_and_copyright(path_to_processed_images: str) -> None:
    """Resize images and update copyright metadata."""
    # Iterate over all files in the directory
    for filename in os.listdir(path_to_processed_images):
        if not filename.startswith("."):  # Ignore hidden files
            print(f"Processing {filename}...") # print each file name
            # Construct full file path
            processed_filepath = os.path.join(path_to_processed_images, filename)

            # Open and resize image using Pillow
            with Image.open(processed_filepath) as img:
                img.thumbnail((2000, 2000))  # Resize images larger than 2000x2000
                # save back to same file at good quality
                img.save(processed_filepath, "JPEG", quality=85)  # Compress image

            # Update EXIF metadata
            exif_dict = piexif.load(processed_filepath) # load it
            # add correct copyright information
            exif_dict["0th"][piexif.ImageIFD.Copyright] = "Bart Lab and Nature Up North"
            # convert back into exif binary format
            exif_bytes = piexif.dump(exif_dict)
            # insert metadata back into the image
            piexif.insert(exif_bytes, processed_filepath)
            print(f"Updated metadata for {filename}")
"""
def copy_raw_images_change_file_size_and_copyright(
    path_to_raw_images: str,
    path_to_processed_images: str,
    camera_number: str,
    sd_card_number: str,
) -> None:
    # Make sure camera and sd numbers have leading zeros
    # TO DO: Should add check here that there are no more than three digits
    camera_number_with_leading_zeros = camera_number.zfill(3)
    sd_card_number_with_leading_zeros = sd_card_number.zfill(3)

    # Copy and rename images from raw_images_path to processed_images_path
    for filename in os.listdir(path_to_raw_images):
        # Only process unhidden images
        if not filename.startswith("."):
            raw_filepath = os.path.join(path_to_raw_images, filename)
            timestamp_code = get_timestamp_code_for_filename(raw_filepath)
            filename_with_prefix = f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}_{timestamp_code}_{filename}"
            processed_filepath = os.path.join(
                path_to_processed_images, filename_with_prefix
            )
            print(f"copying {raw_filepath} to {processed_filepath}")
            shutil.copy(raw_filepath, processed_filepath)

    change_file_size_and_copyright(path_to_processed_images)

"""

def copy_raw_images_change_file_size_and_copyright(
    path_to_raw_images: str, path_to_processed_images: str, camera_number: str, sd_card_number: str
) -> None:
    """Copy and rename images before resizing and modifying metadata."""
    # 3 digits for camera and sd card numbers
    camera_number = camera_number.zfill(3)
    sd_card_number = sd_card_number.zfill(3)
    # Iterate through each file in the raw images directory
    for filename in os.listdir(path_to_raw_images):
        # Ignore hidden files
        if not filename.startswith("."):
            # Construct full file path
            raw_filepath = os.path.join(path_to_raw_images, filename)
            # extract timestamp
            timestamp_code = get_timestamp_code_for_filename(raw_filepath)
            # new filename with camera number, sd number, timestamp, filename
            new_filename = f"C{camera_number}_SD{sd_card_number}_{timestamp_code}_{filename}"
            # Construct destination file path
            processed_filepath = os.path.join(path_to_processed_images, new_filename)

            print(f"Copying {raw_filepath} to {processed_filepath}")
            # copy to processed images directory
            shutil.copy(raw_filepath, processed_filepath)
    # Resize and update metadata for all processed images
    change_file_size_and_copyright(path_to_processed_images)


def completely_process_images_from_sd_card(
    memory_card_path: str,
    path_to_raw_images: str,
    path_to_processed_images: str,
    camera_number: int,
    sd_card_number: int,
):
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
            raw_images_filepath = os.path.join(path_to_raw_images, filename)
            print(f"copying {sd_filepath} to {raw_images_filepath}")
            shutil.copy(sd_filepath, raw_images_filepath)

    # Copy and rename images from raw_images_path to processed_images_path
    for filename in os.listdir(path_to_raw_images):
        # Only process unhidden images
        if not filename.startswith("."):
            raw_filepath = os.path.join(path_to_raw_images, filename)
            timestamp_code = get_timestamp_code_for_filename(raw_filepath)
            filename_with_prefix = f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}_{timestamp_code}_{filename}"
            processed_filepath = os.path.join(
                path_to_processed_images, filename_with_prefix
            )
            print(f"copying {raw_filepath} to {processed_filepath}")
            shutil.copy(raw_filepath, processed_filepath)

    change_file_size_and_copyright(path_to_processed_images)


def get_args():
    parser = argparse.ArgumentParser(
        description="This script takes images from one of three entry points (see subcommands) and performs the specified processing steps (any of: copying, renaming, resizing, changing copyright information)"
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
        if a three digit integer is not provided, the script will automatically add leading zeros""",
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

    namespace_args = parser.parse_args()
    subcommand = namespace_args.func
    return subcommand, {k: v for k, v in vars(namespace_args).items() if k != "func"}


# argparse these variables
#if __name__ == "__main__":
#    func, args = get_args()
#    func(**args)

# Testing for week of (2/11)
if __name__ == "__main__":
    test_image_path = "test_raw_images"
    processed_image_path = "processed_images"  # Directory to save processed images

    # checking if directories exist
    os.makedirs(test_image_path, exist_ok=True)
    os.makedirs(processed_image_path, exist_ok=True)


    test_image_file = r"C:\Users\bkara\repos\North-Country-Wild\PythonScripts\test_raw_images\metadata_img.jpeg"
    # Test resizing and metadata change
    change_file_size_and_copyright(test_image_path)

    # Test copying, renaming, timestamp code, and resizing images
    copy_raw_images_change_file_size_and_copyright(test_image_path, processed_image_path, "1", "1")
    get_timestamp_code_for_filename(test_image_file)

