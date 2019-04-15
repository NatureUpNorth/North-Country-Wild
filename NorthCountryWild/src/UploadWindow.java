import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.demo.FullDemo;

import org.json.*;

public class UploadWindow implements ActionListener, ItemListener, ChangeListener, MouseListener, FocusListener {

	// instance variables
	private JFrame frame;
	private JTextField filePath;
	private String[] groupList = {"List of schools", "Unaffiliated"};
	private JButton fileButton;
	private JLabel fileLabel;
	private JButton submit;
	private JButton latChange;
	private JButton lonChange;
	private JCheckBox one;
	private JCheckBox two;
	private JCheckBox three;
	private JCheckBox four;
	private JCheckBox five;
	private JCheckBox six;
	private JCheckBox seven;
	private JCheckBox eight;
	private JCheckBox nine;
	private JCheckBox rural;
	private JCheckBox sub;
	private JCheckBox urban;
	private JSlider lat;
	private JSlider lon;
	private JTextField latLabel;
	private JTextField lonLabel;
	private String latitude;
	private String longitude;
	JComboBox<String> groups; 
	ArrayList<String> habitats;
	String urbanized;
	private volatile boolean uploading = false;
	private final String DEGREE  = "\u00b0";
	private boolean completed = false;
	private int count;
	private int fileTotal;
	private boolean count_interrupt;
	static DatePicker startDatePicker;
	static DatePicker endDatePicker;
	
	private JTextField selectFolderHelp;
	private JTextField groupHelp;
	private JTextField lonHelp;
	private JTextField latHelp;
	private JTextField habitatHelp;
	private JTextField datesHelp;
	private JTextField urbanHelp;
	private JTextArea selectFolderHelpText;
	private JTextArea groupHelpText;
	private JTextArea lonlatHelpText;
	private JTextArea habitatHelpText;
	private JTextArea datesHelpText;
	private JTextArea urbanHelpText;
	JPanel datesHelpPanel= new JPanel();
	JPanel folderHelpPanel = new JPanel();
	JPanel groupHelpPanel = new JPanel();
	JPanel lonlatHelpPanel = new JPanel();
	JPanel habitatHelpPanel = new JPanel();
	JPanel urbanHelpPanel = new JPanel();
	
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
		JPanel urbanPanel = new JPanel();
		JPanel datePanel = new JPanel();
		JPanel submitPanel = new JPanel();
		JPanel changeLatPanel = new JPanel();
		JPanel changeLonPanel = new JPanel();
		JPanel locationLabelPanel = new JPanel();
		JPanel habitatLabelPanel = new JPanel();
		JPanel urbanLabelPanel = new JPanel();
		
		JLabel groupLabel = new JLabel("Please identify the group you are associated with, if any:");
		groups = new JComboBox<String>(groupList);
		fileLabel = new JLabel("Select the pictures to be uploaded:");
		filePath = new JTextField(20);
		fileButton = new JButton("Browse");
		submit = new JButton("Submit");
		fileButton.addActionListener(this);
		submit.addActionListener(this);
		latChange = new JButton();
		latChange.setText("Convert to Degrees, Minutes, Seconds");
		latChange.addActionListener(this);
		lonChange = new JButton();
		lonChange.setText("Convert to Degrees, Minutes, Seconds");
		lonChange.addActionListener(this);

		JLabel redStarFile = new JLabel("*");
		JLabel redStarStart = new JLabel("*");
		JLabel redStarEnd = new JLabel("*");
		JLabel redStarGroup = new JLabel("*");
		JLabel redStarHabitat = new JLabel("*");
		JLabel redStarUrban = new JLabel("*");
		redStarFile.setForeground(Color.RED);
		redStarFile.setFont(new Font("Dialog", Font.BOLD, 24));
		redStarStart.setForeground(Color.RED);
		redStarStart.setFont(new Font("Dialog", Font.BOLD, 24));
		redStarEnd.setForeground(Color.RED);
		redStarEnd.setFont(new Font("Dialog", Font.BOLD, 24));
		redStarGroup.setForeground(Color.RED);
		redStarGroup.setFont(new Font("Dialog", Font.BOLD, 24));
		redStarHabitat.setForeground(Color.RED);
		redStarHabitat.setFont(new Font("Dialog", Font.BOLD, 24));
		redStarUrban.setForeground(Color.RED);
		redStarUrban.setFont(new Font("Dialog", Font.BOLD, 24));
		
