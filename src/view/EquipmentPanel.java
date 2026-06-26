package view;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EquipmentPanel extends JPanel {
	
	private JList<String> equipmentList;
	
	public EquipmentPanel() {
		
		setPreferredSize(new Dimension(220, 0));
		
		DefaultListModel<String> model = new DefaultListModel<>();
		
		model.addElement("マイク");
        model.addElement("スピーカー");
        model.addElement("ミキサー");
        model.addElement("PARライト");
        model.addElement("バミリ");
        model.addElement("平台");
        model.addElement("箱馬");
        model.addElement("棒人間");
        
        equipmentList = new JList<>(model);
        
        equipmentList.setSelectedIndex(0);
        
        add(new JScrollPane(equipmentList));
        
	}
	
	public String getSelectedEquipment() {
		
		return equipmentList.getSelectedValue();
	}

}
