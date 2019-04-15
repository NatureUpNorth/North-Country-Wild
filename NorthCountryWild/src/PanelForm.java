
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class PanelForm extends TabItem {

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

        // Add hint if present
        if (getHint() != "") {
            JLabel hint = new JLabel(getHint());
            panel.add(hint);
        }

        // Add textField
        JTextField textBox = new JTextField();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(textBox, constraints);

    }

}
