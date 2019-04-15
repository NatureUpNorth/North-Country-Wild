import javax.swing.JPanel;

public class Tab {
	
	private TabItem[] panels;
	private String title;
	private JPanel panel;
	
	public Tab(TabItem[] panels, String title) {
		this.panels = panels;
		this.title = title;
		panel = new JPanel();
		for (int i = 0; i < panels.length; i++) {
			panel.add(panels[i].getPanel());
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
