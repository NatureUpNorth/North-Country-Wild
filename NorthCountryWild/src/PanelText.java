
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class PanelText extends TabItem {

    public PanelText (JSONObject jsonpanel) {

        super(jsonpanel);

        // Set panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;

        // Add text description
        JLabel description = new JLabel(getDesc());
        constraints.insets = new Insets(15, 15, 15, 15);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);

    }

}
