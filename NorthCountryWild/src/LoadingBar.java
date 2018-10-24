import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class LoadingBar extends JPanel {
	private int total_files = 0;
	private int uploaded_files = 0;
	private String filePath = "";

	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawRect(10, 10, 470, 20);
        if (total_files > 0) {
        	g.setColor(Color.BLUE);
        	g.fillRect(10, 10, (int)(((double) uploaded_files / total_files) * 470), 20);
        }
        g.setColor(Color.black);
        g.drawString(filePath + "...", 10, 60);
    }
	
	// for incrementing the loading bar as progress is made
	// total = total number of files to upload (always the same whenever it's called)
	// uploaded = number of files already uploaded when it's called
	public void increment(int total, int uploaded, String path) {
		total_files = total;
		uploaded_files = uploaded;
		filePath = path;
		this.repaint();
	}

}
