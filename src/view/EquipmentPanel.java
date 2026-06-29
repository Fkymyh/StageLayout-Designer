package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class EquipmentPanel extends JPanel {

    private JTabbedPane tabbedPane;

    private Map<String, JList<String>> equipmentLists = new HashMap<>();

    public EquipmentPanel() {

        setLayout(new BorderLayout());

        setPreferredSize(new Dimension(220, 0));

        tabbedPane = new JTabbedPane();

        addEquipmentTab(
                "音響",
                new String[] {
                        "マイク",
                        "スピーカー",
                        "ミキサー"
                });

        addEquipmentTab(
                "照明",
                new String[] {
                        "PARライト"
                });

        addEquipmentTab(
                "舞台",
                new String[] {
                        "バミリ",
                        "平台",
                        "箱馬"
                });

        addEquipmentTab(
                "人物",
                new String[] {
                        "棒人間"
                });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void addEquipmentTab(String categoryName, String[] equipmentNames) {

        DefaultListModel<String> model = new DefaultListModel<>();

        for (String name : equipmentNames) {
            model.addElement(name);
        }

        JList<String> list = new JList<>(model);

        if (model.getSize() > 0) {
            list.setSelectedIndex(0);
        }

        equipmentLists.put(categoryName, list);

        tabbedPane.addTab(
                categoryName,
                new JScrollPane(list));
    }

    public String getSelectedEquipment() {

        String categoryName =
                tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());

        JList<String> list = equipmentLists.get(categoryName);

        if (list == null) {
            return null;
        }

        return list.getSelectedValue();
    }
}