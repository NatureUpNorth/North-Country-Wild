
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PanelDropdown extends TabItem {
	
	private String[] options;

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
        JComboBox<String> box = new JComboBox<String>(options);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(box, constraints);
    }
    
    public String[] getOptions() {
    	return options;
    }

}
