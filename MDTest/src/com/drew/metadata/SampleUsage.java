package com.drew.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.metadata.adobe.AdobeJpegReader;
import com.drew.metadata.eps.EpsDirectory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.icc.IccReader;
import com.drew.metadata.iptc.IptcReader;
import com.drew.metadata.jfif.JfifReader;
import com.drew.metadata.jfxx.JfxxReader;
import com.drew.metadata.jpeg.JpegCommentReader;
import com.drew.metadata.jpeg.JpegDhtReader;
import com.drew.metadata.jpeg.JpegDnlReader;
import com.drew.metadata.jpeg.JpegReader;
import com.drew.metadata.photoshop.DuckyReader;
import com.drew.metadata.photoshop.PhotoshopReader;
import com.drew.metadata.xmp.XmpReader;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Showcases the most popular ways of using the metadata-extractor library.
 * <p>
 * For more information, see the project wiki: https://github.com/drewnoakes/metadata-extractor/wiki/GettingStarted
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class SampleUsage
{
    public static void main(String[] args)
    {
        File file = new File("/Users/remileblanc/Dropbox/Nature Up North/dog and human/IMG_0001.JPG");
      
        // There are multiple ways to get a Metadata object for a file

        //
        // SCENARIO 1: UNKNOWN FILE TYPE
        //
        // This is the most generic approach.  It will transparently determine the file type and invoke the appropriate
        // readers.  In most cases, this is the most appropriate usage.  This will handle JPEG, TIFF, GIF, BMP and RAW
        // (CRW/CR2/NEF/RW2/ORF) files and extract whatever metadata is available and understood.
        //
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            print(metadata, "Using ImageMetadataReader");
        } catch (ImageProcessingException e) {
            print(e);
        } catch (IOException e) {
            print(e);
        }

        
        
//        //
//        // SCENARIO 2: SPECIFIC FILE TYPE
//        //
//        // If you know the file to be a JPEG, you may invoke the JpegMetadataReader, rather than the generic reader
//        // used in approach 1.  Similarly, if you knew the file to be a TIFF/RAW image you might use TiffMetadataReader,
//        // PngMetadataReader for PNG files, BmpMetadataReader for BMP files, or GifMetadataReader for GIF files.
//        //
//        // Using the specific reader offers a very, very slight performance improvement.
//        //
//        try {
//        	Metadata metadata = JpegMetadataReader.readMetadata(file);
//
//        	print(metadata, "Using JpegMetadataReader");
//        } catch (JpegProcessingException e) {
//        	print(e);
//        } catch (IOException e) {
//        	print(e);
//        }
//
//        //
//        // APPROACH 3: SPECIFIC METADATA TYPE
//        //
//        // If you only wish to read a subset of the supported metadata types, you can do this by
//        // passing the set of readers to use.
//        //
//        // This currently only applies to JPEG file processing.
//        //
//        try {
//        	// We are only interested in handling
//        	Iterable<JpegSegmentMetadataReader> readers = Arrays.asList(
//            
//                    new ExifReader(),
//                    new IptcReader()
//        			);
//
//        	Metadata metadata = JpegMetadataReader.readMetadata(file, readers);
//
//        	print(metadata, "Using JpegMetadataReader for Exif and IPTC only");
//        } catch (JpegProcessingException e) {
//        	print(e);
//        } catch (IOException e) {
//        	print(e);
//        }
    }

    /**
     * Write all extracted values to stdout.
     */
    private static void print(Metadata metadata, String method)
    {
    	System.out.println();
    	System.out.print("-------------------------------------------------");
    	System.out.print(method);
    	System.out.println("-------------------------------------------------");
    	System.out.println();

    	//
    	// A Metadata object contains multiple Directory objects
    	//
    	
//    	Directory directory = metadata.get
//        if( directory != null )
//        {
//            // Read the date
//            Date date = directory.getDate( EpsDirectory );
//            DateFormat df = DateFormat.getDateInstance();
//            df.format( date );
//            int year = df.getCalendar().get( Calendar.YEAR );
//            int month = df.getCalendar().get( Calendar.MONTH ) + 1;
//
//            System.out.println( "Year: " + year + ", Month: " + month );
//
//            System.out.println( "Date: " + date );
//
//            System.out.println( "Tags" );
//        }
//    	
    	for (Directory directory : metadata.getDirectories()) {

    		//
    		// Each Directory stores values in Tag objects
    		//
    		for (Tag tag : directory.getTags()) {
    			System.out.println(tag);
//    			if(tag.getTagName().equals("Date/Time")){
//    			System.out.print(tag.getTagName() + " - ");
//    			System.out.println(tag.getDescription());
//    			}
    		}

    		//
    		// Each Directory may also contain error messages
    		//
    		for (String error : directory.getErrors()) {
    			System.err.println("ERROR: " + error);
    		}
    	}
    }

    private static void print(Exception exception)
    {
        System.err.println("EXCEPTION: " + exception);
    }
}