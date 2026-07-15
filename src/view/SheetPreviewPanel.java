package view;

import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import model.BackgroundMap;
import model.DrawLine;
import model.LayoutItem;
import model.ProjectInfo;
import model.RoomObject;
import model.RoomTemplate;
import model.TextBoxItem;
import util.BackgroundImageLoader;
import util.ImageLoader;

public class SheetPreviewPanel extends JPanel{

    public static final String PREVIEW_CONTENT = "配置物に合わせる";
    public static final String PREVIEW_STAGE = "ステージ範囲に合わせる";
    public static final String PREVIEW_VENUE = "会場テンプレートに合わせる";
    public static final String PREVIEW_BACKGROUND = "背景図面に合わせる";
    public static final String PREVIEW_SHEET = "作業シート全体";
	
	private ProjectInfo projectInfo;
	
	private List<LayoutItem> items;

	private List<RoomObject> customRoomObjects;

	private List<DrawLine> drawLines;

    private BackgroundMap backgroundMap;

    private List<TextBoxItem> textBoxes;
	
	private RoomTemplate roomTemplate;

    private int sheetWidth;

    private int sheetHeight;

    private boolean showNames;

    private String previewRangeMode = PREVIEW_CONTENT;
	
	private String orientation;
	
	private double previewScale = 0.85;

    private static final double DEFAULT_METERS_PER_GRID = 0.5;

    private static final int GRID_SIZE = 20;
	
	public SheetPreviewPanel(
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
		
		this.projectInfo = projectInfo;
		this.items = items;
		this.customRoomObjects = customRoomObjects;
		this.drawLines = drawLines;
        this.backgroundMap = backgroundMap;
        this.textBoxes = textBoxes;
		this.roomTemplate = roomTemplate;
        this.sheetWidth = sheetWidth;
        this.sheetHeight = sheetHeight;
        this.showNames = showNames;
		this.orientation = orientation;
		
		if (PreviewDialog.ORIENTATION_LANDSCAPE.equals(orientation)) {
		    setPreferredSize(
		            new Dimension(
		                    (int) (1200 * previewScale),
		                    (int) (850 * previewScale)));
		} else {
		    setPreferredSize(
		            new Dimension(
		                    (int) (850 * previewScale),
		                    (int) (1200 * previewScale)));
		}
	}

    public void setPreviewRangeMode(String previewRangeMode) {

        if (previewRangeMode == null || previewRangeMode.isBlank()) {
            this.previewRangeMode = PREVIEW_CONTENT;
        } else {
            this.previewRangeMode = previewRangeMode;
        }

        repaint();
    }
	
	@Override
	protected void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g.create();

        applyQualityHints(g2);
		
		g2.scale(previewScale, previewScale);
		
		int pageW = 1120;
	    int pageH = 780;

	    int scaledPanelW = (int) (getWidth() / previewScale);
	    int scaledPanelH = (int) (getHeight() / previewScale);

	    int pageX = Math.max(20, (scaledPanelW - pageW) / 2);
	    int pageY = 30;
		
        // プレビューは作業画面ではなく提出用の1ページとして描く。
        // ヘッダー、レイアウト図、必要機材一覧、メモ欄を固定の用紙上に配置する。
		g2.setColor(Color.WHITE);
		g2.fillRect(pageX, pageY, pageW, pageH);
		
		 // 用紙外枠
        g2.setColor(Color.BLACK);
        g2.drawRect(pageX, pageY, pageW, pageH);

        drawHeader(g2, pageX, pageY, pageW);

        drawLayoutArea(g2, pageX, pageY, pageW, pageH);

        drawEquipmentList(g2, pageX, pageY, pageW, pageH);

        drawNoteArea(g2, pageX, pageY, pageW, pageH);

        drawFooter(g2, pageX, pageY, pageW, pageH);
        
