import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class SplashScreen implements ActionListener{
	
	private JFrame frame;
	private JButton beginButton;
	private volatile boolean open = true;
	private Image logo;
	private Image title;
	private Image gamecam;
	private Font nun;
	
	public SplashScreen() {
		//set up the components
		frame = new JFrame("North Country Wild");
		//set window size
		frame.setSize(920,530);
		frame.setResizable(false);
		
		JPanel panel = new JPanel();
		JPanel topPanel = new JPanel();
		JPanel introPanel = new JPanel();
		JPanel infoPanel = new JPanel();
		JPanel beginPanel = new JPanel();
		JPanel creditsPanel = new JPanel();
		JPanel versionPanel = new JPanel();
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream logoInput = classLoader.getResourceAsStream("nun_SLU.jpg");
		InputStream titleInput = classLoader.getResourceAsStream("title.jpg");
		InputStream gameInput = classLoader.getResourceAsStream("game_camera_pic.JPG");

		try {
			logo = ImageIO.read(logoInput);
			title = ImageIO.read(titleInput);
			gamecam = ImageIO.read(gameInput);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageIcon logoIcon = new ImageIcon(logo);
		ImageIcon titleIcon = new ImageIcon(title);
		ImageIcon gameIcon = new ImageIcon(gamecam);
		
		JLabel imageLabel = new JLabel();
		JLabel titleLabel = new JLabel();
		JLabel gameLabel = new JLabel();
		JTextArea intro = new JTextArea();
		JTextArea info = new JTextArea();
		beginButton = new JButton("Begin");
		JTextArea credits = new JTextArea();
		JTextArea version = new JTextArea();
		
		imageLabel.setIcon(logoIcon);
		titleLabel.setIcon(titleIcon);
		gameLabel.setIcon(gameIcon);
		
//		try {
//		     GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Roadgeek 2005 New Parks.otf")));
//		     String []fontFamilies = ge.getAvailableFontFamilyNames();
//		     for(String s: fontFamilies) {
//		    	 	System.out.print(s);
//		     }
//		} catch (IOException|FontFormatException e) {
//		     e.printStackTrace();
//		}
		 
		try {
			InputStream is = classLoader.getResourceAsStream("Roadgeek 2005 New Parks.otf");
			nun = Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
		
		Color brown = new Color(82, 46, 1);
		Color green = new Color(39, 81, 15);
		
		intro.setFont(nun);
		intro.setFont(intro.getFont().deriveFont(16f));
		intro.setForeground(brown);
		intro.setSize(880, 100);
        intro.setLineWrap(true);
        intro.setWrapStyleWord(true);
        intro.setOpaque(false);
        intro.setEditable(false);
		intro.setText("North Country Wild is a Nature Up North community-based science project that uses game cameras "+
				"to study wildlife diversity and activity in the region. For more information about the project please visit "+
				"www.natureupnorth.org/North-Country_Wild");
		
		//info.setFont(new Font("Serif", Font.PLAIN, 16));
		info.setFont(nun);
		info.setFont(info.getFont().deriveFont(16f));
		info.setForeground(green);
		info.setSize(880, 300);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setOpaque(false);
        info.setEditable(false);
		info.setText("Before you begin you should:\n\n"
				+ "1) Organize your photos on your hard drive so that there is a single folder for each camera deployment. "
				+ "Each folder should contain only the image files that were produced during the deployment, and should include "
				+ "ALL image files, even if there are no animals in the picture. You will use the Photo Uploader once for "
				+ "each camera deployment.\n\n2) Make sure you have the latitude and longitude information for where the camera "
				+ "was located during the deployment and that you know the start date and end date that the camera was in the field."
				+ "\n\n3) Make sure you know your Nature Up North username and password. Please contact us at info@natureupnorth.org "
				+ "for assistance if needed.");
		
		credits.setFont(nun);
		credits.setFont(info.getFont().deriveFont(12f));
		credits.setSize(500, 50);
		credits.setForeground(brown);
		credits.setLineWrap(true);
		credits.setWrapStyleWord(true);
		credits.setOpaque(false);
		credits.setEditable(false);
		credits.setText("  Program created for Nature Up North by St. Lawrence University computer\nscience interns Aleksei Bingham, Guinevere Gilman, and Remi LeBlanc in 2018.");
		creditsPanel.setSize(credits.getSize());
		creditsPanel.setLocation(frame.getWidth()-475, frame.getHeight()-60);
		creditsPanel.add(credits);

		version.setFont(nun);
		version.setFont(info.getFont().deriveFont(11f));
		version.setSize(100, 25);
		version.setForeground(brown);
//		version.setLineWrap(true);
//		version.setWrapStyleWord(true);
		version.setOpaque(false);
		version.setEditable(false);
		version.setText("Version 1.2.1"); //(1st major version).(second meeting presenting).(first github push) ???? or version 1.0.0
		versionPanel.setSize(version.getSize());
		versionPanel.setLocation(frame.getWidth()-927, frame.getHeight()-45);
		versionPanel.add(version);
		
		//add components to panel
		topPanel.add(gameLabel);
		topPanel.add(titleLabel);
		topPanel.add(imageLabel);
		introPanel.add(intro);
		infoPanel.add(info);
		beginPanel.add(beginButton);
		
		panel.add(topPanel);
		panel.add(introPanel);
		panel.add(infoPanel);
		panel.add(beginPanel);
		
		//make the button click 
		beginButton.addActionListener(this);
		
		// make window appear in middle of screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-460, dim.height/2-250);

		frame.add(versionPanel);
		frame.add(creditsPanel);
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
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if(evt.getSource() == beginButton) {
			open = false;
		}
		
	}

	public static void main(String[] args) {
		//for testing
		new SplashScreen();

	}

}
