package view;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

        setSize(400, 350);

        setLocationRelativeTo(owner);

        setLayout(new GridLayout(11, 1));

        titleField = new JTextField(projectInfo.getTitle());
        dateField = new JTextField(projectInfo.getDate());
        placeField = new JTextField(projectInfo.getPlace());
        plannerField = new JTextField(projectInfo.getPlanner());
        noteArea = new JTextArea(projectInfo.getNote());

        add(new JLabel("タイトル"));
        add(titleField);

        add(new JLabel("日付"));
        add(dateField);

        add(new JLabel("場所"));
        add(placeField);

        add(new JLabel("担当者"));
        add(plannerField);

        add(new JLabel("全体メモ"));
        add(noteArea);

        JButton saveButton = new JButton("保存");

        add(saveButton);

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

