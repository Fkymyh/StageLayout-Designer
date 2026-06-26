package view;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class PropertyPanel extends JPanel {

    public PropertyPanel() {

        setLayout(new GridLayout(1,3));

        add(new JTextArea("使用機材"));

        add(new JTextArea("必要数"));

        add(new JTextArea("注意事項"));

    }

}