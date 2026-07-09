package view;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.DrawLine;
import model.LayoutItem;
import model.ProjectInfo;
import model.RoomObject;
import model.RoomTemplate;

public class PreviewDialog extends JDialog{
	
	public static final String ORIENTATION_PORTRAIT = "PORTRAIT";
	public static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

	
	public PreviewDialog(
			MainFrame owner,
			ProjectInfo projectInfo,
			List<LayoutItem> items,
			List<RoomObject> customRoomObjects,
			List<DrawLine> drawLines,
			RoomTemplate roomTemplate,
			String orientation) {
		
		super(owner, "プレビュー", true);
		
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
						customRoomObjects,
						drawLines,
						roomTemplate,
						orientation);
		
		JScrollPane scrollPane =
				new JScrollPane(previewPanel);
		
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		JButton exportButton = new JButton("PNG画像として保存");
		JButton closeButton = new JButton("閉じる");

		exportButton.addActionListener(e -> exportPreviewImage(previewPanel));
		closeButton.addActionListener(e -> dispose());

		buttonPanel.add(exportButton);
		buttonPanel.add(closeButton);

		add(buttonPanel, BorderLayout.SOUTH);
		
		
	}

	private void exportPreviewImage(SheetPreviewPanel previewPanel) {

	    JFileChooser fileChooser = new JFileChooser();

	    fileChooser.setSelectedFile(new File("stage-layout.png"));

	    int result = fileChooser.showSaveDialog(this);

	    if (result != JFileChooser.APPROVE_OPTION) {
	        return;
	    }

	    File file = fileChooser.getSelectedFile();

	    if (!file.getName().toLowerCase().endsWith(".png")) {
	        file = new File(file.getParentFile(), file.getName() + ".png");
	    }

	    try {

	        BufferedImage image = previewPanel.createExportImage();

	        ImageIO.write(image, "png", file);

	        JOptionPane.showMessageDialog(
	                this,
	                "PNG画像を保存しました。\n" + file.getAbsolutePath());

	    } catch (Exception ex) {

	        JOptionPane.showMessageDialog(
	                this,
	                "PNG画像の保存に失敗しました。\n" + ex.getMessage());

	        ex.printStackTrace();
	    }
	}
	

}
