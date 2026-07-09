package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import model.DrawLine;
import model.LayoutItem;
import model.ProjectInfo;
import model.RoomObject;
import model.RoomTemplate;
import util.ImageLoader;

public class SheetPreviewPanel extends JPanel{
	
	private ProjectInfo projectInfo;
	
	private List<LayoutItem> items;

	private List<RoomObject> customRoomObjects;

	private List<DrawLine> drawLines;
	
	private RoomTemplate roomTemplate;
	
	private String orientation;
	
	private double previewScale = 0.85;
	
	public SheetPreviewPanel(
			ProjectInfo projectInfo,
			List<LayoutItem> items,
			List<RoomObject> customRoomObjects,
			List<DrawLine> drawLines,
			RoomTemplate roomTemplate,
			String orientation) {
		
		this.projectInfo = projectInfo;
		this.items = items;
		this.customRoomObjects = customRoomObjects;
		this.drawLines = drawLines;
		this.roomTemplate = roomTemplate;
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
		
		//用紙背景
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

        int x = pageX + 50;
        int y = pageY + 70;
        int w = pageW - 100;
        int h = 470;

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

        drawGrid(g, layoutX, layoutY, layoutW, layoutH);

        drawPreviewItems(g, layoutX, layoutY, layoutW, layoutH);
    }

    private void drawEquipmentList(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 60;
        int y = pageY + 560;
        int w = 460;
        int h = 140;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("必要機材一覧", x + 15, y + 25);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));

        Map<String, Integer> summary = createEquipmentSummary();

        int textY = y + 50;
        int lineHeight = 22;
        int maxLines = Math.max(1, (y + h - 15 - textY) / lineHeight + 1);
        int drawnCount = 0;
        int totalCount = summary.size();

        for (Map.Entry<String, Integer> entry : summary.entrySet()) {

            if (drawnCount >= maxLines) {
                break;
            }

            boolean shouldReserveOverflowLine =
                    totalCount > maxLines && drawnCount == maxLines - 1;

            if (shouldReserveOverflowLine) {
                int remainingCount = totalCount - drawnCount;

                drawFittedString(
                        g,
                        "ほか " + remainingCount + " 件",
                        x + 20,
                        textY,
                        w - 40);

                break;
            }

            drawFittedString(
                    g,
                    entry.getKey() + " × " + entry.getValue(),
                    x + 20,
                    textY,
                    w - 40);

            textY += lineHeight;
            drawnCount++;
        }
    }

    private void drawNoteArea(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 560;
        int y = pageY + 560;
        int w = 500;
        int h = 140;

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
                && roomTemplate == null) {
            return;
        }

        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;

        if (roomTemplate != null) {

            maxX = roomTemplate.getWidth();
            maxY = roomTemplate.getHeight();

        } else {

            minX = Integer.MAX_VALUE;
            minY = Integer.MAX_VALUE;
            maxX = Integer.MIN_VALUE;
            maxY = Integer.MIN_VALUE;

            if (items != null) {

                for (LayoutItem item : items) {

                    minX = Math.min(minX, item.getX());
                    minY = Math.min(minY, item.getY());

                    maxX = Math.max(
                            maxX,
                            item.getX() + item.getWidth());

                    maxY = Math.max(
                            maxY,
                            item.getY() + item.getHeight());
                }
            }

            if (customRoomObjects != null) {

                for (RoomObject object : customRoomObjects) {

                    minX = Math.min(minX, object.getX());
                    minY = Math.min(minY, object.getY());

                    maxX = Math.max(
                            maxX,
                            object.getX() + object.getWidth());

                    maxY = Math.max(
                            maxY,
                            object.getY() + object.getHeight());
                }
            }

            if (drawLines != null) {

                for (DrawLine line : drawLines) {

                    minX = Math.min(
                            minX,
                            Math.min(line.getStartX(), line.getEndX()));

                    minY = Math.min(
                            minY,
                            Math.min(line.getStartY(), line.getEndY()));

                    maxX = Math.max(
                            maxX,
                            Math.max(line.getStartX(), line.getEndX()));

                    maxY = Math.max(
                            maxY,
                            Math.max(line.getStartY(), line.getEndY()));
                }
            }
        }

        if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE) {
            minX = 0;
            minY = 0;
        }

        int contentW = maxX - minX;
        int contentH = maxY - minY;

        if (contentW <= 0 || contentH <= 0) {
            return;
        }

        int margin = 40;

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

                itemG.setColor(Color.BLACK);

                itemG.drawRect(
                        drawX,
                        drawY,
                        drawW,
                        drawH);

                itemG.dispose();

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

                g2.drawString(
                        item.getEquipment().getName(),
                        drawX,
                        drawY + drawH + 12);
            }
        }

        g2.setClip(oldClip);
        g2.dispose();
    }

    private String safe(String value) {

        return value == null ? "" : value;
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

                g2.setColor(Color.BLACK);
                g2.drawOval(x, y, w, h);

            } else {

                g2.setColor(new Color(232, 235, 238));
                g2.fillRect(x, y, w, h);

                g2.setColor(Color.BLACK);
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

            g2.setColor(line.getColor());
            g2.setStroke(
                    new BasicStroke(
                            Math.max(1, (float) (line.getStrokeWidth() * scale))));

            g2.drawLine(x1, y1, x2, y2);

            if (line.getLabel() != null && !line.getLabel().isBlank()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(Color.BLACK);
                g2.drawString(
                        line.getLabel(),
                        (x1 + x2) / 2 + 4,
                        (y1 + y2) / 2 - 4);
            }
        }

        g2.setStroke(new BasicStroke(1));
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

            g.setColor(Color.DARK_GRAY);

            g.drawString(
                    object.getName(),
                    x + 3,
                    y + 12);
        }

        g.setColor(Color.BLACK);
    }
}
