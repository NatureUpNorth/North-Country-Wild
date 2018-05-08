import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {

	public static void main(String[] args) {
//		SplashScreen splash = new SplashScreen();
//		while(splash.isOpen());
//		splash.close();
//		
//		LoginWindow login = new LoginWindow();
//		while (login.isOpen());  // wait for them to submit before closing the window
//		String name = login.getName();
//		login.close();
		
		String name = "Remi LeBlanc";
		UploadWindow upload = new UploadWindow();
		while (true) {
			while (!upload.isUploading());  // wait for them to hit submit
			String affiliation = upload.getGroup();
			String time = String.valueOf(System.nanoTime());
			String destination = affiliation + "/" + name + "/" + time;
			String filepath = upload.getFilepath();
			String hab = "";
			for(String s: upload.habitats) {hab = hab.concat(s+ " & ");}
			if(hab.length()>3) {hab = hab.substring(0, hab.length()-3);}
			String urb = "";
			for(String s: upload.urbanized) {urb = urb.concat(s);}
			UploadThread thread = new UploadThread(filepath, destination, upload, upload.getCount(), hab, urb, affiliation);
			thread.start();
			while(thread.isUploading()) {
				if (!upload.isUploading()) {
					thread.terminate();
				}
			};  // wait for it to be done uploading files
			upload.setUploading(false);
			if(upload.cancelled()==0) {
			JOptionPane.showMessageDialog(new JFrame(),
					"Congratulations! You have successfully uploaded your images to Nature Up North's Dropbox!\nYou may either continue"
					+ " on the app and submit more photos, or close out of the app if you are done.\n\n"
					+ "Thank you for your contributions to science!");
			} else if(upload.cancelled()>0){
				JOptionPane.showMessageDialog(new JFrame(),
						"Your upload has been cancelled.");
			}
		}
	}

}
