
import javax.swing.*;
import java.awt.*;

public class PanelReview extends TabItem {

    private JLabel description;
    JPanel[] tabPanels;
    static JButton[] buttons;
    private GridBagConstraints constraints;
    private static String[][] data;
    static boolean dataFilled;

    public PanelReview(String desc, String hint, String returnValue) {
        super(desc, hint, returnValue, false);
        
        panel.setLayout(new GridBagLayout());
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;

        description = new JLabel(desc);
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);
        
        int tabCount = TestWindow.getTabs().length - 1;  // Number of non-review tabs
        tabPanels = new JPanel[tabCount];
        buttons = new JButton[tabCount];
        for (int tabIndex = 0; tabIndex < tabCount; tabIndex ++ ) {  // For each tab
        	
        	JPanel header = new JPanel();
        	JPanel thisTab = new JPanel();
        	header.setLayout(new GridBagLayout());
        	thisTab.setLayout(new GridBagLayout());
        	thisTab.setBackground(new Color(230, 240, 230));
        	header.add(new JLabel(TestWindow.getTabs()[tabIndex].getTitle()), constraints);  // Label section with tab title
        	
        	// Add button to panel and ArrayList (for button press tracking)
        	JButton button = new JButton("Edit");
        	buttons[tabIndex] = button;
        	constraints.gridx = 1;
        	header.add(button, constraints);
        	
        	// Add header to section
        	constraints.gridx = 0;
        	constraints.gridy = 0;
        	thisTab.add(header, constraints);
        	
        	if(dataFilled) {
        		
        		// Add all relevant return values
        		int gridy = 1;
            	for (int labelNumber = 0; labelNumber < data[tabIndex].length; labelNumber ++) {  // For each subPanel of tab
            		if (data[tabIndex][labelNumber] != "") {  // If has return value
        	    		constraints.gridx = 0;
        				constraints.gridy = gridy;
        				gridy++;
        				if(data[tabIndex][labelNumber].contains("Comments")) {
        					data[tabIndex][labelNumber] = data[tabIndex][labelNumber].substring(0,50)+"...";
        					System.out.println(data[tabIndex][labelNumber]);
        				}
        				thisTab.add(new JLabel(data[tabIndex][labelNumber]), constraints);
        				
        			}
        		}
        	}
        	        	
        	// Add to review tab
        	constraints.gridx = 0;
        	constraints.gridy = tabIndex + 1;
        	panel.add(thisTab, constraints);
        }
    }
    
    public static void setData(String[][] values) {
    	data = values;
    }

    public void setText(String s) {
        description.setText(s);
    }

    public String getText() {
        return description.getText();
    }

    public void appendText(String s) {
        String str = description.getText();
        description.setText("<html>" + str + s + "</html>");
    }
}
