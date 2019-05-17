import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.*;

public class TestWindow extends JPanel implements ActionListener, ChangeListener {

	private static Tab[] pages;
	static int length;
	private static int current;  // current page
	private JButton next;
	private JButton prev;
	private JTabbedPane tabularpane;
	private int max = 0;
	private JPanel buttons;
	private boolean uploading = false;
	private String[][] tabText;
	private ArrayList<String> values;
	static JFrame frame = new JFrame();
	private boolean switched = false; // for stateChanged, keeps track of if we need to display a dialogue box or not
	private static int tabularW = 800;
	private static int tabularH = 550;
	private static int frameW = ((Toolkit.getDefaultToolkit().getScreenSize()).width-tabularW)/2;
	private static int frameH = ((Toolkit.getDefaultToolkit().getScreenSize()).height-tabularW)/2;
	
	public TestWindow() {
		refreshPane();
		
		File file = new File("config/config.json"); 
		String str = new String();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String curr = new String();
			while ((curr = br.readLine()) != null) {
				str += curr;
			}
			br.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		JSONObject obj = new JSONObject(str);
		JSONArray tabs = obj.getJSONArray("tabs");
		pages = new Tab[tabs.length() + 1];
		
		for (int i = 0; i < tabs.length(); i++) {
			JSONObject tab = (JSONObject) tabs.get(i);
			JSONArray panels = tab.getJSONArray("panels");
			TabItem[] jpanels = makePanels(panels);
			String title = tab.getString("title");
			Tab newTab = new Tab(jpanels, title);
			pages[i] = newTab;
		}
		
		// add the tab which will be the review tab
		TabItem[] reviews = {new PanelReview("Please review the information you have entered: ", "", "")};
		Tab review = new Tab(reviews, "Review");
		pages[pages.length - 1] = review;
	
		refreshPane();
		tabularpane.addChangeListener(this);
		length = pages.length;
		current = 0;  // start on first page
		
		next = new JButton("Next");
		prev = new JButton("Previous");
		next.setPreferredSize(new Dimension(100, 50));
		prev.setPreferredSize(new Dimension(100, 50));
		
		next.addActionListener(this);
		prev.addActionListener(this);
		
		// Start with only first tab enabled
		tabularpane.add(pages[0].getPanel(), pages[0].getTitle());

		for(int index = 0; index < length; index++) {
			tabularpane.add(pages[index].getPanel(), pages[index].getTitle());
			tabularpane.setEnabledAt(index, false);
		}
		tabularpane.setEnabledAt(0, true);
		tabularpane.addChangeListener(this);

		prev.setEnabled(false);
		this.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		this.add(tabularpane, constraints);

		buttons = new JPanel();
		buttons.add(prev);
		buttons.add(next);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.add(buttons, constraints);
		
		frame.setResizable(false);
		frame.setLocation(frameW, frameH);		
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	// go through the panels in a tab and make the required displays
	private TabItem[] makePanels(JSONArray panels) {
		TabItem[] jpanels = new TabItem[panels.length()];
		for (int index = 0; index < panels.length(); index++) {
			JSONObject panel = (JSONObject) panels.get(index);
			String type = panel.getString("type");
			TabItem thisPanel;
			
			switch (type) {
				case "fileSelect":
					thisPanel = new PanelFileSelect(panel);
                    jpanels[index] = (thisPanel);
					break;
				case "multiPanel":
					TabItem[] subpanels = makePanels(panel.getJSONArray("subPanels"));
                    thisPanel = new PanelMultiSubpanels(panel, subpanels);
					jpanels[index] = (thisPanel);
					break;
				case "dropdown":
					thisPanel = new PanelDropdown(panel);
                    jpanels[index] = (thisPanel);
					break;
				case "text":
					thisPanel = new PanelText(panel);
                    jpanels[index] = (thisPanel);
					break;
				case "date":
					thisPanel = new PanelDate(panel);
                    jpanels[index] = (thisPanel);
					break;
				case "slider":
					thisPanel = new PanelSlider(panel);
                    jpanels[index] = (thisPanel);
					break;
				case "multiChoiceList":
					thisPanel = new PanelMultiChoice(panel);
                    jpanels[index] = (thisPanel);
					break;
				case "singleChoiceList":
					thisPanel = new PanelSingleChoice(panel);
                    jpanels[index] = (thisPanel);
					break;
				case "form":
					thisPanel = new PanelForm(panel);
                    jpanels[index] = (thisPanel);
					break;
				default:
					throw new RuntimeException("Error: unrecognized panel type: " + type);
			}
		}
		return jpanels;
	}
	
	// display the current panel
	private void display() {
		// check if buttons need to be en-/disabled
		if (current == 0) {
			prev.setEnabled(false);
		} else if (current == length - 2) {
			next.setText("Review");
		} else {
			prev.setEnabled(true);
			next.setEnabled(true);
		}
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		this.add(tabularpane, constraints);
		
		tabularpane.setSelectedIndex(current);
		this.removeAll();
		this.add(tabularpane);
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.add(buttons, constraints);

		this.revalidate();
		this.repaint();
		this.revalidate();
		this.repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		// Check for button press in PanelReview buttons
		if (PanelReview.dataFilled) {
			for (int i = 0; i < PanelReview.buttons.length; i++) {
				if (arg0.getSource().equals(PanelReview.buttons[i])) {
					current = i;
				}
			}
		}

		if (arg0.getSource().equals(prev)) {
			next.setText("Next");
			current--;
		} else if (arg0.getSource().equals(next)) {
			if (next.getText().equals("Review")) {
				Tab tab = pages[current];
				TabItem[] tabpanels = tab.getPanels();
				boolean pass = true;
				for (int i = 0; i < tabpanels.length; i++) {
					if (!tabpanels[i].check()) {
						pass = false;
						break;
					}
				}
				if (pass) {
					review();
					current = pages.length - 1;
					
					if (max < current) {
						max = current;
					}
					refreshPane();
					tabularpane.addChangeListener(this);
					int curr = current;
					
					for(int index = 0; index < length; index++) {
						tabularpane.add(pages[index].getPanel(), pages[index].getTitle());
						tabularpane.setEnabledAt(index, false);
					}
					for(int i = 0; i <= max; i++) {
						tabularpane.setEnabledAt(i, true);
					}
					current = curr;
					next.setText("Upload");
				}
			} else if (next.getText().equals("Upload")) {
				if (!uploading) {
					this.setUploading(true);
				}
			} else {
				Tab tab = pages[current];
				TabItem[] tabpanels = tab.getPanels();
				boolean pass = true;
				for (int i = 0; i < tabpanels.length; i++) {
					if (!tabpanels[i].check()) {
						pass = false;
					}
				}
				if (pass) {
					current++;
					if (max < current) {
						max = current;
					}
				}
			}

			refreshPane();
			for(int index = 0; index < length; index++) {
				tabularpane.add(pages[index].getPanel(), pages[index].getTitle());
				tabularpane.setEnabledAt(index, false);
			}
			for(int i = 0; i <= max; i++) {
				tabularpane.setEnabledAt(i, true);
			}
			tabularpane.setSelectedIndex(current);
			tabularpane.addChangeListener(this);
		}
		display();
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource().equals(tabularpane) && max >= 1) {
			if (!switched) {
				Tab tab = pages[current];
				TabItem[] tabpanels = tab.getPanels();
				boolean pass = true;
				for (int i = 0; i < tabpanels.length; i++) {
					if (!tabpanels[i].check()) {
						pass = false;
						if (!switched) {
							switched = true;
							tabularpane.setSelectedIndex(current);
						}
						break;
					}
				} 
				if (pass) {
					current = tabularpane.getSelectedIndex();
					if (current == pages.length - 1) {
						next.setText("Upload");
					} else if (current == pages.length - 2) {
						next.setText("Review");
					} else {
						next.setText("Next");
					}
					
					if (current == 0) {
						prev.setEnabled(false);
					} else {
						prev.setEnabled(true);
					}
				}
			} else {
				switched = false;
			}
		}
	}
	
