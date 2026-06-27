package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import model.Equipment;
import model.LayoutItem;

public class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener {
	
	private EquipmentPanel equipmentPanel;
	
	private List<LayoutItem> items = new ArrayList<>();
	
	private LayoutItem selectedItem;
	
	private boolean dragging = false;
	
	//グリッド
	private final int GRID_SIZE = 25;
	
	
	public CanvasPanel(EquipmentPanel equipmentPanel) {
		
		this.equipmentPanel = equipmentPanel;
		
		setBackground(Color.WHITE);
		
		Equipment mic = new Equipment("マイク");

		items.add(new LayoutItem(mic,100,100));

		items.add(new LayoutItem(mic,250,150));

		items.add(new LayoutItem(mic,450,250));
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		//グリッド線の色
		g.setColor(new Color(220,220,220));
		
		 // 縦線
        for(int x = 0; x < getWidth(); x += GRID_SIZE) {
            g.drawLine(x,0,x,getHeight());
        }

        // 横線
        for(int y = 0; y < getHeight(); y += GRID_SIZE) {
            g.drawLine(0,y,getWidth(),y);
        }
     // 機材描画
        g.setColor(Color.BLACK);

        for(LayoutItem item : items){
        	
        		if(item == selectedItem) {
        			g.setColor(Color.RED);
        		}else {
        			g.setColor(Color.BLACK);
        		}

            g.fillOval(item.getX(), item.getY(), 20, 20);

            g.drawString(item.getEquipment().getName(),
                    item.getX()+25,
                    item.getY()+15);

        }
        
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		selectedItem = null;
		
		for(LayoutItem item : items) {
			
			int dx = e.getX() - item.getX();
			int dy = e.getY() - item.getY();
			
			if(dx * dx + dy * dy < 100) {
				
				selectedItem = item;
				
				dragging = true;
				
				repaint();
				
				return;
			}
 		}
		
		String name = equipmentPanel.getSelectedEquipment();
		
		Equipment equipment = new Equipment(name);
		
		LayoutItem newItem = new LayoutItem(
				equipment,
				e.getX(),
				e.getY());
		
		items.add(newItem);
		
		selectedItem = newItem;
		
		repaint();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		selectedItem = null;
		
		for(LayoutItem item : items) {
			
			int dx = e.getX() - item.getX();
			int dy = e.getY() - item.getY();
			
			if(dx * dx + dy * dy < 100) {
				
				selectedItem = item;
				
				dragging = true;
				
				repaint();
				
				return;
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		
		dragging = false;
		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	@Override
	public void mouseExited(MouseEvent e) {}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		
		if(selectedItem != null && dragging) {
			
			selectedItem.setX(e.getX());
			selectedItem.setY(e.getY());
			
			repaint();
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {}

}