		JLabel requiredStar = new JLabel("<html><b><font color='red' size = 6>*</font></b> indicates the field is required for submission.</html>");
		//JLabel requiredText = new JLabel(" indicates the field is required for submission.");
		
		
		selectFolderHelp = new JTextField(" ?");
		selectFolderHelp.setEditable(false);
		selectFolderHelp.setPreferredSize(new Dimension(23, 20));
		selectFolderHelp.addMouseListener(this);
		
		groupHelp = new JTextField(" ?");
		groupHelp.setEditable(false);
		groupHelp.setPreferredSize(new Dimension(23, 20));
		groupHelp.addMouseListener(this);
		
		lonHelp = new JTextField(" ?");
		lonHelp.setEditable(false);
		lonHelp.setPreferredSize(new Dimension(23, 20));
		lonHelp.addMouseListener(this);
		
		latHelp = new JTextField(" ?");
		latHelp.setEditable(false);
		latHelp.setPreferredSize(new Dimension(23, 20));
		latHelp.addMouseListener(this);
		
		habitatHelp = new JTextField(" ?");
		habitatHelp.setEditable(false);
		habitatHelp.setPreferredSize(new Dimension(23, 20));
		habitatHelp.addMouseListener(this);
		habitats = new ArrayList<String>();
		
		datesHelp = new JTextField(" ?");
		datesHelp.setEditable(false);
		datesHelp.setPreferredSize(new Dimension(23, 20));
		datesHelp.addMouseListener(this);
		
		urbanHelp = new JTextField(" ?");
		urbanHelp.setEditable(false);
		urbanHelp.setPreferredSize(new Dimension(23, 20));
		urbanHelp.addMouseListener(this);
		
		one = new JCheckBox("Hardwood Forest");
		two = new JCheckBox("Mixed Forest");
		three = new JCheckBox("Evergreen Forest");
		four = new JCheckBox("Plantation Forest");
		five = new JCheckBox("Natural Field or Meadow");
		six = new JCheckBox("Agricultural Field");
		seven = new JCheckBox("Public Park/School Grounds/Lawn");
		eight = new JCheckBox("Wetland Edge");
		nine = new JCheckBox("Edge between two habitats");
		one.addItemListener(this);
		two.addItemListener(this);
		three.addItemListener(this);
		four.addItemListener(this);
		five.addItemListener(this);
		six.addItemListener(this);
		seven.addItemListener(this);
		eight.addItemListener(this);
		nine.addItemListener(this);
		
		rural = new JCheckBox("Rural");
		sub = new JCheckBox("Suburban/Moderately Urban");
		urban = new JCheckBox("Primarily Urban");
		rural.addItemListener(this);
		sub.addItemListener(this);
		urban.addItemListener(this);
		
		// set window size
		frame.setPreferredSize(new Dimension(800, 750));
		frame.setResizable(false);
		
		// make window appear in middle of screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-400, dim.height/2-375);
		
		// add components to panels
		filePanel.add(redStarFile);
		filePanel.add(fileLabel);
		filePanel.add(filePath);
		filePanel.add(fileButton);
		filePanel.add(selectFolderHelp);
		submitPanel.add(submit);
		changeLatPanel.add(latChange);
		changeLatPanel.add(latHelp);
		changeLonPanel.add(lonChange);
		changeLonPanel.add(lonHelp);
		
		groupPanel.add(redStarGroup);
		groupPanel.add(groupLabel);
		groupPanel.add(groups);
		groupPanel.add(groupHelp);
		
