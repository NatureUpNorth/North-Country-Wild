"""
This script requires the Wand and pyexiftool Python packages to be installed.
You should also make sure the exiftool and ImageMagick command line tools have been installed.
Install those packages and the dependencies using the following:
pip install Wand
pip install git+http://github.com/smarnach/pyexiftool.git
brew install imagemagick@6
export MAGICK_HOME=/usr/local/opt/imagemagick@6
Also see: https://github.com/ImageMagick/ImageMagick/issues/953

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

import exiftool
from wand.api import library
from wand.compat import binary
from wand.image import Image


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


class wimage(Image):
    def myDefine(self, key, value):
        """Skip over wand.image.Image.option"""
        return library.MagickSetOption(self.wand, binary(key), binary(value))


def change_file_size_and_copyright(path_to_processed_images: str) -> None:
    for filename in os.listdir(path_to_processed_images):
        processed_filepath = os.path.join(path_to_processed_images, filename)

        with wimage(filename=processed_filepath) as img_to_save:
            img_to_save.transform(resize="2000>")
            img_to_save.myDefine("jpeg:extent", "900kb")
            # img_to_save.format = "jpeg"
            img_to_save.save(filename=processed_filepath)

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
    camera_number_with_leading_zeros: str,
    sd_card_number_with_leading_zeros: str,
) -> None:
    # Make sure camera and sd numbers have leading zeros
    # TO DO: Should add check here that there are no more than three digits
    camera_number_with_leading_zeros = camera_number_with_leading_zeros.zfill(3)
    sd_card_number_with_leading_zeros = sd_card_number_with_leading_zeros.zfill(3)

    # Copy and rename images from raw_images_path to processed_images_path
    for filename in os.listdir(path_to_raw_images):
        raw_filepath = os.path.join(path_to_raw_images, filename)
        filename_with_prefix = f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}_{filename}"
        processed_filepath = os.path.join(
            path_to_processed_images, filename_with_prefix
        )
        shutil.copy(raw_filepath, processed_filepath)

    change_file_size_and_copyright(path_to_processed_images)


def completely_process_images_from_sd_card(
    memory_card_path: str,
    external_drive_path: str,
    year: int,
    project_name: str,
    site_name: str,
    camera_number: int,
    sd_card_number: int,
):
    camera_number_with_leading_zeros = str(camera_number).zfill(3)
    sd_card_number_with_leading_zeros = str(sd_card_number).zfill(3)

    raw_images_path = f"{external_drive_path}/{year}_Game_Camera_Photos/Raw/{project_name}/{site_name}/C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}"
    processed_images_path = f"{external_drive_path}/{year}_Game_Camera_Photos/Processed/{project_name}/{site_name}/C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}"
    # TO DO:
    # Should print message to command line to confirm you want to dowload images to full path
    # Should also prevent overwriting where possible
    # https://stackoverflow.com/questions/82831/how-do-i-check-whether-a-file-exists-without-exceptions

    if not os.path.isdir(raw_images_path):
        os.makedirs(raw_images_path)
    if not os.path.isdir(processed_images_path):
        os.makedirs(processed_images_path)

    # Copy images from memory_card_path to raw_images_path
    for filename in os.listdir(memory_card_path):
        sd_filepath = os.path.join(memory_card_path, filename)
        raw_images_filepath = os.path.join(raw_images_path, filename)
        print(raw_images_filepath)
        shutil.copy(sd_filepath, raw_images_filepath)

    # Copy and rename images from raw_images_path to processed_images_path
    for filename in os.listdir(raw_images_path):
        raw_filepath = os.path.join(raw_images_path, filename)
        filename_with_prefix = f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}_{filename}"
        processed_filepath = os.path.join(processed_images_path, filename_with_prefix)
        shutil.copy(raw_filepath, processed_filepath)

    change_file_size_and_copyright(processed_images_path)


def get_args():
    parser = argparse.ArgumentParser(
        description="This script takes images from one of three entry points (see subcommands) and performs the specified processing steps (any of: copying, renaming, resizing, changing copyright information)"
    )
    subparsers = parser.add_subparsers(title="subcommands")

    file_size_and_copyright_parser = subparsers.add_parser(
        "change_file_size_and_copyright",
        help="this subcommand takes a path to the processed images which have already been renamed and resizes them and adds copyright information",
    )
    file_size_and_copyright_parser.set_defaults(func=change_file_size_and_copyright)
    file_size_and_copyright_parser.add_argument(
        "--path-to-processed-images",
        type=str,
        required=True,
        help="full path to where processed images are held; assumes you want to overwrite files here",
    )

    copy_raw_image_file_size_and_copyright_parser = subparsers.add_parser(
        "copy_raw_images_change_file_size_and_copyright",
        help="this subcommand takes a path to the raw images along with where processed images will be and the camera number and sd number, as three-digit integers, and copies, resizes, and adds copyright information to the images",
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
        "--camera-number-with-leading-zeros",
        type=str,
        required=True,
        help="camera number, as an integer with as many leading zeros as necessary to give three digits",
    )
    copy_raw_image_file_size_and_copyright_parser.add_argument(
        "--sd-card-number-with-leading-zeros",
        type=str,
        required=True,
        help="sd card number, as an integer with as many leading zeros as necessary to give three digits",
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
        "--external-drive-path",
        type=str,
        required=False,
        default="/Volumes/NoCoWild",
        help="full path to external drive where images are stored, as a string",
    )
    from_sd_card_parser.add_argument(
        "--year",
        type=int,
        required=True,
        help="calendar year when images were taken, as an integer",
    )
    from_sd_card_parser.add_argument(
        "--project-name",
        type=str,
        required=True,
        help="specific name of the project that images were taken for, as a string",
    )
    from_sd_card_parser.add_argument(
        "--site-name",
        type=str,
        required=True,
        help="specific name of the site where camera was deployed, as a string",
    )
    from_sd_card_parser.add_argument(
        "--camera-number",
        type=int,
        required=True,
        help="camera number, as an integer",
    )
    from_sd_card_parser.add_argument(
        "--sd-card-number",
        type=int,
        required=True,
        help="sd card number, as an integer",
    )

    namespace_args = parser.parse_args()
    subcommand = namespace_args.func
    return subcommand, {k: v for k, v in vars(namespace_args).items() if k != "func"}


# argparse these variables
if __name__ == "__main__":
    func, args = get_args()
    func(**args)
