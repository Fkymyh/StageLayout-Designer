package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.JToggleButton;

import io.LayoutData;
import io.LayoutFileManager;
import model.ProjectInfo;
import model.RoomTemplate;
import model.RoomTemplateFactory;

public class MainFrame extends JFrame {

    private EquipmentPanel equipmentPanel;
    private PropertyPanel propertyPanel;
    private CanvasPanel canvasPanel;

    private ProjectInfo projectInfo = new ProjectInfo();

    private JLabel statusLabel;

    private JScrollPane equipmentScrollPane;

    private JScrollPane canvasScrollPane;

    private JSplitPane leftSplitPane;

    private JSplitPane rightSplitPane;

    private int lastEquipmentPanelWidth = 320;

    private int lastPropertyPanelWidth = 260;

    private JCheckBox stageLockCheckBox;

    public MainFrame() {

        setTitle("Stage Layout Designer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 950);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        equipmentPanel = new EquipmentPanel();
        propertyPanel = new PropertyPanel();
        canvasPanel = new CanvasPanel(equipmentPanel, propertyPanel);
        canvasPanel.setStageLocked(false);
        
        propertyPanel.setUpdateCallback(() -> {
            canvasPanel.repaint();
            statusLabel.setText("表示名・サイズを反映しました");
        });

        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());

        Component workArea = createWorkArea();

        add(createToolBar(), BorderLayout.NORTH);
        add(workArea, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("ファイル");
        JMenuItem newItem = new JMenuItem("新規");
        JMenuItem saveItem = new JMenuItem("保存");
        JMenuItem loadItem = new JMenuItem("読み込み");
        JMenuItem projectInfoItem = new JMenuItem("イベント情報");
        JMenuItem previewItem = new JMenuItem("プレビュー");
        JMenuItem loadVenueTemplateItem = new JMenuItem("会場テンプレートを読み込み");
        JMenuItem saveVenueTemplateItem = new JMenuItem("会場だけ保存");
        JMenuItem exitItem = new JMenuItem("終了");

        newItem.addActionListener(e -> {
            double[] sheetSize = askSheetSizeMeters();

            if (sheetSize == null) {
                return;
            }

            canvasPanel.clearAll();
            canvasPanel.setSheetSizeMeters(sheetSize[0], sheetSize[1]);
            canvasPanel.setStageLocked(false);
            updateStageLockCheckBox(false);
            projectInfo = new ProjectInfo();
            canvasPanel.fitToView(canvasScrollPane.getViewport().getExtentSize());
            canvasPanel.requestFocusInWindow();
            statusLabel.setText("新規作成しました");
        });
        
        saveItem.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showSaveDialog(this);

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            String fileName =
                    fileChooser.getSelectedFile().getAbsolutePath();

            if (canvasPanel.getRoomTemplate() != null) {
                projectInfo.setTemplateName(canvasPanel.getRoomTemplate().getName());
            } else {
                projectInfo.setTemplateName("");
            }

            try {

                LayoutFileManager.save(
                        canvasPanel.getItems(),
                        canvasPanel.getCustomRoomObjects(),
                        canvasPanel.getDrawLines(),
                        projectInfo,
                        fileName);

                statusLabel.setText("保存しました: " + fileName);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "保存に失敗しました。\n" + ex.getMessage());

                ex.printStackTrace();
            }
        });
        
        loadItem.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showOpenDialog(this);

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            String fileName =
                    fileChooser.getSelectedFile().getAbsolutePath();

            try {

                LayoutData data =
                        LayoutFileManager.load(fileName);

                canvasPanel.setItems(data.getItems());
                canvasPanel.setCustomRoomObjects(data.getCustomRoomObjects());
                canvasPanel.setDrawLines(data.getDrawLines());
                canvasPanel.setStageLocked(false);
                updateStageLockCheckBox(false);

                projectInfo = data.getProjectInfo();

                if (projectInfo == null) {
                    projectInfo = new ProjectInfo();
                }

                if ("214教室".equals(projectInfo.getTemplateName())
                        || "第一教室".equals(projectInfo.getTemplateName())) {

                    canvasPanel.setRoomTemplate(
                            RoomTemplateFactory.createFirstClassroom());

                } else if ("大学野外ステージ".equals(projectInfo.getTemplateName())) {

                    canvasPanel.setRoomTemplate(
                            RoomTemplateFactory.createOutdoorStage());

                } else {

                    canvasPanel.setRoomTemplate(null);
                }

                statusLabel.setText("読み込みました: " + fileName);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "読み込みに失敗しました。\n" + ex.getMessage());

                ex.printStackTrace();
            }
        });

        exitItem.addActionListener(e -> {
            dispose();
        });

        projectInfoItem.addActionListener(e -> {
            ProjectInfoDialog dialog =
                    new ProjectInfoDialog(
                            this,
                            projectInfo,
                            () -> statusLabel.setText("イベント情報を更新しました"));

            dialog.setVisible(true);
        });

        previewItem.addActionListener(e -> {
            PreviewDialog dialog =
                    new PreviewDialog(
                            this,
                            projectInfo,
                            canvasPanel.getItems(),
                            canvasPanel.getCustomRoomObjects(),
                            canvasPanel.getDrawLines(),
                            canvasPanel.getRoomTemplate(),
                            PreviewDialog.ORIENTATION_LANDSCAPE);

            dialog.setVisible(true);
        });

        saveVenueTemplateItem.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showSaveDialog(this);

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            String fileName =
                    fileChooser.getSelectedFile().getAbsolutePath();

            projectInfo.setTemplateName("");

            try {

                LayoutFileManager.save(
                        new ArrayList<>(),
                        canvasPanel.getCustomRoomObjects(),
                        new ArrayList<>(),
                        projectInfo,
                        fileName);

                canvasPanel.setStageLocked(true);
                updateStageLockCheckBox(true);

                statusLabel.setText("会場だけ保存しました: " + fileName);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "会場だけ保存に失敗しました。\n" + ex.getMessage());

                ex.printStackTrace();
            }
        });

        loadVenueTemplateItem.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showOpenDialog(this);

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            if (!canvasPanel.getCustomRoomObjects().isEmpty()) {

                int answer =
                        JOptionPane.showConfirmDialog(
                                this,
                                "現在の会場パーツを読み込んだ会場に置き換えます。\n"
                                        + "機材配置と線はそのまま残ります。",
                                "会場テンプレートを読み込み",
                                JOptionPane.OK_CANCEL_OPTION);

                if (answer != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            String fileName =
                    fileChooser.getSelectedFile().getAbsolutePath();

            try {

                LayoutData data =
                        LayoutFileManager.load(fileName);

                canvasPanel.setCustomRoomObjects(data.getCustomRoomObjects());
                canvasPanel.setRoomTemplate(null);
                canvasPanel.setStageLocked(false);
                updateStageLockCheckBox(false);

                ProjectInfo loadedInfo = data.getProjectInfo();

                if (loadedInfo != null && !loadedInfo.getPlace().isBlank()) {
                    projectInfo.setPlace(loadedInfo.getPlace());
                }

                statusLabel.setText("会場テンプレートを読み込みました: " + fileName);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "会場テンプレートの読み込みに失敗しました。\n" + ex.getMessage());

                ex.printStackTrace();
            }
        });

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(projectInfoItem);
        fileMenu.add(previewItem);
        fileMenu.add(loadVenueTemplateItem);
        fileMenu.add(saveVenueTemplateItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
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

    private Component createToolBar() {

        JToggleButton selectButton = new JToggleButton("機材を動かす");
        JToggleButton lineButton = new JToggleButton("線を引く");
        JButton deleteButton = new JButton("削除");
        JButton rotateButton = new JButton("回転");
        JButton enlargeButton = new JButton("拡大");
        JButton shrinkButton = new JButton("縮小");
        JButton projectInfoButton = new JButton("イベント情報");
        JButton previewButton = new JButton("プレビュー");
        
        selectButton.setToolTipText("配置済みの機材を選択・移動します");
        lineButton.setToolTipText("線モードに切り替えます。別のモードへ切り替えると線は終了します");
        deleteButton.setToolTipText("選択中の機材を削除します");
        rotateButton.setToolTipText("選択中の機材を15度回転します");
        enlargeButton.setToolTipText("選択中の機材を大きくします");
        shrinkButton.setToolTipText("選択中の機材を小さくします");
        projectInfoButton.setToolTipText("イベント名、日付、会場名、担当者、メモを入力します");
        previewButton.setToolTipText("提出用の1枚レイアウトを確認し、PNG画像として保存できます");

        ButtonGroup modeButtonGroup = new ButtonGroup();

        modeButtonGroup.add(selectButton);
        modeButtonGroup.add(lineButton);

        selectButton.setSelected(true);
        
        JCheckBox gridCheckBox = new JCheckBox("グリッド", true);
        JCheckBox nameCheckBox = new JCheckBox("名前", true);
        JCheckBox equipmentPanelCheckBox = new JCheckBox("機材パレット", true);
        JCheckBox propertyPanelCheckBox = new JCheckBox("詳細", true);
        
        JCheckBox lineLengthCheckBox =
                new JCheckBox("線長さ", true);
        lineLengthCheckBox.setToolTipText(
                "線の長さをメートルで表示します");

        gridCheckBox.setToolTipText("グリッドの表示を切り替えます");
        nameCheckBox.setToolTipText("機材名・表示名の表示を切り替えます");
        equipmentPanelCheckBox.setToolTipText("左側の機材パレットの表示を切り替えます");
        propertyPanelCheckBox.setToolTipText("右側の選択中アイテム欄の表示を切り替えます");
        lineLengthCheckBox.setToolTipText("線の長さをメートルで表示します");

        stageLockCheckBox = new JCheckBox("会場固定", false);
        stageLockCheckBox.setToolTipText("ONにすると、会場パーツや常設物を動かせないようにします");

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
                new JComboBox<>(new String[] {"全体", "50%", "75%", "100%", "125%", "150%", "200%"});

        zoomComboBox.setSelectedItem("全体");
        zoomComboBox.setMaximumSize(new Dimension(80, 28));
        zoomComboBox.setPreferredSize(new Dimension(80, 28));

        selectButton.addActionListener(e -> {
        		canvasPanel.setSelectMode();;
            statusLabel.setText("モード: 機材を動かす");
        });

        lineButton.addActionListener(e -> {
            canvasPanel.setDrawLineMode(true);
            statusLabel.setText("モード: 線描画");
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
        projectInfoButton.addActionListener(e -> {
            ProjectInfoDialog dialog =
                    new ProjectInfoDialog(
                            this,
                            projectInfo,
                            () -> statusLabel.setText("イベント情報を更新しました"));

            dialog.setVisible(true);
        });

        previewButton.addActionListener(e -> {
            PreviewDialog dialog =
                    new PreviewDialog(
                            this,
                            projectInfo,
                            canvasPanel.getItems(),
                            canvasPanel.getCustomRoomObjects(),
                            canvasPanel.getDrawLines(),
                            canvasPanel.getRoomTemplate(),
                            PreviewDialog.ORIENTATION_LANDSCAPE);

            dialog.setVisible(true);
        });

        stageLockCheckBox.addActionListener(e -> {

            boolean selected = stageLockCheckBox.isSelected();

            canvasPanel.setStageLocked(selected);

            if (selected) {
                statusLabel.setText("会場固定: ON");
            } else {
                statusLabel.setText("会場固定: OFF / 会場パーツも編集できます");
            }
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

        equipmentPanelCheckBox.addActionListener(e -> {

            boolean selected = equipmentPanelCheckBox.isSelected();

            setEquipmentPanelVisible(selected);

            if (selected) {
                statusLabel.setText("機材パレット: 表示");
            } else {
                statusLabel.setText("機材パレット: 非表示");
            }
        });

        propertyPanelCheckBox.addActionListener(e -> {

            boolean selected = propertyPanelCheckBox.isSelected();

            setPropertyPanelVisible(selected);

            if (selected) {
                statusLabel.setText("詳細パネル: 表示");
            } else {
                statusLabel.setText("詳細パネル: 非表示");
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

            if ("全体".equals(selected)) {
                canvasPanel.fitToView(canvasScrollPane.getViewport().getExtentSize());
            } else if ("50%".equals(selected)) {
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
        
          

        JPanel toolbarPanel = new JPanel(new BorderLayout());

        JPanel mainToolsPanel = createToolRowPanel();
        mainToolsPanel.setBorder(BorderFactory.createTitledBorder("作成・配置"));
        mainToolsPanel.add(projectInfoButton);
        mainToolsPanel.add(previewButton);
        mainToolsPanel.add(selectButton);
        mainToolsPanel.add(lineButton);
        mainToolsPanel.add(stageLockCheckBox);
        mainToolsPanel.add(new JLabel("  表示:"));
        mainToolsPanel.add(gridCheckBox);
        mainToolsPanel.add(nameCheckBox);
        mainToolsPanel.add(new JLabel("倍率"));
        mainToolsPanel.add(zoomComboBox);

        JPanel subToolsPanel = createToolRowPanel();
        subToolsPanel.setBorder(BorderFactory.createTitledBorder("編集・線・パネル"));
        subToolsPanel.add(deleteButton);
        subToolsPanel.add(rotateButton);
        subToolsPanel.add(enlargeButton);
        subToolsPanel.add(shrinkButton);
        subToolsPanel.add(new JLabel("  線:"));
        subToolsPanel.add(new JLabel("色"));
        subToolsPanel.add(colorComboBox);
        subToolsPanel.add(new JLabel("太さ"));
        subToolsPanel.add(strokeComboBox);
        subToolsPanel.add(lineLengthCheckBox);
        subToolsPanel.add(new JLabel("  パネル:"));
        subToolsPanel.add(equipmentPanelCheckBox);
        subToolsPanel.add(propertyPanelCheckBox);

        toolbarPanel.add(mainToolsPanel, BorderLayout.NORTH);
        toolbarPanel.add(subToolsPanel, BorderLayout.SOUTH);

        return toolbarPanel;
    }

    private void updateStageLockCheckBox(boolean selected) {

        if (stageLockCheckBox != null) {
            stageLockCheckBox.setSelected(selected);
        }
    }

    private double[] askSheetSizeMeters() {

        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2, 8, 8));
        JComboBox<String> presetComboBox =
                new JComboBox<>(
                        new String[] {
                                "20m x 15m（迷ったらこれ）",
                                "13m x 4m（練習ステージ）",
                                "17m x 13m（教室・小ホール）",
                                "30m x 20m（大きめ）",
                                "自由入力"
                        });
        javax.swing.JTextField widthField = new javax.swing.JTextField("20");
        javax.swing.JTextField heightField = new javax.swing.JTextField("15");

        presetComboBox.addActionListener(e -> {

            String selected = (String) presetComboBox.getSelectedItem();

            if (selected == null) {
                return;
            }

            if (selected.startsWith("20m")) {
                widthField.setText("20");
                heightField.setText("15");
            } else if (selected.startsWith("13m")) {
                widthField.setText("13");
                heightField.setText("4");
            } else if (selected.startsWith("17m")) {
                widthField.setText("17");
                heightField.setText("13");
            } else if (selected.startsWith("30m")) {
                widthField.setText("30");
                heightField.setText("20");
            }
        });

        panel.add(new JLabel("プリセット"));
        panel.add(presetComboBox);
        panel.add(new JLabel("横m / 縦m"));

        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        widthField.setPreferredSize(new Dimension(54, 26));
        heightField.setPreferredSize(new Dimension(54, 26));
        sizePanel.add(widthField);
        sizePanel.add(new JLabel(" x "));
        sizePanel.add(heightField);

        panel.add(sizePanel);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "作業シートの大きさ",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        try {
            double width = Double.parseDouble(widthField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());
            return new double[] {width, height};
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "横と縦は数字で入力してください。");
            return askSheetSizeMeters();
        }
    }

    private JPanel createToolRowPanel() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));

        panel.setPreferredSize(new Dimension(1000, 46));

        return panel;
    }

    private Component createWorkArea() {

        equipmentScrollPane = new JScrollPane(equipmentPanel);
        canvasScrollPane = new JScrollPane(canvasPanel);

        equipmentScrollPane.setPreferredSize(new Dimension(lastEquipmentPanelWidth, 0));
        equipmentScrollPane.setMinimumSize(new Dimension(120, 0));
        canvasScrollPane.setMinimumSize(new Dimension(480, 0));

        propertyPanel.setPreferredSize(new Dimension(lastPropertyPanelWidth, 0));
        propertyPanel.setMinimumSize(new Dimension(190, 0));

        leftSplitPane =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        equipmentScrollPane,
                        canvasScrollPane);

        leftSplitPane.setContinuousLayout(true);
        leftSplitPane.setOneTouchExpandable(true);
        leftSplitPane.setResizeWeight(0.0);
        leftSplitPane.setDividerSize(8);
        leftSplitPane.setDividerLocation(lastEquipmentPanelWidth);

        rightSplitPane =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        leftSplitPane,
                        propertyPanel);

        rightSplitPane.setContinuousLayout(true);
        rightSplitPane.setOneTouchExpandable(true);
        rightSplitPane.setResizeWeight(1.0);
        rightSplitPane.setDividerSize(8);

        SwingUtilities.invokeLater(
                () -> {
                    setPropertyDividerLocation(lastPropertyPanelWidth);
                    canvasPanel.fitToView(canvasScrollPane.getViewport().getExtentSize());
                });

        return rightSplitPane;
    }

    private void setEquipmentPanelVisible(boolean visible) {

        if (leftSplitPane == null || equipmentScrollPane == null) {
            return;
        }

        if (visible) {

            equipmentScrollPane.setVisible(true);
            leftSplitPane.setDividerSize(8);
            leftSplitPane.setDividerLocation(
                    Math.max(120, lastEquipmentPanelWidth));

        } else {

            if (leftSplitPane.getDividerLocation() > 20) {
                lastEquipmentPanelWidth = leftSplitPane.getDividerLocation();
            }

            equipmentScrollPane.setVisible(false);
            leftSplitPane.setDividerSize(0);
            leftSplitPane.setDividerLocation(0);
        }

        leftSplitPane.revalidate();
        leftSplitPane.repaint();
    }

    private void setPropertyPanelVisible(boolean visible) {

        if (rightSplitPane == null || propertyPanel == null) {
            return;
        }

        if (visible) {

            propertyPanel.setVisible(true);
            rightSplitPane.setDividerSize(8);
            setPropertyDividerLocation(lastPropertyPanelWidth);

        } else {

            int width = rightSplitPane.getWidth()
                    - rightSplitPane.getDividerLocation()
                    - rightSplitPane.getDividerSize();

            if (width > 80) {
                lastPropertyPanelWidth = width;
            }

            propertyPanel.setVisible(false);
            rightSplitPane.setDividerSize(0);
            rightSplitPane.setDividerLocation(1.0);
        }

        rightSplitPane.revalidate();
        rightSplitPane.repaint();
    }

    private void setPropertyDividerLocation(int propertyWidth) {

        if (rightSplitPane == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {

            int totalWidth = rightSplitPane.getWidth();

            if (totalWidth <= 0) {
                return;
            }

            int dividerLocation =
                    totalWidth - propertyWidth - rightSplitPane.getDividerSize();

            rightSplitPane.setDividerLocation(Math.max(480, dividerLocation));
        });
    }

    private Component createStatusBar() {

        JPanel panel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("準備完了");

        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }
    
    
}
