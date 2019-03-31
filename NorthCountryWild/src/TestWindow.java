import java.awt.Dimension;
import java.awt.FlowLayout;
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

import org.json.*;

public class TestWindow extends JPanel implements ActionListener {

	private JPanel[] pages;
	private int length;
	private int current;  // current page
	private JButton next;
	private JButton prev;
	private JTabbedPane tabularpane;
	private int max = 0;
	
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
		for (int i = 0; i < tabs.length(); i++) {
			JSONObject tab = (JSONObject) tabs.get(i);
			JSONArray panels = tab.getJSONArray("panels");
			JPanel[] jpanels = makePanels(panels);
			JPanel temp = new JPanel();
			for (int j = 0; j < jpanels.length; j++) {
				temp.add(jpanels[j]);
			}
			pages[i] = temp;
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
		
		tabularpane.add(pages[0], "Tab 0");
		
		this.setLayout(new FlowLayout());
		this.add(tabularpane);
		prev.setEnabled(false);
		this.add(prev);
		this.add(next);
	}
	
	// go through the panels in a tab and make the required displays
	private JPanel[] makePanels(JSONArray panels) {
		JPanel[] jpanels = new JPanel[panels.length()];
		for (int j = 0; j < panels.length(); j++) {
			JSONObject panel = (JSONObject) panels.get(j);
			String type = panel.getString("type");
			String desc = "";
			String hint = "";
			String returnval = "";
			
			switch (type) {
				case "fileSelect":
					desc = panel.getString("desc");
					hint = panel.getString("hint");
					returnval = panel.getString("returnValue");
					break;
				case "multiPanel":
					JPanel[] subpanels = makePanels(panel.getJSONArray("subPanels"));
					JPanel temp = new JPanel();
					for (int i = 0; i < subpanels.length; i++) {
						temp.add(subpanels[i]);
					}
					jpanels[j] = temp;
					break;
				case "dropdown":
					desc = panel.getString("desc");
					hint = panel.getString("hint");
					returnval = panel.getString("returnValue");
					break;
				case "text":
					break;
				case "date":
					desc = panel.getString("desc");
					hint = panel.getString("hint");
					returnval = panel.getString("returnValue");
					break;
				case "slider":
					desc = panel.getString("desc");
					hint = panel.getString("hint");
					returnval = panel.getString("returnValue");
					break;
				case "multiChoiceList":
					desc = panel.getString("desc");
					hint = panel.getString("hint");
					returnval = panel.getString("returnValue");
					break;
				case "singleChoiceList":
					desc = panel.getString("desc");
					hint = panel.getString("hint");
					returnval = panel.getString("returnValue");
					break;
				case "form":
					desc = panel.getString("desc");
					returnval = panel.getString("returnValue");
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
		
		tabularpane.setSelectedIndex(current);
		// nothing to remove the first time
			this.removeAll();
			this.add(tabularpane);
			this.add(prev);
			this.add(next);
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
			for (int i = 0; i <= max; i++) {
				tabularpane.add(pages[i], "Tab " + Integer.toString(i));
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
	
}
	

