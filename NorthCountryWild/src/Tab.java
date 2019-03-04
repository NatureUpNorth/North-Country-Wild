import java.util.LinkedList;

// class to represent each tab in the GUI
public class Tab {
	
	private String title;
	private int position;
	private LinkedList<TabItem> panels;
	
	public Tab(String title, int pos, LinkedList<TabItem> panels) {
		this.title = title;
		this.position = pos;
		this.panels = panels;
	}
	
	public void addItem(TabItem item) {
		// we'll have to add a panel to the TabItem list here
	}
	
	public void setPos(int pos) {
		position = pos;
	}
	
	public int getPos() {
		return position;
	}
	
	public String getTitle() {
		return title;
	}

}
