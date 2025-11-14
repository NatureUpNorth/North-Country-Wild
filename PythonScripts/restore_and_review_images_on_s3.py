#!/usr/bin/env python3
"""
Script to check S3 file storage classes, restore from Glacier if needed,
and create a PDF from files in standard storage.
"""

import argparse
import csv
import sys
from pathlib import Path
import boto3
from botocore.exceptions import ClientError
from PyPDF2 import PdfMerger, PdfReader
from PIL import Image
from io import BytesIO
import tempfile


def parse_arguments():
    parser = argparse.ArgumentParser(
        description="Check S3 storage classes and create PDF from standard storage files"
    )
    
    input_group = parser.add_mutually_exclusive_group(required=True)
    input_group.add_argument(
        '--files', 
        nargs='+',
        help='List of filenames (space-separated)'
    )
    input_group.add_argument(
        '--csv',
        type=str,
        help='CSV file containing filenames (one per row or in a column)'
    )
    
    parser.add_argument(
        '--bucket',
        type=str,
        required=True,
        help='S3 bucket name'
    )
    
    parser.add_argument(
        '--restore-days',
        type=int,
        default=7,
        help='Number of days to restore files from Glacier (default: 7)'
    )
    
    parser.add_argument(
        '--restore-tier',
        type=str,
        choices=['Expedited', 'Standard', 'Bulk'],
        default='Standard',
        help='Restore tier for Glacier retrieval (default: Standard)'
    )

    parser.add_argument(
        '--output',
        type=str,
        required=True,
        help='Output PDF filename'
    )
    
    parser.add_argument(
        '--csv-column',
        type=str,
        default='filename',
        help='Column name in CSV containing filenames (default: filename)'
    )
    
    return parser.parse_args()


def read_filenames_from_csv(csv_path, column_name):
    """Read filenames from CSV file."""
    filenames = []
    try:
        with open(csv_path, 'r') as f:
            reader = csv.DictReader(f)
            if column_name not in reader.fieldnames:
                # Try first column if specified column doesn't exist
                print(f"Column '{column_name}' not found. Using first column.")
                f.seek(0)
                reader = csv.reader(f)
                next(reader)  # Skip header
                filenames = [row[0] for row in reader if row]
            else:
                filenames = [row[column_name] for row in reader if row[column_name]]
    except Exception as e:
        print(f"Error reading CSV: {e}")
        sys.exit(1)
    
    return filenames


def check_storage_class(s3_client, bucket, filename):
    """Check the storage class of an S3 object."""
    try:
        response = s3_client.head_object(Bucket=bucket, Key=filename)
        storage_class = response.get('StorageClass', 'STANDARD')
        
        # Check if object is being restored
        restore_status = response.get('Restore')
        is_restoring = restore_status and 'ongoing-request="true"' in restore_status
        is_restored = restore_status and 'ongoing-request="false"' in restore_status
        
        return {
            'storage_class': storage_class,
            'is_restoring': is_restoring,
            'is_restored': is_restored,
            'exists': True
        }
    except ClientError as e:
        if e.response['Error']['Code'] == '404':
            return {'exists': False, 'storage_class': None}
        else:
            raise


def restore_from_glacier(s3_client, bucket, filename, days, tier):
    """Initiate restore from Glacier."""
    try:
        s3_client.restore_object(
            Bucket=bucket,
            Key=filename,
            RestoreRequest={
                'Days': days,
                'GlacierJobParameters': {
                    'Tier': tier
                }
            }
        )
        return True
    except ClientError as e:
        if e.response['Error']['Code'] == 'RestoreAlreadyInProgress':
            print(f"  Restore already in progress for {filename}")
            return True
        else:
            print(f"  Error restoring {filename}: {e}")
            return False


def download_file(s3_client, bucket, filename):
    """Download file from S3 to memory."""
    try:
        response = s3_client.get_object(Bucket=bucket, Key=filename)
        return response['Body'].read()
    except ClientError as e:
        print(f"Error downloading {filename}: {e}")
        return None


def is_pdf(filename):
    """Check if file is a PDF based on extension."""
    return filename.lower().endswith('.pdf')


def is_image(filename):
    """Check if file is an image based on extension."""
    image_extensions = {'.jpg', '.jpeg', '.png', '.gif', '.bmp', '.tiff', '.tif'}
    return Path(filename).suffix.lower() in image_extensions


