import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.*;

public class TestWindow extends JPanel implements ActionListener, ChangeListener {

	private Tab[] pages;
	private int length;
	private int current;  // current page
	private JButton next;
	private JButton prev;
	private JTabbedPane tabularpane;
	private int max = 0;
	private JPanel buttons;
	private boolean uploading = false;
	private ArrayList<String> values;
	
	public TestWindow() {
		JTabbedPane tabularpane = new JTabbedPane();
		
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
		TabItem[] reviews = {new PanelText("Please review the information you have entered:<br/><br/>", "", "")};
		Tab review = new Tab(reviews, "Review");
		pages[pages.length - 1] = review;
	
		tabularpane = new JTabbedPane();
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
		
		JFrame frame = new JFrame();
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
		if (arg0.getSource().equals(prev)) {
			next.setText("Next");
			current--;
		} else if (arg0.getSource().equals(next)) {
			if (next.getText().equals("Review")) {
				next.setText("Upload");
				review();
				current++;
				if (max < current) {
					max = current;
				}
				tabularpane = new JTabbedPane();
				tabularpane.addChangeListener(this);
				for(int index = 0; index < length; index++) {
					tabularpane.add(pages[index].getPanel(), pages[index].getTitle());
					tabularpane.setEnabledAt(index, false);
				}
				for(int i = 0; i <= max; i++) {
					tabularpane.setEnabledAt(i, true);
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
			tabularpane = new JTabbedPane();
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
		if (arg0.getSource().equals(tabularpane)) {
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
	}
	
	// set up the review page once it's clicked on
	public void review() {
		((PanelText) pages[pages.length-1].getPanels()[0]).setText("Please review the information you have entered:<br/><br/>");
		String str = "";
		values = new ArrayList<String>();
		for (int i = 0; i < pages.length - 1; i++) {
			Tab tab = pages[i];
			TabItem[] subpanels = tab.getPanels();
			for (int j = 0; j < tab.getPanels().length; j++) {
				TabItem panel = subpanels[j];
				if (panel.getClass().toString().equals("class PanelMultiSubpanels")) {
					str += checkMultiPanel((PanelMultiSubpanels) panel);
				} else if (!panel.getReturnValue().equals("") && !(panel.getReturnValue() == null)) {
					values.add(panel.getReturnValue());
					values.add(panel.getFinalValue());
					str += "<b>" + panel.getReturnValue()+ "</b>" + ": " + panel.getFinalValue() + "<br/><br/>";
				}
			}
		}
		((PanelText) pages[pages.length-1].getPanels()[0]).appendText(str);
	}
	
	private String checkMultiPanel(PanelMultiSubpanels panel) {
		TabItem[] panels = panel.getSubpanels();
		String str = "";
		for (int k = 0; k < panels.length; k++) {
			if (!panels[k].getReturnValue().equals("") && !(panels[k].getReturnValue() == null)) {
				if (panels[k].getClass().toString().equals("class PanelMultiSubpanels")) {
					str += checkMultiPanel((PanelMultiSubpanels) panels[k]);
				} else if (!panels[k].getReturnValue().equals("") && !(panels[k].getReturnValue() == null)) {
					values.add(panels[k].getReturnValue());
					values.add(panels[k].getFinalValue());
					str += "<b>" + panels[k].getReturnValue()+ "</b>" + ": " + panels[k].getFinalValue() + "<br/><br/>";
				}
			}
		}
		return str;
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
	
	public Tab[] getTabs() {
		return pages;
	}
	
	public int getCount() {
		return ((PanelFileSelect) pages[0].getPanels()[0]).getCount();
	}
	
	public ArrayList<String> getValues() {
		return values;
	}
	
}
	

