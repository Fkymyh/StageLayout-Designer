package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import model.RoomObject;
import model.RoomTemplate;



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
	
	private final double METERS_PER_GRID = 0.5;
	
	private int dragOffsetX;
	
	private int dragOffsetY;
	
	private boolean showGrid = true;

	private boolean snapToGrid = true;
	
	private Runnable changeCallback;
	
	private RoomTemplate roomTemplate;
	
	private void showPopupMenu(MouseEvent e){

	    JPopupMenu menu = new JPopupMenu();

	    JMenuItem deleteItem = new JMenuItem("削除");

	    deleteItem.addActionListener(event -> {

	        if(selectedItem != null){

	        	deleteSelectedItem();

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
		
		
		setPreferredSize(new Dimension(1800, 1200));
		
		
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		refreshPanels();
		
	}
	
	public void resizeSelectedItem(int delta) {

	    if (selectedItem == null) {
	        return;
	    }

	    int newWidth = selectedItem.getWidth() + delta;
	    int newHeight = selectedItem.getHeight() + delta;

	    if (newWidth < 10) {
	        newWidth = 10;
	    }

	    if (newHeight < 10) {
	        newHeight = 10;
	    }

	    selectedItem.setSize(newWidth, newHeight);
	    
	    refreshPanels();
	    
	    notifyChanged();

	    repaint();
	}
	
	public void rotateSelectedItem() {

	    if (selectedItem == null) {
	        return;
	    }

	    selectedItem.rotate90();

	    refreshPanels();
	    
	    notifyChanged();

	    repaint();
	}
	
	public void moveSelectedItem(int dx, int dy) {

	    if (selectedItem == null) {
	        return;
	    }

	    int x = selectedItem.getX() + dx;
	    int y = selectedItem.getY() + dy;

	    selectedItem.setX(x);
	    selectedItem.setY(y);
	    
	    notifyChanged();

	    repaint();
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		//グリッド線の色
		if (showGrid) {
		    drawGrid(g);
		}
		
		drawRoomTemplate(g);
	
		
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

        		Graphics2D g2 = (Graphics2D) g.create();
        		
        		double centerX = item.getX() + item.getWidth() / 2.0;
        		double centerY = item.getY() + item.getHeight() / 2.0;
        		
        		g2.rotate(
        				Math.toRadians(item.getRotation()),
        				centerX,
        				centerY);
        		
        		if(equipment.getImage() != null) {
        			
        			g2.drawImage(
        					equipment.getImage(),
        					item.getX(),
        					item.getY(),
        					item.getWidth(),
        					item.getHeight(),
        					this	);
        		}else {
        			
        			g2.setColor(equipment.getColor());
        			
        			g2.fillRect(
        					item.getX(),
        					item.getY(),
        					item.getWidth(),
        					item.getHeight());
        		}
        		
        		if (item == selectedItem) {
        			
        			g2.setColor(Color.RED);
        			g2.setStroke(new BasicStroke(3));
        			
        			g2.drawRect(
        					item.getX(),
        					item.getY(),
        					item.getWidth(),
        					item.getHeight());
        			
        		}else {
        			
        			g2.setColor(Color.BLACK);
        			g2.setStroke(new BasicStroke(1));
        			
        			g2.drawRect(
        					item.getX(),
        					item.getY(),
        					item.getWidth(),
        					item.getHeight());
        		}
        		
        		
        		
        		g2.dispose();
        		
        		if(item == selectedItem) {
        			
        			g.setColor(Color.RED);
        			
        			int labelY = item.getY() - 5;
        			
        			if (labelY < 15) {
        				labelY = item.getY() + item.getHeight() + 15;
        			}
        			
        			g.drawString(
        					"選択中",
        					item.getX(),
        					labelY);
        		}
        		
        		g.setColor(Color.BLACK);
        		
        		g.drawString(
        				item.getEquipment().getName(),
        				item.getX() + 8,
        				item.getY() + item.getHeight() + 15);

        }
        
	}
	
	
	private void drawRoomTemplate(Graphics g) {

	    if (roomTemplate == null) {
	        return;
	    }

	    g.setColor(new Color(245, 245, 245));

	    g.fillRect(
	            0,
	            0,
	            roomTemplate.getWidth(),
	            roomTemplate.getHeight());

	    g.setColor(Color.GRAY);

	    g.drawRect(
	            0,
	            0,
	            roomTemplate.getWidth(),
	            roomTemplate.getHeight());

	    for (RoomObject object : roomTemplate.getObjects()) {
	    	
	    	if (RoomObject.TYPE_LINE.equals(object.getType())) {
	    		
	    		g.drawLine(
	    				object.getX(),
	    				object.getY(),
	    				object.getEndX(),
	    				object.getEndY());
	    		
	    		g.drawString(
	    				object.getName(),
	    				object.getX() + 5,
	    				object.getY() - 5);
	    		
	    	}else {
	    		
	    		g.drawRect(
	    				object.getX(),
	    				object.getY(),
	    				object.getWidth(),
	    				object.getHeight());
	    		
	    		g.drawString(
	    				object.getName(),
	    				object.getX() + 5,
	    				object.getY() +18);
	    	}
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
		
		if (name == null) {
			return;
		}
		
		Equipment equipment = EquipmentFactory.create(name);
		
		int x = e.getX();
		int y = e.getY();

		if (snapToGrid) {
		    x = snapValue(x);
		    y = snapValue(y);
		}

		LayoutItem newItem = new LayoutItem(
		        equipment,
		        x,
		        y);
		
		items.add(newItem);
		
		selectedItem = newItem;
		
		refreshPanels();
		
		notifyChanged();
		
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
		
		if (dragging) {
			notifyChanged();
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

			if (snapToGrid) {

			    x = snapValue(x);
			    y = snapValue(y);
			}
			

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

	    if (e.getKeyCode() == KeyEvent.VK_DELETE) {

	        deleteSelectedItem();

	        return;
	    }
	    
	    int moveAmount = snapToGrid ? GRID_SIZE : 5;

	    if (e.getKeyCode() == KeyEvent.VK_UP) {

	        moveSelectedItem(0, -moveAmount);

	        return;
	    }

	    if (e.getKeyCode() == KeyEvent.VK_DOWN) {

	        moveSelectedItem(0, moveAmount);

	        return;
	    }

	    if (e.getKeyCode() == KeyEvent.VK_LEFT) {

	        moveSelectedItem(-moveAmount, 0);

	        return;
	    }

	    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

	        moveSelectedItem(moveAmount, 0);

	        return;
	    }

	    if (e.getKeyCode() == KeyEvent.VK_PLUS
	            || e.getKeyCode() == KeyEvent.VK_EQUALS
	            || e.getKeyCode() == KeyEvent.VK_ADD
	            || e.getKeyChar() == '+') {

	        resizeSelectedItem(10);

	        return;
	    }
	    
	    if (e.getKeyCode() == KeyEvent.VK_R) {

	        rotateSelectedItem();

	        return;
	    }
	    

	    if (e.getKeyCode() == KeyEvent.VK_MINUS
	            || e.getKeyCode() == KeyEvent.VK_SUBTRACT
	            || e.getKeyChar() == '-') {

	        resizeSelectedItem(-10);

	        return;
	    }

	    if (e.isControlDown()
	            && e.getKeyCode() == KeyEvent.VK_C) {

	        copySelectedItem();

	        return;
	    }

	    if (e.isControlDown()
	            && e.getKeyCode() == KeyEvent.VK_V) {

	        pasteCopiedItem();

	        return;
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
	
	
	
	public void deleteSelectedItem() {
		
		if (selectedItem == null) {
			return;
		}
		items.remove(selectedItem);
		
		selectedItem = null;
		
		refreshPanels();
		
		notifyChanged();
		
		repaint();
	}
	
	public void copySelectedItem() {
		
		if (selectedItem == null) {
			return;
		}
		
		copiedItem = selectedItem;
	}
	
	public void pasteCopiedItem() {
		
		if (copiedItem == null) {
			return;
		}
		
		Equipment equipment =
				EquipmentFactory.create(
						copiedItem.getEquipment().getName());
		
		LayoutItem item =
				new LayoutItem(
						equipment,
						copiedItem.getX() + 30,
						copiedItem.getY() + 30);
		
		item.setSize(
		        copiedItem.getWidth(),
		        copiedItem.getHeight());
		
		item.setRotation(copiedItem.getRotation());
		
		item.setMemo(copiedItem.getMemo());
		
		item.setQuantity(copiedItem.getQuantity());
		
		items.add(item);
		
		selectedItem =item;
		
		refreshPanels();
		
		notifyChanged();
		
		repaint();
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
	
	public void clearItems() {
		items.clear();
		
		selectedItem = null;
		
		refreshPanels();
		
		notifyChanged();
		
		repaint();
	}
	
	public void setShowGrid(boolean showGrid) {

	    this.showGrid = showGrid;

	    repaint();
	}

	public void setSnapToGrid(boolean snapToGrid) {

	    this.snapToGrid = snapToGrid;
	}
	
	private int snapValue(int value) {

	    return Math.round((float) value / GRID_SIZE) * GRID_SIZE;
	}
	
	private LayoutItem findItem(int x, int y) {

	    for (LayoutItem item : items) {

	        Rectangle rect = new Rectangle(
	                item.getX(),
	                item.getY(),
	                item.getWidth(),
	                item.getHeight());

	        if (rect.contains(x, y)) {
	            return item;
	        }
	    }

	    return null;
	}
	
	public void setChangeCallback(Runnable changeCallback) {

	    this.changeCallback = changeCallback;
	}
	//変更通知用メソッド
	private void notifyChanged() {

	    if (changeCallback != null) {
	        changeCallback.run();
	    }
	}
	
	public void setRoomTemplate(RoomTemplate roomTemplate) {

	    this.roomTemplate = roomTemplate;

	    if (roomTemplate != null) {

	        int margin = 100;

	        setPreferredSize(
	                new Dimension(
	                        roomTemplate.getWidth() + margin,
	                        roomTemplate.getHeight() + margin));
	    }else {
	    	
	    	revalidate();
	    	
	    	repaint();
	    }

	    revalidate();

	    repaint();
	}
	
	public RoomTemplate getRoomTemplate() {
	    return roomTemplate;
	}
	
	private void drawGrid(Graphics g) {

	    for (int x = 0; x < getWidth(); x += GRID_SIZE) {

	        int gridIndex = x / GRID_SIZE;

	        if (gridIndex % 2 == 0) {
	            g.setColor(new Color(200, 200, 200));
	        } else {
	            g.setColor(new Color(230, 230, 230));
	        }

	        g.drawLine(x, 0, x, getHeight());
	    }

	    for (int y = 0; y < getHeight(); y += GRID_SIZE) {

	        int gridIndex = y / GRID_SIZE;

	        if (gridIndex % 2 == 0) {
	            g.setColor(new Color(200, 200, 200));
	        } else {
	            g.setColor(new Color(230, 230, 230));
	        }

	        g.drawLine(0, y, getWidth(), y);
	    }

	    drawMeterLabels(g);
	}
	
	private void drawMeterLabels(Graphics g) {

	    g.setColor(Color.GRAY);

	    for (int x = 0; x < getWidth(); x += GRID_SIZE * 2) {

	        double meter = x / (double) GRID_SIZE * METERS_PER_GRID;

	        g.drawString(
	                formatMeter(meter),
	                x + 3,
	                15);
	    }

	    for (int y = 0; y < getHeight(); y += GRID_SIZE * 2) {

	        double meter = y / (double) GRID_SIZE * METERS_PER_GRID;

	        g.drawString(
	                formatMeter(meter),
	                3,
	                y + 15);
	    }
	}
	
	private String formatMeter(double meter) {

	    if (meter == (int) meter) {
	        return (int) meter + "m";
	    }

	    return meter + "m";
	}

}
