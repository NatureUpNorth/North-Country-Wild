// Runs the application.

import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {

	/**
	 * @param args
	 */
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
		
		run(name);			
		
		
	}
	private static void run(String name) {
		TestWindow upload = new TestWindow();
		while (true) {
			while (!upload.isUploading() ) {};  // wait for them to hit submit
			upload.disable();
			String affiliation = "";
			String filepath = "";
			ArrayList<String> values = upload.getValues();
			for (int i = 0; i <values.size(); i++) {
				if (values.get(i).equals("filepath")) {
					filepath = values.get(i + 1);
				} else if (values.get(i).equals("Affiliation")) {
					affiliation = values.get(i + 1);
					
				}
			}
			if(affiliation.equals("Other")) {
				String s = PanelDropdown.getOther();
				affiliation = affiliation+"/"+s;
			}
			String time = String.valueOf(System.nanoTime());
			String destination = affiliation + "/" + name + "/" + time;
			
			UploadThread thread = new UploadThread(filepath, destination, upload, upload.getCount(), values);
			thread.start();
			while(thread.isUploading()) {
				if (!upload.isUploading()) {
					thread.terminate();
				}
			};  // wait for it to be done uploading files

			upload.setUploading(false);
			if(!thread.wasInterrupted()) {
				JOptionPane.showMessageDialog(new JFrame(),
						"Congratulations! You have successfully uploaded your images to Nature Up North's Dropbox!\nYou may either continue"
						+ " on the uploader and submit more photos, or close out of the uploader if you are done.\n\n"
						+ "Thank you for your contributions to science!");
			
			} else {
				JOptionPane.showMessageDialog(new JFrame(),
						"Your upload has been cancelled.");
			}
			upload.enable();
			upload.clear();
			upload.refreshPane();
			run(name);
			
		}
		
	}

}
