package view;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import model.LayoutItem;
import model.ProjectInfo;

public class PreviewDialog extends JDialog{
	
	public PreviewDialog(
			MainFrame owner,
			ProjectInfo projectInfo,
			List<LayoutItem> items) {
		
		super(owner, "提出用プレビュー", true);
		
		setSize(1100, 800);
		
		setLocationRelativeTo(owner);
		
		setLayout(new BorderLayout());
		
		SheetPreviewPanel previewPanel =
				new SheetPreviewPanel(projectInfo, items);
		
		JScrollPane scrollPane =
				new JScrollPane(previewPanel);
		
		add(scrollPane, BorderLayout.CENTER);
	}

}
