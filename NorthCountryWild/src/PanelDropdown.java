
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelDropdown extends TabItem implements ActionListener {
	
	private String[] options;
	private JComboBox<String> box;
	private GridBagConstraints constraints;
	private JTextField textBox;

    public PanelDropdown (JSONObject jsonpanel) {

        super(jsonpanel);

        // Prepare JPanel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 15, 15, 15);

        // Add description
        JLabel description = new JLabel(getDesc());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);

        // Fetch dropdown options and create JComboBox
        JSONArray opt = jsonpanel.getJSONArray("values");
        options = new String[opt.length()];
        for (int i = 0; i < opt.length(); i++) {
            options[i] = (String) opt.get(i);
        }

        // Add comboBoxes
        box = new JComboBox<String>(options);
        box.addActionListener(this);         
        
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(box, constraints);
        
        if (getHint() != "") {
        	constraints.gridx = 1;
        	panel.add(getHelpTag(), constraints);
        }
        
        // Add potential text field
        textBox = new JTextField();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(textBox, constraints);  
        textBox.setEditable(false);
    }
    
    @Override
	public void actionPerformed(ActionEvent arg0) {
    	if (arg0.getSource().equals(box)) {
    		if (box.getSelectedItem().equals("+ ADD NEW")) {
    			textBox.setEditable(true);
       		} else {
    			textBox.setEditable(false);
       		}
    	}		
	}

    
    public String[] getOptions() {
    	return options;
    }
    
    public String getFinalValue() {
    	if (((String)box.getSelectedItem()).equals("+ ADD NEW")) {
    		return textBox.getText();
    	}
    	return (String) box.getSelectedItem();
    }

}
