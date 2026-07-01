package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import model.ProjectInfo;

public class ProjectInfoDialog extends JDialog {

    private JTextField titleField;

    private JTextField dateField;

    private JTextField placeField;

    private JTextField plannerField;

    private JTextArea noteArea;

    public ProjectInfoDialog(
            MainFrame owner,
            ProjectInfo projectInfo,
            Runnable updateCallback) {

        super(owner, "案件情報", true);

        setSize(500, 420);

        setLocationRelativeTo(owner);

        setLayout(new BorderLayout(10, 10));
        
        JPanel inputPanel = new JPanel();
        
        inputPanel.setLayout(new GridLayout(4, 2, 8, 8));

        titleField = new JTextField(projectInfo.getTitle());
        dateField = new JTextField(projectInfo.getDate());
        placeField = new JTextField(projectInfo.getPlace());
        plannerField = new JTextField(projectInfo.getPlanner());

        inputPanel.add(new JLabel("タイトル"));
        inputPanel.add(titleField);
        
        inputPanel.add(new JLabel("日付"));
        inputPanel.add(dateField);
        
        inputPanel.add(new JLabel("場所"));
        inputPanel.add(placeField);
        
        inputPanel.add(new JLabel("担当者"));
        inputPanel.add(plannerField);
        
        add(inputPanel, BorderLayout.NORTH);
        
        noteArea = new JTextArea(projectInfo.getNote());
        
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        
        JScrollPane noteScrollPane = new JScrollPane(noteArea);
        
        noteScrollPane.setBorder(
        		javax.swing.BorderFactory.createTitledBorder("全体メモ"));
        
        add(noteScrollPane, BorderLayout.CENTER);
        
        JButton saveButton = new JButton("保存");
        
        add(saveButton, BorderLayout.SOUTH);
        

        saveButton.addActionListener(e -> {

            projectInfo.setTitle(titleField.getText());
            projectInfo.setDate(dateField.getText());
            projectInfo.setPlace(placeField.getText());
            projectInfo.setPlanner(plannerField.getText());
            projectInfo.setNote(noteArea.getText());

            if (updateCallback != null) {
                updateCallback.run();
            }

            dispose();
        });
    }
}

