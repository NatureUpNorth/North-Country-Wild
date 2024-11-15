"""
This script pulls the data from the deployment google sheet (a resource where researchers and students
can manually enter metadata information regarding camera trap deployments), cleans up
the data, and upserts (i.e.) updates the "deployments" table in the relational database

Note: This script is a work-in-progress and may need modifications before use.

Usage:
If you're in the same repo as the script, run:

python (or python3) sync_deployments_google_sheet.py

Future work: We need to adapt this for different device types and map open-form strings
to standardized ID values (e.g. mapping protocol to protocol_id)
"""

import google_sheets
import database_helpers
from datetime import datetime
import pandas as pd

_camera_deployment_google_sheet_link = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTCRXsH0spbmzN0t1f9yVM0lohfa0QjYMvKuVyny7ahMzamIgEO8IvZcG_HziCk3O8zl3OWadlmWoqu/pub?gid=0&single=true&output=tsv"
TABLE_NAME_TO_UPSERT_INTO = "deployments"
COLUMNS = [
    "deployment_id",
    "deployer_names",
    "latitude",
    "longitude",
    "site",
    "protocol",
    "deployment_date",
    "deployment_time",
    "collection_date",
    "collection_time",
    "last_use_date",
    "device_type",
    "device_id",
    "sd_card_id",
    "media_type",
    "battery_percent_start",
    "battery_percent_end",
    "device_worked_at_collection",
    "tree_species",
    "deployment_notes",
    "flag",
    "flag_notes",
]

if __name__ == "__main__":
    print("Syncing camera deployment google sheet to database")
    bytes_like = google_sheets.get_gsheet_as_byteslike(
        _camera_deployment_google_sheet_link
    )
    deployments_sheet_df = pd.read_csv(bytes_like, sep="\t")
    # Future addition: need to map protocol_id on protocol, right now it's free-form text
    # but we eventually want to standardize how text transaltes to ID
    # E.g., "Snapshot", "snapshot", and "SNAPSHOT" should all map to same ID
    # need to map tree_species_id on tree_species
    deployments_sheet_df.rename(
        columns={
            "camera_trappers": "deployer_names",
            "forest_or_site_name": "site",
            "cam_num": "device_id",
            "sd_num": "sd_card_id",
            "bat_start_percent": "battery_percent_start",
            "bat_end_percent": "battery_percent_end",
            "cam_works_at_pickup": "device_worked_at_collection",
            "FLAG": "flag",
            "FLAG_comments": "flag_comments",
        },
        inplace=True,
    )
    deployments_sheet_df["device_id"] = deployments_sheet_df["device_id"].apply(
        lambda x: f"C{str(x).zfill(3)}"
    )
    deployments_sheet_df["sd_card_id"] = deployments_sheet_df["sd_card_id"].apply(
        lambda x: f"C{str(x).zfill(3)}"
    )
    # The camera deployments google sheet is separate from audiomoth deployment google sheet
    # We'll eventually want to do this same excercise, plugging in audio google sheet
    # and set value here to audio
    deployments_sheet_df["device_type"] = "camera"

    deployments_sheet_df["year"] = deployments_sheet_df["deployment_date"].apply(
        lambda x: datetime.strptime(x, "%d-%b-%Y").year
    )
    deployments_sheet_df["month"] = deployments_sheet_df["deployment_date"].apply(
        lambda x: datetime.strptime(x, "%d-%b-%Y").month
    )
    deployments_sheet_df["day"] = deployments_sheet_df["deployment_date"].apply(
        lambda x: datetime.strptime(x, "%d-%b-%Y").day
    )
    deployments_sheet_df["hour"] = deployments_sheet_df["deployment_date"].apply(
        lambda x: datetime.strptime(x, "%d-%b-%Y").hour
    )
    deployments_sheet_df["minute"] = deployments_sheet_df["deployment_date"].apply(
        lambda x: datetime.strptime(x, "%d-%b-%Y").minute
    )
    deployments_sheet_df["second"] = deployments_sheet_df["deployment_date"].apply(
        lambda x: datetime.strptime(x, "%d-%b-%Y").second
    )
    deployments_sheet_df["deployment_id"] = (
        deployments_sheet_df["device_id"].map(str)
        + "_"
        + deployments_sheet_df["sd_card_id"].map(str)
        + "_"
        + deployments_sheet_df["year"].map(str)
        + deployments_sheet_df["month"].map(str)
        + deployments_sheet_df["day"].map(str)
        + deployments_sheet_df["hour"].map(str)
        + deployments_sheet_df["minute"].map(str)
        + deployments_sheet_df["second"].map(str)
    )
    # convert dates and time to certain formats? (Best format to filter image times or getting effective date, etc)
    upsert_df = deployments_sheet_df[COLUMNS]
    database_helpers.upsert_from_df(
        upsert_df,
        TABLE_NAME_TO_UPSERT_INTO,
        pkeys=["deployment_id"],
    )
