package view;

import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
        
        JSplitPane splitPane = new JSplitPane(
        		JSplitPane.HORIZONTAL_SPLIT,
        		equipmentPanel,
        		canvasPanel);
        
        splitPane.setDividerLocation(250);
        
        add(splitPane, BorderLayout.CENTER);
        add(propertyPanel, BorderLayout.SOUTH);
        
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

    }

}