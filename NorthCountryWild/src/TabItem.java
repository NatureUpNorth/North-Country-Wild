import java.awt.Dimension;

import javax.swing.JPanel;

import org.json.JSONObject;

// parent class for a panel in the tab
public class TabItem {
	
	private String desc;
	private String hint;
	private String returnValue;
	protected JPanel panel;
	
	public TabItem(JSONObject jsonpanel) {
		// Fetch data from config file
        try {
        	desc = jsonpanel.getString("desc");
        } catch (org.json.JSONException e) {
        	desc = "";
        }
        try {
        	returnValue = jsonpanel.getString("returnValue");
        } catch (org.json.JSONException e) {
        	returnValue = "";
        }
        try {
        	hint = jsonpanel.getString("hint");
        } catch (org.json.JSONException e) {
        	hint = "";
        }
		panel = new JPanel();
	}
	
	public String getDesc() {
		return desc;
	}
	public String getHint() {
		return hint;
	}
	public String getReturnValue() {
		return returnValue;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
}
