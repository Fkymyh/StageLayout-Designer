package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import model.Equipment;
import model.EquipmentDefinition;
import model.EquipmentFactory;

public class EquipmentPanel extends JPanel {
	
	private static final int PANEL_WIDTH = 300;

    private static final int BUTTON_WIDTH = 82;

    private static final int BUTTON_HEIGHT = 86;

    private static final int ICON_SIZE = 42;

    private JTabbedPane tabbedPane;

    private String selectedEquipmentName;

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

        tabbedPane.addTab(
                categoryName,
                createEquipmentScrollPane(equipmentNames));
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

        Map<String, Map<String, List<String>>> categoryMap =
                new LinkedHashMap<>();

        Set<String> expandedParentCategories = new HashSet<>();

        for (EquipmentDefinition definition :
                EquipmentFactory.getDefinitions().values()) {

            String category = definition.getCategory();

            if (category != null && category.contains(">")) {

                String[] parts = category.split(">", 2);

                expandedParentCategories.add(parts[0].trim());
            }
        }

        for (EquipmentDefinition definition :
                EquipmentFactory.getDefinitions().values()) {

            String category = definition.getCategory();

            String parentCategory = category;
            String childCategory = null;

            if (category != null && category.contains(">")) {

                String[] parts = category.split(">", 2);

                parentCategory = parts[0].trim();
                childCategory = parts[1].trim();

            } else if (expandedParentCategories.contains(parentCategory)) {

                continue;
            }

            categoryMap.putIfAbsent(
                    parentCategory,
                    new LinkedHashMap<>());

            String mapKey =
                    childCategory == null
                    ? parentCategory
                    : childCategory;

            categoryMap.get(parentCategory).putIfAbsent(
                    mapKey,
                    new ArrayList<>());

            categoryMap.get(parentCategory)
                    .get(mapKey)
                    .add(definition.getName());
        }

        for (Map.Entry<String, Map<String, List<String>>> entry :
                categoryMap.entrySet()) {

            String categoryName = entry.getKey();

            Map<String, List<String>> childCategories = entry.getValue();

            if (childCategories.size() == 1
                    && childCategories.containsKey(categoryName)) {

                List<String> equipmentNames =
                        childCategories.get(categoryName);

                addEquipmentTab(
                        categoryName,
                        equipmentNames.toArray(new String[0]));

            } else {

                JTabbedPane childTabbedPane = new JTabbedPane();

                for (Map.Entry<String, List<String>> childEntry :
                        childCategories.entrySet()) {

                    childTabbedPane.addTab(
                            childEntry.getKey(),
                            createEquipmentScrollPane(
                                    childEntry.getValue()
                                            .toArray(new String[0])));
                }

                tabbedPane.addTab(categoryName, childTabbedPane);
            }
        }
    }

    private JScrollPane createEquipmentScrollPane(String[] equipmentNames) {

        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(0, 3, 6, 6));

        for (String name : equipmentNames) {

            JButton button = createEquipmentButton(name);

            buttons.put(name, button);

            if (selectedEquipmentName == null) {
                selectedEquipmentName = name;
            }

            panel.add(button);
        }

        JScrollPane scrollPane = new JScrollPane(panel);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }
    
    
}
