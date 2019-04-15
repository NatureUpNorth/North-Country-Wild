import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class PanelMultiChoice extends TabItem {
	
	private String[] options;

    public PanelMultiChoice(JSONObject jsonpanel) {

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

}
