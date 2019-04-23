
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class PanelText extends TabItem {
	
	private JLabel description;

    public PanelText(JSONObject jsonpanel) {

        super(jsonpanel);

        // Set panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;

        // Add text description
        description = new JLabel(getDesc());
        constraints.insets = new Insets(15, 15, 15, 15);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);

    }
    
    public PanelText(String desc, String hint, String returnValue) {
    	super(desc, hint, returnValue, false);
    	
    	// Set panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;

        // Add text description
        description = new JLabel(getDesc());
        constraints.insets = new Insets(15, 15, 15, 15);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);
    }
    
    public void setText(String s) {
    	description.setText(s);
    }
    
    public String getText() {
    	return description.getText();
    }
    
    public void appendText(String s) {
    	String str = description.getText();
    	description.setText("<html>" + str + s + "</html>");
    }
    
}
