import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.json.JSONObject;

// parent class for a panel in the tab
public class TabItem implements MouseListener {
	
	private String desc;
	private String hint;
	private String returnValue;
	private String type;
	private boolean required;
	protected JPanel panel;
	private JTextField helpTag;
	private JPanel helpPanel;
	
	public TabItem(JSONObject jsonpanel) {
		
		// Fetch data from config file
		try {
        	type = jsonpanel.getString("type");
        } catch (org.json.JSONException e) {
        	type = "";
        }
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
        		desc = desc + "   *";
        	} else {
        		required = false;
        	}
        } catch (org.json.JSONException e) {
        	required = false;
        }
		panel = new JPanel();
		
		// Add help panel
		helpTag = new JTextField(" ?");
		helpTag.setEditable(false);
		helpTag.setPreferredSize(new Dimension(23, 20));
		helpTag.addMouseListener(this);
        
        JTextArea hint = new JTextArea();
		hint.setText(getHint());
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setEditable(false);
	    
		helpPanel = new JPanel();
        helpPanel.setOpaque(false);
        helpPanel.add(hint);
        helpPanel.setSize(100, 300);
        TestWindow.frame.add(helpPanel);
		helpPanel.setVisible(false);
	}
	
	public TabItem(String desc, String hint, String returnValue, boolean required) {
		this.desc = desc;
		this.hint = hint;
		this.returnValue = returnValue;
		this.required = required;
		panel = new JPanel();
	}
		
	public JPanel getHelpPanel() {
		return helpPanel;
	}
	
	public JTextField getHelpTag() {
		return helpTag;
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
	
	public String getType() {
		return type;
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
	
	@Override
	public void mouseEntered(MouseEvent arg0) {
		///Mouse is over component
        Object source = arg0.getSource();
        if (source == helpTag) {
        	int x = MouseInfo.getPointerInfo().getLocation().x - TestWindow.getFramePosition().x + 20;
        	int y = MouseInfo.getPointerInfo().getLocation().y - TestWindow.getFramePosition().y - helpPanel.getHeight() - 20;
        	if ((x + helpPanel.getWidth()) > TestWindow.getTabularW()) {
        		x = MouseInfo.getPointerInfo().getLocation().x - TestWindow.getFramePosition().x - helpPanel.getWidth() - 20;
        	}
        	if ((y - helpPanel.getHeight()) < TestWindow.getTabularH()) {
        		y = MouseInfo.getPointerInfo().getLocation().y - TestWindow.getFramePosition().x + 2*helpPanel.getHeight() + 20;
        	}
            helpPanel.setLocation(x, y);
            helpPanel.setVisible(true);
        }		
	}
	
	@Override
	public void mouseExited(MouseEvent arg0) {
        //Mouse is over component
        Object source = arg0.getSource();
        if (source == helpTag) {
        	helpPanel.setVisible(false);
        }
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}
	
	public boolean help() {
		return (hint != "");
	}
}