		JPanel latPanel = new JPanel();
		JPanel lonPanel = new JPanel();
		lat = new JSlider(JSlider.HORIZONTAL, -9000, 9000, 0);
		lat.setPreferredSize(new Dimension(300, 45));
		lat.addChangeListener(this);
		latLabel = new JTextField(10);
		latLabel.setText("0.00");
		//latLabel.addActionListener(this);
		latLabel.addFocusListener(this);
		lat.setMajorTickSpacing(2000);
		lat.setMinorTickSpacing(500);
		lat.setPaintTicks(true);
		lat.setPaintLabels(true);
		lon = new JSlider(JSlider.HORIZONTAL, -18000, 18000, 0);
		lon.setPreferredSize(new Dimension(300, 45));
		lonLabel = new JTextField(10);
		lonLabel.setText("0.00");
		lonLabel.addFocusListener(this);
		lon.addChangeListener(this);
		lon.setMajorTickSpacing(4000);
		lon.setMinorTickSpacing(1000);
		lon.setPaintTicks(true);
		lon.setPaintLabels(true);
		
		//Create the label table
		Hashtable latTable = new Hashtable();
		latTable.put( new Integer( 9000 ), new JLabel("90") );
		latTable.put( new Integer( 7000 ), new JLabel("70") );
		latTable.put( new Integer( 5000 ), new JLabel("50") );
		latTable.put( new Integer( 3000 ), new JLabel("30") );
		latTable.put( new Integer( 1000 ), new JLabel("10") );
		latTable.put( new Integer( -7000 ), new JLabel("-70") );
		latTable.put( new Integer( -5000 ), new JLabel("-50") );
		latTable.put( new Integer( -3000 ), new JLabel("-30") );
		latTable.put( new Integer( -1000 ), new JLabel("-10") );
		latTable.put( new Integer( -9000 ), new JLabel("-90") );
		lat.setLabelTable( latTable );
		lat.setPaintLabels(true);
		Hashtable lonTable = new Hashtable();
		lonTable.put( new Integer( 18000 ), new JLabel("180")); 
		lonTable.put( new Integer( 14000 ), new JLabel("140")); 
		lonTable.put( new Integer( 10000 ), new JLabel("100")); 
		lonTable.put( new Integer( 6000 ), new JLabel("60")); 
		lonTable.put( new Integer( 2000 ), new JLabel("20")); 
		lonTable.put( new Integer( -14000 ), new JLabel("-140")); 
		lonTable.put( new Integer( -10000 ), new JLabel("-100")); 
		lonTable.put( new Integer( -6000 ), new JLabel("-60")); 
		lonTable.put( new Integer( -2000 ), new JLabel("-20")); 
		lonTable.put( new Integer( -18000 ), new JLabel("-180")); 
		lon.setLabelTable( lonTable );
		lon.setPaintLabels(true);
		
		latPanel.add(new JLabel(" Select the latitude:"));
		latPanel.add(latLabel);
		latPanel.add(lat);
		lonPanel.add(new JLabel("Select the longitude:"));
		lonPanel.add(lonLabel);
		lonPanel.add(lon);
//		locationPanel.setLayout(new GridLayout(4, 1));
//		locationPanel.add(latPanel);
//		locationPanel.add(changeLatPanel);
//		locationPanel.add(lonPanel);
//		locationPanel.add(changeLonPanel);
		
		
		locationPanel.setLayout(new GridLayout(2, 1));
		locationLabelPanel.add(new JLabel("For this camera deployment, what was the latitude and longitude of the camera location?"));
		locationPanel.add(latPanel);
		locationPanel.add(lonPanel);
		
		habitatLabelPanel.add(redStarHabitat);
		habitatLabelPanel.add(new JLabel("<html>Which of the following terms would characterize the habitat in the location where the camera was placed?<br/>(check all that apply)</html>"));
		habitatLabelPanel.add(habitatHelp);
		//habitatPanel.setPreferredSize(new Dimension(800, 100));
		habitatPanel.setLayout(new GridLayout(3, 3));
		habitatPanel.add(one);
		habitatPanel.add(two);
		habitatPanel.add(three);
		habitatPanel.add(four);
		habitatPanel.add(five);
		habitatPanel.add(six);
		habitatPanel.add(seven);
		habitatPanel.add(eight);
		habitatPanel.add(nine);
		
