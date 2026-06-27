package view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

public class MainFrame extends JFrame {

    public MainFrame() {

        setTitle("Stage Layout Designer");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EquipmentPanel equipmentPanel = new EquipmentPanel();
        CanvasPanel canvasPanel = new CanvasPanel(equipmentPanel);
        
        JSplitPane splitPane = new JSplitPane(
        		JSplitPane.HORIZONTAL_SPLIT,
        		equipmentPanel,
        		canvasPanel);
        
        splitPane.setDividerLocation(250);
        
        add(splitPane, BorderLayout.CENTER);
        add(new PropertyPanel(), BorderLayout.SOUTH);
        
        setJMenuBar(new MenuBar());

    }

}