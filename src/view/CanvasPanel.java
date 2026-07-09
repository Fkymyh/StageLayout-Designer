package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import model.DrawLine;
import model.Equipment;
import model.EquipmentDefinition;
import model.EquipmentFactory;
import model.LayoutItem;
import model.RoomObject;
import model.RoomTemplate;
import util.ImageLoader;



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
	
	private List<RoomObject> customRoomObjects = new ArrayList<>(); 

	private Map<String, Image> roomObjectImageCache = new HashMap<>();

	private String roomObjectAddMode = null;

	private RoomObject selectedRoomObject;
	
	private boolean draggingRoomObject = false;

	private int roomObjectDragOffsetX;

	private int roomObjectDragOffsetY;
	
	private boolean resizingRoomObject = false;

	private int resizeStartMouseX;

	private int resizeStartMouseY;

	private int resizeStartWidth;

	private int resizeStartHeight;
	
	private boolean resizingItem = false;

	private int itemResizeStartMouseX;

	private int itemResizeStartMouseY;

	private int itemResizeStartWidth;

	private int itemResizeStartHeight;

	private static final int RESIZE_HANDLE_SIZE = 8;

    private static final int SHEET_MARGIN = 40;
	
	private boolean showNames = true;
	
	private boolean showLineLength = true;

	private boolean stageLocked = false;

    private static final int MAX_HISTORY_SIZE = 50;

    private Deque<LayoutSnapshot> undoHistory = new ArrayDeque<>();

    private Deque<LayoutSnapshot> redoHistory = new ArrayDeque<>();

    private boolean restoringSnapshot = false;

	private static final Color STAGE_FILL_COLOR = new Color(232, 235, 238);

	private static final Color SEAT_AREA_FILL_COLOR = new Color(226, 236, 244);

	private static final Color COLUMN_FILL_COLOR = new Color(130, 136, 142);

	private static final Color ROOM_OBJECT_BORDER_COLOR = new Color(78, 84, 90);
	
	private double zoom = 1.0;
	
	private int sheetWidth = metersToPixels(20.0);
	
	private int sheetHeight = metersToPixels(15.0);

    private int metersToPixels(double meters) {
        return (int) Math.round((meters / DEFAULT_METERS_PER_GRID) * GRID_SIZE);
    }
	
	private int toCanvasX(MouseEvent e) {
	    return (int) Math.round((e.getX() - LEFT_RULER_WIDTH) / zoom)
	            - SHEET_MARGIN;
	}

	private int toCanvasY(MouseEvent e) {
	    return (int) Math.round((e.getY() - TOP_RULER_HEIGHT) / zoom)
	            - SHEET_MARGIN;
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
	
	private boolean isOnRoomObjectResizeHandle(RoomObject object, int x, int y) {

	    if (object == null) {
	        return false;
	    }

	    int handleX =
	            object.getX() + object.getWidth() - RESIZE_HANDLE_SIZE / 2;

	    int handleY =
	            object.getY() + object.getHeight() - RESIZE_HANDLE_SIZE / 2;

	    Rectangle handleRect =
	            new Rectangle(
	                    handleX,
	                    handleY,
	                    RESIZE_HANDLE_SIZE,
	                    RESIZE_HANDLE_SIZE);

	    return handleRect.contains(x, y);
	}
	
	private boolean isOnItemResizeHandle(LayoutItem item, int x, int y) {

	    if (item == null) {
	        return false;
	    }

	    int handleX =
	            item.getX() + item.getWidth() - RESIZE_HANDLE_SIZE / 2;

	    int handleY =
	            item.getY() + item.getHeight() - RESIZE_HANDLE_SIZE / 2;

	    Rectangle handleRect =
	            new Rectangle(
	                    handleX,
	                    handleY,
	                    RESIZE_HANDLE_SIZE,
	                    RESIZE_HANDLE_SIZE);

	    return handleRect.contains(x, y);
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

	    	if (selectedItem != null || selectedRoomObject != null) {
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
		
		
		setPreferredSize(
		        new Dimension(
		                LEFT_RULER_WIDTH + sheetWidth,
		                TOP_RULER_HEIGHT + sheetHeight));
		
		
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		refreshPanels();
        resetHistory();
		
	}
	
	public void resizeSelectedItem(int delta) {
		
		if (selectedRoomObject != null) {

			if (isLockedRoomObject(selectedRoomObject)) {
				return;
			}

	        int newWidth = selectedRoomObject.getWidth() + delta;
	        int newHeight = selectedRoomObject.getHeight() + delta;

	        if (newWidth < 10) {
	            newWidth = 10;
	        }

	        if (newHeight < 10) {
	            newHeight = 10;
	        }

	        selectedRoomObject.setWidth(newWidth);
	        selectedRoomObject.setHeight(newHeight);

	        notifyChanged();

	        repaint();

	        return;
	    }

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
	    
	    if(drawLineMode) {
	    		roomObjectAddMode = null;
	    }

	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;
	    selectedRoomObject = null;

	    refreshPanels();

	    repaint();
	}
	
	public void moveSelectedItem(int dx, int dy) {
		
		if (selectedRoomObject != null) {

			if (isLockedRoomObject(selectedRoomObject)) {
				return;
			}

	        selectedRoomObject.setX(selectedRoomObject.getX() + dx);
	        selectedRoomObject.setY(selectedRoomObject.getY() + dy);

	        notifyChanged();

	        repaint();

	        return;
	    }

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

            drawSheetBackground(g2);

            g2.translate(SHEET_MARGIN, SHEET_MARGIN);

		
		//グリッド線の色
		 if (showGrid && roomTemplate == null) {
		     drawGrid(g2);
		    }
		
		drawRoomTemplate(g2);
		
		drawCustomRoomObjects(g2);
		
		drawLines(g2);
		
		drawLinePreview(g2);
		
		drawLineStartPoint(g2);
		
		drawItems(g2);

	    g2.dispose();
	}

    private void drawSheetBackground(Graphics2D g2) {

        int width = getSheetContentWidth();
        int height = getSheetContentHeight();

        g2.setColor(new Color(238, 240, 242));
        g2.fillRect(0, 0, width + SHEET_MARGIN * 2, height + SHEET_MARGIN * 2);

        g2.setColor(Color.WHITE);
        g2.fillRect(SHEET_MARGIN, SHEET_MARGIN, width, height);

        g2.setColor(new Color(170, 175, 180));
        g2.drawRect(SHEET_MARGIN, SHEET_MARGIN, width, height);
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
        			
        			drawItemResizeHandle(g2, item);
        			
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
		
		// 四角エリア追加モード
		if ("RECT".equals(roomObjectAddMode)) {

            if (stageLocked) {
                return;
            }

	        int x = canvasX;
	        int y = canvasY;

	        if (snapToGrid) {
	            x = snapValue(x);
	            y = snapValue(y);
	        }

	        addRoomRect(x, y);

	        return;
	    }
		//円柱追加モード
		if ("CIRCLE".equals(roomObjectAddMode)) {

            if (stageLocked) {
                return;
            }

	 	    int x = canvasX;
	 	    int y = canvasY;

	 	    if (snapToGrid) {
	 	        x = snapValue(x);
	 	        y = snapValue(y);
	 	    }

	 	    addRoomCircle(x, y);

	 	    return;
	 	}
		// 線描画モード
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
	 
		// 機材選択を先にする
	    selectedItem = findItem(canvasX, canvasY);


	    if (selectedItem != null) {
	    	
	    		selectedRoomObject = null;
	    		
	    		refreshPanels();

	        repaint();

	        return;
	    }
	    
	    // 次に四角エリア選択
	 	selectedRoomObject = findEditableRoomObject(canvasX, canvasY);

	 	if (selectedRoomObject != null) {

	 		 selectedItem = null;

	 		 refreshPanels();

	 		 repaint();

	 		 return;
	 		}
	 	
	 	

	    // 空いてる場所をダブルクリックした時だけ機材を追加する
	    if (e.getClickCount() < 2) {
	    	
	    		selectedItem = null;
	        selectedRoomObject = null;

	        refreshPanels();

	        repaint();

	        return;
	    }

	    String name = equipmentPanel.getSelectedEquipment();

	    if (name == null) {
	        return;
	    }

	    EquipmentDefinition definition =
	            EquipmentFactory.getDefinitions().get(name);

	    if (isVenueImagePart(definition)) {

            if (stageLocked) {
                return;
            }

	        int x = canvasX;
	        int y = canvasY;

	        if (snapToGrid) {
	            x = snapValue(x);
	            y = snapValue(y);
	        }

	        addRoomImageObject(definition, x, y);

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
	    selectedRoomObject = null;

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

	        if (drawLineMode) {
	            finishCurrentLine();
	            return;
	        }

	        showPopupMenu(e);
	        return;
	    }

	    if (drawLineMode) {
	        return;
	    }
	    
	    if (selectedItem != null
	            && isOnItemResizeHandle(
	                    selectedItem,
	                    canvasX,
	                    canvasY)) {

	        resizingItem = true;

	        itemResizeStartMouseX = canvasX;
	        itemResizeStartMouseY = canvasY;

	        itemResizeStartWidth = selectedItem.getWidth();
	        itemResizeStartHeight = selectedItem.getHeight();

	        repaint();

	        return;
	    }
	    
	    if (selectedRoomObject != null
	            && !isLockedRoomObject(selectedRoomObject)
	            && isOnRoomObjectResizeHandle(
	                    selectedRoomObject,
	                    canvasX,
	                    canvasY)) {

	        resizingRoomObject = true;

	        resizeStartMouseX = canvasX;
	        resizeStartMouseY = canvasY;

	        resizeStartWidth = selectedRoomObject.getWidth();
	        resizeStartHeight = selectedRoomObject.getHeight();

	        repaint();

	        return;
	    }

	    // 先に機材を探す
	    selectedItem = findItem(canvasX, canvasY);

	    if (selectedItem != null) {

	        selectedRoomObject = null;

	        dragOffsetX = canvasX - selectedItem.getX();
	        dragOffsetY = canvasY - selectedItem.getY();

	        dragging = true;

	        refreshPanels();

	        repaint();

	        return;
	    }

	    // 次に四角エリアを探す
	    selectedRoomObject = findEditableRoomObject(canvasX, canvasY);

	    if (selectedRoomObject != null) {

	        selectedItem = null;

	        roomObjectDragOffsetX = canvasX - selectedRoomObject.getX();
	        roomObjectDragOffsetY = canvasY - selectedRoomObject.getY();

	        draggingRoomObject = true;

	        refreshPanels();

	        repaint();

	        return;
	    }

	    selectedItem = null;
	    selectedRoomObject = null;

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

		if (draggingRoomObject) {
		    notifyChanged();
		}

		if (resizingRoomObject) {
		    notifyChanged();
		}
		
		if (resizingItem) {
		    notifyChanged();
		}

		draggingRoomObject = false;

		resizingRoomObject = false;
		
		resizingItem = false;

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
		
		if (resizingItem && selectedItem != null) {

		    int dx = canvasX - itemResizeStartMouseX;
		    int dy = canvasY - itemResizeStartMouseY;

		    int newWidth = itemResizeStartWidth + dx;
		    int newHeight = itemResizeStartHeight + dy;

		    if (snapToGrid) {
		        newWidth = snapValue(newWidth);
		        newHeight = snapValue(newHeight);
		    }

		    if (newWidth < 10) {
		        newWidth = 10;
		    }

		    if (newHeight < 10) {
		        newHeight = 10;
		    }

		    selectedItem.setSize(newWidth, newHeight);

		    refreshPanels();

		    repaint();

		    return;
		}
		
		if (resizingRoomObject && selectedRoomObject != null) {

		    int dx = canvasX - resizeStartMouseX;
		    int dy = canvasY - resizeStartMouseY;

		    int newWidth = resizeStartWidth + dx;
		    int newHeight = resizeStartHeight + dy;

		    if (snapToGrid) {
		        newWidth = snapValue(newWidth);
		        newHeight = snapValue(newHeight);
		    }

		    if (newWidth < 10) {
		        newWidth = 10;
		    }

		    if (newHeight < 10) {
		        newHeight = 10;
		    }

		    selectedRoomObject.setWidth(newWidth);
		    selectedRoomObject.setHeight(newHeight);

		    repaint();

		    return;
		}
		
		if (draggingRoomObject && selectedRoomObject != null) {

		    int x = canvasX - roomObjectDragOffsetX;
		    int y = canvasY - roomObjectDragOffsetY;

		    if (snapToGrid) {
		        x = snapValue(x);
		        y = snapValue(y);
		    }

		    selectedRoomObject.setX(x);
		    selectedRoomObject.setY(y);

		    repaint();

		    return;
		}
		
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
	    
		if (selectedItem != null
		        && isOnItemResizeHandle(
		                selectedItem,
		                mouseX,
		                mouseY)) {

		    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
		    return;
		}
		
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
	    
	    if (selectedRoomObject != null
	            && !isLockedRoomObject(selectedRoomObject)
	            && isOnRoomObjectResizeHandle(
	                    selectedRoomObject,
	                    mouseX,
	                    mouseY)) {

	        setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
	        return;
	    }
		
		LayoutItem item = findItem(mouseX, mouseY);
		
		if(item != null) {
			
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return;
		}
		
		RoomObject roomObject = findEditableRoomObject(mouseX, mouseY);

	    if (roomObject != null) {
	        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        return;
	    }

	    setCursor(Cursor.getDefaultCursor());
	}
		
		
	
	@Override
	public void keyPressed(KeyEvent e) {

        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            undo();
            return;
        }

        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
            redo();
            return;
        }

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
		
		if (selectedRoomObject != null) {

			if (isLockedRoomObject(selectedRoomObject)) {
				return;
			}

		    customRoomObjects.remove(selectedRoomObject);

		    selectedRoomObject = null;

		    notifyChanged();

		    repaint();

		    return;
		}
		
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
	
	public void clearAll() {

	    items.clear();

	    customRoomObjects.clear();

	    drawLines.clear();

	    selectedItem = null;
	    selectedRoomObject = null;
	    copiedItem = null;

	    drawLineMode = false;
	    roomObjectAddMode = null;

	    lineStartX = null;
	    lineStartY = null;

	    dragging = false;
	    draggingRoomObject = false;
	    resizingRoomObject = false;
	    resizingItem = false;

	    refreshPanels();

	    notifyChanged();

	    repaint();

	    requestFocusInWindow();
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

        recordHistory();

	    if (changeCallback != null) {
	        changeCallback.run();
	    }
	}

    public void undo() {

        if (undoHistory.size() <= 1) {
            return;
        }

        redoHistory.addLast(undoHistory.removeLast());
        restoreSnapshot(undoHistory.peekLast());
    }

    public void redo() {

        if (redoHistory.isEmpty()) {
            return;
        }

        LayoutSnapshot snapshot = redoHistory.removeLast();

        undoHistory.addLast(snapshot);
        restoreSnapshot(snapshot);
    }

    public void resetHistory() {

        undoHistory.clear();
        redoHistory.clear();
        recordHistory();
    }

    private void recordHistory() {

        if (restoringSnapshot) {
            return;
        }

        undoHistory.addLast(createSnapshot());

        while (undoHistory.size() > MAX_HISTORY_SIZE) {
            undoHistory.removeFirst();
        }

        redoHistory.clear();
    }

    private LayoutSnapshot createSnapshot() {

        return new LayoutSnapshot(
                copyItems(items),
                copyRoomObjects(customRoomObjects),
                copyDrawLines(drawLines));
    }

    private void restoreSnapshot(LayoutSnapshot snapshot) {

        if (snapshot == null) {
            return;
        }

        restoringSnapshot = true;

        items = copyItems(snapshot.items);
        customRoomObjects = copyRoomObjects(snapshot.roomObjects);
        drawLines = copyDrawLines(snapshot.lines);

        selectedItem = null;
        selectedRoomObject = null;
        dragging = false;
        draggingRoomObject = false;
        resizingItem = false;
        resizingRoomObject = false;

        refreshPanels();
        repaint();

        if (changeCallback != null) {
            changeCallback.run();
        }

        restoringSnapshot = false;
    }

    private List<LayoutItem> copyItems(List<LayoutItem> sourceItems) {

        List<LayoutItem> copiedItems = new ArrayList<>();

        if (sourceItems == null) {
            return copiedItems;
        }

        for (LayoutItem item : sourceItems) {

            LayoutItem copiedItem =
                    new LayoutItem(
                            EquipmentFactory.create(item.getEquipment().getName()),
                            item.getX(),
                            item.getY());

            copiedItem.setSize(item.getWidth(), item.getHeight());
            copiedItem.setQuantity(item.getQuantity());
            copiedItem.setRotation(item.getRotation());
            copiedItem.setMemo(item.getMemo());
            copiedItem.setLabel(item.getLabel());

            copiedItems.add(copiedItem);
        }

        return copiedItems;
    }

    private List<RoomObject> copyRoomObjects(List<RoomObject> sourceObjects) {

        List<RoomObject> copiedObjects = new ArrayList<>();

        if (sourceObjects == null) {
            return copiedObjects;
        }

        for (RoomObject object : sourceObjects) {
            copiedObjects.add(copyRoomObject(object));
        }

        return copiedObjects;
    }

    private RoomObject copyRoomObject(RoomObject object) {

        if (RoomObject.TYPE_IMAGE.equals(object.getType())) {
            return RoomObject.createImage(
                    object.getName(),
                    object.getX(),
                    object.getY(),
                    object.getWidth(),
                    object.getHeight(),
                    object.getImagePath());
        }

        if (RoomObject.TYPE_CIRCLE.equals(object.getType())) {
            return RoomObject.createCircle(
                    object.getName(),
                    object.getX(),
                    object.getY(),
                    object.getWidth(),
                    object.getHeight());
        }

        if (RoomObject.TYPE_LINE.equals(object.getType())) {
            return RoomObject.createLine(
                    object.getName(),
                    object.getX(),
                    object.getY(),
                    object.getEndX(),
                    object.getEndY());
        }

        if (RoomObject.TYPE_ARC.equals(object.getType())) {
            return RoomObject.createArc(
                    object.getName(),
                    object.getX(),
                    object.getY(),
                    object.getWidth(),
                    object.getHeight());
        }

        if (RoomObject.TYPE_TEXT.equals(object.getType())) {
            return RoomObject.createText(
                    object.getName(),
                    object.getX(),
                    object.getY());
        }

        return new RoomObject(
                object.getName(),
                object.getX(),
                object.getY(),
                object.getWidth(),
                object.getHeight());
    }

    private List<DrawLine> copyDrawLines(List<DrawLine> sourceLines) {

        List<DrawLine> copiedLines = new ArrayList<>();

        if (sourceLines == null) {
            return copiedLines;
        }

        for (DrawLine line : sourceLines) {

            DrawLine copiedLine =
                    new DrawLine(
                            line.getStartX(),
                            line.getStartY(),
                            line.getEndX(),
                            line.getEndY());

            copiedLine.setColor(line.getColor());
            copiedLine.setStrokeWidth(line.getStrokeWidth());
            copiedLine.setLabel(line.getLabel());

            copiedLines.add(copiedLine);
        }

        return copiedLines;
    }

    private static class LayoutSnapshot {

        private final List<LayoutItem> items;

        private final List<RoomObject> roomObjects;

        private final List<DrawLine> lines;

        LayoutSnapshot(
                List<LayoutItem> items,
                List<RoomObject> roomObjects,
                List<DrawLine> lines) {

            this.items = items;
            this.roomObjects = roomObjects;
            this.lines = lines;
        }
    }
	
	public RoomTemplate getRoomTemplate() {
	    return roomTemplate;
	}
	
	private void drawGrid(Graphics g) {
		
		int canvasWidth = getSheetContentWidth();
	    int canvasHeight = getSheetContentHeight();

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

        double scaledMargin = SHEET_MARGIN * zoom;

	    for (double x = 0;
	            LEFT_RULER_WIDTH + scaledMargin + x < panelWidth;
	            x += pixelsPerHalfMeter) {

	        int drawX =
	                LEFT_RULER_WIDTH
	                + (int) Math.round(scaledMargin + x);

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
	            TOP_RULER_HEIGHT + scaledMargin + y < panelHeight;
	            y += pixelsPerHalfMeter) {

	        int drawY =
	                TOP_RULER_HEIGHT
	                + (int) Math.round(scaledMargin + y);

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
	    
	    if (drawLineMode) {
	        roomObjectAddMode = null;
	    }

	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;
	    selectedRoomObject = null;

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

	    if (zoom < 0.2) {
	        zoom = 0.2;
	    }

	    if (zoom > 2.0) {
	        zoom = 2.0;
	    }

	    this.zoom = zoom;
	    
	    int width = LEFT_RULER_WIDTH + (int) Math.round(getZoomBaseWidth() * zoom);
	    int height = TOP_RULER_HEIGHT + (int) Math.round(getZoomBaseHeight() * zoom);
	    
	    setPreferredSize(new Dimension(width, height));

	    revalidate();
	    repaint();
	}

	public double getZoom() {
	    return zoom;
	}

    public void fitToView(Dimension viewportSize) {

        if (viewportSize == null
                || viewportSize.width <= LEFT_RULER_WIDTH
                || viewportSize.height <= TOP_RULER_HEIGHT) {
            return;
        }

        double widthZoom =
                (double) (viewportSize.width - LEFT_RULER_WIDTH - 40)
                / getZoomBaseWidth();

        double heightZoom =
                (double) (viewportSize.height - TOP_RULER_HEIGHT - 40)
                / getZoomBaseHeight();

        setZoom(Math.min(widthZoom, heightZoom));
    }

    private int getZoomBaseWidth() {

        if (roomTemplate != null) {
            return roomTemplate.getWidth() + SHEET_MARGIN * 2;
        }

        return sheetWidth + SHEET_MARGIN * 2;
    }

    private int getZoomBaseHeight() {

        if (roomTemplate != null) {
            return roomTemplate.getHeight() + SHEET_MARGIN * 2;
        }

        return sheetHeight + SHEET_MARGIN * 2;
    }

    private int getSheetContentWidth() {

        if (roomTemplate != null) {
            return roomTemplate.getWidth();
        }

        return sheetWidth;
    }

    private int getSheetContentHeight() {

        if (roomTemplate != null) {
            return roomTemplate.getHeight();
        }

        return sheetHeight;
    }

    public void setSheetSizeMeters(double widthMeters, double heightMeters) {

        if (widthMeters < 2.0) {
            widthMeters = 2.0;
        }

        if (heightMeters < 2.0) {
            heightMeters = 2.0;
        }

        sheetWidth = metersToPixels(widthMeters);
        sheetHeight = metersToPixels(heightMeters);

        setRoomTemplate(null);
        setZoom(zoom);
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
	
	public void setRoomObjectAddMode(String mode) {

        if (stageLocked && mode != null) {
            return;
        }

	    this.roomObjectAddMode = mode;
	    
	    drawLineMode = false;
	    
	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;
	    selectedRoomObject = null;
	    
	    refreshPanels();

	    repaint();
	}
	
	private void addRoomRect(int x, int y) {

	    int width = gridSizeForRoomObject(10);
	    int height = gridSizeForRoomObject(5);

	    RoomObject object =
	            new RoomObject(
	                    "床・エリア",
	                    x,
	                    y,
	                    width,
	                    height);

	    customRoomObjects.add(object);

	    selectedRoomObject = object;

	    notifyChanged();

	    repaint();
	}
	
	private void addRoomCircle(int x, int y) {

	    int size = gridSizeForRoomObject(2);

	    RoomObject object =
	            RoomObject.createCircle(
	                    "柱・円",
	                    x,
	                    y,
	                    size,
	                    size);

	    customRoomObjects.add(object);

	    selectedRoomObject = object;

	    notifyChanged();

	    repaint();
	}

	private void addRoomImageObject(
	        EquipmentDefinition definition,
	        int x,
	        int y) {

	    RoomObject object =
	            RoomObject.createImage(
	                    definition.getName(),
	                    x,
	                    y,
	                    definition.getWidth(),
	                    definition.getHeight(),
	                    definition.getImagePath());

	    customRoomObjects.add(object);

	    selectedRoomObject = object;

	    notifyChanged();

	    repaint();
	}

	private boolean isVenueImagePart(EquipmentDefinition definition) {

	    return definition != null
	            && "舞台 > 会場パーツ".equals(definition.getCategory())
	            && definition.getImagePath() != null
	            && !definition.getImagePath().isBlank();
	}

	private int gridSizeForRoomObject(int gridCount) {
	    return gridCount * GRID_SIZE;
	}
	
	private void drawCustomRoomObjects(Graphics g) {
		
		 Graphics2D g2 = (Graphics2D) g.create();

	    for (RoomObject object : customRoomObjects) {

	        boolean selected =
	                object == selectedRoomObject
	                && !isLockedRoomObject(object);

	        if (RoomObject.TYPE_IMAGE.equals(object.getType())) {

	            drawRoomImageObject(g2, object, selected);

	            if (selected) {
	                drawRoomObjectResizeHandle(g2, object);
	            }

	            continue;
	        }

	        Color fillColor = getRoomObjectFillColor(object);

	        if (RoomObject.TYPE_CIRCLE.equals(object.getType())) {

	            g2.setColor(fillColor);
	            g2.fillOval(
	                    object.getX(),
	                    object.getY(),
	                    object.getWidth(),
	                    object.getHeight());

	            g2.setColor(selected
	                    ? new Color(80, 130, 255)
	                    : ROOM_OBJECT_BORDER_COLOR);
	            g2.setStroke(new BasicStroke(selected ? 3 : 1));
	            g2.drawOval(
	                    object.getX(),
	                    object.getY(),
	                    object.getWidth(),
	                    object.getHeight());

	        } else {

	            g2.setColor(fillColor);
	            g2.fillRect(
	                    object.getX(),
	                    object.getY(),
	                    object.getWidth(),
	                    object.getHeight());

	            g2.setColor(selected
	                    ? new Color(80, 130, 255)
	                    : ROOM_OBJECT_BORDER_COLOR);
	            g2.setStroke(new BasicStroke(selected ? 3 : 1));
	            g2.drawRect(
	                    object.getX(),
	                    object.getY(),
	                    object.getWidth(),
	                    object.getHeight());
	        }
	        
	        if (selected) {
	            drawRoomObjectResizeHandle(g2, object);
	        }

	        if (showNames) {
	            drawCenteredRoomObjectName(g2, object);
	        }
	    }
	    g2.dispose();
	}

	private void drawRoomImageObject(
	        Graphics2D g2,
	        RoomObject object,
	        boolean selected) {

	    Image image = getRoomObjectImage(object.getImagePath());

	    if (image != null) {
	        g2.drawImage(
	                image,
	                object.getX(),
	                object.getY(),
	                object.getWidth(),
	                object.getHeight(),
	                this);
	    }

	    if (selected) {
	        g2.setColor(new Color(80, 130, 255));
	        g2.setStroke(new BasicStroke(3));
	        g2.drawRect(
	                object.getX(),
	                object.getY(),
	                object.getWidth(),
	                object.getHeight());
	    }
	}

	private Image getRoomObjectImage(String imagePath) {

	    if (imagePath == null || imagePath.isBlank()) {
	        return null;
	    }

	    Image image = roomObjectImageCache.get(imagePath);

	    if (image == null) {
	        image = ImageLoader.load(imagePath);
	        roomObjectImageCache.put(imagePath, image);
	    }

	    return image;
	}

	private Color getRoomObjectFillColor(RoomObject object) {

	    if (RoomObject.TYPE_CIRCLE.equals(object.getType())) {
	        return COLUMN_FILL_COLOR;
	    }

	    String name = object.getName();

	    if (name != null
	            && (name.contains("客席") || name.contains("Audience"))) {
	        return SEAT_AREA_FILL_COLOR;
	    }

	    return STAGE_FILL_COLOR;
	}

	private void drawCenteredRoomObjectName(Graphics2D g2, RoomObject object) {

	    if (object.getName() == null || object.getName().isBlank()) {
	        return;
	    }

	    FontMetrics metrics = g2.getFontMetrics();

	    int textWidth = metrics.stringWidth(object.getName());
	    int textHeight = metrics.getAscent();

	    int textX = object.getX() + (object.getWidth() - textWidth) / 2;
	    int textY =
	            object.getY()
	            + (object.getHeight() + textHeight) / 2
	            - 3;

	    g2.setColor(Color.BLACK);
	    g2.setStroke(new BasicStroke(1));
	    g2.drawString(object.getName(), textX, textY);
	}
	
	private void drawRoomObjectResizeHandle(Graphics2D g2, RoomObject object) {

	    int handleX =
	            object.getX() + object.getWidth() - RESIZE_HANDLE_SIZE / 2;

	    int handleY =
	            object.getY() + object.getHeight() - RESIZE_HANDLE_SIZE / 2;

	    g2.setColor(Color.WHITE);

	    g2.fillRect(
	            handleX,
	            handleY,
	            RESIZE_HANDLE_SIZE,
	            RESIZE_HANDLE_SIZE);

	    g2.setColor(Color.BLACK);

	    g2.drawRect(
	            handleX,
	            handleY,
	            RESIZE_HANDLE_SIZE,
	            RESIZE_HANDLE_SIZE);
	}
	
	
	private RoomObject findRoomObject(int x, int y) {

	    for (int i = customRoomObjects.size() - 1; i >= 0; i--) {

	        RoomObject object = customRoomObjects.get(i);

	        Rectangle rect =
	                new Rectangle(
	                        object.getX(),
	                        object.getY(),
	                        object.getWidth(),
	                        object.getHeight());

	        if (rect.contains(x, y)) {
	            return object;
	        }
	    }

	    return null;
	}

	private RoomObject findEditableRoomObject(int x, int y) {

	    for (int i = customRoomObjects.size() - 1; i >= 0; i--) {

	        RoomObject object = customRoomObjects.get(i);

	        if (isLockedRoomObject(object)) {
	            continue;
	        }

	        Rectangle rect =
	                new Rectangle(
	                        object.getX(),
	                        object.getY(),
	                        object.getWidth(),
	                        object.getHeight());

	        if (rect.contains(x, y)) {
	            return object;
	        }
	    }

	    return null;
	}

	private boolean isLockedRoomObject(RoomObject object) {

	    return stageLocked
	            && object != null;
	}

	public void setStageLocked(boolean stageLocked) {

	    this.stageLocked = stageLocked;

	    if (stageLocked) {
	        selectedRoomObject = null;
	        draggingRoomObject = false;
	        resizingRoomObject = false;
	    }

	    repaint();
	}

	public boolean isStageLocked() {
	    return stageLocked;
	}
	
	public void setSelectMode() {

	    drawLineMode = false;

	    roomObjectAddMode = null;

	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;
	    selectedRoomObject = null;

	    repaint();
	}
	
	public List<RoomObject> getCustomRoomObjects() {
	    return customRoomObjects;
	}

	public void setCustomRoomObjects(List<RoomObject> customRoomObjects) {

	    this.customRoomObjects = customRoomObjects;

	    selectedRoomObject = null;

	    repaint();
	}

	public List<DrawLine> getDrawLines() {
	    return drawLines;
	}

	public void setDrawLines(List<DrawLine> drawLines) {

	    this.drawLines = drawLines;

	    lineStartX = null;
	    lineStartY = null;

	    repaint();
	}
	
	private void drawItemResizeHandle(Graphics2D g2, LayoutItem item) {

	    int handleX =
	            item.getX() + item.getWidth() - RESIZE_HANDLE_SIZE / 2;

	    int handleY =
	            item.getY() + item.getHeight() - RESIZE_HANDLE_SIZE / 2;

	    g2.setColor(Color.WHITE);

	    g2.fillRect(
	            handleX,
	            handleY,
	            RESIZE_HANDLE_SIZE,
	            RESIZE_HANDLE_SIZE);

	    g2.setColor(Color.BLACK);

	    g2.drawRect(
	            handleX,
	            handleY,
	            RESIZE_HANDLE_SIZE,
	            RESIZE_HANDLE_SIZE);
	}

}
