package com.drew.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MetadataExtractor {
	
	static ArrayList<Metadata> meta = new ArrayList<Metadata>();
	
    public static void main(String[] args) {
    	
        try {
        	File dir = new File("/Users/remileblanc/Dropbox/Nature Up North/dog and human");
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
    		String fileName = System.getProperty("user.home")+"/Desktop/MetadataTest3.csv";
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
    					fileWriter.append(tag.getDescription().replaceAll(",", " ")+",");
    				}
    				for (String error : directory.getErrors()) {
        				System.err.println("ERROR: " + error);
        			}
    			}
    			fileWriter.append("\n");
    		}

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