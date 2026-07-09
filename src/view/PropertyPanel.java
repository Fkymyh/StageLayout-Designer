package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import model.LayoutItem;

public class PropertyPanel extends JPanel {

    private JTextArea equipmentArea;

    private JTextArea quantityArea;

    private JTextArea widthArea;

    private JTextArea heightArea;

    private JTextArea memoArea;

    private JTextArea summaryArea;

    private LayoutItem currentItem;
    
    private JTextArea labelArea;

    private Runnable updateCallback;
    
    private JTextField equipmentField;
    
    private JTextField labelField;
    
    private JTextField quantityField;
    
    private JTextField widthField;
    
    private JTextField heightField;

    private JButton applyButton;

    private List<LayoutItem> allItems;

    public PropertyPanel() {
    	
    	setLayout(new BorderLayout());
    	
    	JPanel editPanel = createEditPanel();
    	
    	add(editPanel, BorderLayout.NORTH);
    	
    	JScrollPane summaryScrollPane = createSummaryPanel();
    	
    	add(summaryScrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createEditPanel() {
    	
    	JPanel editPanel = new JPanel();
    	
    	editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
    	
    	editPanel.setBorder(
    			BorderFactory.createTitledBorder("選択中のアイテム"));
    	
    	equipmentField = new JTextField();
    	labelField = new JTextField();
    quantityField = new JTextField();
    widthField = new JTextField();
    heightField = new JTextField();
    memoArea = new JTextArea(5, 20);
    

    equipmentField.setEditable(false);

    setupTextField(equipmentField);
    setupTextField(labelField);

    quantityField.setPreferredSize(new Dimension(55, 28));
    widthField.setPreferredSize(new Dimension(55, 28));
    heightField.setPreferredSize(new Dimension(55, 28));

    memoArea.setLineWrap(true);
    memoArea.setWrapStyleWord(true);

    JScrollPane memoScrollPane = new JScrollPane(memoArea);
    memoScrollPane.setPreferredSize(new Dimension(190, 110));
    memoScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

    JButton saveButton = new JButton("反映");
    saveButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

    editPanel.add(new JLabel("機材名"));
    editPanel.add(equipmentField);
    editPanel.add(Box.createVerticalStrut(10));

    editPanel.add(new JLabel("表示名"));
    editPanel.add(labelField);
    editPanel.add(Box.createVerticalStrut(10));

    editPanel.add(createNumberPanel());
    editPanel.add(Box.createVerticalStrut(10));

    editPanel.add(new JLabel("注意事項"));
    editPanel.add(memoScrollPane);
    editPanel.add(Box.createVerticalStrut(10));

    editPanel.add(saveButton);

    saveButton.addActionListener(e -> applyChanges());

    return editPanel;

    }
    
    private void setupTextField(JTextField textField) {

        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        textField.setPreferredSize(new Dimension(190, 28));
    }

    private JPanel createNumberPanel() {

        JPanel numberPanel = new JPanel(new GridLayout(2, 3, 6, 4));

        numberPanel.add(new JLabel("必要数"));
        numberPanel.add(new JLabel("幅"));
        numberPanel.add(new JLabel("高さ"));

        numberPanel.add(quantityField);
        numberPanel.add(widthField);
        numberPanel.add(heightField);

        numberPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        return numberPanel;
    }

    private JScrollPane createSummaryPanel() {

        summaryArea = new JTextArea(8, 25);
        summaryArea.setEditable(false);

        JScrollPane summaryScrollPane =
                new JScrollPane(summaryArea);

        summaryScrollPane.setPreferredSize(
                new Dimension(190, 160));

        summaryScrollPane.setBorder(
                BorderFactory.createTitledBorder("必要機材一覧"));

        return summaryScrollPane;
    }

    private void applyChanges() {

        if (currentItem == null) {
            return;
        }

        try {

            int quantity =
                    Integer.parseInt(quantityField.getText().trim());

            int width =
                    Integer.parseInt(widthField.getText().trim());

            int height =
                    Integer.parseInt(heightField.getText().trim());

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

            currentItem.setLabel(labelField.getText());

            currentItem.setMemo(memoArea.getText());

            displayItem(currentItem);

            displaySummary(allItems);

            if (updateCallback != null) {
                updateCallback.run();
            }

        } catch (NumberFormatException ex) {

            quantityField.setText(
                    String.valueOf(currentItem.getQuantity()));

            widthField.setText(
                    String.valueOf(currentItem.getWidth()));

            heightField.setText(
                    String.valueOf(currentItem.getHeight()));
        }
    }

    public void displayItem(LayoutItem item) {

        currentItem = item;

        if (item == null) {

            equipmentField.setText("");
            labelField.setText("");
            quantityField.setText("");
            widthField.setText("");
            heightField.setText("");
            memoArea.setText("");

            return;
        }

        equipmentField.setText(
                item.getEquipment().getName());

        labelField.setText(
                item.getLabel());

        quantityField.setText(
                String.valueOf(item.getQuantity()));

        widthField.setText(
                String.valueOf(item.getWidth()));

        heightField.setText(
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
                    summary.getOrDefault(name, 0) + quantity);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("=== 必要機材一覧 ===\n");

        for (Map.Entry<String, Integer> entry : summary.entrySet()) {

            sb.append(entry.getKey())
              .append(" × ")
              .append(entry.getValue())
              .append("\n");
        }

        summaryArea.setText(sb.toString());
    }

    public void setUpdateCallback(Runnable updateCallback) {

        this.updateCallback = updateCallback;
    }

    
}
