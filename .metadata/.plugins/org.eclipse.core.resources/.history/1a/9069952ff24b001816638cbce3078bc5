import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SplashScreen implements ActionListener{
	
	private JFrame frame;
	private JButton nextButton;
	private JLabel info;
	private volatile boolean open = true;
	
	public SplashScreen() {
		//set up the components
		frame = new JFrame("North Country Wild");
		JPanel panel = new JPanel();
		nextButton = new JButton("Next");
		info = new JLabel();
		setText();
		
		//set window size
		frame.setSize(500,500);
		frame.setResizable(false);
		
		//add components to panel
		panel.add(info);
		panel.add(nextButton);
		
		
		//make the button click 
		nextButton.addActionListener(this);
		
		// make window appear in middle of screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-250, dim.height/2-250);
		
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
		if(evt.getSource() == nextButton) {
			open = false;
		}
		
	}
	
	private void setText() {
		info.setText("North Country Wild is a citizen science project run by Nature Up North.");
	}

	public static void main(String[] args) {
		//for testing
		new SplashScreen();

	}



}
