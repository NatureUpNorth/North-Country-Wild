import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownServiceException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Helps with user authentication at a Drupal site
 * 
 * @author Choong-Soo Lee
 * @version 1.0
 */
public class DrupalJSONAuth implements UserAuthentication {	
	// singleton
	private static DrupalJSONAuth singleton;
	
	protected DrupalJSONAuth() {
		// prevent people from creating objects on their own
	}
	
	// create/access the singleton
	public static DrupalJSONAuth getInstance() {
		if (singleton == null) {
			singleton = new DrupalJSONAuth();
		}
		return singleton;
	}
	
	// based on the documentation at https://www.drupal.org/node/2720655
	// the HTTP code only returns 200 upon successful authentation (it seems)
	// other error codes are returned otherwise
	// it may be necessary to add more cases in case the code 200 is also returned on
	// unsuccessful authentication
	
	// constants
	private static final String host = "www.natureupnorth.org";
	private static final String authPath = "/user/login?_format=json";
	private static final String protocol = "https";
	private static final String errorMessage = " Please contact the developer. Thank you, and sorry for your inconvenience.";

	/**
	 * Takes a username and password and returns whether the combination resulted 
	 * in a successful authentication at the Nature Up North website
	 * 
	 * @param username: username at the Nature Up North website (Not the email address)
	 * @param password: password for the username at the Nature Up North website
	 * 
	 * @returns true if authentication is successful
	 */
	public boolean authenticate(String username, String password) throws RuntimeException {
		// assume username and password are already checked for validity
		// encode username and password for URL
		// assume that the username and password already checked the validation test
		if (username != null && password != null && username.length() > 0 && password.length() > 0) {
			// validate username and password?

			URL accessURL;
			HttpURLConnection connection;
			byte[] authBytes;
			
			// construct a JSON object containing the username and password
			//String authString = "{\"name\":\"" + username + "\", \"pass\":\"" + password + "\"}";
			String authString = "{\"name\":\""+username+"\",\"mail\":\"" + username + "\", \"pass\":\"" + password + "\"}";


			// convert the username/password data to UTF-8 bytes
			try {
				authBytes = authString.getBytes("UTF-8");
			} catch (UnsupportedEncodingException esee) {
				throw new RuntimeException("Failed at username and/or password validation");
			}
			
			// attempt to connect to the server
			try {
				accessURL = new URL(protocol + "://" + host + authPath);
				connection = (HttpURLConnection)accessURL.openConnection();
			} catch (MalformedURLException mue) {
				// should never happen
				throw new RuntimeException("URL Error!" + errorMessage);
			} catch (IOException ioe) {
				throw new RuntimeException("Connection Error!" + errorMessage);
			}
			
			// attempt to configure the HTTP request
			try {
				// format the HTTP request
				connection.setRequestMethod("POST");	
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Content-Length", "" + authBytes.length);
				
				// configure the HTTP connection
				connection.setUseCaches(false); // disable the use of server cache
				connection.setDoOutput(true); // enable sending the data over the HTTP connection				
			} catch (ProtocolException pe) {
				// should never happen
				throw new RuntimeException("Protocol Error!" + errorMessage);
			} catch (SecurityException se) {
				// should never happen
				throw new RuntimeException("Security Error!" + errorMessage);
			} catch (IllegalStateException ise) {
				// should never happen
				throw new RuntimeException("Connnection Already Established Error!" + errorMessage);
			} catch (NullPointerException npe) {
				// should never happen
				throw new RuntimeException("Data Key Error!" + errorMessage); 
			}
			
			// attempt to send the username/password combination to the server over the connection
			try {
				// create an output stream to send the data over the HTTP connection
				DataOutputStream wr = new DataOutputStream( connection.getOutputStream());
				wr.write( authBytes );				
				wr.close();
				
				// check if we got OK 200
				if (connection.getResponseCode() == 200) {
					// the server throws a non-200 http return code if the authentication fails?
					// based on some limited testing, this seems to be so
					return true;
				}
				// need to read the response
			} catch (UnknownServiceException use) {
				// should never happen
				throw new RuntimeException("Unknown Error!" + errorMessage);				
			} catch (IOException ioe) {
				throw new RuntimeException("Sending Error!" + errorMessage);
			} 
		} else {
			// throw an exception for having null objects
			throw new RuntimeException("Invalid username/password Error!" + errorMessage);
		}
		return false;
	}
}