        g2.dispose();
    }

    public BufferedImage createExportImage() {

        int width = 1120;
        int height = 780;

        BufferedImage image =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = image.createGraphics();

        applyQualityHints(g2);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, width - 1, height - 1);

        drawHeader(g2, 0, 0, width);
        drawLayoutArea(g2, 0, 0, width, height);
        drawEquipmentList(g2, 0, 0, width, height);
        drawNoteArea(g2, 0, 0, width, height);
        drawFooter(g2, 0, 0, width, height);

        g2.dispose();

        return image;
    }

    private void drawHeader(
            Graphics g,
            int pageX,
            int pageY,
            int pageW) {

        g.setFont(new Font("SansSerif", Font.BOLD, 22));

        String title = safe(projectInfo == null ? "" : projectInfo.getTitle());

        if (title == null || title.isBlank()) {
            title = "Stage Layout";
        }

        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = pageX + (pageW - titleMetrics.stringWidth(title)) / 2;

        g.drawString(title, titleX, pageY + 40);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));

        g.drawString(
                "Date: " + safe(projectInfo == null ? "" : projectInfo.getDate()),
                pageX + 20,
                pageY + 30);

        g.drawString(
                "Venue: " + safe(projectInfo == null ? "" : projectInfo.getPlace()),
                pageX + 20,
                pageY + 50);

        g.drawString(
                "Planner: " + safe(projectInfo == null ? "" : projectInfo.getPlanner()),
                pageX + pageW - 250,
                pageY + 30);
    }

    private void drawLayoutArea(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 35;
        int y = pageY + 65;
        int w = pageW - 70;
        int h = 490;

        g.setColor(new Color(252, 252, 252));
        g.fillRect(x, y, w, h);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("レイアウト図", x + 10, y + 20);

        int layoutX = x + 1;
        int layoutY = y + 30;
        int layoutW = w - 2;
        int layoutH = h - 31;

        drawPreviewItems(g, layoutX, layoutY, layoutW, layoutH);
    }

    private void drawEquipmentList(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 60;
        int y = pageY + 570;
        int w = 460;
        int h = 135;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("必要機材一覧", x + 15, y + 25);

        Map<String, Integer> summary = createEquipmentSummary();

        if (summary.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.drawString("なし", x + 20, y + 50);
            return;
        }

        // 必要機材一覧は提出時の確認用なので「ほか○件」で省略しない。
        // 件数が多い場合は列数と文字サイズを調整して、できるだけ全件を同じ紙面に収める。
        int startY = y + 50;
        int usableHeight = h - 60;
        int fontSize = 12;
        int lineHeight = fontSize + 5;
        int columnCount = 1;
        int maxRows = Math.max(1, usableHeight / lineHeight + 1);

        for (int candidateColumns = 1; candidateColumns <= 3; candidateColumns++) {
            boolean fits = false;

            for (int candidateFontSize = 12; candidateFontSize >= 7; candidateFontSize--) {
                int candidateLineHeight = candidateFontSize + 5;
                int candidateRows = Math.max(1, usableHeight / candidateLineHeight + 1);

                if (summary.size() <= candidateRows * candidateColumns) {
                    columnCount = candidateColumns;
                    fontSize = candidateFontSize;
                    lineHeight = candidateLineHeight;
                    maxRows = candidateRows;
                    fits = true;
                    break;
                }
            }

            if (fits) {
                break;
            }
        }

        if (summary.size() > maxRows * columnCount) {
            columnCount = 3;
            fontSize = 7;
            lineHeight = 11;
            maxRows = Math.max(1, (int) Math.ceil(summary.size() / (double) columnCount));
        }

        int columnWidth = (w - 40) / columnCount;
        int index = 0;

        for (Map.Entry<String, Integer> entry : summary.entrySet()) {

            int column = index / maxRows;
            int row = index % maxRows;

            g.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
            drawFittedString(
                    g,
                    entry.getKey() + " × " + entry.getValue(),
                    x + 20 + column * columnWidth,
                    startY + row * lineHeight,
                    columnWidth - 12);

            index++;
        }
    }

    private void drawNoteArea(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 560;
        int y = pageY + 570;
        int w = 500;
        int h = 135;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("メモ", x + 15, y + 25);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));

        String note = projectInfo == null ? "" : projectInfo.getNote();

        if (note == null) {
            note = "";
        }

        drawWrappedLimitedText(
                g,
                note,
                x + 20,
                y + 50,
                w - 40,
                h - 60,
                20);
    }

    private void drawFooter(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 180;
        int y = pageY + pageH - 45;
        int w = pageW - 360;
        int h = 25;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));

        g.drawString(
                "Event: " + safe(projectInfo == null ? "" : projectInfo.getTitle()),
                x + 10,
                y + 17);

        g.drawString(
                "Date: " + safe(projectInfo == null ? "" : projectInfo.getDate()),
                x + 360,
                y + 17);

        g.drawString(
                "Venue: " + safe(projectInfo == null ? "" : projectInfo.getPlace()),
                x + 520,
                y + 17);
    }

    private Map<String, Integer> createEquipmentSummary() {

        Map<String, Integer> summary = new LinkedHashMap<>();

        if (items == null) {
            return summary;
        }

        for (LayoutItem item : items) {

            String name = item.getEquipment().getName();

            int quantity = item.getQuantity();

            summary.put(
                    name,
                    summary.getOrDefault(name, 0) + quantity);
        }

        return summary;
    }

    private void applyQualityHints(Graphics2D g2) {

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    
    private void drawGrid(
            Graphics g,
            int x,
            int y,
            int w,
            int h) {

        int gridSize = 25;

        g.setColor(new Color(230, 230, 230));

        for (int gridX = x; gridX <= x + w; gridX += gridSize) {
            g.drawLine(gridX, y, gridX, y + h);
        }

        for (int gridY = y; gridY <= y + h; gridY += gridSize) {
            g.drawLine(x, gridY, x + w, gridY);
        }

        g.setColor(Color.BLACK);
    }
    
   
    
    private void drawPreviewItems(
            Graphics g,
            int areaX,
            int areaY,
            int areaW,
            int areaH) {

        if ((items == null || items.isEmpty())
                && (customRoomObjects == null || customRoomObjects.isEmpty())
                && (drawLines == null || drawLines.isEmpty())
                && (textBoxes == null || textBoxes.isEmpty())
                && backgroundMap == null
                && roomTemplate == null) {
            return;
        }

        // 作業画面の広い余白をそのまま縮小すると見づらい。
        // 選択されたプレビュー範囲に合わせて、会場や配置物が読める大きさになるようにする。
        Rectangle bounds = calculatePreviewBounds();

        int minX = bounds.x;
        int minY = bounds.y;
        int contentW = bounds.width;
        int contentH = bounds.height;

        if (contentW <= 0 || contentH <= 0) {
            return;
        }

        int margin = 20;

        double scaleX =
                (double) (areaW - margin * 2) / contentW;

        double scaleY =
                (double) (areaH - margin * 2) / contentH;

        double scale = Math.min(scaleX, scaleY);

        if (scale > 2.0) {
            scale = 2.0;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        Shape oldClip = g2.getClip();

        g2.setClip(areaX, areaY, areaW, areaH);

        int contentDrawW = (int) (contentW * scale);
        int contentDrawH = (int) (contentH * scale);

        int offsetX =
                areaX + (areaW - contentDrawW) / 2 - (int) (minX * scale);

        int offsetY =
                areaY + (areaH - contentDrawH) / 2 - (int) (minY * scale);

        drawBackgroundMap(
                g2,
                scale,
                offsetX,
                offsetY);

        drawGrid(g2, areaX, areaY, areaW, areaH);

        drawRoomTemplate(
                g2,
                areaX,
                areaY,
                areaW,
                areaH,
                scale,
                offsetX,
                offsetY);

        drawCustomRoomObjects(
                g2,
                scale,
                offsetX,
                offsetY);

        drawPreviewLines(
                g2,
                scale,
                offsetX,
                offsetY);

        if (items != null) {

            for (LayoutItem item : items) {

                int drawX = offsetX + (int) (item.getX() * scale);
                int drawY = offsetY + (int) (item.getY() * scale);
                int drawW = Math.max(8, (int) (item.getWidth() * scale));
                int drawH = Math.max(8, (int) (item.getHeight() * scale));

                if (isBamiriItem(item)) {
                    drawPreviewBamiri(g2, item, drawX, drawY, drawW, drawH);

                    if (showNames && item.isShowLabel()) {
                        g2.setColor(Color.BLACK);
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                        g2.drawString(item.getDisplayName(), drawX + 4, drawY + drawH + 12);
                    }

                    continue;
                }

                double centerX = drawX + drawW / 2.0;
                double centerY = drawY + drawH / 2.0;

                Graphics2D itemG = (Graphics2D) g2.create();

                itemG.rotate(
                        Math.toRadians(item.getRotation()),
                        centerX,
                        centerY);

                Image image = item.getEquipment().getImage();

                if (image != null) {

                    itemG.drawImage(
                            image,
                            drawX,
                            drawY,
                            drawW,
                            drawH,
                            this);

                } else {

                    itemG.setColor(item.getEquipment().getColor());

                    itemG.fillRect(
                            drawX,
                            drawY,
                            drawW,
                            drawH);
                }

                if (image == null) {
                    itemG.setColor(Color.BLACK);

                    itemG.drawRect(
                            drawX,
                            drawY,
                            drawW,
                            drawH);
                }

                itemG.dispose();

                if (showNames && item.isShowLabel()) {
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

                    g2.drawString(
                            item.getDisplayName(),
                            drawX,
                            drawY + drawH + 12);
                }
            }
        }

        drawTextBoxes(
                g2,
                scale,
                offsetX,
                offsetY);

        g2.setClip(oldClip);
        g2.dispose();
    }

    private Rectangle calculatePreviewBounds() {

        // プレビュー範囲は用途で切り替える。
        // 会場全体を見たい時、配置物だけを大きく見たい時、背景図面を確認したい時を分ける。
        if (PREVIEW_SHEET.equals(previewRangeMode)) {
            int width = sheetWidth > 0 ? sheetWidth : 1;
            int height = sheetHeight > 0 ? sheetHeight : 1;
            return new Rectangle(0, 0, width, height);
        }

        if (PREVIEW_STAGE.equals(previewRangeMode)) {
            Rectangle stageBounds = calculateStageBounds();

            if (stageBounds != null) {
                return addPreviewMargin(stageBounds);
            }
        }

        if (PREVIEW_VENUE.equals(previewRangeMode)) {
            Rectangle venueBounds = calculateVenueBounds();

            if (venueBounds != null) {
                return addPreviewMargin(venueBounds);
            }
        }

        if (PREVIEW_BACKGROUND.equals(previewRangeMode)
                && backgroundMap != null
                && backgroundMap.isVisible()) {
            return addPreviewMargin(new Rectangle(
                    backgroundMap.getX(),
                    backgroundMap.getY(),
                    Math.max(1, backgroundMap.getWidth()),
                    Math.max(1, backgroundMap.getHeight())));
        }

        Rectangle contentBounds = calculateContentBounds();

        if (contentBounds != null) {
            return addPreviewMargin(contentBounds);
        }

        if (backgroundMap != null && backgroundMap.isVisible()) {
            return new Rectangle(
                    backgroundMap.getX(),
                    backgroundMap.getY(),
                    Math.max(1, backgroundMap.getWidth()),
                    Math.max(1, backgroundMap.getHeight()));
        }

        int width = sheetWidth > 0 ? sheetWidth : 1;
        int height = sheetHeight > 0 ? sheetHeight : 1;
        return new Rectangle(0, 0, width, height);
    }

    private Rectangle calculateContentBounds() {

        Rectangle bounds = null;

        if (items != null) {
            for (LayoutItem item : items) {
                bounds =
                        union(
                                bounds,
                                new Rectangle(
                                        item.getX(),
                                        item.getY(),
                                        item.getWidth(),
                                        item.getHeight()));
            }
        }

        if (drawLines != null) {
            for (DrawLine line : drawLines) {
                int minX = Math.min(line.getStartX(), line.getEndX());
                int minY = Math.min(line.getStartY(), line.getEndY());
                int width = Math.abs(line.getStartX() - line.getEndX());
                int height = Math.abs(line.getStartY() - line.getEndY());

                bounds =
                        union(
                                bounds,
                                new Rectangle(
                                        minX,
                                        minY,
                                        Math.max(1, width),
                                        Math.max(1, height)));
            }
        }

        if (textBoxes != null) {
            for (TextBoxItem textBox : textBoxes) {
                bounds =
                        union(
                                bounds,
                                new Rectangle(
                                        textBox.getX(),
                                        textBox.getY(),
                                        textBox.getWidth(),
                                        textBox.getHeight()));
            }
        }

        return bounds;
    }

    private Rectangle calculateVenueBounds() {

        Rectangle bounds = null;

        if (roomTemplate != null) {
            bounds = union(bounds, new Rectangle(0, 0, roomTemplate.getWidth(), roomTemplate.getHeight()));
        }

        if (customRoomObjects != null) {
            for (RoomObject object : customRoomObjects) {
                bounds =
                        union(
                                bounds,
                                new Rectangle(
                                        object.getX(),
                                        object.getY(),
                                        object.getWidth(),
                                        object.getHeight()));
            }
        }

        if (backgroundMap != null && backgroundMap.isVisible()) {
            bounds =
                    union(
                            bounds,
                            new Rectangle(
                                    backgroundMap.getX(),
                                    backgroundMap.getY(),
                                    backgroundMap.getWidth(),
                                    backgroundMap.getHeight()));
        }

        return bounds;
    }

    private Rectangle calculateStageBounds() {

        Rectangle bounds = null;

        if (roomTemplate != null) {
            for (RoomObject object : roomTemplate.getObjects()) {
                if (isStageObject(object)) {
                    bounds =
                            union(
                                    bounds,
                                    new Rectangle(
                                            object.getX(),
                                            object.getY(),
                                            object.getWidth(),
                                            object.getHeight()));
                }
            }
        }

        if (customRoomObjects != null) {
            for (RoomObject object : customRoomObjects) {
                if (isStageObject(object)) {
                    bounds =
                            union(
                                    bounds,
                                    new Rectangle(
                                            object.getX(),
                                            object.getY(),
                                            object.getWidth(),
                                            object.getHeight()));
                }
            }
        }

        Rectangle contentBounds = calculateContentBounds();

        if (contentBounds != null) {
            bounds = union(bounds, contentBounds);
        }

        return bounds;
    }

    private boolean hasStageBounds() {

        return calculateStageBounds() != null;
    }

    private boolean isStageObject(RoomObject object) {

        if (object == null || object.getName() == null) {
            return false;
        }

        return object.getName().contains("ステージ")
                || object.getName().contains("舞台");
    }

    private Rectangle union(Rectangle current, Rectangle next) {

        if (next == null) {
            return current;
        }

        if (current == null) {
            return new Rectangle(next);
        }

        return current.union(next);
    }

    private Rectangle addPreviewMargin(Rectangle bounds) {

        int margin = 30;

        return new Rectangle(
                bounds.x - margin,
                bounds.y - margin,
                bounds.width + margin * 2,
                bounds.height + margin * 2);
    }

    private String safe(String value) {

        return value == null ? "" : value;
    }

    private boolean isBamiriItem(LayoutItem item) {

        return item != null
                && item.getEquipment() != null
                && item.getEquipment().getName() != null
                && item.getEquipment().getName().startsWith("バミリ");
    }

    private void drawPreviewBamiri(
            Graphics2D g2,
            LayoutItem item,
            int x,
            int y,
            int w,
            int h) {

        String name = item.getEquipment().getName();
        Graphics2D markG = (Graphics2D) g2.create();

        double centerX = x + w / 2.0;
        double centerY = y + h / 2.0;
        markG.rotate(Math.toRadians(item.getRotation()), centerX, centerY);
        markG.setColor(item.getEquipment().getColor());
        markG.setStroke(
                new BasicStroke(
                        Math.max(2, Math.min(w, h) / 5f),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));

        if ("バミリ X".equals(name)) {

            markG.drawLine(x, y, x + w, y + h);
            markG.drawLine(x + w, y, x, y + h);

        } else if ("バミリ ＋".equals(name)) {

            markG.drawLine(x + w / 2, y, x + w / 2, y + h);
            markG.drawLine(x, y + h / 2, x + w, y + h / 2);

        } else if (name.endsWith(" L")) {

            int thickW = Math.max(3, w / 5);
            int thickH = Math.max(3, h / 5);
            markG.fillRect(x, y, thickW, h);
            markG.fillRect(x, y + h - thickH, w, thickH);

        } else if (name.endsWith(" T")) {

            int thickH = Math.max(3, h / 5);
            int thickW = Math.max(4, w / 5);
            markG.fillRect(x, y, w, thickH);
            markG.fillRect(x + w / 2 - Math.max(2, w / 10), y, thickW, h);

        } else {

            markG.fillRect(x, y, w, h);
        }

        markG.dispose();
    }

    private void drawFittedString(
            Graphics g,
            String text,
            int x,
            int y,
            int maxWidth) {

        if (text == null) {
            text = "";
        }

        FontMetrics metrics = g.getFontMetrics();

        if (metrics.stringWidth(text) <= maxWidth) {
            g.drawString(text, x, y);
            return;
        }

        String suffix = "...";
        int suffixWidth = metrics.stringWidth(suffix);

        while (!text.isEmpty()
                && metrics.stringWidth(text) + suffixWidth > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }

        g.drawString(text + suffix, x, y);
    }

    private void drawWrappedLimitedText(
            Graphics g,
            String text,
            int x,
            int y,
            int maxWidth,
            int maxHeight,
            int lineHeight) {

        List<String> wrappedLines = wrapText(g, text, maxWidth);
        int maxLines = Math.max(1, maxHeight / lineHeight);
        int linesToDraw = Math.min(wrappedLines.size(), maxLines);

        for (int i = 0; i < linesToDraw; i++) {

            String line = wrappedLines.get(i);
            boolean overflow = wrappedLines.size() > maxLines
                    && i == linesToDraw - 1;

            if (overflow) {
                drawFittedString(g, line + " ...", x, y + lineHeight * i, maxWidth);
            } else {
                g.drawString(line, x, y + lineHeight * i);
            }
        }
    }

    private List<String> wrapText(
            Graphics g,
            String text,
            int maxWidth) {

        List<String> lines = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return lines;
        }

        FontMetrics metrics = g.getFontMetrics();
        String[] paragraphs = text.replace("\r\n", "\n").split("\n");

        for (String paragraph : paragraphs) {

            if (paragraph.isBlank()) {
                lines.add("");
                continue;
            }

            StringBuilder currentLine = new StringBuilder();

            for (int i = 0; i < paragraph.length(); i++) {

                String nextChar = paragraph.substring(i, i + 1);
                String candidate = currentLine.toString() + nextChar;

                if (metrics.stringWidth(candidate) > maxWidth
                        && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }

                currentLine.append(nextChar);
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    private void drawBackgroundMap(
            Graphics2D g2,
            double scale,
            int offsetX,
            int offsetY) {

        if (backgroundMap == null
                || !backgroundMap.isVisible()
                || backgroundMap.getImagePath() == null
                || backgroundMap.getImagePath().isBlank()) {
            return;
        }

        try {
            BufferedImage image =
                    BackgroundImageLoader.load(new File(backgroundMap.getImagePath()));

            if (image == null) {
                return;
            }

            Graphics2D imageG = (Graphics2D) g2.create();
            imageG.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            backgroundMap.getOpacity()));

            int x = offsetX + (int) (backgroundMap.getX() * scale);
            int y = offsetY + (int) (backgroundMap.getY() * scale);
            int w = Math.max(1, (int) (backgroundMap.getWidth() * scale));
            int h = Math.max(1, (int) (backgroundMap.getHeight() * scale));

            imageG.drawImage(image, x, y, w, h, this);
            imageG.dispose();

        } catch (Exception ex) {
            // Preview stays usable even if the referenced background image moved.
        }
    }

    private void drawTextBoxes(
            Graphics2D g2,
            double scale,
            int offsetX,
            int offsetY) {

        if (textBoxes == null) {
            return;
        }

        for (TextBoxItem textBox : textBoxes) {
            int x = offsetX + (int) (textBox.getX() * scale);
            int y = offsetY + (int) (textBox.getY() * scale);
            int w = Math.max(8, (int) (textBox.getWidth() * scale));
            int h = Math.max(8, (int) (textBox.getHeight() * scale));

            // 提出用プレビューでは注釈として読みやすくするため、枠や背景は描かず文字だけ表示する。
            g2.setColor(textBox.getTextColor());
            g2.setFont(
                    new Font(
                            "SansSerif",
                            Font.PLAIN,
                            Math.max(8, (int) (textBox.getFontSize() * scale))));

            FontMetrics metrics = g2.getFontMetrics();
            int textY = y + metrics.getAscent() + 4;

            for (String line : wrapText(g2, textBox.getText(), w - 8)) {
                if (textY > y + h - 4) {
                    break;
                }

                drawFittedString(g2, line, x + 4, textY, w - 8);
                textY += metrics.getHeight();
            }
        }
    }

    private void drawCustomRoomObjects(
            Graphics2D g2,
            double scale,
            int offsetX,
            int offsetY) {

        if (customRoomObjects == null) {
            return;
        }

        for (RoomObject object : customRoomObjects) {

            int x = offsetX + (int) (object.getX() * scale);
            int y = offsetY + (int) (object.getY() * scale);
            int w = Math.max(4, (int) (object.getWidth() * scale));
            int h = Math.max(4, (int) (object.getHeight() * scale));

            if (RoomObject.TYPE_IMAGE.equals(object.getType())) {

                Image image = ImageLoader.load(object.getImagePath());

                if (image != null) {
                    g2.drawImage(image, x, y, w, h, this);
                }

            } else if (RoomObject.TYPE_CIRCLE.equals(object.getType())) {

                g2.setColor(new Color(130, 136, 142));
                g2.fillOval(x, y, w, h);

                g2.setColor(new Color(150, 158, 166));
                g2.drawOval(x, y, w, h);

            } else if (isShapeRoomObject(object)) {

                Shape shape = createPreviewRoomObjectShape(object, x, y, w, h);

                g2.setColor(new Color(226, 234, 240));
                g2.fill(shape);

                g2.setColor(new Color(150, 158, 166));
                g2.draw(shape);

            } else {

                g2.setColor(new Color(232, 235, 238));
                g2.fillRect(x, y, w, h);

                g2.setColor(new Color(150, 158, 166));
                g2.drawRect(x, y, w, h);
            }

            if (!RoomObject.TYPE_IMAGE.equals(object.getType())
                    && object.getName() != null
                    && !object.getName().isBlank()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(Color.BLACK);
                g2.drawString(object.getName(), x + 4, y + 14);
            }
        }
    }

    private boolean isShapeRoomObject(RoomObject object) {

        if (object == null) {
            return false;
        }

        String type = object.getType();

        return RoomObject.TYPE_OVAL.equals(type)
                || RoomObject.TYPE_TRACK.equals(type)
                || RoomObject.TYPE_FRONT_ARC.equals(type)
                || RoomObject.TYPE_ROUNDED_RECT.equals(type);
    }

    private Shape createPreviewRoomObjectShape(
            RoomObject object,
            int x,
            int y,
            int w,
            int h) {

        if (RoomObject.TYPE_OVAL.equals(object.getType())) {
            return new Ellipse2D.Double(x, y, w, h);
        }

        if (RoomObject.TYPE_TRACK.equals(object.getType())) {
            return new RoundRectangle2D.Double(x, y, w, h, h, h);
        }

        if (RoomObject.TYPE_FRONT_ARC.equals(object.getType())) {
            Path2D path = new Path2D.Double();
            double arcHeight = Math.max(6, h * 0.35);

            path.moveTo(x, y);
            path.lineTo(x + w, y);
            path.lineTo(x + w, y + h - arcHeight);
            path.quadTo(x + w / 2.0, y + h, x, y + h - arcHeight);
            path.closePath();

            return path;
        }

        return new RoundRectangle2D.Double(
                x,
                y,
                w,
                h,
                Math.min(w, h) / 3.0,
                Math.min(w, h) / 3.0);
    }

    private void drawPreviewLines(
            Graphics2D g2,
            double scale,
            int offsetX,
            int offsetY) {

        if (drawLines == null) {
            return;
        }

        for (DrawLine line : drawLines) {

            int x1 = offsetX + (int) (line.getStartX() * scale);
            int y1 = offsetY + (int) (line.getStartY() * scale);
            int x2 = offsetX + (int) (line.getEndX() * scale);
            int y2 = offsetY + (int) (line.getEndY() * scale);

            g2.setColor(lineColorForType(line));
            g2.setStroke(createStrokeForLine(line, scale));

            g2.drawLine(x1, y1, x2, y2);
            drawFlowArrowHeadIfNeeded(g2, line, x1, y1, x2, y2, scale);

            if (line.isShowLength() && !isBamiriLine(line)) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(Color.BLACK);
                g2.drawString(
                        formatLineLength(line),
                        (x1 + x2) / 2 + 4,
                        (y1 + y2) / 2 - 4);
            }

            if (showNames
                    && line.isShowLabel()
                    && line.getLabel() != null
                    && !line.getLabel().isBlank()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(Color.BLACK);
                g2.drawString(
                        line.getLabel(),
                        (x1 + x2) / 2 + 4,
                        (y1 + y2) / 2 - 18);
            }
        }

        g2.setStroke(new BasicStroke(1));
    }

    private Color lineColorForType(DrawLine line) {

        if (line == null) {
            return Color.BLACK;
        }

        if (DrawLine.TYPE_CABLE.equals(line.getLineType())) {
            return Color.BLACK;
        }

        if (DrawLine.TYPE_FLOW.equals(line.getLineType())) {
            return new Color(40, 110, 210);
        }

        return line.getColor();
    }

    private BasicStroke createStrokeForLine(DrawLine line, double scale) {

        float width =
                Math.max(
                        1f,
                        (float) ((line == null ? 2 : line.getStrokeWidth()) * scale));

        return new BasicStroke(width);
    }

    private void drawFlowArrowHeadIfNeeded(
            Graphics2D g2,
            DrawLine line,
            int x1,
            int y1,
            int x2,
            int y2,
            double scale) {

        if (line == null || !DrawLine.TYPE_FLOW.equals(line.getLineType())) {
            return;
        }

        int size =
                Math.max(
                        8,
                        (int) Math.round(
                                Math.max(10, line.getStrokeWidth() * 3) * scale));

        double angle = Math.atan2(y2 - y1, x2 - x1);

        if (Double.isNaN(angle)) {
            return;
        }

        int leftX = (int) Math.round(x2 - size * Math.cos(angle - Math.PI / 6));
        int leftY = (int) Math.round(y2 - size * Math.sin(angle - Math.PI / 6));
        int rightX = (int) Math.round(x2 - size * Math.cos(angle + Math.PI / 6));
        int rightY = (int) Math.round(y2 - size * Math.sin(angle + Math.PI / 6));

        Color oldColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();

        g2.setColor(new Color(40, 110, 210));
        g2.setStroke(
                new BasicStroke(
                        Math.max(1f, (float) (line.getStrokeWidth() * scale)),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));

        g2.drawLine(x2, y2, leftX, leftY);
        g2.drawLine(x2, y2, rightX, rightY);

        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    private boolean isBamiriLine(DrawLine line) {

        if (line == null) {
            return false;
        }

        if (DrawLine.TYPE_BAMIRI.equals(line.getLineType())) {
            return true;
        }

        return line.getLabel() != null && line.getLabel().contains("繝舌Α繝ｪ");
    }

    private String formatLineLength(DrawLine line) {

        double dx = line.getEndX() - line.getStartX();
        double dy = line.getEndY() - line.getStartY();
        double gridCount = Math.sqrt(dx * dx + dy * dy) / GRID_SIZE;
        double meters = gridCount * DEFAULT_METERS_PER_GRID;

        if (meters >= 10) {
            return String.format("%.1fm", meters);
        }

        return String.format("%.2fm", meters);
    }

    private void drawRoomTemplate(
            Graphics g,
            int areaX,
            int areaY,
            int areaW,
            int areaH,
            double scale,
            int offsetX,
            int offsetY) {

        if (roomTemplate == null) {
            return;
        }

        g.setColor(new Color(245, 245, 245));

        int roomX = offsetX;
        int roomY = offsetY;
        int roomW = (int) (roomTemplate.getWidth() * scale);
        int roomH = (int) (roomTemplate.getHeight() * scale);

        g.fillRect(roomX, roomY, roomW, roomH);

        g.setColor(Color.GRAY);
        g.drawRect(roomX, roomY, roomW, roomH);

        g.setFont(new Font("SansSerif", Font.PLAIN, 10));

        for (RoomObject object : roomTemplate.getObjects()) {

            int x = offsetX + (int) (object.getX() * scale);
            int y = offsetY + (int) (object.getY() * scale);
            int w = Math.max(4, (int) (object.getWidth() * scale));
            int h = Math.max(4, (int) (object.getHeight() * scale));

            g.setColor(Color.GRAY);

            g.drawRect(
                    x,
                    y,
                    w,
                    h);

            if (shouldDrawTemplateObjectName(g, object, w, h)) {
                g.setColor(Color.DARK_GRAY);

                drawFittedString(
                        g,
                        object.getName(),
                        x + 3,
                        y + Math.min(14, h - 3),
                        w - 6);
            }
        }

        g.setColor(Color.BLACK);
    }

    private boolean shouldDrawTemplateObjectName(
            Graphics g,
            RoomObject object,
            int width,
            int height) {

        if (object.getName() == null || object.getName().isBlank()) {
            return false;
        }

        if (width < 45 || height < 18) {
            return false;
        }

        return g.getFontMetrics().stringWidth(object.getName()) <= width - 6;
    }
}
