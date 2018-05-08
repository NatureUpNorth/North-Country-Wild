
public class Main {

	public static void main(String[] args) {
		SplashScreen splash = new SplashScreen();
		while(splash.isOpen());
		splash.close();
		
		LoginWindow login = new LoginWindow();
		while (login.isOpen());  // wait for them to submit before closing the window
		String name = login.getName();
		login.close();
		
		UploadWindow upload = new UploadWindow();
		while (true) {
			while (!upload.isUploading());  // wait for them to hit submit
			String date = upload.getStartDate();
			String affiliation = upload.getGroup();
			String destination = affiliation + "/" + name + "/" + date;
<<<<<<< HEAD
			System.out.println(upload.getFilepath()+" :Path");
			UploadThread thread = new UploadThread(upload.getFilepath(), destination, upload, upload.getCount());
=======
			String filepath = upload.getFilepath();
			UploadThread thread = new UploadThread(filepath, destination, upload, upload.getCount());
>>>>>>> 914adc478936aa6e7c13f23becae2059cc995b5e
			thread.start();
			while(thread.isUploading()) {
				if (!upload.isUploading()) {
					thread.terminate();
				}
			};  // wait for it to be done uploading files
			upload.setUploading(false);
		}
	}

}
