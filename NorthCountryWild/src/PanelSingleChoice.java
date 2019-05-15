import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;

public class PanelSingleChoice extends TabItem {

	private JList<String> choices;
    public PanelSingleChoice(JSONObject jsonpanel) {

    	super(jsonpanel);
    	
        // Prepare panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(15, 15, 15, 15);

        // Add description
        JLabel description = new JLabel(getDesc());
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(description, constraints);
        
        if (help()) {
        	constraints.gridx = 1;
        	panel.add(getHelpTag(), constraints);
        }

        JSONArray values = jsonpanel.getJSONArray("values");
        String[] options = new String[values.length()];
        for (int i = 0; i < jsonpanel.getJSONArray("values").length(); i++)  {

            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.anchor = GridBagConstraints.WEST;
            constraints.gridx = 1;
            constraints.gridy = i;

            String title = (String) values.get(i);
            options[i] = title;
        }
        
        choices = new JList<String>(options);
        choices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        choices.setLayout(new GridBagLayout());

        // Add radioButtons to panel
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(choices, constraints);

    }
    
    public String getFinalValue() {
    	return choices.getSelectedValue();
    }
}
