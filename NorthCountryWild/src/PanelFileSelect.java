import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;

public class PanelFileSelect extends TabItem {

    private String filePath = "";
    private JTextField fileLocation;
    private JTextField help;

    public PanelFileSelect(JSONObject jsonpanel) {
    	
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

        // Add filepath textField
        fileLocation = new JTextField(filePath);
        constraints.insets = new Insets(0, 0, 30, 0);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(fileLocation, constraints);

        // Add browse button
        JButton browse = new JButton("Browse");
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(browse, constraints);

        // Add help panel
        help = new JTextField(" ?");
        help.setEditable(false);
        help.setPreferredSize(new Dimension(23, 20));
//        help.addMouseListener(this);
        constraints.insets = new Insets(0, 15, 15, 15);
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(help, constraints);

    }
    
    public String getFilePath() {
    	return filePath;
    }
    
}
