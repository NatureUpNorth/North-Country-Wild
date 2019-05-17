import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.demo.FullDemo;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;

public class PanelDate extends TabItem {

    private JTextField help;
    private DatePicker calendar;
    
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

        // Prepare row for textField, calendar icon, and help panel
        JPanel row2 = new JPanel();
        row2.setLayout(new GridBagLayout());

        // Add calendar
        DatePickerSettings dateSettings;
        URL dateImageURL = FullDemo.class.getResource("/images/datepickerbutton1.png");
        Image dateExampleImage = Toolkit.getDefaultToolkit().getImage(dateImageURL);
        ImageIcon dateExampleIcon = new ImageIcon(dateExampleImage);

        dateSettings = new DatePickerSettings();
        calendar = new DatePicker(dateSettings);
        calendar.setDateToToday();
        JButton datePickerButton = calendar.getComponentToggleCalendarButton();
        datePickerButton.setText("");
        datePickerButton.setIcon(dateExampleIcon);
        dateSettings.setAllowKeyboardEditing(false);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        row2.add(calendar, constraints);
        
        if (getHint() != "") {
        	// Add help panel
            help = getHelpTag();

            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            row2.add(help, constraints);
            
            getHelpPanel().setLocation(help.RIGHT, help.BOTTOM);
        }

        // Add second row to panel
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(row2, constraints);
    }
    
    public LocalDate getDate() {
    	return calendar.getDate();
    }
    
    public String getFinalValue() {
    	return calendar.getDate().toString();
    }
}