		urbanLabelPanel.setPreferredSize(new Dimension(800, 30));
		urbanLabelPanel.add(redStarUrban);
		urbanLabelPanel.add(new JLabel("How urbanized is the location where the camera was placed? (Please check one)"));
		urbanLabelPanel.add(urbanHelp);
		urbanPanel.setLayout(new GridLayout(3,1));
		urbanPanel.add(rural);
		urbanPanel.add(sub);
		urbanPanel.add(urban);
		
		DatePickerSettings dateSettings;
		DatePickerSettings dateSettings2;
		URL dateImageURL = FullDemo.class.getResource("/images/datepickerbutton1.png");
        Image dateExampleImage = Toolkit.getDefaultToolkit().getImage(dateImageURL);
        ImageIcon dateExampleIcon = new ImageIcon(dateExampleImage);
        // Create the date picker, and apply the image icon.
        dateSettings = new DatePickerSettings();
        dateSettings2 = new DatePickerSettings();
        startDatePicker = new DatePicker(dateSettings);
        endDatePicker = new DatePicker(dateSettings2);
        startDatePicker.setDateToToday();
        endDatePicker.setDateToToday();
        JButton datePickerButton = startDatePicker.getComponentToggleCalendarButton();
        JButton datePickerButton2 = endDatePicker.getComponentToggleCalendarButton();
        datePickerButton.setText("");
        datePickerButton2.setText("");
        datePickerButton.setIcon(dateExampleIcon);
        datePickerButton2.setIcon(dateExampleIcon);
        dateSettings.setAllowKeyboardEditing(false);
        dateSettings2.setAllowKeyboardEditing(false);
        //datePanel.add(startDatePicker);
		
		JPanel startPanel = new JPanel();
		JPanel endPanel = new JPanel();
		startPanel.add(redStarStart);
		startPanel.add(new JLabel("For this deployment, on what date was the camera placed in the field?   "));
		startPanel.add(startDatePicker);
		startPanel.add(datesHelp);
		endPanel.add(redStarEnd);
		endPanel.add(new JLabel("For this deployment, on what date was the camera removed from the field?"));
		endPanel.add(endDatePicker);
		datePanel.setLayout(new GridLayout(2, 1));
		datePanel.add(startPanel);
		datePanel.add(endPanel);
		
		JPanel reqPanel = new JPanel();
		reqPanel.add(requiredStar);
		submitPanel.setPreferredSize(new Dimension(800, 30));
		
