
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.EventListener;

public class PanelSlider extends TabItem implements ActionListener, ChangeListener {
	
	private JTextField help;
	private final String DEGREE  = "\u00b0";
	private JSlider slider;
	private JTextField textBox;
	private JButton button = new JButton();

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
        textBox = new JTextField("0.00");
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
        if (getDesc().equals("Latitude:")) {
            slider = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);
            button.setText("Convert to Degrees, Minutes, Seconds");
            button.addActionListener(this);
        } else {  // desc = "Longitude:" OR default
            slider = new JSlider(JSlider.HORIZONTAL, -180, 180, 0);
            button.setText("Convert to Degrees, Minutes, Seconds");
            button.addActionListener(this);
        }
//        framesPerSecond.addChangeListener(this);
        slider.addChangeListener(this);
        
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
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(button, constraints);

    }
    
    public String DMStoDD(String dms) {
		int x = dms.indexOf(DEGREE);
		String deg = dms.substring(0, x);
		int y = dms.indexOf("'");
		String min = dms.substring(x+1, y);
		int z = dms.indexOf("\"");
		String sec = dms.substring(y+1, z);
		double d = Double.parseDouble(deg);
		double m = Double.parseDouble(min);
		double s = Double.parseDouble(sec);
		Double dd = d + (m/60.0) + (s/3600.0);
		DecimalFormat df4 = new DecimalFormat("#.##");
		dd = Double.valueOf(df4.format(dd));
		return dd.toString();
	}
		
	public String DDtoDMS(String dd) {
		double ddDouble = Double.parseDouble(dd);
		int deg = (int) ddDouble;
		double minD = (ddDouble-deg)*60;
		int min = (int) minD;
		double secD = (ddDouble - deg - (min/60.0)) * 3600;
		int sec = (int) secD;
		String dms = deg+DEGREE+" "+min+"' "+sec+"\"";
		return dms;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (getDesc().equals("Latitude:") || getDesc().equals("Longitude:")) {
			if (!textBox.getText().isEmpty()) {
				if(button.getText().equals("Convert to Degrees, Minutes, Seconds")) {
					String lati = DDtoDMS(textBox.getText());
					textBox.setText(lati);
					button.setText("     Convert to Decimal Degrees     ");
				} else if (button.getText().equals("     Convert to Decimal Degrees     ")) {
					String lati = DMStoDD(textBox.getText());
					textBox.setText(lati);
					button.setText("Convert to Degrees, Minutes, Seconds");
				}
			}
		}
	}
	
	public String getFinalValue() {
		return textBox.getText();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		textBox.setText("" + source.getValue());
		button.setText("Convert to Degrees, Minutes, Seconds");
	}
	
}
