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
		pages = new Tab[tabs.length()];
		
		for (int i = 0; i < tabs.length(); i++) {
			JSONObject tab = (JSONObject) tabs.get(i);
			JSONArray panels = tab.getJSONArray("panels");
			TabItem[] jpanels = makePanels(panels);
			String title = tab.getString("title");
			Tab newTab = new Tab(jpanels, title);
			pages[i] = newTab;
		}
	
		tabularpane = new JTabbedPane();
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
		} else if (current == length - 1) {
			next.setEnabled(false);
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
			current--;
		} else if (arg0.getSource().equals(next)) {
			current++;
			if (max < current) {
				max = current;
			}
			tabularpane = new JTabbedPane();
			for(int index = 0; index < length; index++) {
				tabularpane.add(pages[index].getPanel(), pages[index].getTitle());
				tabularpane.setEnabledAt(index, false);
			}
			for(int i = 0; i <= max; i++) {
				tabularpane.setEnabledAt(i, true);
			}
		}
		display();
	}
	
	public static void main(String args[]) {
		TestWindow test = new TestWindow();
		JFrame frame = new JFrame();
		frame.add(test);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
	

