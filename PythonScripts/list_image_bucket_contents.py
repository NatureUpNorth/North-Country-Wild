import argparse
import boto3
import csv
import re
from collections import defaultdict
from datetime import timezone


# Image filenames follow the pattern built in upload_and_process_images.py:
#   C{cam}_SD{sd}_{timestamp}_{original}   e.g. C004_SD021_20210707181645_IMG_0001.JPG
# The deployment ID is the C###_SD### prefix (camera + SD card combination).
_DEPLOYMENT_ID_RE = re.compile(r'^(C\d+_SD\d+)_')


def _deployment_id(key: str) -> str:
    m = _DEPLOYMENT_ID_RE.match(key)
    return m.group(1) if m else "(other)"


def list_image_bucket_contents(bucket_name: str, output_file: str) -> None:
    session = boto3.Session(profile_name="default")
    s3_client = session.client("s3")

    print(f"Listing contents of s3 bucket '{bucket_name}'...")
    objects = []
    paginator = s3_client.get_paginator('list_objects_v2')
    for page in paginator.paginate(Bucket=bucket_name):
        for obj in page.get('Contents', []):
            objects.append(obj)

    if not objects:
        print("Bucket is empty.")
        return

    # Write CSV
    with open(output_file, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(["filename", "deployment_id", "size_bytes", "last_modified"])
        for obj in objects:
            last_modified = obj['LastModified'].astimezone(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
            writer.writerow([obj['Key'], _deployment_id(obj['Key']), obj['Size'], last_modified])

    print(f"Wrote {len(objects)} records to {output_file}")

    # Summary grouped by deployment ID (C###_SD### camera+SD combination)
    counts = defaultdict(int)
    for obj in objects:
        counts[_deployment_id(obj['Key'])] += 1

    print(f"\nFiles per deployment (camera + SD card):")
    for group in sorted(counts):
        print(f"  {group}: {counts[group]}")
    print(f"\nTotal: {len(objects)} files")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="List all files in an image S3 bucket and write a CSV inventory."
    )
    parser.add_argument(
        "--bucket-name",
        required=True,
        help="name of the image s3 bucket to inventory",
    )
    parser.add_argument(
        "--output-file",
        default=None,
        help="path for the output CSV; defaults to <bucket-name>_image_inventory.csv in the current directory",
    )
    args = parser.parse_args()

    output_file = args.output_file or f"{args.bucket_name}_image_inventory.csv"
    list_image_bucket_contents(
        bucket_name=args.bucket_name,
        output_file=output_file,
    )
