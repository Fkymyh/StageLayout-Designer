package view;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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
        JButton printButton = new JButton("印刷");
		JButton closeButton = new JButton("閉じる");

		exportButton.addActionListener(e -> exportPreviewImage(previewPanel, projectInfo));
        printButton.addActionListener(e -> printPreview(previewPanel));
		closeButton.addActionListener(e -> dispose());

		buttonPanel.add(exportButton);
        buttonPanel.add(printButton);
		buttonPanel.add(closeButton);

		add(buttonPanel, BorderLayout.SOUTH);
		
		
	}

    private void printPreview(SheetPreviewPanel previewPanel) {

        PrinterJob job = PrinterJob.getPrinterJob();

        job.setJobName("StageLayout Designer プレビュー");

        job.setPrintable((Graphics graphics, PageFormat pageFormat, int pageIndex) -> {

            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            BufferedImage image = previewPanel.createExportImage();

            Graphics2D g2 = (Graphics2D) graphics.create();

            // PNG保存と同じ画像を、用紙の印刷可能範囲に収まるよう縮小して印刷する。
            double scaleX = pageFormat.getImageableWidth() / image.getWidth();
            double scaleY = pageFormat.getImageableHeight() / image.getHeight();
            double scale = Math.min(scaleX, scaleY);

            double x =
                    pageFormat.getImageableX()
                            + (pageFormat.getImageableWidth()
                            - image.getWidth() * scale) / 2.0;

            double y =
                    pageFormat.getImageableY()
                            + (pageFormat.getImageableHeight()
                            - image.getHeight() * scale) / 2.0;

            g2.translate(x, y);
            g2.scale(scale, scale);
            g2.drawImage(image, 0, 0, null);
            g2.dispose();

            return Printable.PAGE_EXISTS;
        });

        if (!job.printDialog()) {
            return;
        }

        try {

            job.print();

        } catch (PrinterException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "印刷に失敗しました。\n" + ex.getMessage());

            ex.printStackTrace();
        }
    }

	private void exportPreviewImage(
	        SheetPreviewPanel previewPanel,
	        ProjectInfo projectInfo) {

	    JFileChooser fileChooser = new JFileChooser();

	    fileChooser.setSelectedFile(
	            new File(createDefaultExportFileName(projectInfo)));

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

	private String createDefaultExportFileName(ProjectInfo projectInfo) {

	    String title = projectInfo == null ? "" : projectInfo.getTitle();
	    String date = projectInfo == null ? "" : projectInfo.getDate();

	    title = sanitizeFileName(title);
	    date = sanitizeFileName(date);

	    if (title.isBlank()) {
	        title = "stage-layout";
	    }

	    if (!date.isBlank()) {
	        return date + "_" + title + "_配置図.png";
	    }

	    return title + "_配置図.png";
	}

	private String sanitizeFileName(String value) {

	    if (value == null) {
	        return "";
	    }

	    return value.trim()
	            .replaceAll("[\\\\/:*?\"<>|]", "_")
	            .replaceAll("\\s+", "_");
	}
	

}
