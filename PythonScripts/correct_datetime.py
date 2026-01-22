#!/usr/bin/env python3
"""
Camera Trap Image Timestamp Corrector
Fixes incorrect EXIF timestamps in game camera images while preserving time intervals
"""

import os
from datetime import datetime, timedelta
from pathlib import Path
import piexif
from PIL import Image

def get_image_datetime(image_path):
    """
    Extract the datetime from image EXIF data
    
    Args:
        image_path: Path to the image file
        
    Returns:
        datetime object or None if timestamp cannot be read
    """
    try:
        # Open the image file
        img = Image.open(image_path)
        
        # Load the EXIF data from the image
        # EXIF data contains metadata like camera settings, GPS, and timestamps
        exif_dict = piexif.load(img.info.get('exif', b''))
        
        # Try to get datetime from EXIF - cameras store this in different fields
        # First try DateTimeOriginal (when photo was taken)
        if piexif.ExifIFD.DateTimeOriginal in exif_dict['Exif']:
            dt_str = exif_dict['Exif'][piexif.ExifIFD.DateTimeOriginal].decode('utf-8')
        # If that's not available, try the general DateTime field
        elif piexif.ImageIFD.DateTime in exif_dict['0th']:
            dt_str = exif_dict['0th'][piexif.ImageIFD.DateTime].decode('utf-8')
        else:
            # No timestamp found in EXIF data
            return None
        
        # Parse the datetime string from EXIF format (YYYY:MM:DD HH:MM:SS)
        # into a Python datetime object
        return datetime.strptime(dt_str, "%Y:%m:%d %H:%M:%S")
    
    except Exception as e:
        # If anything goes wrong (corrupt EXIF, missing file, etc.), print error
        print(f"Error reading {image_path}: {e}")
        return None

def set_image_datetime(image_path, new_datetime, output_path=None):
    """
    Set the datetime in image EXIF data
    
    Args:
        image_path: Path to the original image
        new_datetime: New datetime to write (datetime object)
        output_path: Where to save the modified image (None = overwrite original)
        
    Returns:
        True if successful, False otherwise
    """
    try:
        # Open the original image
        img = Image.open(image_path)
        
        # Load existing EXIF data (we want to preserve other metadata)
        exif_dict = piexif.load(img.info.get('exif', b''))
        
        # Format the new datetime in EXIF format (YYYY:MM:DD HH:MM:SS)
        dt_str = new_datetime.strftime("%Y:%m:%d %H:%M:%S")
        
        # Convert string to bytes (EXIF data is stored as bytes)
        dt_bytes = dt_str.encode('utf-8')
        
        # Update all three datetime fields in EXIF to ensure consistency
        # DateTimeOriginal: when the photo was originally taken
        exif_dict['Exif'][piexif.ExifIFD.DateTimeOriginal] = dt_bytes
        # DateTimeDigitized: when the photo was digitized (same as original for digital cameras)
        exif_dict['Exif'][piexif.ExifIFD.DateTimeDigitized] = dt_bytes
        # DateTime: general modification time
        exif_dict['0th'][piexif.ImageIFD.DateTime] = dt_bytes
        
        # Convert the EXIF dictionary back to bytes for saving
        exif_bytes = piexif.dump(exif_dict)
        
        # Determine where to save: original location or specified output path
        if output_path is None:
            output_path = image_path
        
        # Save the image with the updated EXIF data
        # quality=95 maintains high image quality (scale 0-100)
        img.save(output_path, exif=exif_bytes, quality=95)
        
        return True
    
    except Exception as e:
        # If anything goes wrong during writing, print error
        print(f"Error writing {image_path}: {e}")
        return False

