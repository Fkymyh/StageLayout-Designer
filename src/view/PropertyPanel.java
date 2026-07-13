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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import model.LayoutItem;

public class PropertyPanel extends JPanel {

    private JTextArea summaryArea;

    private LayoutItem currentItem;

    private Runnable updateCallback;

    private JTextField equipmentField;

    private JTextField labelField;

    private JTextField widthField;

    private JTextField heightField;

    private JCheckBox showLabelCheckBox;

    private List<LayoutItem> allItems;

    public PropertyPanel() {

        setLayout(new BorderLayout());

        add(createEditPanel(), BorderLayout.NORTH);
        add(createSummaryPanel(), BorderLayout.CENTER);
    }

    private JPanel createEditPanel() {

        JPanel editPanel = new JPanel();

        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
        editPanel.setBorder(BorderFactory.createTitledBorder("選択中のアイテム"));

        equipmentField = new JTextField();
        labelField = new JTextField();
        widthField = new JTextField();
        heightField = new JTextField();
        showLabelCheckBox = new JCheckBox("ラベルを表示", true);

        equipmentField.setEditable(false);

        setupTextField(equipmentField);
        setupTextField(labelField);

        widthField.setPreferredSize(new Dimension(80, 28));
        heightField.setPreferredSize(new Dimension(80, 28));

        JButton saveButton = new JButton("表示名・サイズを反映");
        saveButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        saveButton.addActionListener(e -> applyChanges());

        editPanel.add(new JLabel("機材名"));
        editPanel.add(equipmentField);
        editPanel.add(Box.createVerticalStrut(10));

        editPanel.add(new JLabel("表示名"));
        editPanel.add(labelField);
        editPanel.add(Box.createVerticalStrut(10));

        editPanel.add(createSizePanel());
        editPanel.add(Box.createVerticalStrut(10));
        editPanel.add(showLabelCheckBox);
        editPanel.add(Box.createVerticalStrut(10));

        editPanel.add(saveButton);

        return editPanel;
    }

    private void setupTextField(JTextField textField) {

        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        textField.setPreferredSize(new Dimension(190, 28));
    }

    private JPanel createSizePanel() {

        JPanel sizePanel = new JPanel(new GridLayout(2, 2, 6, 4));

        sizePanel.add(new JLabel("幅"));
        sizePanel.add(new JLabel("高さ"));

        sizePanel.add(widthField);
        sizePanel.add(heightField);

        sizePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        return sizePanel;
    }

    private JScrollPane createSummaryPanel() {

        summaryArea = new JTextArea(8, 25);
        summaryArea.setEditable(false);

        JScrollPane summaryScrollPane = new JScrollPane(summaryArea);

        summaryScrollPane.setPreferredSize(new Dimension(190, 160));
        summaryScrollPane.setBorder(BorderFactory.createTitledBorder("必要機材一覧"));

        return summaryScrollPane;
    }

    private void applyChanges() {

        if (currentItem == null) {
            return;
        }

        try {

            int width = Integer.parseInt(widthField.getText().trim());
            int height = Integer.parseInt(heightField.getText().trim());

            if (width < 10) {
                width = 10;
            }

            if (height < 10) {
                height = 10;
            }

            currentItem.setSize(width, height);
            currentItem.setLabel(labelField.getText());
            currentItem.setShowLabel(showLabelCheckBox.isSelected());

            displayItem(currentItem);
            displaySummary(allItems);

            if (updateCallback != null) {
                updateCallback.run();
            }

        } catch (NumberFormatException ex) {

            widthField.setText(String.valueOf(currentItem.getWidth()));
            heightField.setText(String.valueOf(currentItem.getHeight()));
        }
    }

    public void displayItem(LayoutItem item) {

        currentItem = item;

        if (item == null) {

            equipmentField.setText("");
            labelField.setText("");
            widthField.setText("");
            heightField.setText("");
            showLabelCheckBox.setSelected(true);

            return;
        }

        equipmentField.setText(item.getEquipment().getName());
        labelField.setText(item.getLabel());
        widthField.setText(String.valueOf(item.getWidth()));
        heightField.setText(String.valueOf(item.getHeight()));
        showLabelCheckBox.setSelected(item.isShowLabel());
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

            summary.put(name, summary.getOrDefault(name, 0) + quantity);
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
