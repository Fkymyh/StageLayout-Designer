package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import model.Equipment;
import model.EquipmentFactory;
import model.LayoutItem;

public class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener {
	
	private EquipmentPanel equipmentPanel;
	
	private List<LayoutItem> items = new ArrayList<>();
	
	private LayoutItem selectedItem;
	
	private boolean dragging = false;
	
	private PropertyPanel propertyPanel;	
	
	private final int GRID_SIZE = 25;
	
	private void showPopupMenu(MouseEvent e){

	    JPopupMenu menu = new JPopupMenu();

	    JMenuItem deleteItem = new JMenuItem("削除");

	    deleteItem.addActionListener(event -> {

	        if(selectedItem != null){

	            items.remove(selectedItem);

	            selectedItem = null;

	            repaint();

	        }

	    });

	    menu.add(deleteItem);

	    menu.show(this,e.getX(),e.getY());

	}
	
	//グリッド
	public CanvasPanel(EquipmentPanel equipmentPanel,
						PropertyPanel propertyPanel) {
		
		this.equipmentPanel = equipmentPanel;
		this.propertyPanel = propertyPanel;
		
		setBackground(Color.WHITE);
		
		Equipment mic = EquipmentFactory.create("マイク");

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
        			g.setColor(item.getEquipment().getColor());
        		}
        		//四角を描く
        		Equipment equipment = item.getEquipment();

        		g.fillRect(
        		        item.getX(),
        		        item.getY(),
        		        equipment.getWidth(),
        		        equipment.getHeight());
            
            //枠線を描く
            g.setColor(Color.BLACK);
            g.drawRect(
            	    item.getX(),
            	    item.getY(),
            	    equipment.getWidth(),
            	    equipment.getHeight());
            //文字
            g.drawString(
            	    item.getEquipment().getName(),
            	    item.getX() + 8,
            	    item.getY() + 20);

        }
        
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		selectedItem = findItem(e.getX(), e.getY());
		
		propertyPanel.displayItem(selectedItem);
		
		if(selectedItem != null) {
			
			repaint();
			
			return;
		}
		
		String name = equipmentPanel.getSelectedEquipment();
		
		
		Equipment equipment = EquipmentFactory.create(name);
		
		LayoutItem newItem = new LayoutItem(
				equipment,
				e.getX(),
				e.getY());
		
		items.add(newItem);
		
		selectedItem = newItem;
		
		propertyPanel.displayItem(selectedItem);
		
		repaint();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		if (e.isPopupTrigger()) {
			showPopupMenu(e);
			return;
		}
		
		selectedItem = findItem(e.getX(), e.getY());
		
		propertyPanel.displayItem(selectedItem);
		
		if(selectedItem != null) {
			dragging = true;
		}
		
		repaint();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		
		if (e.isPopupTrigger()) {
			showPopupMenu(e);
			return;
		}
		
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
	
	private LayoutItem findItem(int x, int y) {
		
		for (LayoutItem item : items) {
			
			Equipment equipment = item.getEquipment();
			
			Rectangle rect = new Rectangle(
					item.getX(),
					item.getY(),
					equipment.getWidth(),
					equipment.getHeight());
			
			if(rect.contains(x, y)) {
				return item;
				
			}
		}
		
		return null;
	}

}
