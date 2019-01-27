import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {

	public static void main(String[] args) {
		SplashScreen splash = new SplashScreen();
		while(splash.isOpen());
		splash.close();
		
		LoginWindow login = new LoginWindow();
		while (login.isOpen());  // wait for them to submit before closing the window
		String name = login.getName();
		login.close();
		
		// a new comment
		
		UploadWindow upload = new UploadWindow();
		while (true) {
			while (!upload.isUploading());  // wait for them to hit submit
			upload.disable();
			String affiliation = upload.getGroup();
			String time = String.valueOf(System.nanoTime());
			String destination = affiliation + "/" + name + "/" + time;
			String filepath = upload.getFilepath();
			String hab = "";
			String startDate = upload.getStartDate();
			String endDate = upload.getEndDate();
			//System.out.println(upload.getHabitats().get(0));
			Iterator<String> iter = upload.getHabitats().iterator();
			while (iter.hasNext()) {
			    String str = iter.next();
			    hab = hab+str+" & ";
			}
			//for(String s: upload.habitats) {hab = hab.concat(s+ " & ");}
			if(hab.length()>3) {hab = hab.substring(0, hab.length()-3);}
			String urb = "";
			//for(String s: upload.urbanized) {urb = urb.concat(s);}
			urb = upload.getUrban();
			UploadThread thread = new UploadThread(filepath, destination, upload, upload.getCount(), hab, urb, affiliation, startDate, endDate);
			thread.start();
			while(thread.isUploading()) {
				if (!upload.isUploading()) {
					thread.terminate();
				}
			};  // wait for it to be done uploading files
			upload.reset();
			upload.setUploading(false);
			if(!thread.wasInterrupted()) {
			JOptionPane.showMessageDialog(new JFrame(),
					"Congratulations! You have successfully uploaded your images to Nature Up North's Dropbox!\nYou may either continue"
					+ " on the app and submit more photos, or close out of the app if you are done.\n\n"
					+ "Thank you for your contributions to science!");
			} else {
				JOptionPane.showMessageDialog(new JFrame(),
						"Your upload has been cancelled.");
			}
			upload.enable();
		}
	}

}
