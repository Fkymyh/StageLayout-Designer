package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
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

        // 今回はまだ枠だけ
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
}
