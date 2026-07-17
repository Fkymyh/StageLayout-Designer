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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import model.DrawLine;
import model.BackgroundMap;
import model.LayoutItem;
import model.ProjectInfo;
import model.RoomObject;
import model.RoomTemplate;
import model.TextBoxItem;

public class PreviewDialog extends JDialog{
	
	public static final String ORIENTATION_PORTRAIT = "PORTRAIT";
	public static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

	
	public PreviewDialog(
			MainFrame owner,
			ProjectInfo projectInfo,
			List<LayoutItem> items,
			List<RoomObject> customRoomObjects,
			List<DrawLine> drawLines,
            BackgroundMap backgroundMap,
            List<TextBoxItem> textBoxes,
			RoomTemplate roomTemplate,
            int sheetWidth,
            int sheetHeight,
            boolean showNames,
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
                        backgroundMap,
                        textBoxes,
						roomTemplate,
                        sheetWidth,
                        sheetHeight,
                        showNames,
						orientation);
		
		JScrollPane scrollPane =
				new JScrollPane(previewPanel);
		
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

        JComboBox<String> previewRangeComboBox =
                new JComboBox<>(
                        new String[] {
                                SheetPreviewPanel.PREVIEW_STAGE,
                                SheetPreviewPanel.PREVIEW_VENUE,
                                SheetPreviewPanel.PREVIEW_CONTENT,
                                SheetPreviewPanel.PREVIEW_BACKGROUND,
                                SheetPreviewPanel.PREVIEW_SHEET
                        });

        previewRangeComboBox.setSelectedItem(initialPreviewMode(backgroundMap, roomTemplate, customRoomObjects));
        previewPanel.setPreviewRangeMode((String) previewRangeComboBox.getSelectedItem());
        previewRangeComboBox.addActionListener(e -> {
            String selectedMode = (String) previewRangeComboBox.getSelectedItem();
            previewPanel.setPreviewRangeMode(selectedMode);

            if (backgroundMap != null) {
                backgroundMap.setPreviewMode(selectedMode);
            }
        });

        JCheckBox lineLengthCheckBox = new JCheckBox("線の長さを表示");
        lineLengthCheckBox.setSelected(false);
        previewPanel.setShowLineLength(false);
        lineLengthCheckBox.addActionListener(e ->
                previewPanel.setShowLineLength(lineLengthCheckBox.isSelected()));

		JButton exportButton = new JButton("PNG画像として保存");
        JButton exportPdfButton = new JButton("PDFとして保存");
        JButton printButton = new JButton("印刷");
		JButton closeButton = new JButton("閉じる");

		exportButton.addActionListener(e -> exportPreviewImage(previewPanel, projectInfo));
        exportPdfButton.addActionListener(e -> exportPreviewPdf(previewPanel, projectInfo));
        printButton.addActionListener(e -> printPreview(previewPanel));
		closeButton.addActionListener(e -> dispose());

        buttonPanel.add(new JLabel("プレビュー範囲"));
        buttonPanel.add(previewRangeComboBox);
        buttonPanel.add(lineLengthCheckBox);
		buttonPanel.add(exportButton);
        buttonPanel.add(exportPdfButton);
        buttonPanel.add(printButton);
		buttonPanel.add(closeButton);

		add(buttonPanel, BorderLayout.SOUTH);
		
		
	}

    private String initialPreviewMode(
            BackgroundMap backgroundMap,
            RoomTemplate roomTemplate,
            List<RoomObject> customRoomObjects) {

        if (backgroundMap != null && !backgroundMap.getPreviewMode().isBlank()) {
            return backgroundMap.getPreviewMode();
        }

        if (hasStageObject(roomTemplate, customRoomObjects)) {
            return SheetPreviewPanel.PREVIEW_STAGE;
        }

        if (roomTemplate != null
                || (customRoomObjects != null && !customRoomObjects.isEmpty())) {
            return SheetPreviewPanel.PREVIEW_VENUE;
        }

        if (backgroundMap != null && backgroundMap.isVisible()) {
            return SheetPreviewPanel.PREVIEW_BACKGROUND;
        }

        return SheetPreviewPanel.PREVIEW_SHEET;
    }

    private boolean hasStageObject(
            RoomTemplate roomTemplate,
            List<RoomObject> customRoomObjects) {

        if (roomTemplate != null) {
            for (RoomObject object : roomTemplate.getObjects()) {
                if (isStageObject(object)) {
                    return true;
                }
            }
        }

        if (customRoomObjects != null) {
            for (RoomObject object : customRoomObjects) {
                if (isStageObject(object)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isStageObject(RoomObject object) {

        if (object == null || object.getName() == null) {
            return false;
        }

        return object.getName().contains("ステージ")
                || object.getName().contains("舞台");
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

        // PNG保存はプレビュー画面と同じ描画を画像化する。
        // ファイル名はイベント名や日付から作り、提出用ファイルとして探しやすくする。
	    JFileChooser fileChooser = new JFileChooser();

	    fileChooser.setSelectedFile(
	            new File(createDefaultExportFileName(projectInfo, ".png")));

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

    private void exportPreviewPdf(
            SheetPreviewPanel previewPanel,
            ProjectInfo projectInfo) {

        // PDF保存もプレビュー画像を1ページPDFに貼り付ける方式にしている。
        // 画面表示、PNG保存、PDF保存で見た目がずれないようにするため。
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setSelectedFile(
                new File(createDefaultExportFileName(projectInfo, ".pdf")));

        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }

        try {

            BufferedImage image = previewPanel.createExportImage();

            try (PDDocument document = new PDDocument()) {

                PDPage page =
                        new PDPage(
                                new PDRectangle(
                                        image.getWidth(),
                                        image.getHeight()));

                document.addPage(page);

                PDImageXObject pdfImage =
                        LosslessFactory.createFromImage(document, image);

                try (PDPageContentStream contentStream =
                        new PDPageContentStream(document, page)) {

                    contentStream.drawImage(
                            pdfImage,
                            0,
                            0,
                            image.getWidth(),
                            image.getHeight());
                }

                document.save(file);
            }

            JOptionPane.showMessageDialog(
                    this,
                    "PDFを保存しました。\n" + file.getAbsolutePath());

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "PDFの保存に失敗しました。\n" + ex.getMessage());

            ex.printStackTrace();
        }
    }

	private String createDefaultExportFileName(ProjectInfo projectInfo, String extension) {

	    String title = projectInfo == null ? "" : projectInfo.getTitle();
	    String date = projectInfo == null ? "" : projectInfo.getDate();

	    title = sanitizeFileName(title);
	    date = sanitizeFileName(date);

	    if (title.isBlank()) {
	        title = "stage-layout";
	    }

	    if (!date.isBlank()) {
	        return date + "_" + title + "_配置図" + extension;
	    }

	    return title + "_配置図" + extension;
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