def correct_timestamps(folder_path, correct_first_datetime, output_folder=None, 
                      image_extensions=None, create_backup=True):
    """
    Correct timestamps for all images in a folder while preserving time intervals
    
    Example:
        Image 1: 2020-01-01 12:00:00 (incorrect) → 2024-11-09 13:20:00 (correct)
        Image 2: 2020-01-01 12:03:25 (incorrect) → 2024-11-09 13:23:25 (correct)
        Time difference preserved: 3 minutes 25 seconds
    
    Args:
        folder_path: Path to folder containing images
        correct_first_datetime: Correct datetime for the first image (datetime object or string)
        output_folder: Optional output folder (if None, modifies images in place)
        image_extensions: List of image extensions to process (default: ['.jpg', '.jpeg', '.JPG', '.JPEG'])
        create_backup: If True and output_folder is None, creates backup of original files
    """
    
    # Set default image extensions if none provided
    if image_extensions is None:
        image_extensions = ['.jpg', '.jpeg', '.JPG', '.JPEG']
    
    # Convert string to datetime object if user provided a string
    if isinstance(correct_first_datetime, str):
        correct_first_datetime = datetime.strptime(correct_first_datetime, "%Y-%m-%d %H:%M:%S")
    
    # Convert folder path to Path object for easier file handling
    folder = Path(folder_path)
    
    # Find all image files in the folder with the specified extensions
    image_files = []
    for ext in image_extensions:
        # glob finds all files matching the pattern (e.g., *.jpg)
        image_files.extend(folder.glob(f"*{ext}"))
    
    # Sort files alphabetically to ensure consistent ordering
    # This is crucial - the first file in sorted order becomes our reference point
    image_files.sort()
    
    # Check if we found any images
    if not image_files:
        print(f"No images found in {folder_path}")
        return
    
    print(f"Found {len(image_files)} images")
    
    # Read the timestamp from the first image (our reference point)
    first_image = image_files[0]
    first_original_datetime = get_image_datetime(first_image)
    
    # Make sure we could read the timestamp
    if first_original_datetime is None:
        print(f"Could not read timestamp from first image: {first_image}")
        return
    
    # Calculate the time offset between incorrect and correct timestamps
    # Example: If original is 2020-01-01 12:00:00 and correct is 2024-11-09 13:20:00
    #          offset = ~5 years, 10 months, 8 days, 1 hour, 20 minutes
    time_offset = correct_first_datetime - first_original_datetime
    
    # Display information about the correction to the user
    print(f"\nFirst image: {first_image.name}")
    print(f"Original timestamp: {first_original_datetime}")
    print(f"Correct timestamp: {correct_first_datetime}")
    print(f"Time offset: {time_offset}")
    print(f"\nProcessing images...\n")
    
    # Create output folder if user specified one
    if output_folder:
        output_path = Path(output_folder)
        output_path.mkdir(parents=True, exist_ok=True)  # Create folder and any parent folders
    
    # Process each image file
    success_count = 0  # Track how many images we successfully processed
    for img_file in image_files:
        
        # Read the original (incorrect) timestamp from this image
        original_dt = get_image_datetime(img_file)
        if original_dt is None:
            # Skip this image if we can't read its timestamp
            continue
        
        # Calculate the corrected timestamp by applying the same offset
        # This preserves the time interval between all images
        # Example: If image 2 was 3min 25sec after image 1, it stays 3min 25sec after
        corrected_dt = original_dt + time_offset
        
        # Determine where to save the corrected image
        if output_folder:
            # Save to output folder with same filename
            out_path = Path(output_folder) / img_file.name
        else:
            # Modifying in place
            if create_backup:
                # Create backup by renaming original file (e.g., image.jpg → image.jpg.bak)
                backup_path = img_file.with_suffix(img_file.suffix + '.bak')
                img_file.replace(backup_path)
                # Now save the corrected version with the original name
                out_path = img_file.parent / backup_path.stem
            else:
                # Overwrite the original file (no backup)
                out_path = img_file
        
        # Write the corrected timestamp to the image
        if set_image_datetime(img_file, corrected_dt, out_path):
            # Success! Print the correction that was made
            print(f"✓ {img_file.name}: {original_dt} → {corrected_dt}")
            success_count += 1
        else:
            # Failed to update this image
            print(f"✗ {img_file.name}: Failed to update")
    
    # Print summary of what was accomplished
    print(f"\n{'='*60}")
    print(f"Successfully processed {success_count}/{len(image_files)} images")
    if output_folder:
        print(f"Corrected images saved to: {output_folder}")
    elif create_backup:
        print(f"Backup files created with .bak extension")

# This code only runs if the script is executed directly (not imported as a module)
if __name__ == "__main__":
    import sys
    
    # Check if user provided command line arguments
    if len(sys.argv) < 3:
        # Not enough arguments - print usage instructions
        print("Usage: python script.py <folder_path> <correct_datetime> [output_folder]")
        print("Example: python script.py ./images '2024-11-09 13:20:00' ./corrected")
        print("\nExample scenario:")
        print("  Image 1 shows: 2020-01-01 12:00:00 (wrong)")
        print("  Image 2 shows: 2020-01-01 12:03:25 (wrong)")
        print("  You provide: 2024-11-09 13:20:00 (correct time for image 1)")
        print("  Result:")
        print("    Image 1 corrected to: 2024-11-09 13:20:00")
        print("    Image 2 corrected to: 2024-11-09 13:23:25 (interval preserved!)")
        print("\nOr edit the script to set these values:")
        print()
        
        # USER CONFIGURATION - Edit these values to run without command line arguments
        FOLDER_PATH = "./camera_images"  # Path to your images
        CORRECT_FIRST_DATETIME = "2024-11-09 13:20:00"  # Correct datetime for first image
        OUTPUT_FOLDER = None  # Set to folder path or None to modify in place
        
        # Show current configuration
        print(f"Current settings:")
        print(f"  Folder: {FOLDER_PATH}")
        print(f"  Correct first datetime: {CORRECT_FIRST_DATETIME}")
        print(f"  Output folder: {OUTPUT_FOLDER or 'Modify in place'}")
        sys.exit(1)
    
    # Parse command line arguments
    folder = sys.argv[1]  # First argument: folder path
    correct_dt = sys.argv[2]  # Second argument: correct datetime string
    output = sys.argv[3] if len(sys.argv) > 3 else None  # Third argument (optional): output folder
    
    # Run the timestamp correction
    correct_timestamps(folder, correct_dt, output)