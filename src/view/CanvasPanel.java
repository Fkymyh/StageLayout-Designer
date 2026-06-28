package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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



public class CanvasPanel extends JPanel implements MouseListener, 
												  MouseMotionListener,
												  KeyListener {
	
	private EquipmentPanel equipmentPanel;
	
	private List<LayoutItem> items = new ArrayList<>();
	
	private LayoutItem selectedItem;
	
	private LayoutItem copiedItem;
	
	private boolean dragging = false;
	
	private PropertyPanel propertyPanel;	
	
	private final int GRID_SIZE = 25;
	
	private int dragOffsetX;
	
	private int dragOffsetY;
	
	private void showPopupMenu(MouseEvent e){

	    JPopupMenu menu = new JPopupMenu();

	    JMenuItem deleteItem = new JMenuItem("削除");

	    deleteItem.addActionListener(event -> {

	        if(selectedItem != null){

	            items.remove(selectedItem);

	            selectedItem = null;
	            
	            refreshPanels();

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
		
		setFocusable(true);
		requestFocusInWindow();
		
		setBackground(Color.WHITE);
		
		Equipment mic = EquipmentFactory.create("マイク");

		items.add(new LayoutItem(mic,100,100));

		items.add(new LayoutItem(mic,250,150));

		items.add(new LayoutItem(mic,450,250));
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		refreshPanels();
		
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

        		if(equipment.getImage() != null) {

        		    g.drawImage(
        		            equipment.getImage(),
        		            item.getX(),
        		            item.getY(),
        		            equipment.getWidth(),
        		            equipment.getHeight(),
        		            this);

        		} else {

        		    g.setColor(equipment.getColor());

        		    g.fillRect(
        		            item.getX(),
        		            item.getY(),
        		            equipment.getWidth(),
        		            equipment.getHeight());
        		}
            
            //枠線を描く
        		if(item == selectedItem) {
        		    g.setColor(Color.RED);
        		} else {
        		    g.setColor(Color.BLACK);
        		}
            g.drawRect(
            	    item.getX(),
            	    item.getY(),
            	    equipment.getWidth(),
            	    equipment.getHeight());
            //文字
            g.drawString(
            	    item.getEquipment().getName(),
            	    item.getX() + 8,
            	    item.getY() + equipment.getHeight() + 15);

        }
        
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		selectedItem = findItem(e.getX(), e.getY());
		
		refreshPanels();
		
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
		
		refreshPanels();
		
		repaint();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		requestFocusInWindow();
		
		if (e.isPopupTrigger()) {
			showPopupMenu(e);
			return;
		}
		
		selectedItem = findItem(e.getX(), e.getY());
		
		if(selectedItem != null) {
			
			dragOffsetX = e.getX() - selectedItem.getX();
			dragOffsetY = e.getY() - selectedItem.getY();
			
			dragging = true;
		}
		
		refreshPanels();
		
		
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
			
			
			int x = e.getX() - dragOffsetX;
			int y = e.getY() - dragOffsetY;

			x = Math.round((float)x / GRID_SIZE) * GRID_SIZE;
			y = Math.round((float)y / GRID_SIZE) * GRID_SIZE;

			selectedItem.setX(x);
			selectedItem.setY(y);

			repaint();

		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		
		LayoutItem item = findItem(e.getX(), e.getY());
		
		if(item != null) {
			
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}else {
			setCursor(Cursor.getDefaultCursor());
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {

	    if(e.isControlDown()
	            && e.getKeyCode() == KeyEvent.VK_C) {

	        copiedItem = selectedItem;

	    }
	    if(e.isControlDown()
	            && e.getKeyCode() == KeyEvent.VK_V) {

	        if(copiedItem != null){

	            Equipment equipment =
	                    EquipmentFactory.create(
	                            copiedItem.getEquipment().getName());

	            LayoutItem item =
	                    new LayoutItem(
	                            equipment,
	                            copiedItem.getX()+30,
	                            copiedItem.getY()+30);

	            item.setMemo(
	                    copiedItem.getMemo());

	            item.setQuantity(
	                    copiedItem.getQuantity());

	            items.add(item);

	            selectedItem = item;

	            refreshPanels();

	            repaint();

	        }

	    }

	}
	@Override
	public void keyReleased(KeyEvent e){}

	@Override
	public void keyTyped(KeyEvent e){}
	
	private void refreshPanels() {

		propertyPanel.displayItem(selectedItem);

	    propertyPanel.displaySummary(items);
	}
	
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
	public List<LayoutItem> getItems(){

	    return items;
	}
	
	public void setItems(List<LayoutItem> items) {

	    this.items = items;

	    selectedItem = null;

	    refreshPanels();

	    repaint();
	}

}
