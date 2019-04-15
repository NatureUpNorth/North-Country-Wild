
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class PanelSlider extends TabItem {
	
	private JTextField help;

    public PanelSlider(JSONObject jsonpanel) {

        super(jsonpanel);

        // Prepare panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;

        // Add description, textField, and help to first row
        JPanel row1 = new JPanel();

        // Add description to first row
        JLabel description = new JLabel(getDesc());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(15, 15, 15, 15);
        row1.add(description, constraints);

        // Add textField to first row
        JTextField textBox = new JTextField("0.00");
//        textBox.addFocusListener(this);
//        textBox.addChangeListener(this);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        row1.add(textBox, constraints);

        // Add help icon to first row
        help = new JTextField(" ?");
        help.setEditable(false);
        help.setPreferredSize(new Dimension(23, 20));
//        help.addMouseListener(this);
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        row1.add(help, constraints);

        // Add first row to panel
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(row1);

        // Decide slider parameters
        JSlider slider;
        if (getDesc().equals("Latitude:")) {
            slider = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);
        } else {  // desc = "Longitude:" OR default
            slider = new JSlider(JSlider.HORIZONTAL, -180, 180, 0);
        }
//        framesPerSecond.addChangeListener(this);

        // Set slider design
        slider.setPreferredSize(new Dimension(300, 45));
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(15);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        // Add slider to panel
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(15, 15, 15, 15);
        panel.add(slider, constraints);

    }

}
