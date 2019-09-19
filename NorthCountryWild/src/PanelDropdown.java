
import org.json.JSONArray;
import org.json.JSONObject;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class PanelDropdown extends TabItem implements ActionListener {
	
	private String[] options;
	private JComboBox<String> box;
	private GridBagConstraints constraints;
	private JTextField textBox;
	private static final String ACCESS_TOKEN = "ILJ9haPVAAAAAAAAAAABNzR9TiIIe2XQb_2PWJ-Q3dJRamPN4TEX3xLreLM_j6Us";//"ILJ9haPVAAAAAAAAAAAAR7cBhQSEWdj0K4CkmEPrTYii1sCbJsZ1StCB8sO2YT4k"; //"ILJ9haPVAAAAAAAAAAAAR7cBhQSEWdj0K4CkmEPrTYii1sCbJsZ1StCB8sO2YT4k";//access token for info@natureupnorth.org dropbox
	ArrayList<String> filesList;

    public PanelDropdown (JSONObject jsonpanel) {

        super(jsonpanel);

        // Prepare JPanel layout
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 15, 15, 15);

        // Add description
        JLabel description = new JLabel(getDesc());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(description, constraints);

        
 //---------------------------------------------------------
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        ListFolderResult listing = null;
        filesList = new ArrayList<String>();
			try {
				listing = client.files().listFolderBuilder("").start();
			} catch (ListFolderErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        for (Metadata child : listing.getEntries()) {
        	if(child instanceof FolderMetadata) {
                filesList.add(child.getName());
        	}
			
        }
        
//        // Fetch dropdown options and create JComboBox
//        JSONArray opt = jsonpanel.getJSONArray("values");
//        options = new String[opt.length()];
//        for (int i = 0; i < opt.length(); i++) {
//            options[i] = (String) opt.get(i);
//        }

        // Add comboBoxes
        //box = new JComboBox<String>(options);
        int size = filesList.size()+1;
        String[] folders = filesList.toArray(new String[size]); 
        folders[folders.length-1] = "+ ADD NEW";
        box = new JComboBox<String>(folders);
        box.addActionListener(this);         
        
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(box, constraints);
        
        if (getHint() != "") {
        	constraints.gridx = 1;
        	panel.add(getHelpTag(), constraints);
        }
        
        // Add potential text field
        textBox = new JTextField();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(textBox, constraints);  
        textBox.setEditable(false);
    }
    
    @Override
	public void actionPerformed(ActionEvent arg0) {
    	if (arg0.getSource().equals(box)) {
    		if (box.getSelectedItem().equals("+ ADD NEW")) {
    			textBox.setEditable(true);
       		} else {
    			textBox.setEditable(false);
       		}
    	}		
	}

    
    public String[] getOptions() {
    	return options;
    }
    
    public String getFinalValue() {
    	if (((String)box.getSelectedItem()).equals("+ ADD NEW")) {
    		return textBox.getText();
    	}
    	return (String) box.getSelectedItem();
    }

}
