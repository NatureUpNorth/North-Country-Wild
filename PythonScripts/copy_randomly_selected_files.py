import argparse
import os
import shutil

from pathlib import Path

import pandas as pd

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--path-to-file-csv",
        type=str,
        help="full local path to the csv file that has the audio filenames to copy over",
        default=None,
    )
    parser.add_argument(
        "--dir-with-deployment-dirs",
        type=str,
        help="full local path to the directory that holds all of the deployment directories",
        default=None,
    )
    parser.add_argument(
        "--dir-to-copy-files-to",
        type=str,
        help="full local path where files will be copied to in deployment directories",
        default=None,
    )
    args = parser.parse_args()
    filename_csv_path = args.path_to_file_csv
    all_deployment_dir = args.dir_with_deployment_dirs
    ouput_dir = args.dir_to_copy_files_to

    audio_moth_deployment_ids = [
        "A001_SD001",
        "A002_SD013",
        "A003_SD005",
        "A004_SD012",
        "A005_SD002",
        "A006_SD006",
        "A007_SD017",
        "A008_SD007",
        "A009_SD009",
        "A010_SD014",
        "A011_SD018",
        "A013_SD016",
        "A014_SD021",
        "A015_SD010",
        "A016_SD022",
        "A017_SD024",
    ]
    # Retrieve df with deployment and filename
    filename_df = pd.read_csv(filename_csv_path)
    for deployment in audio_moth_deployment_ids:
        deployment_filename_df = filename_df[filename_df["deployment_id"] == deployment]
        deployment_dir = os.path.join(all_deployment_dir, deployment)
        output_deployment_dir = os.path.join(ouput_dir, deployment)
        Path(output_deployment_dir).mkdir(parents=True, exist_ok=True)
        for filename in deployment_filename_df["filename"].tolist():
            full_orig_filepath = os.path.join(deployment_dir, filename)
            full_dest_filepath = os.path.join(output_deployment_dir, filename)
            shutil.copy(full_orig_filepath, full_dest_filepath)
