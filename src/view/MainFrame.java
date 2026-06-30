package view;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.LayoutData;
import io.LayoutFileManager;
import model.ProjectInfo;

public class MainFrame extends JFrame {
	
	private File currentFile;
	
	private boolean modified = false;
	
	private ProjectInfo projectInfo = new ProjectInfo();

    public MainFrame() {

        setTitle("Stage Layout Designer");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EquipmentPanel equipmentPanel = new EquipmentPanel();
        
        PropertyPanel propertyPanel = new PropertyPanel();
        
        CanvasPanel canvasPanel = 
        		new CanvasPanel(
        				equipmentPanel,
        				propertyPanel);
        
        canvasPanel.setChangeCallback(() -> {
        	
        		canvasPanel.repaint();
        	
        		setModified(true);
        		

        });
        
        JScrollPane canvasScrollPane = new JScrollPane(canvasPanel);
        
        JSplitPane horizontalSplitPane = new JSplitPane(
        		JSplitPane.HORIZONTAL_SPLIT,
        		equipmentPanel,
        		canvasScrollPane);
        
        horizontalSplitPane.setDividerLocation(220);
        horizontalSplitPane.setResizeWeight(0.0);
        
        JSplitPane mainSplitPane = new JSplitPane(
        			JSplitPane.VERTICAL_SPLIT,
        			horizontalSplitPane,
        			propertyPanel);
        
        mainSplitPane.setDividerLocation(680);
        mainSplitPane.setResizeWeight(0.85);
        
        add(mainSplitPane, BorderLayout.CENTER);
        		
        
        MenuBar menuBar = new MenuBar();

        setJMenuBar(menuBar);
        
     // 保存処理
        menuBar.getSaveItem().addActionListener(e -> {

            saveLayout(canvasPanel);

        });
        
     // 名前を付けて保存処理
        menuBar.getSaveAsItem().addActionListener(e -> {

            saveLayoutAs(canvasPanel);

        });
     // 開く処理
        menuBar.getOpenItem().addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();

            FileNameExtensionFilter filter =
                    new FileNameExtensionFilter(
                            "Stage Layout File (*.stage)",
                            "stage");

            chooser.setFileFilter(filter);

            if (chooser.showOpenDialog(this)
                    == JFileChooser.APPROVE_OPTION) {

                try {

                    File file = chooser.getSelectedFile();

                    LayoutData data =
                            LayoutFileManager.load(
                                    file.getAbsolutePath());

                    projectInfo = data.getProjectInfo();

                    canvasPanel.setItems(data.getItems());
                    
                    currentFile = file;

                    setModified(false);

                    JOptionPane.showMessageDialog(
                            this,
                            "読み込みました。",
                            "読込完了",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {

                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                            this,
                            "読み込みに失敗しました。",
                            "エラー",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        
     // 新規作成処理
        menuBar.getNewItem().addActionListener(e -> {

            int result = JOptionPane.showConfirmDialog(
            		this,
            		"現在のレイアウトを破棄して新規作成しますか？",
            		"新規作成",
            		JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
            	
            		canvasPanel.clearItems();
            		
            		projectInfo = new ProjectInfo();
            		
            		currentFile = null;

            		setModified(false);
            }

        });
        
     // 終了処理
        menuBar.getExitItem().addActionListener(e -> {

            int result = JOptionPane.showConfirmDialog(
                    this,
                    "アプリを終了しますか？",
                    "終了確認",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });
        
     // プロジェクト：案件情報
        menuBar.getProjectInfoItem().addActionListener(e -> {

            ProjectInfoDialog dialog =
                    new ProjectInfoDialog(
                            this,
                            projectInfo,
                            () -> {
                                setModified(true);
                            });

            dialog.setVisible(true);
        });
        
     // グリッド表示ON/OFF
        menuBar.getGridVisibleItem().addActionListener(e -> {

            canvasPanel.setShowGrid(
                    menuBar.getGridVisibleItem().isSelected());
        });

        // グリッド吸着ON/OFF
        menuBar.getSnapToGridItem().addActionListener(e -> {

            canvasPanel.setSnapToGrid(
                    menuBar.getSnapToGridItem().isSelected());
        });
        
     // 編集メニュー：削除
        menuBar.getDeleteItem().addActionListener(e -> {

            canvasPanel.deleteSelectedItem();

        });

        // 編集メニュー：コピー
        menuBar.getCopyItem().addActionListener(e -> {

            canvasPanel.copySelectedItem();

        });

        // 編集メニュー：貼り付け
        menuBar.getPasteItem().addActionListener(e -> {

            canvasPanel.pasteCopiedItem();

        });
        
     // 編集メニュー：回転
        menuBar.getRotateItem().addActionListener(e -> {

            canvasPanel.rotateSelectedItem();

        });
        updateTitle();

    }
    private void updateTitle() {

        String title;

        if (currentFile == null) {
            title = "Stage Layout Designer - 新規レイアウト";
        } else {
            title = "Stage Layout Designer - " + currentFile.getName();
        }

        if (modified) {
            title += " *";
        }

        setTitle(title);
    }
    
    private void saveLayout(CanvasPanel canvasPanel) {

        if (currentFile == null) {
            saveLayoutAs(canvasPanel);
            return;
        }

        try {

        	LayoutFileManager.save(
        	        canvasPanel.getItems(),
        	        projectInfo,
        	        currentFile.getAbsolutePath());
            
            setModified(false);

            JOptionPane.showMessageDialog(
                    this,
                    "保存しました。",
                    "保存完了",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "保存に失敗しました。",
                    "エラー",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveLayoutAs(CanvasPanel canvasPanel) {

        JFileChooser chooser = new JFileChooser();

        FileNameExtensionFilter filter =
                new FileNameExtensionFilter(
                        "Stage Layout File (*.stage)",
                        "stage");

        chooser.setFileFilter(filter);

        if (chooser.showSaveDialog(this)
                == JFileChooser.APPROVE_OPTION) {

            try {

                File file = chooser.getSelectedFile();

                if (!file.getName().toLowerCase().endsWith(".stage")) {
                    file = new File(file.getAbsolutePath() + ".stage");
                }

                LayoutFileManager.save(
                        canvasPanel.getItems(),
                        projectInfo,
                        file.getAbsolutePath());

                currentFile = file;
                
                setModified(false);

                updateTitle();

                JOptionPane.showMessageDialog(
                        this,
                        "保存しました。",
                        "保存完了",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {

                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                        this,
                        "保存に失敗しました。",
                        "エラー",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void setModified(boolean modified) {

        this.modified = modified;

        updateTitle();
    }

}