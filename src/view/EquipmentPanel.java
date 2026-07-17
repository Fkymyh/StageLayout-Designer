package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import model.Equipment;
import model.EquipmentDefinition;
import model.EquipmentFactory;

public class EquipmentPanel extends JPanel {

    private static final int PANEL_WIDTH = 300;

    private static final int GENRE_RAIL_WIDTH = 78;

    private static final int GENRE_BUTTON_SIZE = 58;

    private static final int EQUIPMENT_BUTTON_WIDTH = 86;

    private static final int EQUIPMENT_BUTTON_HEIGHT = 92;

    private static final int ICON_SIZE = 48;

    private static final String DEFAULT_TYPE_NAME = "すべて";

    private static final String MODE_BROADCAST = "放送部";

    private static final String MODE_ALL = "すべて";

    private static final String MODE_FAVORITES = "マイリスト";

    private JPanel modeButtonPanel;

    private JPanel genreButtonPanel;

    private JPanel typeListPanel;

    private JPanel equipmentListPanel;

    private String selectedGenreName;

    private String expandedTypeName;

    private String selectedEquipmentName;

    private String selectedModeName = MODE_BROADCAST;

    private int equipmentColumnCount = 2;

    private Map<String, Map<String, List<String>>> categoryMap =
            new LinkedHashMap<>();

    private Map<String, Map<String, List<String>>> allCategoryMap =
            new LinkedHashMap<>();

    private Map<String, JButton> buttons = new LinkedHashMap<>();

    private Map<String, JToggleButton> genreButtons = new LinkedHashMap<>();

    private Map<String, JToggleButton> modeButtons = new LinkedHashMap<>();

    private Set<String> broadcastNames = new LinkedHashSet<>();

    private Set<String> favoriteNames = new LinkedHashSet<>();

    private Runnable equipmentSelectionCallback;

