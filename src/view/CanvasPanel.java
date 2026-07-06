package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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

import model.DrawLine;
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
	
	private boolean drawLineMode = false;

	private Integer lineStartX = null;

	private Integer lineStartY = null;
	
	private int mouseX;
	private int mouseY;
	
	private Color currentLineColor = Color.RED;

	private int currentLineStrokeWidth = 3;
	
	private LayoutItem copiedItem;
	
	private boolean dragging = false;
	
	private PropertyPanel propertyPanel;	
	
	private final int GRID_SIZE = 20;
	
	private static final double DEFAULT_METERS_PER_GRID = 0.5;

	private static final double TEMPLATE_214_METERS_PER_GRID = 0.33;

	private double metersPerGrid = DEFAULT_METERS_PER_GRID;
	
	private static final int LEFT_RULER_WIDTH = 35;

	private static final int TOP_RULER_HEIGHT = 25;
	
	private int dragOffsetX;
	
	private int dragOffsetY;
	
	private boolean controlDown = false;
	
	private boolean shiftDown = false;
	
	private boolean showGrid = true;

	private boolean snapToGrid = true;
	
	private Runnable changeCallback;
	
	private RoomTemplate roomTemplate;
	
	private boolean showNames = true;
	
	private boolean showLineLength = true;
	
	private double zoom = 1.0;
	
	private final int BASE_WIDTH = 1800;
	
	private final int BASE_HEIGHT = 1200;
	
	private int toCanvasX(MouseEvent e) {
	    return (int) Math.round((e.getX() - LEFT_RULER_WIDTH) / zoom);
	}

	private int toCanvasY(MouseEvent e) {
	    return (int) Math.round((e.getY() - TOP_RULER_HEIGHT) / zoom);
	}
	
	private double calculateLineLengthMeters(DrawLine line) {

	    int dx = line.getEndX() - line.getStartX();
	    int dy = line.getEndY() - line.getStartY();

	    double pixelLength = Math.sqrt(dx * dx + dy * dy);

	    double gridLength = pixelLength / GRID_SIZE;

	    return gridLength * metersPerGrid;
	}
	
	private boolean isInDrawingArea(MouseEvent e) {

	    return e.getX() >= LEFT_RULER_WIDTH
	            && e.getY() >= TOP_RULER_HEIGHT;
	}
	
	private double calculatePreviewLineLengthMeters(int previewX,
												   int previewY) {

	    if (lineStartX == null || lineStartY == null) {
	        return 0;
	    }

	    int dx = previewX - lineStartX;
	    int dy = previewY - lineStartY;

	    double pixelLength = Math.sqrt(dx * dx + dy * dy);

	    double gridLength = pixelLength / GRID_SIZE;

	    return gridLength * metersPerGrid;
	}
	
	private List<DrawLine> drawLines = new ArrayList<>();
	
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
		
		
		setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
		
		
		
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

	    selectedItem.rotateBy(15);

	    refreshPanels();
	    
	    notifyChanged();

	    repaint();
	}
	
	public void toggleDrawLineMode() {

	    drawLineMode = !drawLineMode;

	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;

	    refreshPanels();

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
		
		Graphics2D rulerG = (Graphics2D) g.create();
		
		 	drawRulers(rulerG);

		    rulerG.dispose();

		    Graphics2D g2 = (Graphics2D) g.create();

		    g2.translate(
		            LEFT_RULER_WIDTH,
		            TOP_RULER_HEIGHT);

		    g2.scale(
		            zoom,
		            zoom);

		
		//グリッド線の色
		 if (showGrid && roomTemplate == null) {
		     drawGrid(g2);
		    }
		
		drawRoomTemplate(g2);
		
		drawLines(g2);
		
		drawLinePreview(g2);
		
		drawLineStartPoint(g2);
		
		drawItems(g2);

	    g2.dispose();
	}
	
		
     // 機材描画
	private void drawItems(Graphics g) {
		
        g.setColor(Color.BLACK);

        for(LayoutItem item : items){
        	
        	String itemName = item.getEquipment().getName();

        	if (itemName.startsWith("バミリ")) {
        	    drawBamiri(g, item);

        	    g.setColor(Color.BLACK);
        	    
        	    if (showNames) {
        	        g.drawString(
        	                item.getDisplayName(),
        	                item.getX() + 8,
        	                getRotatedLabelY(item));
        	    }

        	    continue;
        	}
        	
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
        		
        		if (equipment.getImage() != null) {
        			
        			drawImageKeepingAspectRatio(
        					g2,
        					equipment.getImage(),
        					item);
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
        	
        		if (showNames) {
        		    g.drawString(
        		            item.getDisplayName(),
        		            item.getX() + 8,
        		            getRotatedLabelY(item));
        		}

        }
        
	}
	
	private void drawLineStartPoint(Graphics g) {

	    if (!drawLineMode) {
	        return;
	    }

	    if (lineStartX == null || lineStartY == null) {
	        return;
	    }

	    g.setColor(Color.RED);

	    g.fillOval(
	            lineStartX - 4,
	            lineStartY - 4,
	            8,
	            8);

	    g.drawString(
	            "始点",
	            lineStartX + 6,
	            lineStartY - 6);
	}
	
	private void drawRoomTemplate(Graphics g) {

	    if (roomTemplate == null) {
	        return;
	    }

	    // テンプレート背景
	    g.setColor(new Color(250, 250, 250));

	    g.fillRect(
	            0,
	            0,
	            roomTemplate.getWidth(),
	            roomTemplate.getHeight());

	    // テンプレート内だけのマス
	    drawRoomTemplateGrid(g);

	    // テンプレート外枠
	    g.setColor(Color.GRAY);

	    g.drawRect(
	            0,
	            0,
	            roomTemplate.getWidth(),
	            roomTemplate.getHeight());

	    // 教室内の机・柱・ステージなど
	    for (RoomObject object : roomTemplate.getObjects()) {

	        switch (object.getType()) {

	            case RoomObject.TYPE_LINE:
	                drawRoomLine(g, object);
	                break;

	            case RoomObject.TYPE_CIRCLE:
	                drawRoomCircle(g, object);
	                break;

	            case RoomObject.TYPE_ARC:
	                drawRoomArc(g, object);
	                break;

	            case RoomObject.TYPE_TEXT:
	                drawRoomText(g, object);
	                break;

	            case RoomObject.TYPE_RECT:
	            default:
	                drawRoomRect(g, object);
	                break;
	        }
	    }
	    
	}
	
	
	
	private void drawRoomTemplateGrid(Graphics g) {

	    if (roomTemplate == null) {
	        return;
	    }

	    Graphics2D g2 = (Graphics2D) g.create();

	    for (int x = 0; x <= roomTemplate.getWidth(); x += GRID_SIZE) {

	        int gridIndex = x / GRID_SIZE;

	        if (gridIndex % 5 == 0) {
	            g2.setColor(new Color(170, 170, 170));
	        } else {
	            g2.setColor(new Color(210, 210, 210));
	        }

	        g2.drawLine(
	                x,
	                0,
	                x,
	                roomTemplate.getHeight());
	    }

	    for (int y = 0; y <= roomTemplate.getHeight(); y += GRID_SIZE) {

	        int gridIndex = y / GRID_SIZE;

	        if (gridIndex % 5 == 0) {
	            g2.setColor(new Color(170, 170, 170));
	        } else {
	            g2.setColor(new Color(210, 210, 210));
	        }

	        g2.drawLine(
	                0,
	                y,
	                roomTemplate.getWidth(),
	                y);
	    }

	    g2.dispose();
	}
	private void drawRoomLine(Graphics g, RoomObject object) {

	    g.setColor(Color.DARK_GRAY);

	    g.drawLine(
	            object.getX(),
	            object.getY(),
	            object.getEndX(),
	            object.getEndY());

	    drawRoomObjectName(g, object);
	}
	
	private void drawRoomRect(Graphics g, RoomObject object) {

	    g.setColor(Color.DARK_GRAY);

	    g.drawRect(
	            object.getX(),
	            object.getY(),
	            object.getWidth(),
	            object.getHeight());

	    drawRoomObjectName(g, object);
	}
	
	private void drawRoomCircle(Graphics g, RoomObject object) {

	    Graphics2D g2 = (Graphics2D) g.create();

	    g2.setColor(Color.DARK_GRAY);
	    g2.setStroke(new BasicStroke(2));

	    g2.drawOval(
	            object.getX(),
	            object.getY(),
	            object.getWidth(),
	            object.getHeight());

	    g2.dispose();

	    drawRoomObjectName(g, object);
	}
	
	private void drawRoomArc(Graphics g, RoomObject object) {

	    Graphics2D g2 = (Graphics2D) g.create();

	    g2.setColor(Color.BLACK);
	    g2.setStroke(new BasicStroke(8));

	    // 0度から180度の半円アーチ
	    g2.drawArc(
	            object.getX(),
	            object.getY(),
	            object.getWidth(),
	            object.getHeight(),
	            0,
	            180);

	    g2.dispose();

	    drawRoomObjectName(g, object);
	}
	
	private void drawRoomText(Graphics g, RoomObject object) {

	    g.setColor(Color.BLACK);

	    g.drawString(
	            object.getName(),
	            object.getX(),
	            object.getY());
	}
	
	private void drawRoomObjectName(Graphics g, RoomObject object) {

	    if (!showNames) {
	        return;
	    }

	    if (object.getName() == null || object.getName().isBlank()) {
	        return;
	    }

	    g.setColor(Color.BLACK);

	    g.drawString(
	            object.getName(),
	            object.getX() + 5,
	            object.getY() + 18);
	}
	private void drawLines(Graphics g) {

	    Graphics2D g2 = (Graphics2D) g.create();

	    for (DrawLine line : drawLines) {

	        g2.setColor(line.getColor());

	        g2.setStroke(
	                new BasicStroke(
	                        line.getStrokeWidth(),
	                        BasicStroke.CAP_ROUND,
	                        BasicStroke.JOIN_ROUND));

	        g2.drawLine(
	                line.getStartX(),
	                line.getStartY(),
	                line.getEndX(),
	                line.getEndY());
	        
	        int labelX =
	                (line.getStartX() + line.getEndX()) / 2 + 6;

	        int labelY =
	                (line.getStartY() + line.getEndY()) / 2 - 6;
	        
	        if (showLineLength) {

	            double meters = calculateLineLengthMeters(line);

	            g2.setColor(Color.BLACK);
	            g2.setStroke(new BasicStroke(1));

	            g2.drawString(
	                    formatLineLength(meters),
	                    labelX,
	                    labelY);
	        }

	        if (showNames
	                && line.getLabel() != null
	                && !line.getLabel().isBlank()) {

	            g2.setColor(Color.BLACK);
	            g2.setStroke(new BasicStroke(1));

	            g2.drawString(
	                    line.getLabel(),
	                    labelX,
	                    labelY - 14);
	        }
	    }

	    g2.dispose();
	}
	
	private void drawBamiri(Graphics g, LayoutItem item) {

	    String name = item.getEquipment().getName();

	    int x = item.getX();
	    int y = item.getY();
	    int w = item.getWidth();
	    int h = item.getHeight();

	    g.setColor(item.getEquipment().getColor());

	    if ("バミリ X".equals(name)) {

	        g.drawLine(x, y, x + w, y + h);
	        g.drawLine(x + w, y, x, y + h);

	    } else if ("バミリ ＋".equals(name)) {

	        g.drawLine(x + w / 2, y, x + w / 2, y + h);
	        g.drawLine(x, y + h / 2, x + w, y + h / 2);

	    } else {

	        // バミリ 横・縦
	        g.fillRect(x, y, w, h);
	    }

	    if (item == selectedItem) {
	        g.setColor(Color.RED);
	        g.drawRect(x - 3, y - 3, w + 6, h + 6);
	    }
	}
	
	private void drawModeNotice(Graphics g) {

	    if (!drawLineMode) {
	        return;
	    }

	    g.setColor(new Color(255, 240, 180));
	    g.fillRect(10, 25, 230, 35);

	    g.setColor(Color.ORANGE);
	    g.drawRect(10, 25, 230, 35);

	    g.setColor(Color.BLACK);
	    String modeText;

	    if (isTemplateMode()) {
	        modeText = "線描画：Shift=直線固定 / テンプレートのマス優先";
	    } else {
	        modeText = "線描画：Shift=直線固定 / Ctrl=1m吸着";
	    }

	    g.drawString(
	            modeText,
	            20,
	            48);

	    g.setColor(currentLineColor);
	    g.fillRect(250, 32, 30, 18);

	    g.setColor(Color.BLACK);
	    g.drawRect(250, 32, 30, 18);

	    g.drawString(
	            "太さ " + currentLineStrokeWidth,
	            290,
	            48);
	}
	
	private void drawLinePreview(Graphics g) {

	    if (!drawLineMode) {
	        return;
	    }

	    if (lineStartX == null || lineStartY == null) {
	        return;
	    }

	    Graphics2D g2 = (Graphics2D) g.create();

	    g2.setColor(currentLineColor);
	    
	    g2.setStroke(
	            new BasicStroke(
	                    currentLineStrokeWidth,
	                    BasicStroke.CAP_ROUND,
	                    BasicStroke.JOIN_ROUND));

	    int previewX = mouseX;
	    int previewY = mouseY;

	    int[] adjusted = adjustLinePointForCurrentMode(previewX, previewY);

	    previewX = adjusted[0];
	    previewY = adjusted[1];

	    g2.drawLine(
	            lineStartX,
	            lineStartY,
	            previewX,
	            previewY);
	    if (showLineLength) {

	        double meters = calculatePreviewLineLengthMeters(previewX, previewY);
	        
	        String text = formatLineLength(meters);

	        int labelX = (lineStartX + previewX) / 2 + 6;
	        int labelY = (lineStartY + previewY) / 2 - 6;

	        int boxWidth = 58;
	        int boxHeight = 18;

	        g2.setColor(Color.WHITE);
	        g2.fillRect(
	                labelX - 3,
	                labelY - 14,
	                boxWidth,
	                boxHeight);

	        g2.setColor(Color.GRAY);
	        g2.drawRect(
	                labelX - 3,
	                labelY - 14,
	                boxWidth,
	                boxHeight);

	        g2.setColor(Color.BLACK);
	        g2.drawString(
	                text,
	                labelX,
	                labelY);
	    }


	    g2.dispose();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		if(!isInDrawingArea(e)) {
			return;
		}
		
		int canvasX = toCanvasX(e);
		int canvasY = toCanvasY(e);

	    if (drawLineMode) {
	    	
	    		if(e.getButton() != MouseEvent.BUTTON1) {
	    			return;
	    		}

	        handleDrawLineClick(
	        		canvasX, 
	        		canvasY,
	        		e.isControlDown(),
	        		e.isShiftDown());

	        return;
	    }

	    selectedItem = findItem(canvasX, canvasY);

	    refreshPanels();

	    if (selectedItem != null) {

	        repaint();

	        return;
	    }

	    // 空いてる場所をダブルクリックした時だけ機材を追加する
	    if (e.getClickCount() < 2) {
	        return;
	    }

	    String name = equipmentPanel.getSelectedEquipment();

	    if (name == null) {
	        return;
	    }

	    Equipment equipment = EquipmentFactory.create(name);

	    int x = canvasX;
	    int y = canvasY;

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
	
	private void handleDrawLineClick(int x,
									int y, 
									boolean meterSnap,
									boolean orthogonalLock) {
		
		shiftDown = orthogonalLock;
		controlDown = meterSnap;

		int[] adjusted = adjustLinePointForCurrentMode(x, y);

		x = adjusted[0];
		y = adjusted[1];

	    // 1回目クリック：始点を保存
	    if (lineStartX == null || lineStartY == null) {

	        lineStartX = x;
	        lineStartY = y;

	        repaint();

	        return;
	    }

	    // 2回目クリック：線を追加
	    DrawLine line =
	            new DrawLine(
	                    lineStartX,
	                    lineStartY,
	                    x,
	                    y);

	    line.setColor(currentLineColor);
	    line.setStrokeWidth(currentLineStrokeWidth);

	    drawLines.add(line);
	    
	 // 重要：
	    // 終点を次の始点にする
	    lineStartX = x;
	    lineStartY = y;

	    notifyChanged();

	    repaint();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		requestFocusInWindow();
		
		if (!isInDrawingArea(e)) {
	        return;
	    }
		
		int canvasX = toCanvasX(e);
		int canvasY = toCanvasY(e);
		
		if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
			
			if(drawLineMode) {
				finishCurrentLine();
				return;
			}
			
			showPopupMenu(e);
			return;
		}
		
		if (drawLineMode) {
			return;
		}
		
		selectedItem = findItem(canvasX, canvasY);
		
		if(selectedItem != null) {
			
			dragOffsetX = canvasX - selectedItem.getX();
			dragOffsetY = canvasY - selectedItem.getY();
			
			dragging = true;
		}
		
		refreshPanels();
		
		
		repaint();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		
		if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3){
			
			if(drawLineMode) {
				finishCurrentLine();
				return;
			}
			showPopupMenu(e);
			return;
		}
		
		if(drawLineMode) {
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
		
		if (!isInDrawingArea(e)) {
	        return;
	    }
		
		if(drawLineMode) {
			return;
		}
		
		int canvasX = toCanvasX(e);
		int canvasY = toCanvasY(e);
		
		if(selectedItem != null && dragging) {
			
			
			int x = canvasX - dragOffsetX;
			int y = canvasY - dragOffsetY;

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
		
		if (!isInDrawingArea(e)) {
	        setCursor(Cursor.getDefaultCursor());
	        return;
	    }
		
		mouseX = toCanvasX(e);
	    mouseY = toCanvasY(e);
	    
	    controlDown = e.isControlDown();
	    shiftDown = e.isShiftDown();

	    if (drawLineMode && lineStartX != null && lineStartY != null) {
	        repaint();
	        return;
	    }
		
		LayoutItem item = findItem(mouseX, mouseY);
		
		if(item != null) {
			
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}else {
			setCursor(Cursor.getDefaultCursor());
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {

	    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

	        if (drawLineMode) {
	            finishCurrentLine();
	            return;
	        }
	    }

	    if (e.getKeyCode() == KeyEvent.VK_L) {

	        toggleDrawLineMode();

	        return;
	    }

	    if (drawLineMode) {

	        if (e.getKeyCode() == KeyEvent.VK_1) {
	            currentLineColor = Color.BLACK;
	            repaint();
	            return;
	        }

	        if (e.getKeyCode() == KeyEvent.VK_2) {
	            currentLineColor = Color.RED;
	            repaint();
	            return;
	        }

	        if (e.getKeyCode() == KeyEvent.VK_3) {
	            currentLineColor = Color.BLUE;
	            repaint();
	            return;
	        }

	        if (e.getKeyCode() == KeyEvent.VK_4) {
	            currentLineColor = Color.GREEN;
	            repaint();
	            return;
	        }

	        if (e.getKeyCode() == KeyEvent.VK_PLUS
	                || e.getKeyCode() == KeyEvent.VK_EQUALS
	                || e.getKeyCode() == KeyEvent.VK_ADD
	                || e.getKeyChar() == '+') {

	            increaseLineStrokeWidth();
	            return;
	        }

	        if (e.getKeyCode() == KeyEvent.VK_MINUS
	                || e.getKeyCode() == KeyEvent.VK_SUBTRACT
	                || e.getKeyChar() == '-') {

	            decreaseLineStrokeWidth();
	            return;
	        }

	        return;
	    }

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

	    if (e.getKeyCode() == KeyEvent.VK_MINUS
	            || e.getKeyCode() == KeyEvent.VK_SUBTRACT
	            || e.getKeyChar() == '-') {

	        resizeSelectedItem(-10);

	        return;
	    }

	    if (e.getKeyCode() == KeyEvent.VK_R) {

	        if (e.isShiftDown()) {
	            rotateSelectedItemReverse();
	        } else {
	            rotateSelectedItem();
	        }

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
	
	public void rotateSelectedItemReverse() {

	    if (selectedItem == null) {
	        return;
	    }

	    selectedItem.rotateBy(-15);

	    refreshPanels();

	    notifyChanged();

	    repaint();
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
		
		item.setLabel(copiedItem.getLabel());
		
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
	
	public RoomTemplate getRoomTemplate() {
	    return roomTemplate;
	}
	
	private void drawGrid(Graphics g) {
		
		int canvasWidth = (int) Math.round((getWidth() - LEFT_RULER_WIDTH) / zoom);
	    int canvasHeight = (int) Math.round((getHeight() - TOP_RULER_HEIGHT) / zoom);

	    for (int x = 0; x < canvasWidth; x += GRID_SIZE) {

	        int gridIndex = x / GRID_SIZE;

	        if (gridIndex % 2 == 0) {
	            g.setColor(new Color(200, 200, 200));
	        } else {
	            g.setColor(new Color(230, 230, 230));
	        }

	        g.drawLine(x, 0, x, canvasHeight);
	    }

	    for (int y = 0; y < canvasHeight; y += GRID_SIZE) {

	        int gridIndex = y / GRID_SIZE;

	        if (gridIndex % 2 == 0) {
	            g.setColor(new Color(200, 200, 200));
	        } else {
	            g.setColor(new Color(230, 230, 230));
	        }

	        g.drawLine(0, y, canvasWidth, y);
	    }
	    
	}
	
	private void drawRulers(Graphics g) {

	    Graphics2D g2 = (Graphics2D) g.create();

	    int panelWidth = getWidth();
	    int panelHeight = getHeight();

	    g2.setColor(new Color(245, 245, 245));

	    g2.fillRect(
	            LEFT_RULER_WIDTH,
	            0,
	            panelWidth - LEFT_RULER_WIDTH,
	            TOP_RULER_HEIGHT);

	    g2.fillRect(
	            0,
	            TOP_RULER_HEIGHT,
	            LEFT_RULER_WIDTH,
	            panelHeight - TOP_RULER_HEIGHT);

	    g2.setColor(new Color(235, 235, 235));

	    g2.fillRect(
	            0,
	            0,
	            LEFT_RULER_WIDTH,
	            TOP_RULER_HEIGHT);

	    g2.setColor(Color.GRAY);

	    g2.drawLine(
	            LEFT_RULER_WIDTH,
	            0,
	            LEFT_RULER_WIDTH,
	            panelHeight);

	    g2.drawLine(
	            0,
	            TOP_RULER_HEIGHT,
	            panelWidth,
	            TOP_RULER_HEIGHT);

	    double pixelsPerMeter =
	            (GRID_SIZE / metersPerGrid) * zoom;

	    double pixelsPerHalfMeter =
	            pixelsPerMeter / 2.0;

	    for (double x = 0;
	            LEFT_RULER_WIDTH + x < panelWidth;
	            x += pixelsPerHalfMeter) {

	        int drawX =
	                LEFT_RULER_WIDTH + (int) Math.round(x);

	        boolean major =
	                Math.abs(
	                        (x / pixelsPerMeter)
	                        - Math.round(x / pixelsPerMeter))
	                        < 0.001;

	        int tickHeight = major ? 10 : 5;

	        g2.setColor(Color.GRAY);

	        g2.drawLine(
	                drawX,
	                TOP_RULER_HEIGHT - tickHeight,
	                drawX,
	                TOP_RULER_HEIGHT);

	        if (major) {

	            int meter =
	                    (int) Math.round(x / pixelsPerMeter);

	            g2.setColor(Color.BLACK);

	            g2.drawString(
	                    meter + "m",
	                    drawX + 2,
	                    14);
	        }
	    }

	    for (double y = 0;
	            TOP_RULER_HEIGHT + y < panelHeight;
	            y += pixelsPerHalfMeter) {

	        int drawY =
	                TOP_RULER_HEIGHT + (int) Math.round(y);

	        boolean major =
	                Math.abs(
	                        (y / pixelsPerMeter)
	                        - Math.round(y / pixelsPerMeter))
	                        < 0.001;

	        int tickWidth = major ? 10 : 5;

	        g2.setColor(Color.GRAY);

	        g2.drawLine(
	                LEFT_RULER_WIDTH - tickWidth,
	                drawY,
	                LEFT_RULER_WIDTH,
	                drawY);

	        if (major) {

	            int meter =
	                    (int) Math.round(y / pixelsPerMeter);

	            g2.setColor(Color.BLACK);

	            g2.drawString(
	                    meter + "m",
	                    2,
	                    drawY + 4);
	        }
	    }

	    g2.dispose();
	}
	
	
	
	    private String formatLineLength(double meters) {

	        if (meters < 10) {
	            return String.format("%.2fm", meters);
	        }

	        return String.format("%.1fm", meters);
	    
	}
	
	
	public void setShowNames(boolean showNames) {
	    this.showNames = showNames;
	    repaint();
	}

	public boolean isShowNames() {
	    return showNames;
	}
	
	private int getRotatedLabelY(LayoutItem item) {

	    double radians = Math.toRadians(item.getRotation());

	    double sin = Math.abs(Math.sin(radians));
	    double cos = Math.abs(Math.cos(radians));

	    int rotatedHeight =
	            (int) Math.round(
	                    item.getWidth() * sin
	                    + item.getHeight() * cos);

	    int centerY = item.getY() + item.getHeight() / 2;

	    return centerY + rotatedHeight / 2 + 18;
	}
	
	private void drawImageKeepingAspectRatio(
	        Graphics2D g2,
	        Image image,
	        LayoutItem item) {

	    int imageW = image.getWidth(this);
	    int imageH = image.getHeight(this);

	    if (imageW <= 0 || imageH <= 0) {
	        return;
	    }

	    double scaleX = item.getWidth() / (double) imageW;
	    double scaleY = item.getHeight() / (double) imageH;

	    double scale = Math.min(scaleX, scaleY);

	    int drawW = (int) Math.round(imageW * scale);
	    int drawH = (int) Math.round(imageH * scale);

	    int drawX = item.getX() + (item.getWidth() - drawW) / 2;
	    int drawY = item.getY() + (item.getHeight() - drawH) / 2;

	    g2.drawImage(
	            image,
	            drawX,
	            drawY,
	            drawW,
	            drawH,
	            this);
	}
	
	public void setDrawLineMode(boolean drawLineMode) {

	    this.drawLineMode = drawLineMode;

	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;

	    refreshPanels();

	    repaint();
	}

	public boolean isDrawLineMode() {
	    return drawLineMode;
	}

	public void setCurrentLineColor(Color color) {

	    this.currentLineColor = color;

	    repaint();
	}

	public void increaseLineStrokeWidth() {

	    currentLineStrokeWidth++;

	    repaint();
	}

	public void decreaseLineStrokeWidth() {

	    currentLineStrokeWidth--;

	    if (currentLineStrokeWidth < 1) {
	        currentLineStrokeWidth = 1;
	    }

	    repaint();
	}

	public void cancelLineStartPoint() {

	    lineStartX = null;
	    lineStartY = null;

	    repaint();
	}
	
	public void finishCurrentLine() {

	    lineStartX = null;
	    lineStartY = null;

	    repaint();
	}
	
	public void setCurrentLineStrokeWidth(int strokeWidth) {

	    if (strokeWidth < 1) {
	        strokeWidth = 1;
	    }

	    this.currentLineStrokeWidth = strokeWidth;

	    repaint();
	}
	
	public void setZoom(double zoom) {

	    if (zoom < 0.5) {
	        zoom = 0.5;
	    }

	    if (zoom > 2.0) {
	        zoom = 2.0;
	    }

	    this.zoom = zoom;
	    
	    int width = LEFT_RULER_WIDTH + (int) Math.round(BASE_WIDTH * zoom);
	    int height = TOP_RULER_HEIGHT + (int) Math.round(BASE_HEIGHT * zoom);
	    
	    if (roomTemplate != null) {
	    	int margin = 200;

	        width = LEFT_RULER_WIDTH + (int) Math.round(
	                (roomTemplate.getWidth() + margin) * zoom);

	        height = TOP_RULER_HEIGHT + (int) Math.round(
	                (roomTemplate.getHeight() + margin) * zoom);
	    }
	    
	    setPreferredSize(new Dimension(width, height));

	    revalidate();
	    repaint();
	}

	public double getZoom() {
	    return zoom;
	}
	
	public void setRoomTemplate(RoomTemplate roomTemplate) {

	    this.roomTemplate = roomTemplate;

	    if (roomTemplate == null) {

	        metersPerGrid = DEFAULT_METERS_PER_GRID;

	    } else {

	        metersPerGrid = TEMPLATE_214_METERS_PER_GRID;
	    }

	    setZoom(zoom);

	    repaint();
	}
	
	public void setShowLineLength(boolean showLineLength) {
	    this.showLineLength = showLineLength;
	    repaint();
	}
	
	

	public boolean isShowLineLength() {
	    return showLineLength;
	}
	
	private double getPixelsPerMeter() {
	    return GRID_SIZE / metersPerGrid;
	}
	
	private int snapToMeterValue(int value) {

	    double pixelsPerMeter = getPixelsPerMeter();

	    return (int) Math.round(Math.round(value / pixelsPerMeter) * pixelsPerMeter);
	}
	
	private int[] applyOrthogonalLock(int x, int y) {

	    if (lineStartX == null || lineStartY == null) {
	        return new int[] {x, y};
	    }

	    int dx = Math.abs(x - lineStartX);
	    int dy = Math.abs(y - lineStartY);

	    if (dx >= dy) {
	        y = lineStartY;
	    } else {
	        x = lineStartX;
	    }

	    return new int[] {x, y};
	}
	
	private int[] snapLineLengthToMeter(int x, int y) {

	    if (lineStartX == null || lineStartY == null) {
	        return new int[] {x, y};
	    }

	    int dx = x - lineStartX;
	    int dy = y - lineStartY;

	    double length = Math.sqrt(dx * dx + dy * dy);

	    if (length == 0) {
	        return new int[] {x, y};
	    }

	    double pixelsPerMeter = getPixelsPerMeter();

	    double snappedLength =
	            Math.round(length / pixelsPerMeter) * pixelsPerMeter;

	    if (snappedLength < pixelsPerMeter) {
	        snappedLength = pixelsPerMeter;
	    }

	    double scale = snappedLength / length;

	    int snappedX =
	            lineStartX + (int) Math.round(dx * scale);

	    int snappedY =
	            lineStartY + (int) Math.round(dy * scale);

	    return new int[] {snappedX, snappedY};
	}
	
	private int[] adjustLinePointForCurrentMode(int x, int y) {

	    if (shiftDown) {
	        int[] locked = applyOrthogonalLock(x, y);
	        x = locked[0];
	        y = locked[1];
	    }
	    
	    if (isTemplateMode()) {

	        if (snapToGrid) {
	            x = snapValue(x);
	            y = snapValue(y);
	        }

	        return new int[] {x, y};
	    }


	    if (controlDown) {
	    		
	    		int[] snapped = snapLineLengthToMeter(x, y);
	    			x = snapped[0];
	    			y = snapped[1];
	        
	    } else if (snapToGrid) {
	        x = snapValue(x);
	        y = snapValue(y);
	    }

	    return new int[] {x, y};
	}
	
	private boolean isTemplateMode() {
	    return roomTemplate != null;
	}
	
	

}
