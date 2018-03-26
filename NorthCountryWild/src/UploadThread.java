import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadErrorException;

public class UploadThread extends Thread {
	
	private String filePath = "";
	private String destinationPath = "";
	private static final String ACCESS_TOKEN = "kdJxVuoW-DAAAAAAAAAADR6XusrUze5Zz-H40SyBBAOLazBmHvzNP8_qZbH_18Bx";
	private volatile boolean uploading;
	private static ArrayList<Metadata> meta = new ArrayList<Metadata>();
	
	UploadThread(String path, String destination) {
		filePath = path;
		destinationPath = destination;
		uploading = true;
	}
	
	private void write(ArrayList<Metadata> meta, String method) {

    	FileWriter fileWriter = null; 
    	try{
    		String fileName = filePath;
    		fileWriter = new FileWriter(fileName + "/metadata.csv");

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
	
	
	public void run() {
		File dir = null;
		try {
        	dir = new File(filePath);
    		for(File file: dir.listFiles()){
    			if (file.getName().endsWith(".JPG")) {
	    			Metadata metadata = ImageMetadataReader.readMetadata(file);
	    			meta.add(metadata);
    			}
    		}
    		write(meta, "Using ImageMetadataReader");
            
        } catch (ImageProcessingException e) {
        } catch (IOException e) {
        }
		
		// Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);       
        
        // upload pics and csv file to dropbox client
        for(File file: dir.listFiles()) {
        	try (InputStream in = new FileInputStream(file.getAbsolutePath())) {
        		try {
					client.files().uploadBuilder(destinationPath + "/" + file.getName()).uploadAndFinish(in);
				} catch (DbxException e) {
				}
        	} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
        
        // delete the csv from the user's files
        try {
			Files.deleteIfExists(Paths.get(filePath + "/metadata.csv"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        uploading = false;
	}
	
	public boolean isUploading() {
		return uploading;
	}
	
}
