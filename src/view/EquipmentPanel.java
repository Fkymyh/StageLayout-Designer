package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import model.Equipment;
import model.EquipmentDefinition;
import model.EquipmentFactory;

public class EquipmentPanel extends JPanel {
	
	private static final int PANEL_WIDTH = 170;

    private static final int BUTTON_WIDTH = 72;

    private static final int BUTTON_HEIGHT = 78;

    private static final int ICON_SIZE = 34;

    private JTabbedPane tabbedPane;

    private String selectedEquipmentName = "マイク";

    private Map<String, JButton> buttons = new LinkedHashMap<>();

    public EquipmentPanel() {

        setLayout(new BorderLayout());

        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        
        setBorder(
                javax.swing.BorderFactory.createTitledBorder("機材パレット"));

        tabbedPane = new JTabbedPane();

        createTabsFromDefinitions();

        add(tabbedPane, BorderLayout.CENTER);

        updateButtonSelection();
    }

    private void addEquipmentTab(String categoryName, String[] equipmentNames) {

        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(0, 2, 4, 4));

        for (String name : equipmentNames) {

            JButton button = createEquipmentButton(name);

            buttons.put(name, button);

            panel.add(button);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        tabbedPane.addTab(categoryName, scrollPane);
    }

    private JButton createEquipmentButton(String name) {

        Equipment equipment = EquipmentFactory.create(name);

        JButton button = new JButton();

        button.setText(shortenName(name));
        
        button.setToolTipText(name);

        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setHorizontalTextPosition(JButton.CENTER);

        button.setPreferredSize(
                new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

        button.setMaximumSize(
                new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

        button.setFont(
                button.getFont().deriveFont(Font.PLAIN, 10f));

        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);

        if (equipment.getImage() != null) {

            Image scaledImage =
                    equipment.getImage().getScaledInstance(
                            ICON_SIZE,
                            ICON_SIZE,
                            Image.SCALE_SMOOTH);

            button.setIcon(new ImageIcon(scaledImage));
        }

        button.addActionListener(e -> {

            selectedEquipmentName = name;

            updateButtonSelection();
        });

        return button;
    }

    private String shortenName(String name) {

        if (name == null) {
            return "";
        }

        if (name.length() <= 6) {
            return name;
        }

        return name.substring(0, 6);
    }

    private void updateButtonSelection() {

        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {
        		
        	String name = entry.getKey();

            JButton button = entry.getValue();

            if (entry.getKey().equals(selectedEquipmentName)) {

                button.setText("▶ " + shortenName(name));
                button.setBackground(new Color(180, 210, 255));

            } else {

                button.setText(shortenName(name));
                button.setBackground(Color.WHITE);
            }
        }
    }
    public String getSelectedEquipment() {

        return selectedEquipmentName;
    }
    
    private void createTabsFromDefinitions() {

        Map<String, List<String>> categoryMap = new LinkedHashMap<>();

        for (EquipmentDefinition definition :
                EquipmentFactory.getDefinitions().values()) {

            String category = definition.getCategory();

            categoryMap.putIfAbsent(category, new ArrayList<>());

            categoryMap.get(category).add(definition.getName());
        }

        for (Map.Entry<String, List<String>> entry : categoryMap.entrySet()) {

            String categoryName = entry.getKey();

            List<String> equipmentNames = entry.getValue();

            addEquipmentTab(
                    categoryName,
                    equipmentNames.toArray(new String[0]));
        }
    }
    
    
}