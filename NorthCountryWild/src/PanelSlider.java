
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.EventListener;
import java.util.Hashtable;

public class PanelSlider extends TabItem implements ChangeListener, FocusListener {
	
	private JTextField help;
	private final String DEGREE  = "\u00b0";
	private JSlider slider;
	private JTextField textBox;

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
        textBox.addFocusListener(this);
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
        	slider = new JSlider(JSlider.HORIZONTAL, -9000, 9000, 0);
    		slider.setPreferredSize(new Dimension(300, 45));
    		slider.addChangeListener(this);
    		slider.setMajorTickSpacing(2000);
    		slider.setMinorTickSpacing(500);
    		slider.setPaintTicks(true);
    		slider.setPaintLabels(true);
    		
    		//Create the label table
    		Hashtable latTable = new Hashtable();
    		latTable.put( new Integer( 9000 ), new JLabel("90") );
    		latTable.put( new Integer( 7000 ), new JLabel("70") );
    		latTable.put( new Integer( 5000 ), new JLabel("50") );
    		latTable.put( new Integer( 3000 ), new JLabel("30") );
    		latTable.put( new Integer( 1000 ), new JLabel("10") );
    		latTable.put( new Integer( -7000 ), new JLabel("-70") );
    		latTable.put( new Integer( -5000 ), new JLabel("-50") );
    		latTable.put( new Integer( -3000 ), new JLabel("-30") );
    		latTable.put( new Integer( -1000 ), new JLabel("-10") );
    		latTable.put( new Integer( -9000 ), new JLabel("-90") );
    		slider.setLabelTable( latTable );
    		slider.setPaintLabels(true);
        } else {  // desc = "Longitude:" OR default
        	slider = new JSlider(JSlider.HORIZONTAL, -18000, 18000, 0);
    		slider.setPreferredSize(new Dimension(300, 45));
    		slider.addChangeListener(this);
    		slider.setMajorTickSpacing(4000);
    		slider.setMinorTickSpacing(1000);
    		slider.setPaintTicks(true);
    		slider.setPaintLabels(true);
    		
    		Hashtable lonTable = new Hashtable();
    		lonTable.put( new Integer( 18000 ), new JLabel("180")); 
    		lonTable.put( new Integer( 14000 ), new JLabel("140")); 
    		lonTable.put( new Integer( 10000 ), new JLabel("100")); 
    		lonTable.put( new Integer( 6000 ), new JLabel("60")); 
    		lonTable.put( new Integer( 2000 ), new JLabel("20")); 
    		lonTable.put( new Integer( -14000 ), new JLabel("-140")); 
    		lonTable.put( new Integer( -10000 ), new JLabel("-100")); 
    		lonTable.put( new Integer( -6000 ), new JLabel("-60")); 
    		lonTable.put( new Integer( -2000 ), new JLabel("-20")); 
    		lonTable.put( new Integer( -18000 ), new JLabel("-180")); 
    		slider.setLabelTable( lonTable );
    		slider.setPaintLabels(true);
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

	
	public String getFinalValue() {
		return textBox.getText();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		textBox.setText("" + source.getValue());
	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent evt) {
		String value = textBox.getText();
		if (value.indexOf("'") > -1) {
			value = DMStoDD(value);
		}
		double pos = Double.parseDouble(value);
		if (pos > 0 && getDesc().equals("Longitude:")) {
			pos = -pos;
		}
		double posUpdated = pos*100;
		slider.setValue((int)posUpdated);
	}
	
}
