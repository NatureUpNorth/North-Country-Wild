/**
 * A UserAuthentication is to help wth a generic authentication using a username/password combination
 * 
 * @author Choong-Soo Lee
 * @version 1.0
 */
public interface UserAuthentication {
	/**
	 * Takes a username and password and returns whether the combination resulted 
	 * in a successful authentication at the Nature Up North website
	 * 
	 * @param username: username at the Nature Up North website (Not the email address)
	 * @param password: password for the username at the Nature Up North website
	 * 
	 * @returns true if authentication is successful
	 * @exception RuntimeException if the connection fails or the authentication is unsuccessful
	 */
	public boolean authenticate(String username, String password) throws RuntimeException;
}