def image_to_pdf_bytes(image_data):
    """Convert image bytes to PDF bytes with image at top of page."""
    img = Image.open(BytesIO(image_data))
    
    # Convert to RGB if necessary
    if img.mode in ('RGBA', 'LA', 'P'):
        background = Image.new('RGB', img.size, (255, 255, 255))
        if img.mode == 'P':
            img = img.convert('RGBA')
        background.paste(img, mask=img.split()[-1] if img.mode in ('RGBA', 'LA') else None)
        img = background
    elif img.mode != 'RGB':
        img = img.convert('RGB')
    
    # Create letter-sized page (8.5" x 11" at 72 DPI)
    page_width, page_height = 612, 792
    
    # Scale image to fit page width while maintaining aspect ratio
    img_width, img_height = img.size
    scale = page_width / img_width
    new_height = int(img_height * scale)
    img_resized = img.resize((page_width, new_height), Image.Resampling.LANCZOS)
    
    # Create white page and paste image at top
    page = Image.new('RGB', (page_width, page_height), (255, 255, 255))
    page.paste(img_resized, (0, 0))
    
    pdf_bytes = BytesIO()
    page.save(pdf_bytes, format='PDF')
    pdf_bytes.seek(0)
    return pdf_bytes


def create_pdf_from_files(s3_client, bucket, filenames, output_path):
    """Download files and create merged PDF."""
    merger = PdfMerger()
    files_processed = 0
    
    for filename in filenames:
        print(f"Processing {filename}...")
        file_data = download_file(s3_client, bucket, filename)
        
        if file_data is None:
            continue
        
        try:
            if is_pdf(filename):
                # Handle PDF files
                pdf_file = BytesIO(file_data)
                merger.append(pdf_file)
                files_processed += 1
            elif is_image(filename):
                # Convert images to PDF
                pdf_bytes = image_to_pdf_bytes(file_data)
                merger.append(pdf_bytes)
                files_processed += 1
            else:
                print(f"  Skipping {filename}: not a PDF or image")
        except Exception as e:
            print(f"  Error processing {filename}: {e}")

    if files_processed == 0:
        print("No files were successfully processed.")
        return False
    
    try:
        merger.write(output_path)
        merger.close()
        print(f"\nSuccessfully created PDF with {files_processed} files: {output_path}")
        return True
    except Exception as e:
        print(f"Error writing output PDF: {e}")
        return False


def main():
    args = parse_arguments()
    
    # Get list of filenames
    if args.files:
        filenames = args.files
    elif args.csv:
        filenames = read_filenames_from_csv(args.csv, args.csv_column)
    else:
        print("No filenames provided.")
        sys.exit(1)

    print(f"Processing {len(filenames)} files from bucket '{args.bucket}'")

    # Initialize S3 client
    s3_client = boto3.client('s3')

    # Check storage class for each file
    print("\nChecking storage classes...")
    storage_info = {}
    glacier_files = []
    standard_files = []
    missing_files = []
    
    for filename in filenames:
        info = check_storage_class(s3_client, args.bucket, filename)
        storage_info[filename] = info
        
        if not info['exists']:
            missing_files.append(filename)
            print(f"  {filename}: NOT FOUND")
        elif info['storage_class'] in ['GLACIER', 'DEEP_ARCHIVE']:
            glacier_files.append(filename)
            status = "RESTORING" if info['is_restoring'] else "RESTORED" if info['is_restored'] else info['storage_class']
            print(f"  {filename}: {status}")
        else:
            standard_files.append(filename)
            print(f"  {filename}: {info['storage_class']}")

    if missing_files:
        print(f"\nWarning: {len(missing_files)} files not found in bucket")

    # Case 1: All files are in Glacier
    if glacier_files and not standard_files:
        print(f"\nAll {len(glacier_files)} files are in Glacier storage.")
        response = input(f"Do you want to restore them? (Tier: {args.restore_tier}, Days: {args.restore_days}) [y/N]: ")
        
        if response.lower() in ['y', 'yes']:
            print("\nInitiating restore...")
            restored_count = 0
            for filename in glacier_files:
                if not storage_info[filename]['is_restoring'] and not storage_info[filename]['is_restored']:
                    if restore_from_glacier(s3_client, args.bucket, filename, args.restore_days, args.restore_tier):
                        restored_count += 1
                        print(f"  Restore initiated for {filename}")
                else:
                    print(f"  {filename} already restoring/restored")
            
            print(f"\nRestore initiated for {restored_count} files.")
            print(f"Files will be available for {args.restore_days} days once restored.")
        else:
            print("Restore cancelled.")
        
        sys.exit(0)
    
    # Case 2: Some or all files are in standard storage
    if standard_files:
        print(f"\n{len(standard_files)} files available in standard storage.")
        if glacier_files:
            print(f"{len(glacier_files)} files in Glacier will be skipped.")
        
        print(f"\nCreating PDF from available files...")
        success = create_pdf_from_files(s3_client, args.bucket, standard_files, args.output)
        
        if success:
            sys.exit(0)
        else:
            sys.exit(1)
    else:
        print("\nNo files available in standard storage.")
        sys.exit(1)


if __name__ == '__main__':
    main()
