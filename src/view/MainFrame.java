package view;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import model.RoomTemplateFactory;

public class MainFrame extends JFrame {
	
	private File currentFile;
	
	private boolean modified = false;
	
	private ProjectInfo projectInfo = new ProjectInfo();

    public MainFrame() {

        setTitle("Stage Layout Designer");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        EquipmentPanel equipmentPanel = new EquipmentPanel();
        
        PropertyPanel propertyPanel = new PropertyPanel();
        
        CanvasPanel canvasPanel = 
        		new CanvasPanel(
        				equipmentPanel,
        				propertyPanel);
        
        
        
    		addWindowListener(new WindowAdapter() {

    			@Override
    			public void windowClosing(WindowEvent e) {

    				if (!confirmSaveIfNeeded(canvasPanel)) {
    					return;
    				}

    				dispose();
    				System.exit(0);
    			}
    		});
        
        canvasPanel.setChangeCallback(() -> {
        	
        		canvasPanel.repaint();
        	
        		setModified(true);
        		

        });
        
        propertyPanel.setUpdateCallback(() -> {

            canvasPanel.repaint();

            setModified(true);
        });
        
        JScrollPane canvasScrollPane = new JScrollPane(canvasPanel);
        
        JSplitPane horizontalSplitPane = new JSplitPane(
        		JSplitPane.HORIZONTAL_SPLIT,
        		equipmentPanel,
        		canvasScrollPane);
        
        horizontalSplitPane.setDividerLocation(280);
        horizontalSplitPane.setResizeWeight(0.0);
        
        JSplitPane mainSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                horizontalSplitPane,
                propertyPanel);

        mainSplitPane.setResizeWeight(1.0);

        int bottomPanelHeight = 220;

        mainSplitPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {

                int dividerLocation =
                        mainSplitPane.getHeight() - bottomPanelHeight;

                if (dividerLocation > 0) {
                    mainSplitPane.setDividerLocation(dividerLocation);
                }
            }
        });

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
        	
        		if (!confirmSaveIfNeeded(canvasPanel)) {
        			return;
        		}

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
                    
                    canvasPanel.setRoomTemplate(
                            RoomTemplateFactory.createByName(
                                    projectInfo.getTemplateName()));
                    
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
        	
        		if (!confirmSaveIfNeeded(canvasPanel)) {
        			return;
        		}

            int result = JOptionPane.showConfirmDialog(
            		this,
            		"現在のレイアウトを破棄して新規作成しますか？",
            		"新規作成",
            		JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
            	
            		
            		
            		canvasPanel.clearItems();

            		projectInfo = new ProjectInfo();

            		canvasPanel.setRoomTemplate(null);

            		currentFile = null;

            		setModified(false);
            		
            }

        });
        
     // 終了処理
        menuBar.getExitItem().addActionListener(e -> {
        	
        	if (!confirmSaveIfNeeded(canvasPanel)) {
                return;
            }

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
        
     // 会場テンプレート：第一教室
        menuBar.getFirstClassroomItem().addActionListener(e -> {

            canvasPanel.setRoomTemplate(
                    RoomTemplateFactory.createFirstClassroom());
            
            projectInfo.setTemplateName("第一教室");

            setModified(true);
        });
        
     // 会場テンプレート：なし
        menuBar.getClearTemplateItem().addActionListener(e -> {

            canvasPanel.setRoomTemplate(null);
            
            projectInfo.setTemplateName("");

            setModified(true);
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
        
        //名前のON/OFF
        menuBar.getShowNamesItem().addActionListener(e -> {
            canvasPanel.setShowNames(
                    menuBar.getShowNamesItem().isSelected());
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
        //プレビュー処理
        menuBar.getPreviewItem().addActionListener(e -> {

        	PreviewDialog dialog =
        	        new PreviewDialog(
        	                this,
        	                projectInfo,
        	                canvasPanel.getItems(),
        	                canvasPanel.getRoomTemplate(),
        	                PreviewDialog.ORIENTATION_LANDSCAPE);

        	dialog.setVisible(true);
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
    
    private boolean confirmSaveIfNeeded(CanvasPanel canvasPanel) {
    	
    	if (!modified) {
    		return true;
    	}
    	
    	int result = JOptionPane.showConfirmDialog(
    			this,
    			"保存されていない変更があります。\n保存しますか？",
    			"保存確認",
    			JOptionPane.YES_NO_CANCEL_OPTION);
    	
    	if (result == JOptionPane.CANCEL_OPTION
    			|| result == JOptionPane.CLOSED_OPTION) {
    		
    		return false;
    	}
    	
    	if (result == JOptionPane.YES_OPTION) {
    		
    		saveLayout(canvasPanel);
    		
    		return !modified;
    	}
    	
    	return true;
    }
    	
    
    	
    
    private void setModified(boolean modified) {

        this.modified = modified;

        updateTitle();
    }

}