    public EquipmentPanel() {

        setLayout(new BorderLayout(6, 6));
        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        setBorder(BorderFactory.createTitledBorder("機材パレット"));

        loadEquipmentLists();
        buildCategoryMap();
        refreshCategoryMapForMode();

        genreButtonPanel = new JPanel();
        genreButtonPanel.setLayout(new BoxLayout(genreButtonPanel, BoxLayout.Y_AXIS));

        typeListPanel = new JPanel();
        typeListPanel.setLayout(new BoxLayout(typeListPanel, BoxLayout.Y_AXIS));

        equipmentListPanel = new JPanel();
        equipmentListPanel.setLayout(new BoxLayout(equipmentListPanel, BoxLayout.Y_AXIS));

        add(createModeSwitchPanel(), BorderLayout.NORTH);
        add(createGenreRailPanel(), BorderLayout.WEST);
        add(createTypePanel(), BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                updateEquipmentColumnCount();
            }
        });

        renderModeButtons();
        renderGenreButtons();

        String firstGenre = firstGenreWithEquipment();

        if (firstGenre != null) {
            selectGenre(firstGenre);
        }
    }

    private JPanel createModeSwitchPanel() {

        modeButtonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        modeButtonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        return modeButtonPanel;
    }

    private void renderModeButtons() {

        modeButtons.clear();
        modeButtonPanel.removeAll();

        addModeButton(MODE_BROADCAST, "放送部用に登録した機材を表示します");
        addModeButton(MODE_ALL, "登録されている機材をすべて表示します");
        addModeButton(MODE_FAVORITES, "マイリストに追加した機材を表示します");

        updateModeButtonSelection();

        modeButtonPanel.revalidate();
        modeButtonPanel.repaint();
    }

    private void addModeButton(String modeName, String toolTip) {

        JToggleButton button = new JToggleButton(modeName);

        button.setPreferredSize(new Dimension(86, 34));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setToolTipText(toolTip);
        button.addActionListener(e -> selectMode(modeName));

        modeButtons.put(modeName, button);
        modeButtonPanel.add(button);
    }

    private void selectMode(String modeName) {

        if (modeName == null || modeName.equals(selectedModeName)) {
            return;
        }

        selectedModeName = modeName;
        selectedGenreName = null;
        expandedTypeName = null;
        selectedEquipmentName = null;

        refreshCategoryMapForMode();
        renderModeButtons();
        renderGenreButtons();

        String firstGenre = firstGenreWithEquipment();

        if (firstGenre != null) {
            selectGenre(firstGenre);
        } else {
            renderTypeBars();
        }
    }

    private void updateModeButtonSelection() {

        for (Map.Entry<String, JToggleButton> entry : modeButtons.entrySet()) {
            JToggleButton button = entry.getValue();
            boolean selected = entry.getKey().equals(selectedModeName);

            button.setSelected(selected);
            button.setBackground(selected ? new Color(170, 205, 245) : Color.WHITE);
        }
    }

    private String firstGenreWithEquipment() {

        if (categoryMap.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, Map<String, List<String>>> entry :
                categoryMap.entrySet()) {

            Map<String, List<String>> typeMap = entry.getValue();

            if (typeMap == null) {
                continue;
            }

            for (List<String> equipmentNames : typeMap.values()) {

                if (equipmentNames != null && !equipmentNames.isEmpty()) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    private JPanel createGenreRailPanel() {

        JPanel railPanel = new JPanel(new BorderLayout(0, 4));

        railPanel.setPreferredSize(new Dimension(GENRE_RAIL_WIDTH, 0));
        railPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 0));
        railPanel.add(new JLabel("ジャンル"), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(genreButtonPanel);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        railPanel.add(scrollPane, BorderLayout.CENTER);

        return railPanel;
    }

    private JPanel createTypePanel() {

        JPanel typePanel = new JPanel(new BorderLayout(0, 4));
        JPanel typeHeaderPanel = new JPanel(new BorderLayout(0, 4));

        typePanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 4));
        typeHeaderPanel.add(new JLabel("種類"), BorderLayout.NORTH);
        typeHeaderPanel.add(typeListPanel, BorderLayout.CENTER);

        typePanel.add(typeHeaderPanel, BorderLayout.NORTH);
        typePanel.add(createEquipmentScrollPane(), BorderLayout.CENTER);

        return typePanel;
    }

    private JScrollPane createEquipmentScrollPane() {

        JPanel scrollContentPanel = new JPanel(new BorderLayout());

        scrollContentPanel.add(equipmentListPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(scrollContentPanel);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    private void buildCategoryMap() {

        allCategoryMap.clear();

        Set<String> expandedParentCategories = new HashSet<>();

        for (EquipmentDefinition definition :
                EquipmentFactory.getDefinitions().values()) {

            String category = normalizeCategory(definition.getCategory());

            if (category.contains(">")) {

                String[] parts = category.split(">", 2);

                expandedParentCategories.add(parts[0].trim());
            }
        }

        for (EquipmentDefinition definition :
                EquipmentFactory.getDefinitions().values()) {

            String category = normalizeCategory(definition.getCategory());

            String parentCategory = category;
            String childCategory = DEFAULT_TYPE_NAME;

            if (category.contains(">")) {

                String[] parts = category.split(">", 2);

                parentCategory = parts[0].trim();
                childCategory = parts[1].trim();

            } else if (expandedParentCategories.contains(parentCategory)) {

                continue;
            }

            allCategoryMap.putIfAbsent(parentCategory, new LinkedHashMap<>());
            allCategoryMap.get(parentCategory).putIfAbsent(
                    childCategory,
                    new ArrayList<>());
            allCategoryMap.get(parentCategory)
                    .get(childCategory)
                    .add(definition.getName());
        }
    }

    private void refreshCategoryMapForMode() {

        categoryMap.clear();

        if (MODE_ALL.equals(selectedModeName)) {
            copyCategoryMap(allCategoryMap, categoryMap);
            return;
        }

        if (MODE_FAVORITES.equals(selectedModeName)) {
            copyFilteredCategoryMap(
                    allCategoryMap,
                    categoryMap,
                    name -> favoriteNames.contains(name));
            return;
        }

        copyFilteredCategoryMap(
                allCategoryMap,
                categoryMap,
                name -> broadcastNames.contains(name));
    }

    private void copyCategoryMap(
            Map<String, Map<String, List<String>>> source,
            Map<String, Map<String, List<String>>> target) {

        copyFilteredCategoryMap(source, target, name -> true);
    }

    private void copyFilteredCategoryMap(
            Map<String, Map<String, List<String>>> source,
            Map<String, Map<String, List<String>>> target,
            EquipmentNameFilter filter) {

        for (Map.Entry<String, Map<String, List<String>>> genreEntry : source.entrySet()) {
            for (Map.Entry<String, List<String>> typeEntry : genreEntry.getValue().entrySet()) {
                for (String name : typeEntry.getValue()) {
                    if (!filter.accept(name)) {
                        continue;
                    }

                    target.putIfAbsent(genreEntry.getKey(), new LinkedHashMap<>());
                    target.get(genreEntry.getKey()).putIfAbsent(
                            typeEntry.getKey(),
                            new ArrayList<>());
                    target.get(genreEntry.getKey()).get(typeEntry.getKey()).add(name);
                }
            }
        }
    }

    private interface EquipmentNameFilter {

        boolean accept(String name);
    }

    private String normalizeCategory(String category) {

        if (category == null || category.trim().isEmpty()) {
            return "その他";
        }

        return category.trim();
    }

    private void renderGenreButtons() {

        genreButtons.clear();
        genreButtonPanel.removeAll();

        for (String genreName : categoryMap.keySet()) {

            JToggleButton button = createGenreButton(genreName);

            genreButtons.put(genreName, button);
            genreButtonPanel.add(button);
            genreButtonPanel.add(Box.createVerticalStrut(6));
        }

        updateGenreButtonSelection();

        genreButtonPanel.revalidate();
        genreButtonPanel.repaint();
    }

    private JToggleButton createGenreButton(String genreName) {

        JToggleButton button = new JToggleButton(genreName);

        button.setIcon(new GenreIcon(genreName));
        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setPreferredSize(new Dimension(GENRE_BUTTON_SIZE, GENRE_BUTTON_SIZE));
        button.setMaximumSize(new Dimension(GENRE_BUTTON_SIZE, GENRE_BUTTON_SIZE));
        button.setMinimumSize(new Dimension(GENRE_BUTTON_SIZE, GENRE_BUTTON_SIZE));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMargin(new Insets(2, 2, 2, 2));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 11f));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setToolTipText(genreName + "の機材を表示します");

        button.addActionListener(e -> selectGenre(genreName));

        return button;
    }

    private void selectGenre(String genreName) {

        if (genreName == null) {
            selectedGenreName = null;
            expandedTypeName = null;
            selectedEquipmentName = null;
            updateGenreButtonSelection();
            renderTypeBars();
            return;
        }

        boolean changed = !genreName.equals(selectedGenreName);

        selectedGenreName = genreName;

        Map<String, List<String>> typeMap = categoryMap.get(selectedGenreName);

        if (changed || typeMap == null || !typeMap.containsKey(expandedTypeName)) {
            expandedTypeName = firstTypeName(typeMap);
            selectedEquipmentName = null;

            if (expandedTypeName != null && typeMap != null) {
                selectedEquipmentName =
                        firstEquipmentName(typeMap.get(expandedTypeName));
            }
        }

        updateGenreButtonSelection();
        renderTypeBars();
    }

    private String firstTypeName(Map<String, List<String>> typeMap) {

        if (typeMap == null || typeMap.isEmpty()) {
            return null;
        }

        return typeMap.keySet().iterator().next();
    }

    private String firstEquipmentName(List<String> equipmentNames) {

        if (equipmentNames == null || equipmentNames.isEmpty()) {
            return null;
        }

        return equipmentNames.get(0);
    }

    private void updateGenreButtonSelection() {

        for (Map.Entry<String, JToggleButton> entry : genreButtons.entrySet()) {

            String genreName = entry.getKey();
            JToggleButton button = entry.getValue();
            boolean selected = genreName.equals(selectedGenreName);

            button.setSelected(selected);

            if (selected) {
                button.setBackground(new Color(180, 210, 255));
            } else {
                button.setBackground(Color.WHITE);
            }
        }
    }

    private void renderTypeBars() {

        buttons.clear();
        typeListPanel.removeAll();

        Map<String, List<String>> typeMap = categoryMap.get(selectedGenreName);

        if (typeMap == null || typeMap.isEmpty()) {

            typeListPanel.revalidate();
            typeListPanel.repaint();
            renderEquipmentList();

            return;
        }

        for (Map.Entry<String, List<String>> entry : typeMap.entrySet()) {

            String typeName = entry.getKey();
            boolean expanded = typeName.equals(expandedTypeName);

            typeListPanel.add(createTypeBarButton(typeName, expanded));
            typeListPanel.add(Box.createVerticalStrut(4));
        }

        renderEquipmentList();

        typeListPanel.revalidate();
        typeListPanel.repaint();
    }

    private JButton createTypeBarButton(String typeName, boolean expanded) {

        JButton button = new JButton((expanded ? "▼ " : "▶ ") + typeName);

        button.setHorizontalAlignment(JButton.LEFT);
        button.setFocusPainted(false);
        button.setBackground(expanded ? new Color(220, 235, 255) : new Color(238, 238, 238));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        button.setPreferredSize(new Dimension(PANEL_WIDTH, 32));
        button.setToolTipText("クリックで開閉します");

        button.addActionListener(e -> {

            if (typeName.equals(expandedTypeName)) {
                expandedTypeName = null;
                selectedEquipmentName = null;
            } else {
                expandedTypeName = typeName;
                Map<String, List<String>> typeMap =
                        categoryMap.get(selectedGenreName);

                if (typeMap != null) {
                    selectedEquipmentName =
                            firstEquipmentName(typeMap.get(expandedTypeName));
                }
            }

            renderTypeBars();
        });

        return button;
    }

    private JPanel createEquipmentGridPanel(List<String> equipmentNames) {

        JPanel panel = new JPanel(
                new GridLayout(0, equipmentColumnCount, 6, 6));

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        for (String name : equipmentNames) {

            JButton button = createEquipmentButton(name);

            buttons.put(name, button);
            panel.add(button);
        }

        return panel;
    }

    private void renderEquipmentList() {

        buttons.clear();
        equipmentListPanel.removeAll();

        Map<String, List<String>> typeMap = categoryMap.get(selectedGenreName);

        if (typeMap == null || expandedTypeName == null) {

            equipmentListPanel.revalidate();
            equipmentListPanel.repaint();

            return;
        }

        List<String> equipmentNames = typeMap.get(expandedTypeName);

        if (equipmentNames == null || equipmentNames.isEmpty()) {

            if (MODE_BROADCAST.equals(selectedModeName)
                    || MODE_FAVORITES.equals(selectedModeName)) {
                String targetName =
                        MODE_BROADCAST.equals(selectedModeName)
                                ? "放送部"
                                : "マイリスト";
                JLabel emptyLabel =
                        new JLabel("<html>すべて から機材を右クリックして<br>"
                                + targetName
                                + "に追加できます</html>");

                emptyLabel.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));
                equipmentListPanel.add(emptyLabel);
            }

            equipmentListPanel.revalidate();
            equipmentListPanel.repaint();

            return;
        }

        if (!equipmentNames.contains(selectedEquipmentName)) {
            selectedEquipmentName = firstEquipmentName(equipmentNames);
        }

        equipmentListPanel.add(createEquipmentGridPanel(equipmentNames));

        updateButtonSelection();

        equipmentListPanel.revalidate();
        equipmentListPanel.repaint();
    }

    private void updateEquipmentColumnCount() {

        int columnCount = calculateEquipmentColumnCount();

        if (columnCount == equipmentColumnCount) {
            return;
        }

        equipmentColumnCount = columnCount;

        if (expandedTypeName != null) {
            SwingUtilities.invokeLater(() -> renderEquipmentList());
        }
    }

    private int calculateEquipmentColumnCount() {

        int width = equipmentListPanel == null ? 0 : equipmentListPanel.getWidth();

        if (width <= 0) {
            width = Math.max(PANEL_WIDTH - GENRE_RAIL_WIDTH - 24, 180);
        }

        return Math.max(1, width / (EQUIPMENT_BUTTON_WIDTH + 8));
    }

    private JButton createEquipmentButton(String name) {

        Equipment equipment = EquipmentFactory.create(name);

        JButton button = new JButton();

        button.setText(createEquipmentButtonText(name, false));
        button.setToolTipText(
                name + "を選択します。キャンバスをダブルクリックで配置できます。");
        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setPreferredSize(
                new Dimension(EQUIPMENT_BUTTON_WIDTH, EQUIPMENT_BUTTON_HEIGHT));
        button.setMaximumSize(
                new Dimension(EQUIPMENT_BUTTON_WIDTH, EQUIPMENT_BUTTON_HEIGHT));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 10f));
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

            if (equipmentSelectionCallback != null) {
                equipmentSelectionCallback.run();
            }

            updateButtonSelection();
        });

        button.setComponentPopupMenu(createEquipmentPopupMenu(name));

        return button;
    }

    public void setEquipmentSelectionCallback(Runnable equipmentSelectionCallback) {
        this.equipmentSelectionCallback = equipmentSelectionCallback;
    }

    private JPopupMenu createEquipmentPopupMenu(String name) {

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem broadcastItem;
        JMenuItem favoriteItem;

        if (broadcastNames.contains(name)) {
            broadcastItem = new JMenuItem("放送部から外す");
            broadcastItem.addActionListener(e -> removeBroadcast(name));
        } else {
            broadcastItem = new JMenuItem("放送部に追加");
            broadcastItem.addActionListener(e -> addBroadcast(name));
        }

        if (favoriteNames.contains(name)) {
            favoriteItem = new JMenuItem("マイリストから外す");
            favoriteItem.addActionListener(e -> removeFavorite(name));
        } else {
            favoriteItem = new JMenuItem("マイリストに追加");
            favoriteItem.addActionListener(e -> addFavorite(name));
        }

        popupMenu.add(broadcastItem);
        popupMenu.add(favoriteItem);

        return popupMenu;
    }

    private void addBroadcast(String name) {

        if (name == null || !EquipmentFactory.getDefinitions().containsKey(name)) {
            return;
        }

        broadcastNames.add(name);
        saveEquipmentList(getBroadcastFilePath(), broadcastNames, "放送部");
        refreshCurrentFilteredView();
    }

    private void removeBroadcast(String name) {

        broadcastNames.remove(name);
        saveEquipmentList(getBroadcastFilePath(), broadcastNames, "放送部");
        refreshCurrentFilteredView();
    }

    private void addFavorite(String name) {

        if (name == null || !EquipmentFactory.getDefinitions().containsKey(name)) {
            return;
        }

        favoriteNames.add(name);
        saveEquipmentList(getFavoriteFilePath(), favoriteNames, "マイリスト");
        refreshCurrentFilteredView();
    }

    private void removeFavorite(String name) {

        favoriteNames.remove(name);
        saveEquipmentList(getFavoriteFilePath(), favoriteNames, "マイリスト");
        refreshCurrentFilteredView();
    }

    private void refreshCurrentFilteredView() {

        refreshCategoryMapForMode();

        if (MODE_BROADCAST.equals(selectedModeName)
                || MODE_FAVORITES.equals(selectedModeName)) {
            if (selectedGenreName == null || !categoryMap.containsKey(selectedGenreName)) {
                selectedGenreName = firstGenreWithEquipment();
            }

            renderGenreButtons();
            if (selectedGenreName != null) {
                selectGenre(selectedGenreName);
            } else {
                expandedTypeName = null;
                selectedEquipmentName = null;
                renderTypeBars();
            }
        } else {
            renderEquipmentList();
        }
    }

    private void loadEquipmentLists() {

        loadEquipmentList(getBroadcastFilePath(), broadcastNames, "放送部");
        loadEquipmentList(getFavoriteFilePath(), favoriteNames, "マイリスト");
    }

    private void loadEquipmentList(
            Path path,
            Set<String> targetNames,
            String listName) {


        if (!Files.exists(path)) {
            return;
        }

        try {

            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {

                String name = line.trim();

                if (!name.isEmpty()
                        && EquipmentFactory.getDefinitions().containsKey(name)) {
                    targetNames.add(name);
                }
            }

        } catch (IOException ex) {

            System.out.println(
                    listName
                    + "の読み込みに失敗しました: "
                    + ex.getMessage());
        }
    }

    private void saveEquipmentList(
            Path path,
            Set<String> names,
            String listName) {

        try {

            Files.write(
                    path,
                    names,
                    StandardCharsets.UTF_8);

        } catch (IOException ex) {

            System.out.println(
                    listName
                    + "の保存に失敗しました: "
                    + ex.getMessage());
        }
    }

    private Path getBroadcastFilePath() {

        return Paths.get(
                System.getProperty("user.home"),
                ".stage-layout-designer-broadcast.txt");
    }

    private Path getFavoriteFilePath() {

        return Paths.get(
                System.getProperty("user.home"),
                ".stage-layout-designer-favorites.txt");
    }

    private String createEquipmentButtonText(String name, boolean selected) {

        String displayName = shortenName(name);
        String prefix = selected ? "▶ " : "";

        if (displayName.length() <= 5) {
            return prefix + displayName;
        }

        int splitIndex = Math.min(5, displayName.length());

        return "<html><center>"
                + prefix
                + escapeHtml(displayName.substring(0, splitIndex))
                + "<br>"
                + escapeHtml(displayName.substring(splitIndex))
                + "</center></html>";
    }

    private String shortenName(String name) {

        if (name == null) {
            return "";
        }

        if (name.length() <= 10) {
            return name;
        }

        return name.substring(0, 9) + "...";
    }

    private String escapeHtml(String text) {

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void updateButtonSelection() {

        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {

            String name = entry.getKey();
            JButton button = entry.getValue();

            if (name.equals(selectedEquipmentName)) {

                button.setText(createEquipmentButtonText(name, true));
                button.setBackground(new Color(180, 210, 255));

            } else {

                button.setText(createEquipmentButtonText(name, false));
                button.setBackground(Color.WHITE);
            }
        }
    }

    public String getSelectedEquipment() {

        return selectedEquipmentName;
    }

    public void selectEquipmentByName(String equipmentName) {

        if (equipmentName == null
                || !EquipmentFactory.getDefinitions().containsKey(equipmentName)) {
            return;
        }

        selectedEquipmentName = equipmentName;
        updateButtonSelection();
    }

    private static class GenreIcon implements Icon {

        private final String genreName;

        GenreIcon(String genreName) {
            this.genreName = genreName;
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 22;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {

            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(iconColor());

            if (genreName.contains("音響")) {
                drawSpeaker(g2, x, y);
            } else if (genreName.contains("照明")) {
                drawLight(g2, x, y);
            } else if (genreName.contains("舞台")) {
                drawStage(g2, x, y);
            } else if (genreName.contains("映像")) {
                drawScreen(g2, x, y);
            } else if (genreName.contains("人物")) {
                drawPerson(g2, x, y);
            } else if (genreName.contains("楽器")) {
                drawInstrument(g2, x, y);
            } else if (genreName.contains("案内")) {
                drawGuide(g2, x, y);
            } else {
                drawDefault(g2, x, y);
            }

            g2.dispose();
        }

        private Color iconColor() {

            if (genreName.contains("音響")) {
                return new Color(60, 90, 130);
            } else if (genreName.contains("照明")) {
                return new Color(160, 120, 40);
            } else if (genreName.contains("舞台")) {
                return new Color(80, 95, 105);
            } else if (genreName.contains("映像")) {
                return new Color(80, 110, 150);
            } else if (genreName.contains("人物")) {
                return new Color(90, 120, 90);
            } else if (genreName.contains("楽器")) {
                return new Color(120, 80, 100);
            } else if (genreName.contains("案内")) {
                return new Color(90, 90, 90);
            }

            return new Color(80, 80, 80);
        }

        private void drawSpeaker(Graphics2D g2, int x, int y) {

            g2.drawRect(x + 2, y + 8, 5, 7);

            Polygon cone = new Polygon();
            cone.addPoint(x + 8, y + 6);
            cone.addPoint(x + 16, y + 2);
            cone.addPoint(x + 16, y + 20);
            cone.addPoint(x + 8, y + 16);

            g2.drawPolygon(cone);
            g2.drawArc(x + 15, y + 6, 8, 10, -45, 90);
        }

        private void drawLight(Graphics2D g2, int x, int y) {

            Polygon beam = new Polygon();
            beam.addPoint(x + 12, y + 3);
            beam.addPoint(x + 22, y + 20);
            beam.addPoint(x + 2, y + 20);

            g2.drawPolygon(beam);
            g2.drawOval(x + 7, y + 2, 10, 6);
        }

        private void drawStage(Graphics2D g2, int x, int y) {

            g2.drawRect(x + 3, y + 5, 18, 10);
            g2.drawLine(x + 5, y + 17, x + 19, y + 17);
            g2.drawLine(x + 7, y + 20, x + 17, y + 20);
        }

        private void drawScreen(Graphics2D g2, int x, int y) {

            g2.drawRect(x + 3, y + 4, 18, 12);
            g2.drawLine(x + 12, y + 16, x + 12, y + 20);
            g2.drawLine(x + 7, y + 20, x + 17, y + 20);
        }

        private void drawPerson(Graphics2D g2, int x, int y) {

            g2.drawOval(x + 8, y + 2, 8, 8);
            g2.drawLine(x + 12, y + 11, x + 12, y + 20);
            g2.drawLine(x + 6, y + 14, x + 18, y + 14);
            g2.drawLine(x + 12, y + 20, x + 7, y + 22);
            g2.drawLine(x + 12, y + 20, x + 17, y + 22);
        }

        private void drawInstrument(Graphics2D g2, int x, int y) {

            g2.drawOval(x + 4, y + 10, 10, 8);
            g2.drawLine(x + 12, y + 10, x + 22, y + 3);
            g2.drawLine(x + 14, y + 14, x + 23, y + 7);
            g2.drawOval(x + 7, y + 12, 3, 3);
        }

        private void drawGuide(Graphics2D g2, int x, int y) {

            g2.drawRect(x + 4, y + 4, 16, 10);
            g2.drawLine(x + 12, y + 14, x + 12, y + 22);
            g2.drawLine(x + 8, y + 22, x + 16, y + 22);
        }

        private void drawDefault(Graphics2D g2, int x, int y) {

            g2.drawRect(x + 4, y + 4, 16, 16);
            g2.drawLine(x + 12, y + 4, x + 12, y + 20);
            g2.drawLine(x + 4, y + 12, x + 20, y + 12);
        }
    }
}
