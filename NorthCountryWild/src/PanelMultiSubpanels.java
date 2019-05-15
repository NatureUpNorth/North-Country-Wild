import org.json.JSONObject;
import java.awt.*;
import java.time.LocalDate;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PanelMultiSubpanels extends TabItem {
	
	private TabItem[] subpanels;
    public PanelMultiSubpanels(JSONObject jsonpanel, TabItem[] subpanels) {

    	super(jsonpanel);
        this.subpanels = subpanels;
        
        // Prepare panel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        for (int i = 0; i < subpanels.length; i++) {
        	constraints.gridx = 0;
            constraints.gridy = i;
        	panel.add(subpanels[i].getPanel(), constraints);
		}
    }
    
    public TabItem[] getSubpanels() {
    	return subpanels;
    }
    
    // currently only used by date subpanels, but others could be implemented
    public boolean check(String type) {
    	if (type.equals("Date")) {
			LocalDate startDate = null;
	    	LocalDate endDate = null;
	    	for (int i = 0; i < subpanels.length; i++) {
	    		if (subpanels[i].getClass().toString().equals("class PanelDate")) {
	    			if (subpanels[i].getReturnValue().equals("Start Date")) {
	    				PanelDate startpanel = (PanelDate) subpanels[i];
	    				startDate = startpanel.getDate();
	    			} else {
	    				PanelDate endpanel = (PanelDate) subpanels[i];
	    				endDate = endpanel.getDate();
	    			}
	    		}
	    	}
	    	if((startDate==null || endDate == null) && isRequired()){
				JOptionPane.showMessageDialog(new JFrame(),
						"Incorrect date entry. Please enter all the required fields.");
				return false;
			}
	    	if (startDate.isAfter(endDate)) {
	    		JOptionPane.showMessageDialog(new JFrame(),
						"Incorrect date entry. The start date is after the end date.");
	    		return false;
	    	}
	    	LocalDate today = LocalDate.now();
	    	if(startDate.isAfter(today)) {
				JOptionPane.showMessageDialog(new JFrame(),
						"Incorrect date entry. The start date is after the current date.");
				return false;
	    	}
    	} else {
    		for (int i = 0; i < subpanels.length; i++) {
    			if (!subpanels[i].check()) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
}
