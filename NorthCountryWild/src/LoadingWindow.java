import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LoadingWindow implements ActionListener {
	// instance variables
		private JFrame frame;
		private LoadingBar bar;
		private boolean uploading;
		private JButton cancel;
		
		public LoadingWindow() {
			// set up the components
			frame = new JFrame("Uploading");
			bar = new LoadingBar();
			uploading = true;
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			JPanel cancelPanel = new JPanel();
			
			// set window size
			frame.setPreferredSize(new Dimension(500, 130));
			frame.setResizable(false);
			
			// make window appear in middle of screen
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(dim.width/2-250, dim.height/2-50);
			
			// add all the components to the frame
			bar.setLayout(new BorderLayout());
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(cancel);
			bar.add(buttonPanel,BorderLayout.SOUTH);
			frame.add(bar);
			frame.pack();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
		public void close() {
			frame.dispose();
		}
		
		public void changeBar(int total, int uploaded, String path) {
			bar.increment(total, uploaded, path);
		}
		
		public boolean isUploading() {
			return uploading;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(cancel)) {
				// cancel the upload
				uploading = false;
				close();
			}
			
		}

}
