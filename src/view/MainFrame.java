package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import model.RoomTemplate;
import model.RoomTemplateFactory;

public class MainFrame extends JFrame {

    private EquipmentPanel equipmentPanel;
    private PropertyPanel propertyPanel;
    private CanvasPanel canvasPanel;

    private JLabel statusLabel;

    public MainFrame() {

        setTitle("Stage Layout Designer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        equipmentPanel = new EquipmentPanel();
        propertyPanel = new PropertyPanel();
        canvasPanel = new CanvasPanel(equipmentPanel, propertyPanel);
        
        propertyPanel.setUpdateCallback(() -> {
            canvasPanel.repaint();
            statusLabel.setText("変更を反映しました");
        });

        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());

        add(createToolBar(), BorderLayout.NORTH);
        add(createLeftPanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("ファイル");
        JMenuItem newItem = new JMenuItem("新規");
        JMenuItem exitItem = new JMenuItem("終了");

        newItem.addActionListener(e -> {
            canvasPanel.clearItems();
            statusLabel.setText("新規作成しました");
        });

        exitItem.addActionListener(e -> {
            dispose();
        });

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu templateMenu = new JMenu("テンプレート");
        JMenuItem classroomItem = new JMenuItem("214教室");

        classroomItem.addActionListener(e -> {

            RoomTemplate template =
                    RoomTemplateFactory.createFirstClassroom();

            canvasPanel.setRoomTemplate(template);

            statusLabel.setText("テンプレート: 214教室");
        });
        
        JMenuItem outdoorStageItem = new JMenuItem("大学野外ステージ");

        outdoorStageItem.addActionListener(e -> {

            RoomTemplate template =
                    RoomTemplateFactory.createOutdoorStage();

            canvasPanel.setRoomTemplate(template);

            statusLabel.setText("テンプレート: 大学野外ステージ");
        });

        JMenuItem clearTemplateItem = new JMenuItem("テンプレート解除");

        clearTemplateItem.addActionListener(e -> {
            canvasPanel.setRoomTemplate(null);
            statusLabel.setText("テンプレートを解除しました");
        });

        templateMenu.add(classroomItem);
        templateMenu.add(outdoorStageItem);
        templateMenu.addSeparator();
        templateMenu.add(clearTemplateItem);

        JMenu viewMenu = new JMenu("表示");

        JMenuItem showGridItem = new JMenuItem("グリッド表示");
        showGridItem.addActionListener(e -> {
            canvasPanel.setShowGrid(true);
            statusLabel.setText("グリッド: ON");
        });

        JMenuItem hideGridItem = new JMenuItem("グリッド非表示");
        hideGridItem.addActionListener(e -> {
            canvasPanel.setShowGrid(false);
            statusLabel.setText("グリッド: OFF");
        });

        JMenuItem showNamesItem = new JMenuItem("名前表示");
        showNamesItem.addActionListener(e -> {
            canvasPanel.setShowNames(true);
            statusLabel.setText("名前表示: ON");
        });

        JMenuItem hideNamesItem = new JMenuItem("名前非表示");
        hideNamesItem.addActionListener(e -> {
            canvasPanel.setShowNames(false);
            statusLabel.setText("名前表示: OFF");
        });

        viewMenu.add(showGridItem);
        viewMenu.add(hideGridItem);
        viewMenu.addSeparator();
        viewMenu.add(showNamesItem);
        viewMenu.add(hideNamesItem);

        menuBar.add(fileMenu);
        menuBar.add(templateMenu);
        menuBar.add(viewMenu);

        return menuBar;
    }

    private JToolBar createToolBar() {

        JToolBar toolBar = new JToolBar();
        
        toolBar.setFloatable(false);

        JButton selectButton = new JButton("選択");
        JButton lineButton = new JButton("線を引く");
        JButton finishLineButton = new JButton("線終了");
        JButton deleteButton = new JButton("削除");
        JButton rotateButton = new JButton("回転");
        JButton enlargeButton = new JButton("拡大");
        JButton shrinkButton = new JButton("縮小");
        
        selectButton.setToolTipText("配置済みの機材を選択・移動します");
        lineButton.setToolTipText("クリックして線を連続で描画します");
        finishLineButton.setToolTipText("現在の線の始点を解除します");
        deleteButton.setToolTipText("選択中の機材を削除します");
        rotateButton.setToolTipText("選択中の機材を15度回転します");
        enlargeButton.setToolTipText("選択中の機材を大きくします");
        shrinkButton.setToolTipText("選択中の機材を小さくします");
        
        JCheckBox gridCheckBox = new JCheckBox("グリッド", true);
        JCheckBox nameCheckBox = new JCheckBox("名前", true);
        
        JCheckBox lineLengthCheckBox =
                new JCheckBox("線長さ", true);
        lineLengthCheckBox.setToolTipText(
                "線の長さをメートルで表示します");

        gridCheckBox.setToolTipText("グリッドの表示を切り替えます");
        nameCheckBox.setToolTipText("機材名・表示名の表示を切り替えます");
        lineLengthCheckBox.setToolTipText("線の長さをメートルで表示します");

        JComboBox<String> colorComboBox =
                new JComboBox<>(new String[] {"赤", "黒", "青", "緑"});

        JComboBox<Integer> strokeComboBox =
                new JComboBox<>(new Integer[] {1, 2, 3, 4, 5, 6, 8, 10});

        strokeComboBox.setSelectedItem(3);
        
        colorComboBox.setMaximumSize(new Dimension(80, 28));
        colorComboBox.setPreferredSize(new Dimension(80, 28));

        strokeComboBox.setMaximumSize(new Dimension(60, 28));
        strokeComboBox.setPreferredSize(new Dimension(60, 28));
        
        JComboBox<String> zoomComboBox =
                new JComboBox<>(new String[] {"50%", "75%", "100%", "125%", "150%", "200%"});

        zoomComboBox.setSelectedItem("100%");
        zoomComboBox.setMaximumSize(new Dimension(80, 28));
        zoomComboBox.setPreferredSize(new Dimension(80, 28));

        selectButton.addActionListener(e -> {
            canvasPanel.setDrawLineMode(false);
            statusLabel.setText("モード: 選択");
        });

        lineButton.addActionListener(e -> {
            canvasPanel.setDrawLineMode(true);
            statusLabel.setText("モード: 線描画");
        });

        finishLineButton.addActionListener(e -> {
            canvasPanel.finishCurrentLine();
            statusLabel.setText("線の始点を解除しました");
        });

        deleteButton.addActionListener(e -> {
            canvasPanel.deleteSelectedItem();
            statusLabel.setText("選択中のアイテムを削除しました");
        });

        rotateButton.addActionListener(e -> {
            canvasPanel.rotateSelectedItem();
            statusLabel.setText("選択中のアイテムを回転しました");
        });

        enlargeButton.addActionListener(e -> {
            canvasPanel.resizeSelectedItem(10);
            statusLabel.setText("選択中のアイテムを拡大しました");
        });

        shrinkButton.addActionListener(e -> {
            canvasPanel.resizeSelectedItem(-10);
            statusLabel.setText("選択中のアイテムを縮小しました");
        });

        gridCheckBox.addActionListener(e -> {

            boolean selected = gridCheckBox.isSelected();

            canvasPanel.setShowGrid(selected);

            if (selected) {
                statusLabel.setText("グリッド: ON");
            } else {
                statusLabel.setText("グリッド: OFF");
            }
        });

        nameCheckBox.addActionListener(e -> {

            boolean selected = nameCheckBox.isSelected();

            canvasPanel.setShowNames(selected);

            if (selected) {
                statusLabel.setText("名前表示: ON");
            } else {
                statusLabel.setText("名前表示: OFF");
            }
        });
        
        lineLengthCheckBox.addActionListener(e -> {

            boolean selected = lineLengthCheckBox.isSelected();

            canvasPanel.setShowLineLength(selected);

            if (selected) {
                statusLabel.setText("線の長さ表示: ON");
            } else {
                statusLabel.setText("線の長さ表示: OFF");
            }
        });

        colorComboBox.addActionListener(e -> {

            String colorName = (String) colorComboBox.getSelectedItem();

            if ("赤".equals(colorName)) {
                canvasPanel.setCurrentLineColor(Color.RED);
            } else if ("黒".equals(colorName)) {
                canvasPanel.setCurrentLineColor(Color.BLACK);
            } else if ("青".equals(colorName)) {
                canvasPanel.setCurrentLineColor(Color.BLUE);
            } else if ("緑".equals(colorName)) {
                canvasPanel.setCurrentLineColor(Color.GREEN);
            }

            statusLabel.setText("線の色: " + colorName);
        });

        strokeComboBox.addActionListener(e -> {

            Integer strokeWidth =
                    (Integer) strokeComboBox.getSelectedItem();

            if (strokeWidth == null) {
                return;
            }

            canvasPanel.setCurrentLineStrokeWidth(strokeWidth);

            statusLabel.setText("線の太さ: " + strokeWidth + "px");
        });
        
        zoomComboBox.addActionListener(e -> {

            String selected = (String) zoomComboBox.getSelectedItem();

            if ("50%".equals(selected)) {
                canvasPanel.setZoom(0.5);
            } else if ("75%".equals(selected)) {
                canvasPanel.setZoom(0.75);
            } else if ("100%".equals(selected)) {
                canvasPanel.setZoom(1.0);
            } else if ("125%".equals(selected)) {
                canvasPanel.setZoom(1.25);
            } else if ("150%".equals(selected)) {
                canvasPanel.setZoom(1.5);
            } else if ("200%".equals(selected)) {
                canvasPanel.setZoom(2.0);
            }

            statusLabel.setText("表示倍率: " + selected);
        });
               

        toolBar.add(selectButton);
        toolBar.add(lineButton);
        toolBar.add(finishLineButton);

        toolBar.addSeparator();

        toolBar.add(deleteButton);
        toolBar.add(rotateButton);
        toolBar.add(enlargeButton);
        toolBar.add(shrinkButton);

        toolBar.addSeparator();

        toolBar.add(new JLabel(" 色: "));
        toolBar.add(colorComboBox);

        toolBar.add(new JLabel(" 太さ: "));
        toolBar.add(strokeComboBox);

        toolBar.addSeparator();

        toolBar.add(gridCheckBox);
        toolBar.add(nameCheckBox);
        toolBar.add(lineLengthCheckBox);

        toolBar.addSeparator();

        toolBar.add(new JLabel(" 表示: "));
        toolBar.add(zoomComboBox);

        return toolBar;
    }

    private Component createLeftPanel() {

        JScrollPane scrollPane = new JScrollPane(equipmentPanel);

        scrollPane.setPreferredSize(new Dimension(180, 0));

        return scrollPane;
    }

    private Component createCenterPanel() {

        JScrollPane scrollPane = new JScrollPane(canvasPanel);

        return scrollPane;
    }

    private Component createRightPanel() {

        propertyPanel.setPreferredSize(new Dimension(240, 0));

        return propertyPanel;
    }

    private Component createStatusBar() {

        JPanel panel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("準備完了");

        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }
}