	// set up the review page once it's clicked on
	public void review() {
		int nonReviewTabCount = pages.length - 1;
		tabText = new String[nonReviewTabCount][];
		values = new ArrayList<String>();  // Used for error checking
		
		for (int i = 0; i < nonReviewTabCount; i++) {  // For each tab
			TabItem[] subpanels = parseSubpanels(i);
			tabText[i] = new String[subpanels.length];
			
			for (int j = 0; j < tabText[i].length; j++) {  // For each subpanel
				String str = "";
				TabItem panel = subpanels[j];

				if (!panel.getFinalValue().equals("") && !(panel.getFinalValue() == null)) {
					values.add(panel.getReturnValue());
					values.add(panel.getFinalValue());					
					str = panel.getReturnValue() + ": " + panel.getFinalValue();
				}
				
				tabText[i][j] = str;  // Add string to this tab's index
			}
		}
		
		PanelReview.setData(tabText); 
		PanelReview.dataFilled = true;
		PanelReview reviewPanel = new PanelReview("Please review the information you have entered: ", "", "");
		
		for (int i = 0; i < PanelReview.buttons.length; i++) {
			PanelReview.buttons[i].addActionListener(this);
		}
	
		TabItem[] reviewTabItem = {reviewPanel};
		Tab reviewTab = new Tab (reviewTabItem, "Review");
		pages[pages.length - 1] = reviewTab;
	}
	
	private TabItem[] parseSubpanels(int page) {
		Tab tab = pages[page];
		ArrayList<TabItem> listSubs = new ArrayList<>();
		TabItem[] tempSubs = tab.getPanels();
		
		// ArrayList of all panels, including those within multiPanels
		for (int i = 0; i < tempSubs.length; i++) {  // For each main subPanel
			TabItem panel = tempSubs[i];
			if (panel.getClass().toString().equals("class PanelMultiSubpanels")) {  // If multiPanel
				TabItem[] multiSubs = ((PanelMultiSubpanels) panel).getSubpanels();
				for (int j = 0; j < multiSubs.length; j++) {  // For each subPanel
					listSubs.add(multiSubs[j]);
				}
			} else {
				listSubs.add(panel);
			}
		}
		
		// Translate ArrayList to array
		TabItem[] subs = new TabItem[listSubs.size()];
		for (int k = 0; k < listSubs.size(); k++) {
			subs[k] = listSubs.get(k);
		}
		return subs;
	}
	
	public void refreshPane() {
		tabularpane = new JTabbedPane();
		tabularpane.setPreferredSize(new Dimension(tabularW, tabularH));
	}
	
	public void setUploading(boolean status) {
		uploading = status;
	}
	
	public void disable() {
		next.setEnabled(false);
		prev.setEnabled(false);
	}
	
	public void enable() {
		next.setEnabled(true);
		prev.setEnabled(true);
	}
	
	public synchronized boolean isUploading() {
		return uploading;
	}
	
	public static Tab[] getTabs() {
		return pages;
	}
	
	public int getCount() {
		return ((PanelFileSelect) pages[0].getPanels()[0]).getCount();
	}
	
	public ArrayList<String> getValues() {
		return values;
	}
	
	public static Point getFramePosition() {
		return new Point(frameW, frameH);
	}
	
	public static int getTabularW() {
		return tabularW;
	}
	
	public static int getTabularH() {
		return tabularH;
	}
	
	public static void setCurrent(int pageNum) {
		current = pageNum;
	}
		
}
	

