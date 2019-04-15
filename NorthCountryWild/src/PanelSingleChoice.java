import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;

public class PanelSingleChoice extends TabItem {
	
	private String[] options;

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

        // Add radioButtons to their own panel (enables alignment)
        JPanel radioButtons = new JPanel();
        radioButtons.setLayout(new GridBagLayout());

        JSONArray values = jsonpanel.getJSONArray("values");
        options = new String[values.length()];
        for (int i = 0; i < jsonpanel.getJSONArray("values").length(); i++)  {

            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.anchor = GridBagConstraints.WEST;
            constraints.gridx = 1;
            constraints.gridy = i;

            String title = (String) values.get(i);
            JRadioButton option = new JRadioButton(title);
            options[i] = title;
            radioButtons.add(option, constraints);
        }

        // Add radioButtons to panel
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(radioButtons, constraints);

    }
    
    public String[] getOptions() {
    	return options;
    }

}
