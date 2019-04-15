
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

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

}
