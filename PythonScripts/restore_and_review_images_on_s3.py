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
from PIL import Image, ImageDraw, ImageFont
from io import BytesIO
import tempfile


def parse_arguments():
    parser = argparse.ArgumentParser(
        description="Check S3 storage classes and create PDF from standard storage files"
    )
    
    parser.add_argument(
        '--csv',
        type=str,
        required=True,
        help='CSV file with columns: subject_ids, Img1, Img2, Img3'
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
    
    return parser.parse_args()


def read_rows_from_csv(csv_path):
    """Read rows from CSV file with subject_id, img1, img2, img3 columns."""
    rows = []
    try:
        with open(csv_path, 'r') as f:
            reader = csv.DictReader(f)
            for row in reader:
                rows.append({
                    'subject_id': row.get('subject_ids', ''),
                    'img1': row.get('Img1', '').strip(),
                    'img2': row.get('Img2', '').strip(),
                    'img3': row.get('Img3', '').strip()
                })
    except Exception as e:
        print(f"Error reading CSV: {e}")
        sys.exit(1)
    
    return rows


def check_storage_class(s3_client, bucket, filename):
    """Check the storage class of an S3 object."""
    if not filename:
        return {'exists': False, 'storage_class': None}
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
    if not filename:
        return None
    try:
        response = s3_client.get_object(Bucket=bucket, Key=filename)
        return response['Body'].read()
    except ClientError as e:
        print(f"Error downloading {filename}: {e}")
        return None


def create_page_with_images(s3_client, bucket, subject_id, img_filenames):
    """Create a PDF page with up to 3 images side-by-side and subject_id below."""
    page_width, page_height = 612, 792
    page = Image.new('RGB', (page_width, page_height), (255, 255, 255))
    
    # Download and process images
    images = []
    for filename in img_filenames:
        if filename:
            file_data = download_file(s3_client, bucket, filename)
            if file_data:
                try:
                    img = Image.open(BytesIO(file_data))
                    if img.mode in ('RGBA', 'LA', 'P'):
                        background = Image.new('RGB', img.size, (255, 255, 255))
                        if img.mode == 'P':
                            img = img.convert('RGBA')
                        background.paste(img, mask=img.split()[-1] if img.mode in ('RGBA', 'LA') else None)
                        img = background
                    elif img.mode != 'RGB':
                        img = img.convert('RGB')
                    images.append(img)
                except Exception as e:
                    print(f"  Error loading {filename}: {e}")
    
    if not images:
        print(f"  No valid images for subject {subject_id}")
        return None
    
    # Calculate dimensions for side-by-side layout
    num_images = len(images)
    img_width = page_width // num_images
    max_img_height = 600  # Leave space for subject_id
    
    # Paste images side-by-side
    x_offset = 0
    for img in images:
        # Scale image to fit allocated width while maintaining aspect ratio
        scale = img_width / img.width
        new_height = int(img.height * scale)
        if new_height > max_img_height:
            scale = max_img_height / img.height
            new_height = max_img_height
            new_width = int(img.width * scale)
        else:
            new_width = img_width
        
        img_resized = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
        page.paste(img_resized, (x_offset, 0))
        x_offset += img_width
    
    # Add subject_id text below images
    draw = ImageDraw.Draw(page)
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 24)
    except:
        font = ImageFont.load_default()
    
    text = f"Subject ID: {subject_id}"
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_x = (page_width - text_width) // 2
    text_y = max_img_height + 20
    draw.text((text_x, text_y), text, fill=(0, 0, 0), font=font)
    
    # Convert to PDF bytes
    pdf_bytes = BytesIO()
    page.save(pdf_bytes, format='PDF')
    pdf_bytes.seek(0)
    return pdf_bytes


def create_pdf_from_rows(s3_client, bucket, rows, output_path):
    """Create PDF with one page per row showing 3 images and subject_id."""
    merger = PdfMerger()
    pages_processed = 0
    
    for row in rows:
        subject_id = row['subject_id']
        img_filenames = [row['img1'], row['img2'], row['img3']]
        
        print(f"Processing subject {subject_id}...")
        pdf_page = create_page_with_images(s3_client, bucket, subject_id, img_filenames)
        
        if pdf_page:
            merger.append(pdf_page)
            pages_processed += 1

    if pages_processed == 0:
        print("No pages were successfully processed.")
        return False
    
    try:
        merger.write(output_path)
        merger.close()
        print(f"\nSuccessfully created PDF with {pages_processed} pages: {output_path}")
        return True
    except Exception as e:
        print(f"Error writing output PDF: {e}")
        return False


def main():
    args = parse_arguments()
    
    # Get rows from CSV
    rows = read_rows_from_csv(args.csv)
    
    # Get all unique filenames
    all_filenames = []
    for row in rows:
        for img in [row['img1'], row['img2'], row['img3']]:
            if img and img not in all_filenames:
                all_filenames.append(img)

    print(f"Processing {len(rows)} subjects with {len(all_filenames)} unique files from bucket '{args.bucket}'")

    # Initialize S3 client
    s3_client = boto3.client('s3')

    # Check storage class for each file
    print("\nChecking storage classes...")
    storage_info = {}
    glacier_files = []
    standard_files = []
    missing_files = []
    
    for filename in all_filenames:
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
        success = create_pdf_from_rows(s3_client, args.bucket, rows, args.output)
        
        if success:
            sys.exit(0)
        else:
            sys.exit(1)
    else:
        print("\nNo files available in standard storage.")
        sys.exit(1)


if __name__ == '__main__':
    main()