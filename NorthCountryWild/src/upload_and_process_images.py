"""
This script requires the Wand and pyexiftool Python packages to be installed.
You should also make sure the exiftool and ImageMagick command line tools have been installed.
Install those packages and the dependencies using the following:
pip install Wand
pip install git+http://github.com/smarnach/pyexiftool.git
brew install imagemagick@6

Export the path to where your imagemagick executable is stored.
This should be in a versioned folder within the imagemagick@6 directory.
You can view your version by cd into: /opt/homebrew/Cellar/imagemagick@6
and ls to view the version number:
export MAGICK_HOME=/opt/homebrew/Cellar/imagemagick@6/6.9.12-63
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
from datetime import datetime
from tkinter import Tk, ttk
from tkinter.filedialog import askdirectory
from typing import Union

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


def get_timestamp_code_for_filename(filename: str) -> str:
    # Get timestamp as unique identifier for when same camera and sd card are used for multiple
    # deployments
    with exiftool.ExifTool() as et:
        metadata = et.get_metadata(filename)
    datetime_string = metadata["EXIF:DateTimeOriginal"]
    datetime_obj = datetime.strptime(datetime_string, "%Y:%m:%d %H:%M:%S")
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
    def get_processed_images_dir():
        global processed_images_dir
        processed_images_dir = askdirectory(title="CHOOSE PROCESSED IMAGES DIR")
        win.destroy()

    if path_to_processed_images is None:
        win = Tk()
        win.title("Process North Country Wild Photos")
        win.geometry("600x300")
        b2 = ttk.Button(
            win, text="CHOOSE PROCESSED IMAGES DIR", command=get_processed_images_dir
        ).pack(side="top", padx=50, pady=120)
        win.mainloop()
        path_to_processed_images = processed_images_dir

    print(path_to_processed_images)
    for filename in os.listdir(path_to_processed_images):
        # Only process unhidden images
        if not filename.startswith("."):
            print(f"resizing and changing copyright info for {filename}")
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
    camera_number: str,
    sd_card_number: str,
) -> None:
    # Make sure camera and sd numbers have leading zeros
    # TO DO: Should add check here that there are no more than three digits
    camera_number_with_leading_zeros = camera_number.zfill(3)
    sd_card_number_with_leading_zeros = sd_card_number.zfill(3)

    def get_raw_images_dir():
        global raw_images_dir
        raw_images_dir = askdirectory(title="CHOOSE RAW IMAGES DIR")
        win.destroy()

    def get_processed_images_dir():
        global processed_images_dir
        processed_images_dir = askdirectory(title="CHOOSE PROCESSED IMAGES DIR")
        win.destroy()

    win = Tk()
    win.title("Process North Country Wild Photos")
    win.geometry("600x300")
    b1 = ttk.Button(win, text="CHOOSE RAW IMAGES DIR", command=get_raw_images_dir).pack(
        side="top", padx=50, pady=120
    )
    win.mainloop()

    win = Tk()
    win.title("Process North Country Wild Photos")
    win.geometry("600x300")
    b2 = ttk.Button(
        win, text="CHOOSE PROCESSED IMAGES DIR", command=get_processed_images_dir
    ).pack(side="top", padx=50, pady=120)
    win.mainloop()

    # Copy and rename images from raw_images_path to processed_images_path
    for filename in os.listdir(raw_images_dir):
        # Only process unhidden images
        if not filename.startswith("."):
            raw_filepath = os.path.join(raw_images_dir, filename)
            timestamp_code = get_timestamp_code_for_filename(filename)
            filename_with_prefix = f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}_{timestamp_code}_{filename}"
            processed_filepath = os.path.join(
                processed_images_dir, filename_with_prefix
            )
            print(f"copying {raw_filepath} to {processed_filepath}")
            shutil.copy(raw_filepath, processed_filepath)

    change_file_size_and_copyright(processed_images_dir)


def completely_process_images_from_sd_card(
    memory_card_path: str,
    camera_number: int,
    sd_card_number: int,
):
    camera_number_with_leading_zeros = str(camera_number).zfill(3)
    sd_card_number_with_leading_zeros = str(sd_card_number).zfill(3)

    # TO DO:
    # Should print message to command line to confirm you want to download images to full path
    # Should also prevent overwriting where possible
    # https://stackoverflow.com/questions/82831/how-do-i-check-whether-a-file-exists-without-exceptions

    def get_raw_images_dir():
        global raw_images_dir
        raw_images_dir = askdirectory(title="CHOOSE RAW IMAGES DIR")
        win.destroy()

    def get_processed_images_dir():
        global processed_images_dir
        processed_images_dir = askdirectory(title="CHOOSE PROCESSED IMAGES DIR")
        win.destroy()

    win = Tk()
    win.title("Process North Country Wild Photos")
    win.geometry("600x300")
    b1 = ttk.Button(win, text="CHOOSE RAW IMAGES DIR", command=get_raw_images_dir).pack(
        side="top", padx=50, pady=120
    )
    win.mainloop()

    win = Tk()
    win.title("Process North Country Wild Photos")
    win.geometry("600x300")
    b2 = ttk.Button(
        win, text="CHOOSE PROCESSED IMAGES DIR", command=get_processed_images_dir
    ).pack(side="top", padx=50, pady=120)
    win.mainloop()

    # Copy images from memory_card_path to raw_images_path
    for filename in os.listdir(memory_card_path):
        # Only process unhidden images
        if not filename.startswith("."):
            sd_filepath = os.path.join(memory_card_path, filename)
            raw_images_filepath = os.path.join(raw_images_dir, filename)
            print(f"copying {sd_filepath} to {raw_images_filepath}")
            shutil.copy(sd_filepath, raw_images_filepath)

    # Copy and rename images from raw_images_path to processed_images_path
    for filename in os.listdir(raw_images_dir):
        # Only process unhidden images
        if not filename.startswith("."):
            raw_filepath = os.path.join(raw_images_dir, filename)
            timestamp_code = get_timestamp_code_for_filename(filename)
            filename_with_prefix = f"C{camera_number_with_leading_zeros}_SD{sd_card_number_with_leading_zeros}_{timestamp_code}_{filename}"
            processed_filepath = os.path.join(
                processed_images_dir, filename_with_prefix
            )
            print(f"copying {raw_filepath} to {processed_filepath}")
            shutil.copy(raw_filepath, processed_filepath)

    change_file_size_and_copyright(processed_images_dir)


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
        required=False,
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
if __name__ == "__main__":
    func, args = get_args()
    func(**args)
