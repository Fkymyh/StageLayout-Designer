package view;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import model.LayoutItem;
import model.ProjectInfo;
import model.RoomTemplate;

public class PreviewDialog extends JDialog{
	
	public static final String ORIENTATION_PORTRAIT = "PORTRAIT";
	public static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

	
	public PreviewDialog(
			MainFrame owner,
			ProjectInfo projectInfo,
			List<LayoutItem> items,
			RoomTemplate roomTemplate,
			String orientation) {
		
		super(owner, "提出用プレビュー", true);
		
		if (ORIENTATION_LANDSCAPE.equals(orientation)) {
			setSize(1100, 800);
		}else {
			setSize(800, 1050);
		}
		
		setLocationRelativeTo(owner);
		
		setLayout(new BorderLayout());
		
		SheetPreviewPanel previewPanel =
				new SheetPreviewPanel(
						projectInfo,
						items,
						roomTemplate,
						orientation);
		
		JScrollPane scrollPane =
				new JScrollPane(previewPanel);
		
		add(scrollPane, BorderLayout.CENTER);
		
		
	}
	

}
