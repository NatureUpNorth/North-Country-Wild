
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
	private static final String ACCESS_TOKEN = "ILJ9haPVAAAAAAAAAAABPD4L9Dh7CtKvyUoh1gh--sQzmSL_aaM7bxpU1-QxExrR";
	ArrayList<String> filesList;
	public static String getAffiliation;

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


        // Add comboBoxes
        //box = new JComboBox<String>(options);
        int size = filesList.size();
        String[] folders = filesList.toArray(new String[size]); 
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
    		if (box.getSelectedItem().equals("Other")) {
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
    	if (((String)box.getSelectedItem()).equals("Other")) {
    		getAffiliation = textBox.getText();
    		//return textBox.getText();
    	}
    	return (String) box.getSelectedItem();
    }
    
    public static String getOther() {
    	return getAffiliation;
    }

}
