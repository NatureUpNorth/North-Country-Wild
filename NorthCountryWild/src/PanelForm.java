
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class PanelForm extends TabItem {
	
	private JTextArea textBox;
    GridBagConstraints constraints = new GridBagConstraints();

    public PanelForm(JSONObject jsonpanel) {

        super(jsonpanel);

        // Prepare panel layout
        JPanel header = new JPanel();
        header.setLayout(new GridBagLayout());
        panel.setLayout(new GridBagLayout());
        constraints.insets = new Insets(15, 15, 15, 15);

        // Add description
        JLabel description = new JLabel(getDesc());      
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        header.add(description, constraints);
        
        if (help()) {
        	constraints.gridx = 1;
        	header.add(getHelpTag(), constraints);
        }
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(header, constraints);
        
        // Add textField
        textBox = new JTextArea();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(textBox, constraints);

    }
    
    public String getFinalValue() {
    	return textBox.getText();
    }
    
    public void enlarge() {
    	textBox.setLineWrap(true);
    	textBox.setWrapStyleWord(true);
    	textBox.setPreferredSize(new Dimension(panel.getSize().width, 100)); 
    }
}
