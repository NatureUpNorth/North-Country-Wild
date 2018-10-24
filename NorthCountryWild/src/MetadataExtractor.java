
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MetadataExtractor {
	
	private static ArrayList<Metadata> meta = new ArrayList<Metadata>();
	//private static String directories = "JPEG Exif IFD0 Exif SubIFD Huffman File Type File";
	//Interoperability, Reconyx HyperFire Makernote, Exif Thumbnail, GPS   
	
	//*JPEG: all data and all common
	//*Exif IFD0: all data and not all common (Make = RECONYX is good, few different ones make it not common)
	//*Exif SubIFD: all data and not all common (same not common as Exif IFD0)
	//Huffman: all data and all common
	//*File Type: all data and all common
	//*File: all data and all common
	//Interoperability: not all data and not all common
	//*Reconyx HyperFire Makernote: not all data and all common 
	//Exif Thumbnail: not all data and not all common
	//GPS: not all data and all common
	
    public static void main(String[] args) {
    	
        try {
        	File dir = new File("/Users/remileblanc/Desktop/Nature Up North/No animals 2");
    		for(File file: dir.listFiles()){
    			Metadata metadata = ImageMetadataReader.readMetadata(file);
    			meta.add(metadata);
    		}
    		write(meta, "Using ImageMetadataReader");
            
        } catch (ImageProcessingException e) {
            print(e);
        } catch (IOException e) {
            print(e);
        }
    }

    private static void write(ArrayList<Metadata> meta, String method) {

    	FileWriter fileWriter = null; 
    	try{
    		String fileName = System.getProperty("user.home")+"/Desktop/Nature Up North/Excel docs/MetadataTest.csv";
    		fileWriter = new FileWriter(fileName);

    		for (Directory directory : meta.get(0).getDirectories()) {

    			for (Tag tag : directory.getTags()) {
    				fileWriter.append(tag.getTagName()+",");
    			}
    		}
    		

    		fileWriter.append("\n");
    		
    		for(Metadata metadata : meta){
    			for(Directory directory : metadata.getDirectories()){
    				for(Tag tag : directory.getTags()){
    					if(tag.getDescription()!=null){
//    						if(tag.getTagName().equals("Model")){
//    							if(!tag.getDescription().equals("HC600 HYPERFIREv")){
//    								System.out.println(tag.getDescription());
//    							}
//    						}
    						fileWriter.append(tag.getDescription().replaceAll(",", " ")+",");
    					} else {
    						fileWriter.append(",");
    					}
    				}
    				for (String error : directory.getErrors()) {
    					System.err.println("ERROR: " + error);
        			}
    			}
    			fileWriter.append("\n");
    		}
    		
//			for (Directory directory : meta.get(0).getDirectories()) {
//				if(directories.contains(directory.getName())){
//					for (Tag tag : directory.getTags()) {
//						fileWriter.append(tag.getTagName()+",");
//					}
//				}
//			}
//			
//			fileWriter.append("\n");
//    		
//    		for(Metadata metadata : meta){    			
//    			for(Directory directory : metadata.getDirectories()){
//    				if(directories.contains(directory.getName())){
//    					//fileWriter.append(directory.getName()+",");
//    					for(Tag tag : directory.getTags()){
//    						if(tag.getDescription()!=null){
//    							fileWriter.append(tag.getDescription().replaceAll(",", " ")+",");
//    						} else{
//    							fileWriter.append(",");
//    						}
//    					}
//    				}
//    				for (String error : directory.getErrors()) {
//    					System.err.println("ERROR: " + error);
//        			}
//    		}
//    			fileWriter.append("\n");
//    		}
//    		
//
    		System.out.println("CSV file was created successfully");

    	} 
    	catch(Exception e){
    		System.out.println("Error in CsvFileWriter");
    		e.printStackTrace();
    	} finally{
    		try{
    			fileWriter.flush();
    			fileWriter.close();
    		} catch(IOException e){
    			System.out.println("Error while flushing/closing fileWriter");
    			e.printStackTrace();
    		}
    	}
	}

    private static void print(Exception exception) {
        System.err.println("EXCEPTION: " + exception);
    }
}