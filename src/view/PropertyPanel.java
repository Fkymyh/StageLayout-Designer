package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import model.LayoutItem;

public class PropertyPanel extends JPanel {
	
	private JTextArea equipmentArea;
	
	private JTextArea quantityArea;
	
	private JTextArea memoArea;
	
	private JTextArea summaryArea;
	
	private LayoutItem currentItem;
	
	private JTextArea widthArea;
	
	private JTextArea heightArea;
	
	private Runnable updateCallback;
	
	private List<LayoutItem> allItems;
	
	
	public PropertyPanel() {
		
		setLayout(new BorderLayout());

        JPanel editPanel = new JPanel(new GridLayout(11, 1));
        
        equipmentArea = new JTextArea();
        quantityArea = new JTextArea();
        memoArea = new JTextArea(4, 20);
        
        widthArea = new JTextArea();

        heightArea = new JTextArea();
        
        equipmentArea.setEditable(false);
        
        JButton saveButton = new JButton("更新");
      

        add(new JLabel("使用機材"));
        editPanel.add(equipmentArea);

        add(new JLabel("必要数"));
        editPanel.add(quantityArea);
        
        editPanel.add(new JLabel("幅"));
        editPanel.add(widthArea);

        editPanel.add(new JLabel("高さ"));
        editPanel.add(heightArea);


        add(new JLabel("注意事項"));
        editPanel.add(memoArea);

        editPanel.add(saveButton);
        
        
        add(editPanel, BorderLayout.WEST);
        
        summaryArea = new JTextArea(8, 30);
        summaryArea.setEditable(false);
        
        add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        
        saveButton.addActionListener(e -> {

            if(currentItem == null){
                return;
            }

            try {

                int quantity = 
                			Integer.parseInt(quantityArea.getText().trim());
                
                int width =
                        Integer.parseInt(widthArea.getText().trim());

                int height =
                        Integer.parseInt(heightArea.getText().trim());

                
                if (quantity < 1) {
                		quantity = 1;
                }
                
                if (width < 10) {
                    width = 10;
                }

                if (height < 10) {
                    height = 10;
                }

                
                currentItem.setQuantity(quantity);
                
                currentItem.setSize(width, height);
                
                currentItem.setMemo(memoArea.getText());
                
                displayItem(currentItem);
                
                displaySummary(allItems);

            } catch(NumberFormatException ex) {

            	quantityArea.setText(
                        String.valueOf(currentItem.getQuantity()));

                widthArea.setText(
                        String.valueOf(currentItem.getWidth()));

                heightArea.setText(
                        String.valueOf(currentItem.getHeight()));

            }

        });

    }
	
    public void displayItem(LayoutItem item) {
    	
    	currentItem = item;
    	
    	if(item == null) {
    		
    		equipmentArea.setText("");
    		
    		quantityArea.setText("");
    		
    		widthArea.setText("");
    		
        heightArea.setText("");
    		
    		memoArea.setText("");
    		
    		
    		return;
    	}
    	
    	equipmentArea.setText(
    			item.getEquipment().getName());
    	
    	quantityArea.setText(
    			String.valueOf(item.getQuantity()));
    	
    	widthArea.setText(
                String.valueOf(item.getWidth()));

    heightArea.setText(
                String.valueOf(item.getHeight()));
    	
    	memoArea.setText(
    			item.getMemo());
    	
    }
    
    public void displaySummary(List<LayoutItem> items) {
    	
    	allItems = items;
    	
    	if (items == null || items.isEmpty()) {
    		
    		summaryArea.setText("配置されている機材はありません。");
    		
    		return;
    	}
    	
    	
    	
    	Map<String, Integer> summary = new LinkedHashMap<>();
    	
    	for (LayoutItem item : items) {
    		
    		String name = item.getEquipment().getName();
    		
    		int quantity = item.getQuantity();
    		
    		summary.put(
    				name,
    				summary.getOrDefault(name,0) + quantity);
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append("=== 必要機材一覧 ===\n");
    	
    	for (Map.Entry<String, Integer> entry : summary.entrySet()) {
    		
    		sb.append(entry.getKey())
    		  .append(" x ")
    		  .append(entry.getValue())
    		  .append("\n");
    	}
    	
    	summaryArea.setText(sb.toString());
    }
    
    public void setUpdateCallback(Runnable updateCallback) {

        this.updateCallback = updateCallback;
    }
    
}