package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
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

        String title = projectInfo.getTitle();

        if (title == null || title.isBlank()) {
            title = "Stage Layout";
        }

        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = pageX + (pageW - titleMetrics.stringWidth(title)) / 2;

        g.drawString(title, titleX, pageY + 40);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));

        g.drawString(
                "Date: " + projectInfo.getDate(),
                pageX + 20,
                pageY + 30);

        g.drawString(
                "Venue: " + projectInfo.getPlace(),
                pageX + 20,
                pageY + 50);

        g.drawString(
                "Planner: " + projectInfo.getPlanner(),
                pageX + pageW - 250,
                pageY + 30);
    }

    private void drawLayoutArea(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 60;
        int y = pageY + 80;
        int w = pageW - 120;
        int h = 420;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("レイアウト図", x + 10, y + 20);

        drawGrid(g, x, y, w, h);

        drawPreviewItems(g, x, y, w, h);
    
        // 次の段階でここに機材配置を縮小描画する
    }

    private void drawEquipmentList(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 60;
        int y = pageY + 530;
        int w = 460;
        int h = 190;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("必要機材一覧", x + 15, y + 25);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));

        Map<String, Integer> summary = createEquipmentSummary();

        int textY = y + 50;

        for (Map.Entry<String, Integer> entry : summary.entrySet()) {

            g.drawString(
                    entry.getKey() + " × " + entry.getValue(),
                    x + 20,
                    textY);

            textY += 22;

            if (textY > y + h - 15) {
                break;
            }
        }
    }

    private void drawNoteArea(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 560;
        int y = pageY + 530;
        int w = 500;
        int h = 190;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Notes", x + 15, y + 25);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));

        String note = projectInfo.getNote();

        if (note == null) {
            note = "";
        }

        String[] lines = note.split("\n");

        int textY = y + 50;

        for (String line : lines) {

            g.drawString(line, x + 20, textY);

            textY += 22;

            if (textY > y + h - 15) {
                break;
            }
        }
    }

    private void drawFooter(
            Graphics g,
            int pageX,
            int pageY,
            int pageW,
            int pageH) {

        int x = pageX + 180;
        int y = pageY + pageH - 60;
        int w = pageW - 360;
        int h = 35;

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));

        g.drawString(
                "Title: " + projectInfo.getTitle(),
                x + 10,
                y + 23);

        g.drawString(
                "Date: " + projectInfo.getDate(),
                x + 360,
                y + 23);

        g.drawString(
                "Place: " + projectInfo.getPlace(),
                x + 520,
                y + 23);
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

        if (scale > 1.0) {
            scale = 1.0;
        }

        Graphics2D g2 = (Graphics2D) g.create();

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

        g2.dispose();
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
