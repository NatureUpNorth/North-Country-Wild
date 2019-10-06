import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

public class UploadThread extends Thread {
	
	private String filePath;
	private String destinationPath;
	private int total_files;
	private int count;
	private static final String ACCESS_TOKEN = "ILJ9haPVAAAAAAAAAAABPD4L9Dh7CtKvyUoh1gh--sQzmSL_aaM7bxpU1-QxExrR";//"ILJ9haPVAAAAAAAAAAAAR7cBhQSEWdj0K4CkmEPrTYii1sCbJsZ1StCB8sO2YT4k"; //"ILJ9haPVAAAAAAAAAAAAR7cBhQSEWdj0K4CkmEPrTYii1sCbJsZ1StCB8sO2YT4k";//access token for info@natureupnorth.org dropbox
	private volatile boolean uploading;
	private static ArrayList<Metadata> meta = new ArrayList<Metadata>();
	private LoadingWindow loading;
	private TestWindow upload;
	private volatile boolean running = true;
	private ArrayList<String> values;
	private static String path;
	/*
	private String habitat;
	private String urbanized;
	private String startDate;
	private String endDate;
	*/
	private boolean interrupted;
	
	/*
	UploadThread(String path, String destination, UploadWindow uw, int files, String habit, String urba, String a, String st, String en) {
		filePath = path;
		destinationPath = destination;
		uploading = true;
		loading = new LoadingWindow();
		upload = uw;
		total_files = files;
		habitat = habit;
		urbanized = urba;
		affiliation = a;
		startDate = st;
		endDate = en;
		interrupted = false;
	}
	*/
	
	UploadThread(String path, String destination, TestWindow uw, int files, ArrayList<String> values) {
		filePath = path;
		destinationPath = destination;
		uploading = true;
		loading = new LoadingWindow();
		upload = uw;
		total_files = files;
		this.values = values;
		interrupted = false;
	}
	
	private void write(ArrayList<Metadata> meta, String method, String fileName) {

    	FileWriter fileWriter = null; 
    	
    	try{
    		fileWriter = new FileWriter(fileName + File.separator + "metadata.csv");

    		for (Directory directory : meta.get(0).getDirectories()) {

    			for (Tag tag : directory.getTags()) {
    				fileWriter.append(tag.getTagName()+",");
    			}
    		}
    		
    		for (int i = 0; i < values.size(); i+=2) {
    			fileWriter.append(values.get(i)+",");
    		}
    		//fileWriter.append("Affiliation,Longitude,Latitude,Habitat,Urban,Start Date,End Date,File Path");
    		fileWriter.append("\n");
    		
    		for(Metadata metadata : meta){
    			for(Directory directory : metadata.getDirectories()){
    				for(Tag tag : directory.getTags()){
    					if(tag.getDescription()!=null){
    						fileWriter.append(tag.getDescription().replaceAll(",", " ")+",");
    					} else {
    						fileWriter.append(",");
    					}
    				}
    				for (String error : directory.getErrors()) {
    					System.err.println("ERROR: " + error);
        			}
    			}
    			for (int i = 1; i < values.size(); i+=2) {
    				fileWriter.append(values.get(i)+",");
        			System.out.println(values.get(i));

    			}
    			//fileWriter.append(affiliation+","+lat+","+lon+","+habitat+","+urbanized+","+startDate+","+endDate+","+destinationPath);
    			fileWriter.append("\n");
    		}

    	} 
    	catch(Exception e){
    		System.out.println("Error in CsvFileWriter");
    		e.printStackTrace();
    	} finally{
    		try{
    			fileWriter.flush();
    			fileWriter.close();
    		} catch(IOException e){
    			e.printStackTrace();
    		}
    	}
	}
	
	
	public void run() {
		count = 0;
		upload(path, destinationPath);
	}
	
	public void terminate() {
		running = false;
	}
	
	private File[] getFiles(File dir) {
		File[] files = new File[total_files];
		int k = 0;
		for (File file: dir.listFiles()) {
			if (file.isDirectory()) {
				File[] new_files = getFiles(file);
				for (int i = 0; i < new_files.length; i++) {
					try {
						new_files[i].getAbsolutePath();  // i don't get this bs but this needs to be here
						files[k] = new_files[i];
						k++;
					} catch (NullPointerException e) {
						break;
					}
				}
			} else
				try {
					if(ImageIO.read(file) != null && loading.isUploading() && file != null) {
						Metadata metadata = ImageMetadataReader.readMetadata(file);
						meta.add(metadata);
						files[k] = file;
						k++;
					}
				} catch (ImageProcessingException | IOException e) {
				}
		}
		return files;
	}
	
	public void upload(String filePath, String destinationPath) {
		// Create Dropbox client
		DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
		File dir = new File(filePath);
		File[] files = getFiles(dir);

		// upload pics and csv file to dropbox client
		for(int i = 0; i < files.length; i++) {
			File file = files[i];
			try (InputStream in = new FileInputStream(file.getAbsolutePath())) {
				try {
					if(loading.isUploading()) {
						if (running) {
							client.files().uploadBuilder("/" + destinationPath + "/" + file.getName()).uploadAndFinish(in);
							count++;
							loading.changeBar(total_files, count, "Uploading " + file.getAbsolutePath());
						} else {
							loading.close();
							break;
						}
					} else if (!loading.isUploading()) {
	    				interrupted = true;
	    			}
				} catch (DbxException e) {
				}
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
        
		if (count == total_files) {
			write(meta, "Using ImageMetadataReader", filePath);
			InputStream in = null;
			try {
				in = new FileInputStream(filePath + File.separator + "metadata.csv");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				client.files().uploadBuilder("/" + destinationPath + "/metadata.csv").uploadAndFinish(in);
				in.close();
			} catch (DbxException | IOException e) {
			}
			
		}
        
        // delete the csv from the user's files
		String userMetadataPath = path + File.separator + "metadata.csv";
		File userMetadataFile = new File(userMetadataPath);
		try(FileWriter fileWriter = new FileWriter(userMetadataFile)) {
		    String fileContent = "";
		    fileWriter.write(fileContent);
		    fileWriter.close();
		} catch (IOException e) {
		    // exception handling
		}
		
		if (userMetadataFile.exists()) {
			userMetadataFile.delete();
		}
		if (filePath.equals(path)) {
        	uploading = false;
        }
        
        // closing the loading bar window
        loading.close();
        meta.clear();
    }
	
	public boolean wasInterrupted() {
		return interrupted;
	}
	
	public boolean isUploading() {
		return uploading;
	}
	
	public static void setPath(String filepath) {
		path = filepath;
	}
	
}
