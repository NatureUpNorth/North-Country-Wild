
public class Main {

	public static void main(String[] args) {
		LoginWindow login = new LoginWindow();
		while (login.isOpen());  // wait for them to submit before closing the window
		String name = login.getName();
		login.close();
		UploadWindow upload = new UploadWindow();
		while (true) {
			while (!upload.isUploading());  // wait for them to hit submit
			LoadingWindow loading = new LoadingWindow();
			String date = upload.getStartDate();
			String destination = name + "/" + date;
			UploadThread thread = new UploadThread(upload.getFilepath(), destination);
			thread.start();
			while(thread.isUploading());  // wait for it to be done uploading files
			loading.close();
			upload.setUploading(false);
			//remi was here
		}
	}

}
