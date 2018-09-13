import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class LoginWindow implements ActionListener {
	
	// instance variables
	private String username = "";
	private String password = "";
	private JPasswordField passField;
	private JTextField userField;
	private JFrame frame;
	private volatile boolean open = true;
	
	public LoginWindow() {
		// set up the components
		frame = new JFrame("North Country Wild");
		JPanel panel = new JPanel();
		userField = new JTextField(20);
		passField = new JPasswordField(20);
		JLabel userLabel = new JLabel("Username: ");
		JLabel passLabel = new JLabel("Password: ");
		JButton submit = new JButton("Submit");
		
		// set window size
		frame.setSize(350, 125);
		frame.setResizable(false);
		
		// add components to panel
		panel.add(userLabel);
		panel.add(userField);
		panel.add(passLabel);
		panel.add(passField);
		panel.add(submit);
		
		// make the text submit-able by pressing the button or hitting enter on either field
		submit.addActionListener(this);
		passField.addActionListener(this);
		userField.addActionListener(this);
		
		// make window appear in middle of screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-175, dim.height/2-62);
		
		frame.add(panel);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void close() {
		frame.dispose();
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public void actionPerformed(ActionEvent evt) {
		// parse the username/password entered
		password = passField.getText();
		username = userField.getText();
		if (password.isEmpty() || username.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Invalid username/password error! Please contact the developer. \nThank you, and sorry for your inconvenience.");
		}
		else {
			if (verify(username, password)) {
				open = false;
			} else {
				JOptionPane.showMessageDialog(frame, "Invalid username/password error! Please contact the developer. \nThank you, and sorry for your inconvenience.");
			}
		}
	}
	
	public boolean verify(String username, String password) {
		return DrupalJSONAuth.getInstance().authenticate(username, password);
	}
	
	public String getName() {
		return username;
	}

	public static void main(String[] args) {
		// for testing
		new LoginWindow();
	}

}