		panel.add(filePanel);
		panel.add(groupPanel);
		panel.add(locationLabelPanel);
		panel.add(locationPanel);
		panel.add(latPanel);
		panel.add(changeLatPanel);
		panel.add(lonPanel);
		panel.add(changeLonPanel);
		panel.add(habitatLabelPanel);
		panel.add(habitatPanel);
		panel.add(urbanLabelPanel);
		panel.add(urbanPanel);
		panel.add(datePanel);
		panel.add(submitPanel);
		panel.add(reqPanel);
		
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		
		selectFolderHelpText = new JTextArea();
		selectFolderHelpText.setLineWrap(true);
		selectFolderHelpText.setWrapStyleWord(true);
		selectFolderHelpText.setEditable(false);
		selectFolderHelpText.setText("Browse for the folder that contains the images that you wish to upload. This folder can have sub-folders "
				+ "within it.");
		selectFolderHelpText.setSize(200,111);
		selectFolderHelpText.setBorder(BorderFactory.createCompoundBorder(border,
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		groupHelpText = new JTextArea();
		groupHelpText.setLineWrap(true);
		groupHelpText.setWrapStyleWord(true);
		groupHelpText.setEditable(false);
		groupHelpText.setText("Some people engage with North Country Wild through collaborations with school groups or by checking "
				+ "a game camera out from Nature Up North or local library. If you are affiliated with a group, please enter it here.");
		groupHelpText.setSize(200,200);
	    groupHelpText.setBorder(BorderFactory.createCompoundBorder(border,
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		lonlatHelpText = new JTextArea();
		lonlatHelpText.setLineWrap(true);
		lonlatHelpText.setWrapStyleWord(true);
		lonlatHelpText.setEditable(false);
		lonlatHelpText.setText("If you do not know the longitude and latitude of the camera location, you can search for them here: "
				+ "https://mynasadata.larc.nasa.gov/latitudelongitude-finder/");
		lonlatHelpText.setSize(220,125);
	    lonlatHelpText.setBorder(BorderFactory.createCompoundBorder(border,
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		habitatHelpText = new JTextArea();
		habitatHelpText.setLineWrap(true);
		habitatHelpText.setWrapStyleWord(true);
		habitatHelpText.setEditable(false);
		habitatHelpText.setText("Hardwood Forest: Dominated by mainly hardwood species including for example maple, beech, cherry, and birch.\n\n"
				+ "Evergreen Forest: Dominated by mainly evergreen species including for example white pine, red spruce, balsam fir, hemlock, and "
				+ "possibly plantation pines such as Scots pine.\n\nMixed Forest: Hardwood and Evergreen trees are present in a balanced mix.\n\n"
				+ "Plantation Forest: Trees (usually Pines) are evenly spaced in an organized fashion throuhgout the forest and there are mainly "
				+ "one single species making up the forest leaf-layer (canpoy).\n\nNatural Field or Meadow: Area dominated by grasses and plants "
				+ "such as goldenrod and milkweed; possibly interspersed with a few young trees such as pine or cedar. Not mowed.\n\nPublic Park/"
				+ "School Grounds/Lawn: Primarily open area with some trees interspersed; grass is the predominant vegetation, perhaps supplemented "
				+ "with e.g. flower beds. Grass is regularly maintained by mowing.\n\nWetland edge: If the camera was placed within 15 feet of a pond"
				+ "or wetland, please check this box.\n\nEdge between two habitats: If the camera is located within about 25 feet of more than one "
				+ "of the habitats listed above, please select this choice.");
		habitatHelpText.setSize(500,450);
	    habitatHelpText.setBorder(BorderFactory.createCompoundBorder(border,
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		datesHelpText = new JTextArea();
		datesHelpText.setLineWrap(true);
		datesHelpText.setWrapStyleWord(true);
		datesHelpText.setEditable(false);
		datesHelpText.setText("Enter the the start and end dates of deployment for the camera. Click the calender icon to select date.");
		datesHelpText.setSize(150,125);
		datesHelpText.setBorder(BorderFactory.createCompoundBorder(border,
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		urbanHelpText = new JTextArea();
		urbanHelpText.setLineWrap(true);
		urbanHelpText.setWrapStyleWord(true);
		urbanHelpText.setEditable(false);
		urbanHelpText.setText("Rural: Camera was placed on public or private land in a rural location away from much human influence.\n\n"
				+ "Suburan/moderately urban: Camera was placed near the edge of a town or village where there are more homes and other "
				+ "development than in rural places.\n\nPrimarily urban: Camera was placed inside of a town or village in a location where automobile"
				+ "traffic is prevalent as is the amount of developed space (storefront, parking areas, etc.).");
		urbanHelpText.setSize(300,260);
		urbanHelpText.setBorder(BorderFactory.createCompoundBorder(border,
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		//-------------------------------------------------------
		
		datesHelpPanel.setSize(datesHelpText.getSize());
		datesHelpPanel.setLocation(500,500);
		datesHelpPanel.add(datesHelpText);
		frame.add(datesHelpPanel);
		datesHelpPanel.setVisible(false);
		
		lonlatHelpPanel.setSize(lonlatHelpText.getSize());
		lonlatHelpPanel.setLocation(570,200);
		lonlatHelpPanel.add(lonlatHelpText);
		frame.add(lonlatHelpPanel);
		lonlatHelpPanel.setVisible(false);
		
		habitatHelpPanel.setSize(habitatHelpText.getSize());
		habitatHelpPanel.setLocation(60,7);
		habitatHelpPanel.add(habitatHelpText);
		frame.add(habitatHelpPanel);
		habitatHelpPanel.setVisible(false);
		
		folderHelpPanel.setSize(selectFolderHelpText.getSize());
		folderHelpPanel.setLocation(470,35);
		folderHelpPanel.add(selectFolderHelpText);
		frame.add(folderHelpPanel);
		folderHelpPanel.setVisible(false);
		
		groupHelpPanel.setSize(groupHelpText.getSize());
		groupHelpPanel.setLocation(250, 35);
		groupHelpPanel.add(groupHelpText);
		frame.add(groupHelpPanel);
		groupHelpPanel.setVisible(false);
		
		urbanHelpPanel.setSize(urbanHelpText.getSize());
		urbanHelpPanel.setLocation(300, 200);
		urbanHelpPanel.add(urbanHelpText);
		frame.add(urbanHelpPanel);
		urbanHelpPanel.setVisible(false);
		
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
	}
	
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == fileButton) {
			disable();
			JOptionPane.showMessageDialog(new JFrame(),
					"When choosing images to upload, be sure to only upload images\nfrom a single camera deployment. If you wish to upload images\nfrom more than one deployment, you have the option to upload again\nafter you complete this upload.");
			count_interrupt = false;
			fc = new JFileChooser(System.getProperty("user.home"));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(fc.showOpenDialog(fc) != JFileChooser.APPROVE_OPTION) {
				filePath.setText("");
				enable();
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
								fileButton.doClick();
							}
							if(n == JOptionPane.NO_OPTION) {
								filePath.setText("");
							}
							fileTotal=0;
				    	}
				    	enable();
				    }
				};
				t.start();
			}
		}
		if (evt.getSource() == submit) {
			if (!uploading) {
				latitude = latLabel.getText();
				longitude = lonLabel.getText();
				//startDateStr = startDate.getText();
				//endDateStr = endDate.getText();
				completed();
				if(completed) {

					LocalDate today = LocalDate.now();
					LocalDate startDate = null;
					LocalDate endDate = null;
					boolean load1 = false;
					boolean load2 = false;
					boolean load3 = false;
					startDate = startDatePicker.getDate();
					endDate = endDatePicker.getDate();
					if(startDate==null || endDate == null){
						JOptionPane.showMessageDialog(new JFrame(),
								"Incorrect date entry. Please enter all the required fields.");
					} else {

						if(startDate.isAfter(endDate)) {
							JOptionPane.showMessageDialog(new JFrame(),
									"Incorrect date entry. The start date is after the end date.");
							load1 = false;
						} else {
							load1 = true;
						}
						if(startDate.isAfter(today) && load1) {
							JOptionPane.showMessageDialog(new JFrame(),
									"Incorrect date entry. The start date is after the current date.");
							load2 = false;
						} else {
							load2 = true;
						}
						if(endDate.isAfter(today) && load1 && load2) {
							JOptionPane.showMessageDialog(new JFrame(),
									"Incorrect date entry. The end date is after the current date.");
							load3 = false;
						} else {
							load3 = true;
						}
						if(load1 && load2 && load3) {
							this.setUploading(true);
							
						}
					}

				} else {
					JOptionPane.showMessageDialog(new JFrame(),
							"Please enter all the required fields");
				}
			} else if (uploading) {
				this.setUploading(false);
			}
		}
		
		
		if(evt.getSource() == latChange) {
			if(!latLabel.getText().isEmpty()) {
				if(latLabel.getText().contains(".") && latChange.getText().equals("Convert to Degrees, Minutes, Seconds")){
					String lati = DDtoDMS(latLabel.getText());
					latLabel.setText(lati);
					latChange.setText("     Convert to Decimal Degrees     ");
				}else if (latLabel.getText().contains("\"") && latChange.getText().equals("     Convert to Decimal Degrees     ")){
					String lati = DMStoDD(latLabel.getText());
					latLabel.setText(lati);
					latChange.setText("Convert to Degrees, Minutes, Seconds");
				}
			}
		}

		if(evt.getSource() == lonChange) {
			if(!lonLabel.getText().isEmpty()) {
				if(lonLabel.getText().contains(".") && lonChange.getText().equals("Convert to Degrees, Minutes, Seconds")) {
					String longi = DDtoDMS(lonLabel.getText());
					lonLabel.setText(longi);
					lonChange.setText("     Convert to Decimal Degrees     ");
				} else if (lonLabel.getText().contains("\"") && lonChange.getText().equals("     Convert to Decimal Degrees     ")) {
					String longi = DMStoDD(lonLabel.getText());
					lonLabel.setText(longi);
					lonChange.setText("Convert to Degrees, Minutes, Seconds");
				}
			}
		}
		
//		if(evt.getSource() == latLabel) {
//			String value = latLabel.getText();
//			double latPos = Double.parseDouble(value);
//			double latPosUpdated = latPos*100;
//			lat.setValue((int)latPosUpdated);
//		}
	}

	public String getFilepath() {
		return filePath.getText();
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

	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox cb = (JCheckBox) e.getItem();
		if(e.getStateChange() == ItemEvent.SELECTED) {
			if(!cb.getText().equals("Rural") && !cb.getText().equals("Suburban/Moderately Urban") && !cb.getText().equals("Primarily Urban")) {
				habitats.add(cb.getText());
			} else {
				if(cb.getText().equals("Rural")){
					rural.setSelected(true);
					sub.setSelected(false);
					urban.setSelected(false);
					urbanized = "Rural";
				} else if(cb.getText().equals("Suburban/Moderately Urban")) {
					rural.setSelected(false);
					sub.setSelected(true);
					urban.setSelected(false);
					urbanized = "Suburban/Moderately Urban";
				} else if (cb.getText().equals("Primarily Urban")) {
					rural.setSelected(false);
					sub.setSelected(false);
					urban.setSelected(true);
					urbanized = "Primarily Urban";
				}
				
			}
		} else if (e.getStateChange() == ItemEvent.DESELECTED) {
			habitats.remove(cb.getText());
			if(cb.getText().equals("Rural")){
				if(cb.isSelected()) {
					urbanized = "Rural";
				}
			} else if(cb.getText().equals("Suburban/Moderately Urban")) {
				if(cb.isSelected()) {
					urbanized = "Suburban/Moderately Urban";
				}
				
			} else if (cb.getText().equals("Primarily Urban")) {
				if(cb.isSelected()) {
					urbanized = "Primarily Urban";
				}
			}
		} 
	}
	
	// disable main window during loading times
	public void disable() {
		submit.setEnabled(false);
		fileButton.setEnabled(false);
	}
	
	// re-enable
	public void enable() {
		submit.setEnabled(true);
		fileButton.setEnabled(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (source == lat) {
			latLabel.setText("" + source.getValue()/100.0);
			latChange.setText("Convert to Degrees, Minutes, Seconds");
		} else if (source == lon) {
			lonLabel.setText("" + source.getValue()/100.0);
			lonChange.setText("Convert to Degrees, Minutes, Seconds");
		}
	}
	
	public String getLat() {
		if(latitude.equals("0.00") || latitude.equals("0.0")) {
			return "";
		} else {
			return latitude;
		}
	}
	
	public String getLon() {
		if(longitude.equals("0.00") || longitude.equals("0.0")) {
			return "";
		} else {
			return longitude;
		}
	}
	
	public String getStartDate() {
		return startDatePicker.getDate().toString();
	}
	
	public String getEndDate() {
		return endDatePicker.getDate().toString();
	}
	
	//not used
	public ArrayList<String> getHabitats() {
		return habitats;
	}
	
	public String getGroup() {
		return groups.getSelectedItem().toString();
	}
	
	//not used
	public String getUrban() {
		return urbanized;
	}
	
	public String DMStoDD(String dms) {
		int x = dms.indexOf(DEGREE);
		String deg = dms.substring(0, x);
		int y = dms.indexOf("'");
		String min = dms.substring(x+1, y);
		int z = dms.indexOf("\"");
		String sec = dms.substring(y+1, z);
		double d = Double.parseDouble(deg);
		double m = Double.parseDouble(min);
		double s = Double.parseDouble(sec);
		Double dd = d + (m/60.0) + (s/3600.0);
		DecimalFormat df4 = new DecimalFormat("#.##");
		dd = Double.valueOf(df4.format(dd));
		return dd.toString();
	}
		
	public String DDtoDMS(String dd) {
		double ddDouble = Double.parseDouble(dd);
		int deg = (int) ddDouble;
		double minD = (ddDouble-deg)*60;
		int min = (int) minD;
		double secD = (ddDouble - deg - (min/60.0)) * 3600;
		int sec = (int) secD;
		String dms = deg+DEGREE+" "+min+"' "+sec+"\"";
		return dms;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
        //Mouse is over component
        Object source = e.getSource();
        if (source == groupHelp) {
            groupHelpPanel.setVisible(true);
        }
        if (source == datesHelp) {
            datesHelpPanel.setVisible(true);
        }
        if (source == habitatHelp) {
            habitatHelpPanel.setVisible(true);
        }
        if (source == selectFolderHelp) {
            folderHelpPanel.setVisible(true);
        }
        if (source == lonHelp) {
            lonlatHelpPanel.setVisible(true);
        }
        if (source == latHelp) {
            lonlatHelpPanel.setVisible(true);
        }
        if (source == urbanHelp) {
            urbanHelpPanel.setVisible(true);
        }
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		Object source = e.getSource();
		if (source == groupHelp) {
			groupHelpPanel.setVisible(false);
		}
		if (source == datesHelp) {
			datesHelpPanel.setVisible(false);
		}
		if (source == habitatHelp) {
			habitatHelpPanel.setVisible(false);
		}
		if (source == selectFolderHelp) {
			folderHelpPanel.setVisible(false);
		}
		if (source == lonHelp) {
			lonlatHelpPanel.setVisible(false);
		}
        if (source == latHelp) {
            lonlatHelpPanel.setVisible(false);
        }
		if (source == urbanHelp) {
			urbanHelpPanel.setVisible(false);
		}

	}

	public void completed() {
		boolean s = false;
		boolean e = false;
		boolean hab = false;
		boolean ur = false;
		boolean g = false;
		boolean f = false;
		LocalDate today = LocalDate.now();
		if(startDatePicker.getDate()!=null){
			s = true;
		}
		if(endDatePicker.getDate()!=null) {
			e = true;
		}
		if(one.isSelected()|| two.isSelected() || three.isSelected() || four.isSelected() || five.isSelected() || six.isSelected()
				|| seven.isSelected() || eight.isSelected() || nine.isSelected() ){
			hab = true;
		}
		if(!one.isSelected() && !two.isSelected() && !three.isSelected() && !four.isSelected() && !five.isSelected() && !six.isSelected()
				&& !seven.isSelected() && !eight.isSelected() && !nine.isSelected() ){
			hab = false;
		}
		if(rural.isSelected() || sub.isSelected() || urban.isSelected()) {
			ur = true;
		}
		if(!rural.isSelected() && !sub.isSelected() && !urban.isSelected()) {
			ur = false;
		}
		if(groups.getSelectedIndex() != -1) {
			g = true;
		}
		if(!filePath.getText().isEmpty()) {
			f = true;
		}
		if(s && e && hab && ur && g && f) {
			completed = true;
		}
		
	}
	
	void reset() {
		one.setSelected(false);
		two.setSelected(false);
		three.setSelected(false);
		four.setSelected(false);
		five.setSelected(false);
		six.setSelected(false);
		seven.setSelected(false);
		eight.setSelected(false);
		nine.setSelected(false);
		rural.setSelected(false);
		sub.setSelected(false);
		urban.setSelected(false);
		filePath.setText(null);
		groups.setSelectedIndex(-1);
		latLabel.setText("0.00");
		lonLabel.setText("0.00");
		lat.setValue(0);
		lon.setValue(0);
		startDatePicker.setDateToToday();
		endDatePicker.setDateToToday();
		habitats.clear();
		urbanized = "";
		fileButton.setText("Browse");
		fileTotal = 0;
	}


	@Override
	public void focusLost(FocusEvent evt) {
		
		if(evt.getSource() == lonLabel) {
			String value = lonLabel.getText();
			double lonPos = Double.parseDouble(value);
			double lonPosUpdated = lonPos*100;
			lon.setValue((int)lonPosUpdated);
		} else if(evt.getSource() == latLabel) {
			String value = latLabel.getText();
			double latPos = Double.parseDouble(value);
			double latPosUpdated = latPos*100;
			lat.setValue((int)latPosUpdated);
		} 

	}

	@Override
	public void focusGained(FocusEvent e) {
		
	}

}
