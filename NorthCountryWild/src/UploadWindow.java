import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class UploadWindow implements ActionListener, ItemListener, ChangeListener {
	
	// instance variables
	private JFrame frame;
	private JTextField filePath;
	private String[] groupList = {"List of schools", "Unaffiliated"};
	private JButton fileButton;
	private JLabel fileLabel;
	private JButton submit;
	private JCheckBox one;
	private JCheckBox two;
	private JCheckBox three;
	private JSlider lat;
	private JSlider lon;
	private JTextField latLabel;
	private JTextField lonLabel;
	private JTextField startDate;
	private JTextField endDate;
	private String startDateStr;
	private String endDateStr;
	private String latitude;
	private String longitude;
	private ArrayList<String> habitats;
	private volatile boolean uploading = false;
	
	// these ones are for directory browsing (drives only)
	private JFileChooser fc; 

	public UploadWindow() {
		// set up the components
		frame = new JFrame("North Country Wild");
		JPanel panel = new JPanel();
		JPanel filePanel = new JPanel();
		JPanel groupPanel = new JPanel();
		JPanel locationPanel = new JPanel();
		JPanel habitatPanel = new JPanel();
		JPanel datePanel = new JPanel();
		JPanel submitPanel = new JPanel();
		
		JLabel groupLabel = new JLabel("Select your affiliation:");
		JComboBox<String> groups = new JComboBox<String>(groupList);
		fileLabel = new JLabel("Select the pictures to be uploaded:");
		filePath = new JTextField(20);
		fileButton = new JButton("Browse");
		submit = new JButton("Submit");
		fileButton.addActionListener(this);
		submit.addActionListener(this);
		
		one = new JCheckBox("Habitat one");
		two = new JCheckBox("Habitat two");
		three = new JCheckBox("Habitat three");
		one.addItemListener(this);
		two.addItemListener(this);
		three.addItemListener(this);
		
		// set window size
		frame.setPreferredSize(new Dimension(700, 700));
		frame.setResizable(false);
		
		// make window appear in middle of screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-350, dim.height/2-350);
		
		// add components to panels
		filePanel.add(fileLabel);
		filePanel.add(filePath);
		filePanel.add(fileButton);
		submitPanel.add(submit);
		
		groupPanel.add(groupLabel);
		groupPanel.add(groups);
		
		JPanel latPanel = new JPanel();
		JPanel lonPanel = new JPanel();
		lat = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);
		lat.setPreferredSize(new Dimension(300, 50));
		lat.addChangeListener(this);
		latLabel = new JTextField(10);
		latLabel.addActionListener(this);
		lat.setMajorTickSpacing(20);
		lat.setMinorTickSpacing(5);
		lat.setPaintTicks(true);
		lat.setPaintLabels(true);
		lon = new JSlider(JSlider.HORIZONTAL, -180, 180, 0);
		lon.setPreferredSize(new Dimension(300, 50));
		lonLabel = new JTextField(10);
		lonLabel.addActionListener(this);
		lon.addChangeListener(this);
		lon.setMajorTickSpacing(40);
		lon.setMinorTickSpacing(10);
		lon.setPaintTicks(true);
		lon.setPaintLabels(true);
		latPanel.add(new JLabel(" Select the latitude:"));
		latPanel.add(latLabel);
		latPanel.add(lat);
		lonPanel.add(new JLabel("Select the longitude:"));
		lonPanel.add(lonLabel);
		lonPanel.add(lon);
		locationPanel.setLayout(new GridLayout(2, 1));
		locationPanel.add(latPanel);
		locationPanel.add(lonPanel);
		
		habitatPanel.setLayout(new GridLayout());
		habitatPanel.add(new JLabel("Select the habitat:"));
		habitatPanel.add(one);
		habitatPanel.add(two);
		habitatPanel.add(three);
		
		JPanel startPanel = new JPanel();
		JPanel endPanel = new JPanel();
		startDate = new JTextField(20);
		endDate = new JTextField(20);
		startPanel.add(new JLabel("Enter the start date of deployment (MM-DD-YYYY):"));
		startPanel.add(startDate);
		endPanel.add(new JLabel("    Enter the end date of deployment (MM-DD-YYYY):"));
		endPanel.add(endDate);
		datePanel.add(startPanel);
		datePanel.add(endPanel);
		
		panel.setLayout(new GridLayout(6, 1));
		panel.add(filePanel);
		panel.add(groupPanel);
		panel.add(locationPanel);
		panel.add(habitatPanel);
		panel.add(datePanel);
		panel.add(submitPanel);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public boolean isUploading() {
		return uploading;
	}
	
	public void setUploading(boolean status) {
		uploading = status;
		if (status) {
			submit.setText("Cancel");
		} else {
			submit.setText("Submit");
		}
	}
	
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == fileButton) {
			fc = new JFileChooser(System.getProperty("user.home"));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			/*
			fc.setFileFilter(new javax.swing.filechooser.FileFilter() {

				@Override
				public boolean accept(File f) {
					currentPath = fc.getCurrentDirectory().toString();
					return (f.isDirectory() && f.getAbsolutePath().endsWith(":\\"));
				}

				@Override
				public String getDescription() {
					return "Only drives";
				}
				
			});
			*/
			if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION) {
				filePath.setText(fc.getSelectedFile().toString());
			}
		}
		if (evt.getSource() == submit && !uploading) {
			latitude = latLabel.getText();
			longitude = lonLabel.getText();
			startDateStr = startDate.getText();
			endDateStr = endDate.getText();
			setUploading(true);
		}
	}
	
	public String getFilepath() {
		return filePath.getText();
	}
	
	public static void main(String[] args) {
		new UploadWindow();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getItemSelectable() == one) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				habitats.add("one");
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				habitats.remove("one");
			}
		} else if (e.getItemSelectable() == two) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				habitats.add("two");
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				habitats.remove("two");
			}
		} else if (e.getItemSelectable() == three) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				habitats.add("three");
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				habitats.remove("three");
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (source == lat) {
			latLabel.setText("" + source.getValue());
		} else if (source == lon) {
			lonLabel.setText("" + source.getValue());
		}
	}
	
	public String getLat() {
		return latitude;
	}
	
	public String getLon() {
		return longitude;
	}
	
	public String getStartDate() {
		return startDateStr;
	}
	
	public String getEndDate() {
		return endDateStr;
	}
	
	public ArrayList<String> getHabitats() {
		return habitats;
	}

}
