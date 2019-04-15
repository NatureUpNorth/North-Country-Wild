import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.demo.FullDemo;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Date;

public class PanelDate extends TabItem {

    private JTextField help;
    private Date start;
    private Date end;
    
    public PanelDate(JSONObject jsonpanel) {

        super(jsonpanel);

        // Prepare JPanel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 15, 15, 15);
        constraints.anchor = GridBagConstraints.WEST;

        // Add description
        JLabel description = new JLabel(getDesc());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);

        // Add hint if present
        if (getHint() != "") {
        	JLabel hint = new JLabel(getHint());
        }

        // Prepare row for textField, calendar icon, and help panel
        JPanel row2 = new JPanel();
        row2.setLayout(new GridBagLayout());

        // Add calendar
        DatePickerSettings dateSettings;
        URL dateImageURL = FullDemo.class.getResource("/images/datepickerbutton1.png");
        Image dateExampleImage = Toolkit.getDefaultToolkit().getImage(dateImageURL);
        ImageIcon dateExampleIcon = new ImageIcon(dateExampleImage);

        dateSettings = new DatePickerSettings();
        DatePicker calendar = new DatePicker(dateSettings);
        calendar.setDateToToday();
        JButton datePickerButton = calendar.getComponentToggleCalendarButton();
        datePickerButton.setText("");
        datePickerButton.setIcon(dateExampleIcon);
        dateSettings.setAllowKeyboardEditing(false);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        row2.add(calendar, constraints);

        // Add help panel
        help = new JTextField(" ?");
        help.setEditable(false);
        help.setPreferredSize(new Dimension(23, 20));
//        help.addMouseListener(this);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        row2.add(help, constraints);

        // Add second row to panel
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(row2, constraints);
    }

    public boolean validDate () {
        return (start.before(end));
    }
    
    public Date[] getDates() {
    	Date[] dates = {start, end};
    	return dates;
    }

}
