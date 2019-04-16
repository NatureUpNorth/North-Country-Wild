import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;

public class PanelMultiChoice extends TabItem implements ChangeListener {
	
	private String[] options;  // a list of all available options
	private ArrayList<String> choices;  // a list of all the ones the user has selected

    public PanelMultiChoice(JSONObject jsonpanel) {

        super(jsonpanel);
        choices = new ArrayList<String>();
        
        // Prepare panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(15, 15, 15, 15);

        // Add description
        JLabel description = new JLabel(getDesc());
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(description, constraints);

        // Add checkboxes to their own panel (enables alignment)
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new GridBagLayout());
        JSONArray values = jsonpanel.getJSONArray("values");
        options = new String[values.length()];
        for (int i = 0; i < values.length(); i++)  {

            String title = (String) values.get(i);
            JCheckBox option = new JCheckBox(title);

            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.gridx = 0;
            constraints.gridy = i;
            
            options[i] = title;
            option.addChangeListener(this);
            option.setRolloverEnabled(false);
            checkBoxPanel.add(option, constraints);
        }

        // Add checkboxes to panel
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(15, 15, 15, 15);
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(checkBoxPanel, constraints);
    }
    
    public String[] getOptions() {
    	return options;
    }
    
    public String getFinalValue() {
    	if (choices.size() == 0) {
    		return "";
    	}
    	String str = choices.get(0);
    	for (int i = 1; i < choices.size(); i++) {
    		str += ", " + choices.get(i);
    	}
    	return str;
    }

	@Override
	public void stateChanged(ChangeEvent e) {
		JCheckBox cb = (JCheckBox) e.getSource();
		if(cb.isSelected() && choices.indexOf(cb.getText()) < 0) {
			choices.add(cb.getText());
		} else {
			choices.remove(cb.getText());
		} 
	}

}
