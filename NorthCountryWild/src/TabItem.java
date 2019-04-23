import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.JSONObject;

// parent class for a panel in the tab
public class TabItem {
	
	private String desc;
	private String hint;
	private String returnValue;
	private boolean required;
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
        try {
        	String str = jsonpanel.getString("required");
        	if (str.equals("true")) {
        		required = true;
        	} else {
        		required = false;
        	}
        } catch (org.json.JSONException e) {
        	required = false;
        }
		panel = new JPanel();
	}
	
	public TabItem(String desc, String hint, String returnValue, boolean required) {
		this.desc = desc;
		this.hint = hint;
		this.returnValue = returnValue;
		this.required = required;
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
	
	public boolean isRequired() {
		return required;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	// to be implemented by children
	public String getFinalValue() {
		return "";
	}
	
	public boolean check() {
		if ((getFinalValue() == null || getFinalValue().equals("")) && required) {
			JOptionPane.showMessageDialog(new JFrame(),
					"Incorrect entry for " + returnValue.toLowerCase() + ". Please enter all the required fields.");
			return false;
		} else {
			return true;
		}
	}
}
