import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class PanelFileSelect extends TabItem implements ActionListener {

    private JTextField filePath;
    private JTextField help;
    private JButton browse;
    private JFileChooser fc;
    private boolean count_interrupt = false;
    private int count = 0;
    private int fileTotal = 0;

    public PanelFileSelect(JSONObject jsonpanel) {
    	
        super(jsonpanel);

        // Prepare panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(15, 15, 15, 15);

        // Add description
        JLabel description = new JLabel(getDesc());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);

        // Add filepath textField
        filePath = new JTextField("");
        constraints.insets = new Insets(0, 0, 30, 0);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(filePath, constraints);

        // Add browse button
        browse = new JButton("Browse");
        browse.addActionListener(this);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(browse, constraints);

        // Add help panel
        help = new JTextField(" ?");
        help.setEditable(false);
        help.setPreferredSize(new Dimension(23, 20));
//        help.addMouseListener(this);
        constraints.insets = new Insets(0, 15, 15, 15);
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(help, constraints);

    }
    
    public void actionPerformed(ActionEvent evt) {
		browse.setEnabled(false);
		JOptionPane.showMessageDialog(new JFrame(),
				"When choosing images to upload, be sure to only upload images\nfrom a single camera deployment. If you wish to upload images\nfrom more than one deployment, you have the option to upload again\nafter you complete this upload.");
		count_interrupt = false;
		fc = new JFileChooser(System.getProperty("user.home"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(fc.showOpenDialog(fc) != JFileChooser.APPROVE_OPTION) {
			filePath.setText("");
			browse.setEnabled(true);
		}

		else {
			filePath.setText(fc.getSelectedFile().toString());
			
			Thread t = new Thread() {
			    public void run() {
			    	LoadingWindow loading = new LoadingWindow();
			    	int total = checkDirectory(getFilepath(), loading, 0);
			    	loading.close();
			    	if (!count_interrupt) {
				    	count = total;
				    	JFrame optionFrame = new JFrame();
						String[] options = {"Yes", "No"};
						int n = JOptionPane.showOptionDialog(
							    optionFrame, "This folder has approximately " + fileTotal + " files, and "+total+" images to be uploaded in it. Is this correct?",
							    "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 1) {
							browse.doClick();
						}
						if(n == JOptionPane.NO_OPTION) {
							filePath.setText("");
						}
						fileTotal=0;
			    	}
			    	browse.setEnabled(true);
			    }
			};
			t.start();
		}
	}
    
    public String getFilepath() {
		return filePath.getText();
	}
    
    public String getFinalValue() {
    	return getFilepath();
    }

	public int getCount() {
		return count;
	}

	public int checkDirectory(String path, LoadingWindow loading, int total) {
		if (!count_interrupt) {
			File dir = new File(path);
			int checked = 0;
			int inner_total = 0;
			File[] files = dir.listFiles();
			
			// count all files except for the directories and hidden files
			for (File file: files) {
				if (!file.isDirectory() && !file.isHidden()) {
					fileTotal++;
					inner_total++;
				}
			}
			
			// recurse through the directories first
			for (File file: files) {
				if (file.isDirectory() && !count_interrupt) {
					total = checkDirectory(file.getAbsolutePath(), loading, total);
					if (!loading.isUploading()) {
						fileTotal = 0;
						count_interrupt = true;
					}
				}
			}
			
			// reset the loading bar to 0 for the new directory of photos
			loading.changeBar(0,  0, path);
			for (File file: files) {
				try {
					if (ImageIO.read(file) != null && !count_interrupt) {
						total++;
						if (!loading.isUploading()) {
							fileTotal = 0;
							count_interrupt = true;
							filePath.setText("");
						}
					} else if (count_interrupt) {
						break;
					}
				} catch (IOException e) {
				}
				if (!file.isDirectory() && !file.isHidden() && !count_interrupt) {
					checked++;
					loading.changeBar(inner_total, checked, path);
				}
			}
			return total;
		} else {
			return 0;
		}
	}
    
}
