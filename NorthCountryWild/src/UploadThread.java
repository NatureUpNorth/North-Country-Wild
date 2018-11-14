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
	private static final String ACCESS_TOKEN = "Ot337FVMgnAAAAAAAAAAsbxu_FAGR3s-rifTdgzY9-mIjanUH1hPKX6f9Jnb4pAP"; //"ILJ9haPVAAAAAAAAAAAAR7cBhQSEWdj0K4CkmEPrTYii1sCbJsZ1StCB8sO2YT4k";//access token for info@natureupnorth.org dropbox
	private volatile boolean uploading;
	private static ArrayList<Metadata> meta = new ArrayList<Metadata>();
	private LoadingWindow loading;
	private UploadWindow upload;
	private volatile boolean running = true;
	private final String DEGREE  = "\u00b0";
	private String habitat;
	private String urbanized;
	private String affiliation;
	private String startDate;
	private String endDate;
	private boolean interrupted;
	
	
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
	
	private void write(ArrayList<Metadata> meta, String method, String fileName) {

    	FileWriter fileWriter = null; 
    	
    	try{
    		fileWriter = new FileWriter(fileName + "/metadata.csv");

    		for (Directory directory : meta.get(0).getDirectories()) {

    			for (Tag tag : directory.getTags()) {
    				fileWriter.append(tag.getTagName()+",");
    			}
    		}
    		fileWriter.append("Affiliation,Longitude,Latitude,Habitat,Urban,Start Date,End Date,File Path");
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
    			String lat = upload.getLat();
    			String lon = upload.getLon();
    			if(lat.contains("\"")) {
    				lat = DMStoDD(lat);
    			}
    			if(lon.contains("\"")) {
    				lon = DMStoDD(lon);
    			}
    			fileWriter.append(affiliation+","+lat+","+lon+","+habitat+","+urbanized+","+startDate+","+endDate+","+destinationPath);
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
		upload(filePath, destinationPath);
	}
	
	public void terminate() {
		running = false;
	}
	
	public void upload(String filePath, String destinationPath) {
		// Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);  
		
		File dir = null;
		try {
        	dir = new File(filePath);
    		for(File file: dir.listFiles()){
    			if (file.isDirectory()) {
    				upload(file.getAbsolutePath(), destinationPath);
    			} else if(ImageIO.read(file) != null && loading.isUploading()) {
	    			Metadata metadata = ImageMetadataReader.readMetadata(file);
	    			meta.add(metadata);
    			} else if (!loading.isUploading()) {
    				interrupted = true;
    			}
    		}
            
        } catch (ImageProcessingException e) {
        } catch (IOException e) {
        }     

		// upload pics and csv file to dropbox client
		for(File file: dir.listFiles()) {
			try (InputStream in = new FileInputStream(file.getAbsolutePath())) {
				try {
					if(ImageIO.read(file) != null && loading.isUploading()) {
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
				in = new FileInputStream(filePath + "/metadata.csv");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				client.files().uploadBuilder("/" + destinationPath + "/metadata.csv").uploadAndFinish(in);
			} catch (DbxException | IOException e) {
			}
		}
        
        // delete the csv from the user's files
        try {
			Files.deleteIfExists(Paths.get(filePath + "/metadata.csv"));
		} catch (IOException e) {
		}
        if (filePath.equals(this.filePath)) {
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
	
	public String DMStoDD(String dms) {
		int x = dms.indexOf(DEGREE);
		String deg = dms.substring(0, x);
		int y = dms.indexOf("'");
		String min = dms.substring(x+1, y);
		int z = dms.indexOf("\"");
		String sec = dms.substring(y+1, z);
		double d = Double.parseDouble(deg);
		double m = Double.parseDouble(min);
		double s = Double.parseDouble(sec);
		Double dd = d + (m/60.0) + (s/3600.0);
		DecimalFormat df4 = new DecimalFormat("#.##");
		dd = Double.valueOf(df4.format(dd));
		return dd.toString();
	}
	
}
