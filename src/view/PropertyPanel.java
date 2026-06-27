package view;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import model.LayoutItem;

public class PropertyPanel extends JPanel {
	
	private JTextArea equipmentArea;
	
	private JTextArea quantityArea;
	
	private JTextArea memoArea;
	
	private LayoutItem currentItem;
	
	
	public PropertyPanel() {

        setLayout(new GridLayout(7, 1));
        
        equipmentArea = new JTextArea();
        quantityArea = new JTextArea();
        memoArea = new JTextArea(4, 20);
        
        JButton saveButton = new JButton("更新");
        

        equipmentArea.setEditable(false);

        add(new JLabel("使用機材"));
        add(equipmentArea);

        add(new JLabel("必要数"));
        add(quantityArea);

        add(new JLabel("注意事項"));
        add(memoArea);

        add(saveButton);
        
        saveButton.addActionListener(e -> {

            if(currentItem == null){
                return;
            }

            try {

                currentItem.setQuantity(
                        Integer.parseInt(quantityArea.getText()));

                currentItem.setMemo(
                        memoArea.getText());

            } catch(NumberFormatException ex) {

                quantityArea.setText("1");

            }

        });

    }
	
    public void displayItem(LayoutItem item) {
    	
    	currentItem = item;
    	
    	if(item == null) {
    		
    		equipmentArea.setText("");
    		
    		quantityArea.setText("");
    		
    		memoArea.setText("");
    		
    		
    		return;
    	}
    	
    	equipmentArea.setText(
    			item.getEquipment().getName());
    	
    	quantityArea.setText(
    			String.valueOf(item.getQuantity()));
    	
    	memoArea.setText(
    			item.getMemo());
    	
    }
    
}