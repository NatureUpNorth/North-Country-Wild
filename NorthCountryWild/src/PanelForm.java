
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class PanelForm extends TabItem {
	
	private JTextField textBox;
    public PanelForm(JSONObject jsonpanel) {

        super(jsonpanel);

        // Prepare panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(15, 15, 15, 15);

        // Add description
        JLabel description = new JLabel(getDesc());      
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);
        
        if (help()) {
        	constraints.gridx = 1;
        	panel.add(getHelpTag(), constraints);
        }
        
        // Add textField
        textBox = new JTextField();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(textBox, constraints);

    }
    
    public String getFinalValue() {
    	return textBox.getText();
    }
}
