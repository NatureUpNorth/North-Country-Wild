import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;

public class Tab {
	
	private TabItem[] panels;
	private String title;
	private JPanel panel;
	
	public Tab(TabItem[] panels, String title) {
		this.panels = panels;
		this.title = title;
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        
        // Determine if tab consists of just one form (in which case is should be enlarged)
        int formCount = 0;
        for (int i = 0; i < panels.length; i++) {
        	String type;
			type = panels[i].getType();
			if (type == null) {
				type = "none";
			}
			if(type.contentEquals("form")) {
				formCount++;
			}
        }
        
        // Add panels to tab
		for (int i = 0; i < panels.length; i++) {
			String type;			
			type = panels[i].getType();
			if (type == null) {
				type = "none";
			}
			
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.WEST;
			
			if(formCount == 1) {
				((PanelForm) panels[i]).enlarge();
			}
		
			constraints.gridx = 0;
			constraints.gridy = i;
			panel.add(panels[i].getPanel(), constraints);			
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
