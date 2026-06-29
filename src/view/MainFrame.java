package view;

import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import io.LayoutFileManager;

public class MainFrame extends JFrame {

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
        
        propertyPanel.setUpdateCallback(() -> {

            canvasPanel.repaint();

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
        
        //保存処理
        menuBar.getSaveItem().addActionListener(e -> {
        	
        		JFileChooser chooser = new JFileChooser();
        		
        		if(chooser.showSaveDialog(this)
        				== JFileChooser.APPROVE_OPTION) {
        			
        			try {

        			    LayoutFileManager.save(

        			            canvasPanel.getItems(),

        			            chooser.getSelectedFile().getAbsolutePath()

        			    );

        			} catch(Exception ex){

        			    ex.printStackTrace();

        			}
        			
        		}
        	
        });
        //開く処理
        menuBar.getOpenItem().addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();

            if(chooser.showOpenDialog(this)
                    == JFileChooser.APPROVE_OPTION){

                try{

                    canvasPanel.setItems(
                        LayoutFileManager.load(
                            chooser.getSelectedFile().getAbsolutePath()));

                }catch(Exception ex){

                    ex.printStackTrace();

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

    }

}