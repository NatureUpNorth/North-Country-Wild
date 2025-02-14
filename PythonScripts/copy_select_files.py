"""
Copies files from a directory of deployment directories to a new directory based on a csv file that has the deployment id, filename, and species
"""

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

    # Retrieve df with deployment and filename
    filename_df = pd.read_csv(filename_csv_path)

    # Confirm that deployment + filepath combinations are all unique
    assert len(filename_df) == len(filename_df[["deployment_id", "filename"]].drop_duplicates())

    for deployment in filename_df["deployment_id"].unique():
        deployment_filename_df = filename_df[filename_df["deployment_id"] == deployment]
        deployment_dir = os.path.join(all_deployment_dir, deployment)
        for species in deployment_filename_df["species"].unique():
            species_df = deployment_filename_df[deployment_filename_df["species"] == species]
            output_deployment_dir = os.path.join(ouput_dir, deployment, species)
            Path(output_deployment_dir).mkdir(parents=True, exist_ok=True)
            for filename in species_df["filename"].tolist():
                full_orig_filepath = os.path.join(deployment_dir, filename)
                full_dest_filepath = os.path.join(output_deployment_dir, filename)
                shutil.copy(full_orig_filepath, full_dest_filepath)
