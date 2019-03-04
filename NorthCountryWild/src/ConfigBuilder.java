import java.util.LinkedList;

// class for reading the config file and building an item from it
public class ConfigBuilder {
	
	// a list of the types of tab items that can be implemented, by their keyword from the JSON file
	private static final String[] TYPES = {"form", "date", "slider", "singleChoiceList", "multiChoiceList", 
			"text", "fileSelect", "multiPanel", "dropdown"}; 
	
	private LinkedList<Tab> tabs;
	
	public ConfigBuilder(String filename) {
		this.parse(filename);
	}
	
	private void parse(String filename) {
		// this is where the JSON file will need to be parsed
	}
	
	public LinkedList<Tab> changePos(int firstPos, int secondPos) {
		// switch the tabs at the two indices given
		// also return the new list of tabs for convenience
		
		return tabs;
	}
	
	public LinkedList<Tab> getTabs() {
		return tabs;
	}

	
}
