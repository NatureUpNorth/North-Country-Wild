import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;

public class Tab {
	
	private TabItem[] panels;
	private String title;
	private JPanel panel;
	private boolean multiChoice;
	
	public Tab(TabItem[] panels, String title) {
		this.panels = panels;
		this.title = title;
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        
        // Determine if one of the panel types is multiChoice (extra long - format side-by-side)
        for (int i = 0; i < panels.length; i++) {
        	String type;			
			type = panels[i].getType();
			if (type == null) {
				type = "none";
			}
			if(type.equals("multiChoiceList")) {
	    		multiChoice = true;
    		}
        }
        
		for (int i = 0; i < panels.length; i++) {
			String type;			
			type = panels[i].getType();
			if (type == null) {
				type = "none";
			}
			
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.WEST;
			
			if(multiChoice) {
				panel.setLayout(new GridLayout(1, 0));
	    		constraints.gridx = i;
				constraints.gridy = 0;
				constraints.weightx = 0.0;
				if(type.equals("multiChoiceList")) {
					constraints.weightx = 100;
				}
				panel.add(panels[i].getPanel());			
	    	} else {
				constraints.gridx = 0;
				constraints.gridy = i;
				panel.add(panels[i].getPanel(), constraints);			
	    	}
		}
	}
	
	public TabItem[] getPanels() {
		return panels;
	}
	
	public String getTitle() {
		return title;
	}
	
	public JPanel getPanel() {
		return panel;
	}
}
