package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

    private JTabbedPane tabbedPane;

    private String selectedEquipmentName = "マイク";

    private Map<String, JButton> buttons = new LinkedHashMap<>();

    public EquipmentPanel() {

        setLayout(new BorderLayout());

        setPreferredSize(new Dimension(260, 0));

        tabbedPane = new JTabbedPane();

        createTabsFromDefinitions();

        add(tabbedPane, BorderLayout.CENTER);

        updateButtonSelection();
    }

    private void addEquipmentTab(String categoryName, String[] equipmentNames) {

        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(0, 2, 6, 6));

        for (String name : equipmentNames) {

            JButton button = createEquipmentButton(name);

            buttons.put(name, button);

            panel.add(button);
        }

        JScrollPane scrollPane = new JScrollPane(panel);

        tabbedPane.addTab(categoryName, scrollPane);
    }

    private JButton createEquipmentButton(String name) {

        Equipment equipment = EquipmentFactory.create(name);

        JButton button = new JButton();

        button.setText(name);

        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setHorizontalTextPosition(JButton.CENTER);

        button.setPreferredSize(new Dimension(105, 90));

        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);

        if (equipment.getImage() != null) {

            Image scaledImage =
                    equipment.getImage().getScaledInstance(
                            48,
                            48,
                            Image.SCALE_SMOOTH);

            button.setIcon(new ImageIcon(scaledImage));
        }

        button.addActionListener(e -> {

            selectedEquipmentName = name;

            updateButtonSelection();
        });

        return button;
    }

    private void updateButtonSelection() {

        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {

            JButton button = entry.getValue();

            if (entry.getKey().equals(selectedEquipmentName)) {

                button.setText("▶ " + entry.getKey());
                button.setBackground(new Color(180, 210, 255));

            } else {

                button.setText(entry.getKey());
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