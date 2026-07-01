package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import model.LayoutItem;
import model.ProjectInfo;

public class SheetPreviewPanel extends JPanel{
	
	private ProjectInfo projectInfo;
	
	private List<LayoutItem> items;
	
	public SheetPreviewPanel(
			ProjectInfo projectInfo,
			List<LayoutItem> items) {
		
		this.projectInfo = projectInfo;
		this.items = items;
		
		setPreferredSize(new Dimension(1200, 850));
		
		setBackground(Color.DARK_GRAY);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		int pageX = 40;
		int pageY = 30;
		int pageW = 1120;
		int pageH = 780;
		
		//用紙背景
		g.setColor(Color.WHITE);
		g.fillRect(pageX, pageY, pageW, pageH);
		
		 // 用紙外枠
        g.setColor(Color.BLACK);
        g.drawRect(pageX, pageY, pageW, pageH);

        drawHeader(g, pageX, pageY, pageW);

        drawLayoutArea(g, pageX, pageY, pageW, pageH);

        drawEquipmentList(g, pageX, pageY, pageW, pageH);

        drawNoteArea(g, pageX, pageY, pageW, pageH);

        drawFooter(g, pageX, pageY, pageW, pageH);
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

        g.drawString(title, pageX + 420, pageY + 40);

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

        if (items == null || items.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        double scale = 0.45;

        int offsetX = areaX + 40;
        int offsetY = areaY + 40;

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

        g2.dispose();
    }
}
