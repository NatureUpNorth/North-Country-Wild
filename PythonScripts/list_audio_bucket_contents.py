import argparse
import boto3
import csv
from collections import defaultdict
from datetime import timezone
from pathlib import Path


def list_audio_bucket_contents(bucket_name: str, output_file: str) -> None:
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
        writer.writerow(["filename", "size_bytes", "last_modified"])
        for obj in objects:
            last_modified = obj['LastModified'].astimezone(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
            writer.writerow([obj['Key'], obj['Size'], last_modified])

    print(f"Wrote {len(objects)} records to {output_file}")

    # Summary: group by deployment dir (AM prefix before first underscore)
    counts = defaultdict(int)
    for obj in objects:
        key = obj['Key']
        first_part = key.split('_')[0]
        group = first_part if first_part.startswith("AM") else "(other)"
        counts[group] += 1

    print(f"\nFiles per deployment dir:")
    for group in sorted(counts):
        print(f"  {group}: {counts[group]}")
    print(f"\nTotal: {len(objects)} files")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="List all files in an audio S3 bucket and write a CSV inventory."
    )
    parser.add_argument(
        "--bucket-name",
        required=True,
        help="name of the audio s3 bucket to inventory",
    )
    parser.add_argument(
        "--output-file",
        default=None,
        help="path for the output CSV; defaults to <bucket-name>_audio_inventory.csv in the current directory",
    )
    args = parser.parse_args()

    output_file = args.output_file or f"{args.bucket_name}_audio_inventory.csv"
    list_audio_bucket_contents(
        bucket_name=args.bucket_name,
        output_file=output_file,
    )
