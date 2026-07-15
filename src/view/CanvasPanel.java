package view;

import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import model.BackgroundMap;
import model.DrawLine;
import model.Equipment;
import model.EquipmentDefinition;
import model.EquipmentFactory;
import model.LayoutItem;
import model.RoomObject;
import model.RoomTemplate;
import model.TextBoxItem;
import util.BackgroundImageLoader;
import util.ImageLoader;



public class CanvasPanel extends JPanel implements MouseListener, 
												  MouseMotionListener,
												  KeyListener {
	
	private EquipmentPanel equipmentPanel;
	
	private List<LayoutItem> items = new ArrayList<>();

    private List<TextBoxItem> textBoxes = new ArrayList<>();
	
	private LayoutItem selectedItem;

    private TextBoxItem selectedTextBox;

    private TextBoxItem hoveredTextBox;

    private boolean textMode = false;
	
	private boolean drawLineMode = false;

	private Integer lineStartX = null;

	private Integer lineStartY = null;
	
	private int mouseX;
	private int mouseY;
	
	private Color currentLineColor = Color.RED;

	private int currentLineStrokeWidth = 5;

    private String currentLineLabel = "";

    private String currentLineType = DrawLine.TYPE_NORMAL;

    private int nextLineGroupNumber = 1;

    private String activeLineGroupId = "";
	
	private LayoutItem copiedItem;

    private TextBoxItem copiedTextBox;

    private RoomObject copiedRoomObject;
	
	private boolean dragging = false;

    private boolean alignmentGuideEnabled = false;

    private Integer activeAlignmentGuideX = null;

    private Integer activeAlignmentGuideY = null;

    private static final int ALIGNMENT_GUIDE_THRESHOLD = 8;

    private boolean popupShownOnPress = false;

    private static final Color SELECTION_COLOR = new Color(40, 120, 220);

    private static final Color SELECTION_HANDLE_BORDER_COLOR = new Color(25, 95, 190);
	
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

    private boolean rotatingItem = false;

	private int itemResizeStartMouseX;

	private int itemResizeStartMouseY;

	private int itemResizeStartWidth;

	private int itemResizeStartHeight;

    private boolean resizingTextBox = false;

    private int textBoxResizeStartMouseX;

    private int textBoxResizeStartMouseY;

    private int textBoxResizeStartWidth;

    private int textBoxResizeStartHeight;

	private static final int RESIZE_HANDLE_SIZE = 12;

    private static final int ROTATE_HANDLE_SIZE = 12;

    private static final int ROTATE_HANDLE_DISTANCE = 24;

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
        // 画面上のマウス位置を、ルーラー余白、ズーム、作業シート余白を除いた座標に戻す。
        // 以降の選択、移動、保存はこのキャンバス座標を基準にする。
	    return (int) Math.round((e.getX() - LEFT_RULER_WIDTH) / zoom)
	            - SHEET_MARGIN;
	}

	private int toCanvasY(MouseEvent e) {
        // X座標と同じ考え方で、表示上のY座標を作業シート内のY座標へ変換する。
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

        return isPointOnRotatedItemShape(
                item,
                getItemResizeHandle(item),
                x,
                y);
	}

    private boolean isOnItemRotateHandle(LayoutItem item, int x, int y) {

        if (item == null) {
            return false;
        }

        return isPointOnRotatedItemShape(
                item,
                getItemRotateHandle(item),
                x,
                y);
    }

    private Rectangle getItemResizeHandle(LayoutItem item) {

        int handleX =
                item.getX() + item.getWidth() - RESIZE_HANDLE_SIZE / 2;

        int handleY =
                item.getY() + item.getHeight() - RESIZE_HANDLE_SIZE / 2;

        return new Rectangle(
                handleX,
                handleY,
                RESIZE_HANDLE_SIZE,
                RESIZE_HANDLE_SIZE);
    }

    private boolean isPointOnRotatedItemShape(
            LayoutItem item,
            Shape shape,
            int x,
            int y) {

        double centerX = item.getX() + item.getWidth() / 2.0;
        double centerY = item.getY() + item.getHeight() / 2.0;

        AffineTransform transform =
                AffineTransform.getRotateInstance(
                        Math.toRadians(item.getRotation()),
                        centerX,
                        centerY);

        return transform.createTransformedShape(shape).contains(x, y);
    }

    private Rectangle getItemRotateHandle(LayoutItem item) {

        int handleX =
                item.getX()
                + item.getWidth() / 2
                - ROTATE_HANDLE_SIZE / 2;

        int handleY =
                item.getY()
                - ROTATE_HANDLE_DISTANCE
                - ROTATE_HANDLE_SIZE / 2;

        return new Rectangle(
                handleX,
                handleY,
                ROTATE_HANDLE_SIZE,
                ROTATE_HANDLE_SIZE);
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

    private DrawLine selectedLine;

    private boolean draggingLineStart = false;

    private boolean draggingLineEnd = false;

    private boolean draggingLineGroup = false;

    private int lastLineDragX;

    private int lastLineDragY;

    private BackgroundMap backgroundMap;

    private BufferedImage backgroundImage;

    private boolean backgroundSelected = false;

    private boolean draggingBackground = false;

    private boolean draggingTextBox = false;

    private int backgroundDragOffsetX;

    private int backgroundDragOffsetY;

    private int textBoxDragOffsetX;

    private int textBoxDragOffsetY;
	
	private void showPopupMenu(MouseEvent e){

        selectTargetForPopup(toCanvasX(e), toCanvasY(e));

	    JPopupMenu menu = new JPopupMenu();

        if (selectedItem != null) {
            JMenuItem editLabelItem = new JMenuItem("表示名を変更");
            editLabelItem.addActionListener(event -> editSelectedItemLabel());
            menu.add(editLabelItem);

            JMenuItem toggleLabelItem =
                    new JMenuItem(
                            selectedItem.isShowLabel()
                                    ? "ラベルを非表示"
                                    : "ラベルを表示");
            toggleLabelItem.addActionListener(event -> toggleSelectedItemLabel());
            menu.add(toggleLabelItem);

            menu.addSeparator();

            JMenuItem copyItem = new JMenuItem("コピー");
            copyItem.addActionListener(event -> copySelectedItem());
            menu.add(copyItem);

            JMenuItem pasteItem = new JMenuItem("貼り付け");
            pasteItem.setEnabled(copiedItem != null);
            pasteItem.addActionListener(event -> pasteCopiedItem());
            menu.add(pasteItem);

            menu.addSeparator();

            JMenuItem frontItem = new JMenuItem("前面へ");
            frontItem.addActionListener(event -> bringSelectedItemToFront());
            menu.add(frontItem);

            JMenuItem backItem = new JMenuItem("背面へ");
            backItem.addActionListener(event -> sendSelectedItemToBack());
            menu.add(backItem);

            menu.addSeparator();
        } else if (selectedTextBox != null) {
            JMenuItem editTextItem = new JMenuItem("文字を編集");
            editTextItem.addActionListener(event -> editSelectedTextBox());
            menu.add(editTextItem);

            JMenuItem copyTextItem = new JMenuItem("コピー");
            copyTextItem.addActionListener(event -> copySelectedTextBox());
            menu.add(copyTextItem);

            JMenuItem pasteTextItem = new JMenuItem("貼り付け");
            pasteTextItem.setEnabled(copiedTextBox != null);
            pasteTextItem.addActionListener(event -> pasteCopiedTextBox());
            menu.add(pasteTextItem);

            menu.addSeparator();
        } else if (selectedRoomObject != null) {
            JMenuItem copyRoomItem = new JMenuItem("コピー");
            copyRoomItem.addActionListener(event -> copySelectedRoomObject());
            menu.add(copyRoomItem);

            JMenuItem pasteRoomItem = new JMenuItem("貼り付け");
            pasteRoomItem.setEnabled(copiedRoomObject != null);
            pasteRoomItem.addActionListener(event -> pasteCopiedRoomObject());
            menu.add(pasteRoomItem);

            menu.addSeparator();

            JMenuItem frontRoomItem = new JMenuItem("前面へ");
            frontRoomItem.addActionListener(event -> bringSelectedRoomObjectToFront());
            menu.add(frontRoomItem);

            JMenuItem backRoomItem = new JMenuItem("背面へ");
            backRoomItem.addActionListener(event -> sendSelectedRoomObjectToBack());
            menu.add(backRoomItem);

            menu.addSeparator();
        } else if (selectedLine != null) {
            JMenuItem normalLineItem = new JMenuItem("通常線にする");
            normalLineItem.addActionListener(event -> changeSelectedLineType(DrawLine.TYPE_NORMAL));
            menu.add(normalLineItem);

            JMenuItem cableLineItem = new JMenuItem("ケーブル線にする");
            cableLineItem.addActionListener(event -> changeSelectedLineType(DrawLine.TYPE_CABLE));
            menu.add(cableLineItem);

            JMenuItem flowLineItem = new JMenuItem("導線にする");
            flowLineItem.addActionListener(event -> changeSelectedLineType(DrawLine.TYPE_FLOW));
            menu.add(flowLineItem);

            JMenuItem bamiriLineItem = new JMenuItem("バミリ線にする");
            bamiriLineItem.addActionListener(event -> changeSelectedLineType(DrawLine.TYPE_BAMIRI));
            menu.add(bamiriLineItem);

            menu.addSeparator();

        } else if (backgroundSelected && backgroundMap != null) {
            JMenuItem toggleLockItem =
                    new JMenuItem(backgroundMap.isLocked() ? "固定を解除" : "固定する");
            toggleLockItem.addActionListener(event -> toggleBackgroundLocked());
            menu.add(toggleLockItem);

            JMenuItem toggleVisibleItem =
                    new JMenuItem(backgroundMap.isVisible() ? "非表示にする" : "表示する");
            toggleVisibleItem.addActionListener(event -> toggleBackgroundVisible());
            menu.add(toggleVisibleItem);

            menu.addSeparator();
        } else if (copiedItem != null || copiedTextBox != null || copiedRoomObject != null) {
            JMenuItem pasteItem = new JMenuItem("貼り付け");
            pasteItem.addActionListener(event -> pasteBestCopiedObjectAt(toCanvasX(e), toCanvasY(e)));
            menu.add(pasteItem);
            menu.addSeparator();
        }

	    JMenuItem deleteItem = new JMenuItem("削除");

	    deleteItem.addActionListener(event -> {

            if (selectedItem != null
                    || selectedRoomObject != null
                    || selectedLine != null
                    || selectedTextBox != null
                    || backgroundSelected) {
	            deleteSelectedItem();
	        }

	    });

	    menu.add(deleteItem);

        deleteItem.setEnabled(
                selectedItem != null
                        || selectedRoomObject != null
                        || selectedLine != null
                        || selectedTextBox != null
                        || backgroundSelected);

	    menu.show(this,e.getX(),e.getY());

	}

    private void selectTargetForPopup(int canvasX, int canvasY) {

        selectedItem = findItem(canvasX, canvasY);

        if (selectedItem != null) {
            selectedRoomObject = null;
            selectedLine = null;
            selectedTextBox = null;
            backgroundSelected = false;
            refreshPanels();
            repaint();
            return;
        }

        selectedRoomObject = findEditableRoomObject(canvasX, canvasY);

        if (selectedRoomObject != null) {
            selectedLine = null;
            selectedTextBox = null;
            backgroundSelected = false;
            refreshPanels();
            repaint();
            return;
        }

        selectedLine = findLine(canvasX, canvasY);

        if (selectedLine != null) {
            selectedTextBox = null;
            backgroundSelected = false;
            refreshPanels();
            repaint();
            return;
        }

        selectedTextBox = findTextBox(canvasX, canvasY);

        if (selectedTextBox != null) {
            backgroundSelected = false;
            refreshPanels();
            repaint();
            return;
        }

        if (isBackgroundAt(canvasX, canvasY)) {
            backgroundSelected = true;
            refreshPanels();
            repaint();
            return;
        }

        selectedItem = null;
        selectedRoomObject = null;
        selectedLine = null;
        selectedTextBox = null;
        backgroundSelected = false;
        refreshPanels();
        repaint();
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
            currentLineLabel = "";
	    }

	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;
	    selectedRoomObject = null;
        selectedLine = null;
        selectedLine = null;

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
		
        // ルーラーはズーム対象の作業シートとは別物として先に描く。
        // その後、ズームと余白を適用した座標系でシート本体を重ねていく。
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

        // 作業シート上の描画順をここでまとめて管理する。
        // 背景図面を下絵にし、グリッド、会場パーツ、線、機材、文字を上に重ねる。
        drawBackgroundMap(g2);

        if (showGrid) {
            drawGrid(g2);
        }
		
		drawRoomTemplate(g2);
		
		drawCustomRoomObjects(g2);
		
		drawLines(g2);
		
		drawLinePreview(g2);
		
		drawLineStartPoint(g2);
		
		drawItems(g2);

        drawAlignmentGuides(g2);

        drawTextBoxes(g2);

	    g2.dispose();
	}

    private void drawBackgroundMap(Graphics2D g2) {

        if (backgroundMap == null
                || backgroundImage == null
                || !backgroundMap.isVisible()) {
            return;
        }

        Graphics2D imageG = (Graphics2D) g2.create();

        // 背景図面は下絵なので、透明度を付けて機材や線を読みやすくする。
        imageG.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        backgroundMap.getOpacity()));

        double centerX = backgroundMap.getX() + backgroundMap.getWidth() / 2.0;
        double centerY = backgroundMap.getY() + backgroundMap.getHeight() / 2.0;

        imageG.rotate(
                Math.toRadians(backgroundMap.getRotation()),
                centerX,
                centerY);

        imageG.drawImage(
                backgroundImage,
                backgroundMap.getX(),
                backgroundMap.getY(),
                backgroundMap.getWidth(),
                backgroundMap.getHeight(),
                this);

        imageG.dispose();

        if (backgroundSelected) {
            g2.setColor(SELECTION_COLOR);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(
                    backgroundMap.getX(),
                    backgroundMap.getY(),
                    backgroundMap.getWidth(),
                    backgroundMap.getHeight());
        }
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
        	    
                if (showNames && item.isShowLabel()) {
        	        g.drawString(
        	                item.getDisplayName(),
        	                item.getX() + 8,
        	                getRotatedLabelY(item));
        	    }

        	    continue;
        	}
        	
        		g.setColor(item.getEquipment().getColor());
        		
                // 画像付き機材は画像だけを見せる。通常時の黒い四角枠は描かない。
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
        			
        			g2.setColor(SELECTION_COLOR);
        			g2.setStroke(new BasicStroke(2.5f));
        			
        			g2.drawRect(
        					item.getX(),
        					item.getY(),
        					item.getWidth(),
        					item.getHeight());

                    drawItemRotateHandle(g2, item);
        			
        			drawItemResizeHandle(g2, item);
        			
        		}
        		
        		
        		
        		g2.dispose();
        		
        		g.setColor(Color.BLACK);
        	
                if (showNames && item.isShowLabel()) {
        		    g.drawString(
        		            item.getDisplayName(),
        		            item.getX() + 8,
        		            getRotatedLabelY(item));
        		}

        }
        
	}
	
    private void drawAlignmentGuides(Graphics2D g2) {

        if (!alignmentGuideEnabled
                || activeAlignmentGuideX == null && activeAlignmentGuideY == null) {
            return;
        }

        Graphics2D guideG = (Graphics2D) g2.create();

        guideG.setColor(new Color(40, 140, 255, 180));
        guideG.setStroke(
                new BasicStroke(
                        1.5f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10f,
                        new float[] {7f, 5f},
                        0f));

        if (activeAlignmentGuideX != null) {
            guideG.drawLine(
                    activeAlignmentGuideX,
                    0,
                    activeAlignmentGuideX,
                    getSheetContentHeight());
        }

        if (activeAlignmentGuideY != null) {
            guideG.drawLine(
                    0,
                    activeAlignmentGuideY,
                    getSheetContentWidth(),
                    activeAlignmentGuideY);
        }

        guideG.dispose();
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

    private void drawTextBoxes(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        for (TextBoxItem textBox : textBoxes) {

            boolean selected = textBox == selectedTextBox;
            boolean hovered = textBox == hoveredTextBox;

            // テキストは注釈として使うことが多いので、通常時は文字だけにする。
            // 選択中だけ背景、枠、リサイズハンドルを出して編集対象だと分かるようにする。
            if (selected && textBox.isShowBackground()) {
                g2.setColor(textBox.getBackgroundColor());
                g2.fillRect(
                        textBox.getX(),
                        textBox.getY(),
                        textBox.getWidth(),
                        textBox.getHeight());
            }

            if (selected && textBox.isShowBorder()) {
                g2.setColor(new Color(130, 145, 160));
                g2.drawRect(
                        textBox.getX(),
                        textBox.getY(),
                        textBox.getWidth(),
                        textBox.getHeight());
            }

            g2.setFont(new Font("SansSerif", Font.PLAIN, textBox.getFontSize()));
            g2.setColor(textBox.getTextColor());
            drawTextBoxLines(g2, textBox);

            if (hovered && !selected) {
                g2.setColor(new Color(120, 150, 190, 140));
                g2.setStroke(new BasicStroke(1));
                g2.drawRect(
                        textBox.getX() - 2,
                        textBox.getY() - 2,
                        textBox.getWidth() + 4,
                        textBox.getHeight() + 4);
            }

            if (selected) {
                g2.setColor(SELECTION_COLOR);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(
                        textBox.getX() - 3,
                        textBox.getY() - 3,
                        textBox.getWidth() + 6,
                        textBox.getHeight() + 6);
                drawTextBoxResizeHandle(g2, textBox);
            }
        }

        g2.dispose();
    }

    private void drawTextBoxResizeHandle(Graphics2D g2, TextBoxItem textBox) {

        Rectangle handle = getTextBoxResizeHandle(textBox);

        drawResizeHandle(g2, handle.x, handle.y);
    }

    private void drawTextBoxLines(Graphics2D g2, TextBoxItem textBox) {

        FontMetrics metrics = g2.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int textX = textBox.getX() + 8;
        int textY = textBox.getY() + metrics.getAscent() + 6;

        String[] lines = textBox.getText().split("\\R", -1);

        for (String line : lines) {
            g2.drawString(line, textX, textY);
            textY += lineHeight;
        }
    }
	
	private void drawRoomTemplate(Graphics g) {

	    if (roomTemplate == null) {
	        return;
	    }

        // コードで用意した古いテンプレートは、グリッドを重ねると見づらいので面で見せる。
	    // テンプレート背景
	    g.setColor(new Color(250, 250, 250));

	    g.fillRect(
	            0,
	            0,
	            roomTemplate.getWidth(),
	            roomTemplate.getHeight());

	    // テンプレート外枠
	    g.setColor(new Color(90, 96, 102));

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

	    Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(getTemplateObjectFillColor(object));
        g2.fillRect(
                object.getX(),
                object.getY(),
                object.getWidth(),
                object.getHeight());

	    g2.setColor(new Color(90, 96, 102));
        g2.setStroke(new BasicStroke(2));
	    g2.drawRect(
	            object.getX(),
	            object.getY(),
	            object.getWidth(),
	            object.getHeight());

        g2.dispose();

	    drawRoomObjectName(g, object);
	}
	
	private void drawRoomCircle(Graphics g, RoomObject object) {

	    Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(new Color(110, 118, 126));
        g2.fillOval(
                object.getX(),
                object.getY(),
                object.getWidth(),
                object.getHeight());

	    g2.setColor(new Color(60, 66, 72));
	    g2.setStroke(new BasicStroke(2));

	    g2.drawOval(
	            object.getX(),
	            object.getY(),
	            object.getWidth(),
	            object.getHeight());

	    g2.dispose();

	    drawRoomObjectName(g, object);
	}

    private Color getTemplateObjectFillColor(RoomObject object) {

        String name = object.getName();

        if (name == null) {
            return new Color(226, 231, 235);
        }

        // 名前で最低限の色分けをして、214教室テンプレートを図として読みやすくする。
        if (name.contains("黒板")) {
            return new Color(70, 80, 76);
        }

        if (name.contains("ステージ")
                || name.contains("教壇")) {
            return new Color(210, 218, 224);
        }

        if (name.contains("教卓")) {
            return new Color(196, 204, 210);
        }

        return new Color(226, 231, 235);
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

        // 小さい常設物は名前を空にして、ステージ上の表示を邪魔しないようにできる。
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

	        g2.setColor(lineColorForType(line));
	        g2.setStroke(createStrokeForLine(line));

	        g2.drawLine(
	                line.getStartX(),
	                line.getStartY(),
	                line.getEndX(),
	                line.getEndY());

            drawFlowArrowHeadIfNeeded(g2, line);

            if (line == selectedLine) {
                drawSelectedLineHighlight(g2, line);
            }
	        
	        int labelX =
	                (line.getStartX() + line.getEndX()) / 2 + 6;

	        int labelY =
	                (line.getStartY() + line.getEndY()) / 2 - 6;
	        
	        if (showLineLength && line.isShowLength() && !isBamiriLine(line)) {

	            double meters = calculateLineLengthMeters(line);

	            g2.setColor(Color.BLACK);
	            g2.setStroke(new BasicStroke(1));

	            g2.drawString(
	                    formatLineLength(meters),
	                    labelX,
	                    labelY);
	        }

	        if (showNames
                    && line.isShowLabel()
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

    private Color lineColorForType(DrawLine line) {

        if (line == null) {
            return Color.BLACK;
        }

        if (DrawLine.TYPE_CABLE.equals(line.getLineType())) {
            return Color.BLACK;
        }

        if (DrawLine.TYPE_FLOW.equals(line.getLineType())) {
            return new Color(40, 110, 210);
        }

        return line.getColor();
    }

    private BasicStroke createStrokeForLine(DrawLine line) {

        int width = line == null ? 2 : line.getStrokeWidth();

        return new BasicStroke(
                width,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);
    }

    private void drawFlowArrowHeadIfNeeded(Graphics2D g2, DrawLine line) {

        if (line == null || !DrawLine.TYPE_FLOW.equals(line.getLineType())) {
            return;
        }

        drawArrowHead(
                g2,
                line.getStartX(),
                line.getStartY(),
                line.getEndX(),
                line.getEndY(),
                Math.max(10, line.getStrokeWidth() * 3));
    }

    private void drawArrowHead(
            Graphics2D g2,
            int startX,
            int startY,
            int endX,
            int endY,
            int size) {

        double angle = Math.atan2(endY - startY, endX - startX);

        if (Double.isNaN(angle)) {
            return;
        }

        int leftX = (int) Math.round(endX - size * Math.cos(angle - Math.PI / 6));
        int leftY = (int) Math.round(endY - size * Math.sin(angle - Math.PI / 6));
        int rightX = (int) Math.round(endX - size * Math.cos(angle + Math.PI / 6));
        int rightY = (int) Math.round(endY - size * Math.sin(angle + Math.PI / 6));

        Color oldColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();

        g2.setColor(new Color(40, 110, 210));
        g2.setStroke(
                new BasicStroke(
                        Math.max(2, size / 5),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));

        g2.drawLine(endX, endY, leftX, leftY);
        g2.drawLine(endX, endY, rightX, rightY);

        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    private boolean isBamiriLine(DrawLine line) {

        if (line != null && DrawLine.TYPE_BAMIRI.equals(line.getLineType())) {
            return true;
        }

        return line != null
                && line.getLabel() != null
                && line.getLabel().contains("バミリ");
    }

    private boolean isCurrentBamiriLineMode() {

        return currentLineLabel != null
                && currentLineLabel.contains("バミリ");
    }

    private void drawSelectedLineHighlight(Graphics2D g2, DrawLine line) {

        if (line == null) {
            return;
        }

        Color oldColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();

        g2.setColor(new Color(35, 120, 220, 120));
        g2.setStroke(
                new BasicStroke(
                        Math.max(8, line.getStrokeWidth() + 6),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
        g2.drawLine(
                line.getStartX(),
                line.getStartY(),
                line.getEndX(),
                line.getEndY());

        drawSelectedLineHandle(g2, line.getStartX(), line.getStartY());
        drawSelectedLineHandle(g2, line.getEndX(), line.getEndY());

        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    private void drawSelectedLineHandle(Graphics2D g2, int x, int y) {

        g2.setColor(Color.WHITE);
        g2.fillOval(x - 6, y - 6, 12, 12);

        g2.setColor(new Color(20, 100, 210));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x - 6, y - 6, 12, 12);
    }
	
	private void drawBamiri(Graphics g, LayoutItem item) {

	    String name = item.getEquipment().getName();

	    int x = item.getX();
	    int y = item.getY();
	    int w = item.getWidth();
	    int h = item.getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        double centerX = x + w / 2.0;
        double centerY = y + h / 2.0;
        g2.rotate(Math.toRadians(item.getRotation()), centerX, centerY);

	    g2.setColor(item.getEquipment().getColor());

	    if ("バミリ X".equals(name)) {

	        g2.drawLine(x, y, x + w, y + h);
	        g2.drawLine(x + w, y, x, y + h);

	    } else if ("バミリ ＋".equals(name)) {

	        g2.drawLine(x + w / 2, y, x + w / 2, y + h);
	        g2.drawLine(x, y + h / 2, x + w, y + h / 2);

	    } else if (name.endsWith(" L")) {

	        g2.fillRect(x, y, Math.max(3, w / 5), h);
	        g2.fillRect(x, y + h - Math.max(3, h / 5), w, Math.max(3, h / 5));

	    } else if (name.endsWith(" T")) {

	        g2.fillRect(x, y, w, Math.max(3, h / 5));
	        g2.fillRect(x + w / 2 - Math.max(2, w / 10), y, Math.max(4, w / 5), h);

	    } else {

	        // バミリ 横・縦
	        g2.fillRect(x, y, w, h);
	    }

	    if (item == selectedItem) {
	        g2.setColor(Color.RED);
	        g2.drawRect(x - 3, y - 3, w + 6, h + 6);
	    }

        g2.dispose();
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

	    int previewX = mouseX;
	    int previewY = mouseY;

	    int[] adjusted = adjustLinePointForCurrentMode(previewX, previewY);

	    previewX = adjusted[0];
	    previewY = adjusted[1];

        DrawLine previewLine =
                new DrawLine(lineStartX, lineStartY, previewX, previewY);

        previewLine.setColor(currentLineColor);
        previewLine.setStrokeWidth(currentLineStrokeWidth);
        previewLine.setLineType(currentLineType);

        if (isCurrentBamiriLineMode()) {
            previewLine.setLineType(DrawLine.TYPE_BAMIRI);
        }

	    g2.setColor(lineColorForType(previewLine));
	    g2.setStroke(createStrokeForLine(previewLine));

	    g2.drawLine(
	            lineStartX,
	            lineStartY,
	            previewX,
	            previewY);

        drawFlowArrowHeadIfNeeded(g2, previewLine);

	    if (showLineLength && !isCurrentBamiriLineMode()) {

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

        // クリック時は現在の編集モードを最初に見る。
        // 文字、会場パーツ追加、線描画、通常選択が同時に動かないように上から分岐する。
        if (textMode
                && e.getClickCount() >= 2
                && findTextBox(canvasX, canvasY) == null) {
            addTextBoxAt(canvasX, canvasY);
            return;
        }
		
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

            if (lineStartX == null || lineStartY == null) {
                selectedLine = findLine(canvasX, canvasY);

                if (selectedLine != null) {
                    selectedItem = null;
                    selectedRoomObject = null;
                    selectedTextBox = null;
                    backgroundSelected = false;
                    refreshPanels();
                    repaint();
                    return;
                }

                if (e.getClickCount() < 2) {
                    selectedItem = null;
                    selectedRoomObject = null;
                    selectedLine = null;
                    selectedTextBox = null;
                    backgroundSelected = false;
                    refreshPanels();
                    repaint();
                    return;
                }

                selectedItem = null;
                selectedRoomObject = null;
                selectedLine = null;
                selectedTextBox = null;
                backgroundSelected = false;
            }

	        handleDrawLineClick(
	        		canvasX, 
	        		canvasY,
	        		e.isControlDown(),
	        		e.isShiftDown());

	        return;
	    }
	 
        selectedTextBox = findTextBox(canvasX, canvasY);

        if (selectedTextBox != null) {

            selectedItem = null;
            selectedRoomObject = null;
            selectedLine = null;
            backgroundSelected = false;

            if (e.getClickCount() >= 2) {
                editSelectedTextBox();
            }

            refreshPanels();
            repaint();
            return;
        }

		// 機材選択を先にする
	    selectedItem = findItem(canvasX, canvasY);


	    if (selectedItem != null) {
	    	
	    		selectedRoomObject = null;
                selectedLine = null;
                selectedTextBox = null;
                backgroundSelected = false;
	    		
	    		refreshPanels();

	        repaint();

	        return;
	    }
	    
	    // 次に四角エリア選択
	 	selectedRoomObject = findEditableRoomObject(canvasX, canvasY);

	 	if (selectedRoomObject != null) {

	 		 selectedItem = null;
             selectedLine = null;
             selectedTextBox = null;
             backgroundSelected = false;

	 		 refreshPanels();

	 		 repaint();

	 		 return;
	 		}

        selectedLine = findLine(canvasX, canvasY);

        if (selectedLine != null) {

            selectedItem = null;
            selectedRoomObject = null;
            selectedTextBox = null;
            backgroundSelected = false;

            refreshPanels();

            repaint();

            return;
        }
	 	
	 	

	    // 空いてる場所をダブルクリックした時だけ機材を追加する
	    if (e.getClickCount() < 2) {
	    	
	    		selectedItem = null;
	        selectedRoomObject = null;
            selectedLine = null;
            selectedTextBox = null;
            backgroundSelected = false;

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
        selectedLine = null;
        selectedTextBox = null;
        backgroundSelected = false;

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

            if (activeLineGroupId == null || activeLineGroupId.isBlank()) {
                activeLineGroupId =
                        "line-" + System.nanoTime() + "-" + nextLineGroupNumber++;
            }

	        lineStartX = x;
	        lineStartY = y;

	        repaint();

	        return;
	    }

	    // 2回目クリック：線を追加
        if (lineStartX == x && lineStartY == y) {
            return;
        }

	    DrawLine line =
	            new DrawLine(
	                    lineStartX,
	                    lineStartY,
	                    x,
	                    y);

	    line.setColor(currentLineColor);
	    line.setStrokeWidth(currentLineStrokeWidth);
        line.setLabel(currentLineLabel);
        line.setLineType(currentLineType);
        line.setGroupId(activeLineGroupId);
        if (isCurrentBamiriLineMode()) {
            line.setLineType(DrawLine.TYPE_BAMIRI);
            line.setShowLength(false);
        }

	    drawLines.add(line);
        selectedLine = line;
	    
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

        // 右クリックは削除や編集用の入口。
        // 線描画中でも既存の線を右クリックできるよう、線の判定を先に行う。
	    if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {

	        if (drawLineMode) {
                finishCurrentLine();
                return;
	        }

            popupShownOnPress = true;
	        showPopupMenu(e);
	        return;
	    }

	    if (drawLineMode && selectedLine == null) {
            if (lineStartX != null || lineStartY != null) {
                return;
            }

            selectedLine = findLine(canvasX, canvasY);

            if (selectedLine == null) {
                return;
            }

            selectedItem = null;
            selectedRoomObject = null;
            selectedTextBox = null;
            backgroundSelected = false;
            refreshPanels();
	    }

        if (selectedItem != null
                && isOnItemRotateHandle(
                        selectedItem,
                        canvasX,
                        canvasY)) {

            rotatingItem = true;
            repaint();
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

        if (selectedLine != null && isNearLineStart(selectedLine, canvasX, canvasY)) {

            draggingLineStart = true;
            repaint();
            return;
        }

        if (selectedLine != null && isNearLineEnd(selectedLine, canvasX, canvasY)) {

            draggingLineEnd = true;
            repaint();
            return;
        }

        if (selectedLine != null
                && distanceToLineSegment(selectedLine, canvasX, canvasY) <= 8.0) {

            draggingLineGroup = true;
            lastLineDragX = canvasX;
            lastLineDragY = canvasY;
            repaint();
            return;
        }

        if (selectedTextBox != null
                && isOnTextBoxResizeHandle(
                        selectedTextBox,
                        canvasX,
                        canvasY)) {

            resizingTextBox = true;
            textBoxResizeStartMouseX = canvasX;
            textBoxResizeStartMouseY = canvasY;
            textBoxResizeStartWidth = selectedTextBox.getWidth();
            textBoxResizeStartHeight = selectedTextBox.getHeight();
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

	    // 重なっている時は、当日配置する機材を最優先で選ぶ。
        // その後、会場パーツ、線、テキスト、背景図面の順で編集対象を探す。
	    selectedItem = findItem(canvasX, canvasY);

	    if (selectedItem != null) {

	        selectedRoomObject = null;
            selectedLine = null;
            selectedTextBox = null;
            backgroundSelected = false;

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
            selectedLine = null;
            selectedTextBox = null;
            backgroundSelected = false;

	        roomObjectDragOffsetX = canvasX - selectedRoomObject.getX();
	        roomObjectDragOffsetY = canvasY - selectedRoomObject.getY();

	        draggingRoomObject = true;

	        refreshPanels();

	        repaint();

	        return;
	    }

        selectedLine = findLine(canvasX, canvasY);

        if (selectedLine != null) {
            selectedItem = null;
            selectedRoomObject = null;
            selectedTextBox = null;
            backgroundSelected = false;
            refreshPanels();
            repaint();
            return;
        }

        selectedTextBox = findTextBox(canvasX, canvasY);

        if (selectedTextBox != null) {
            selectedItem = null;
            selectedRoomObject = null;
            selectedLine = null;
            backgroundSelected = false;
            draggingTextBox = true;
            textBoxDragOffsetX = canvasX - selectedTextBox.getX();
            textBoxDragOffsetY = canvasY - selectedTextBox.getY();
            refreshPanels();
            repaint();
            return;
        }

        if (isEditableBackgroundAt(canvasX, canvasY)) {
            backgroundSelected = true;
            selectedItem = null;
            selectedRoomObject = null;
            selectedLine = null;
            selectedTextBox = null;
            draggingBackground = true;
            backgroundDragOffsetX = canvasX - backgroundMap.getX();
            backgroundDragOffsetY = canvasY - backgroundMap.getY();
            refreshPanels();
            repaint();
            return;
        }

	    selectedItem = null;
	    selectedRoomObject = null;
        selectedLine = null;
        selectedTextBox = null;
        backgroundSelected = false;

	    refreshPanels();

	    repaint();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		
		if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3){

            if (popupShownOnPress) {
                popupShownOnPress = false;
                return;
            }
			
			if(drawLineMode) {
				finishCurrentLine();
				return;
			}
			showPopupMenu(e);
			return;
		}
		
		if(drawLineMode && !draggingLineGroup) {
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
		
		if (resizingItem || rotatingItem) {
		    notifyChanged();
		}

        if (draggingLineStart || draggingLineEnd || draggingLineGroup) {
            notifyChanged();
        }

        if (draggingTextBox || resizingTextBox || draggingBackground) {
            notifyChanged();
        }

		draggingRoomObject = false;

		resizingRoomObject = false;
		
		resizingItem = false;
        rotatingItem = false;

		dragging = false;
        clearAlignmentGuides();

        draggingLineStart = false;
        draggingLineEnd = false;
        draggingLineGroup = false;
        draggingTextBox = false;
        resizingTextBox = false;
        draggingBackground = false;
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
		
		if(drawLineMode && !draggingLineGroup) {
			return;
		}
		
        int canvasX = toCanvasX(e);
        int canvasY = toCanvasY(e);

        if (rotatingItem && selectedItem != null) {

            int rotation = calculateRotationFromMouse(selectedItem, canvasX, canvasY);

            if (e.isShiftDown()) {
                rotation = Math.round(rotation / 15f) * 15;
            }

            selectedItem.setRotation(normalizeRotation(rotation));
            refreshPanels();
            repaint();
            return;
        }

        if (resizingTextBox && selectedTextBox != null) {

            int dx = canvasX - textBoxResizeStartMouseX;
            int dy = canvasY - textBoxResizeStartMouseY;

            int newWidth = textBoxResizeStartWidth + dx;
            int newHeight = textBoxResizeStartHeight + dy;

            if (snapToGrid) {
                newWidth = snapValue(newWidth);
                newHeight = snapValue(newHeight);
            }

            selectedTextBox.setWidth(Math.max(40, newWidth));
            selectedTextBox.setHeight(Math.max(20, newHeight));

            repaint();
            return;
        }

        if (draggingTextBox && selectedTextBox != null) {
            int x = canvasX - textBoxDragOffsetX;
            int y = canvasY - textBoxDragOffsetY;

            if (snapToGrid) {
                x = snapValue(x);
                y = snapValue(y);
            }

            selectedTextBox.setX(x);
            selectedTextBox.setY(y);

            repaint();

            return;
        }

        if (draggingBackground && backgroundMap != null) {
            backgroundMap.setX(canvasX - backgroundDragOffsetX);
            backgroundMap.setY(canvasY - backgroundDragOffsetY);

            repaint();

            return;
        }

        if (draggingLineGroup && selectedLine != null) {

            int dx = canvasX - lastLineDragX;
            int dy = canvasY - lastLineDragY;

            if (dx != 0 || dy != 0) {
                moveSelectedLineGroup(dx, dy, false);
                lastLineDragX = canvasX;
                lastLineDragY = canvasY;
            }

            repaint();

            return;
        }

        if ((draggingLineStart || draggingLineEnd) && selectedLine != null) {

            int x = canvasX;
            int y = canvasY;

            if (snapToGrid) {
                x = snapValue(x);
                y = snapValue(y);
            }

            if (draggingLineStart) {
                selectedLine.setStart(x, y);
            } else {
                selectedLine.setEnd(x, y);
            }

            repaint();
            return;
        }
		
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

            int[] aligned = applyAlignmentGuide(selectedItem, x, y);
            x = aligned[0];
            y = aligned[1];
			

			selectedItem.setX(x);
			selectedItem.setY(y);

			repaint();

		}
	}

    private int calculateRotationFromMouse(LayoutItem item, int mouseX, int mouseY) {

        double centerX = item.getX() + item.getWidth() / 2.0;
        double centerY = item.getY() + item.getHeight() / 2.0;
        double angle = Math.toDegrees(Math.atan2(mouseY - centerY, mouseX - centerX));

        return (int) Math.round(angle + 90);
    }

    private int normalizeRotation(int rotation) {

        rotation = rotation % 360;

        if (rotation < 0) {
            rotation += 360;
        }

        return rotation;
    }

    private int[] applyAlignmentGuide(LayoutItem item, int x, int y) {

        activeAlignmentGuideX = null;
        activeAlignmentGuideY = null;

        if (!alignmentGuideEnabled || item == null) {
            return new int[] {x, y};
        }

        int bestDiffX = ALIGNMENT_GUIDE_THRESHOLD + 1;
        int bestDiffY = ALIGNMENT_GUIDE_THRESHOLD + 1;
        Integer guideX = null;
        Integer guideY = null;
        int adjustedX = x;
        int adjustedY = y;

        for (LayoutItem other : items) {

            if (other == item) {
                continue;
            }

            int[] movingAnchorsX =
                    new int[] {
                            x,
                            x + item.getWidth() / 2,
                            x + item.getWidth()
                    };
            int[] movingAnchorOffsetsX =
                    new int[] {
                            0,
                            item.getWidth() / 2,
                            item.getWidth()
                    };
            int[] targetAnchorsX =
                    new int[] {
                            other.getX(),
                            other.getX() + other.getWidth() / 2,
                            other.getX() + other.getWidth()
                    };

            for (int movingIndex = 0;
                    movingIndex < movingAnchorsX.length;
                    movingIndex++) {

                for (int targetX : targetAnchorsX) {

                    int diffX = Math.abs(movingAnchorsX[movingIndex] - targetX);

                    if (diffX < bestDiffX) {
                        bestDiffX = diffX;
                        guideX = targetX;
                        adjustedX = targetX - movingAnchorOffsetsX[movingIndex];
                    }
                }
            }

            int[] movingAnchorsY =
                    new int[] {
                            y,
                            y + item.getHeight() / 2,
                            y + item.getHeight()
                    };
            int[] movingAnchorOffsetsY =
                    new int[] {
                            0,
                            item.getHeight() / 2,
                            item.getHeight()
                    };
            int[] targetAnchorsY =
                    new int[] {
                            other.getY(),
                            other.getY() + other.getHeight() / 2,
                            other.getY() + other.getHeight()
                    };

            for (int movingIndex = 0;
                    movingIndex < movingAnchorsY.length;
                    movingIndex++) {

                for (int targetY : targetAnchorsY) {

                    int diffY = Math.abs(movingAnchorsY[movingIndex] - targetY);

                    if (diffY < bestDiffY) {
                        bestDiffY = diffY;
                        guideY = targetY;
                        adjustedY = targetY - movingAnchorOffsetsY[movingIndex];
                    }
                }
            }
        }

        if (guideX != null) {
            x = adjustedX;
            activeAlignmentGuideX = guideX;
        }

        if (guideY != null) {
            y = adjustedY;
            activeAlignmentGuideY = guideY;
        }

        return new int[] {x, y};
    }
	
	
	@Override
	public void mouseMoved(MouseEvent e) {
		
		if (!isInDrawingArea(e)) {
            if (hoveredTextBox != null) {
                hoveredTextBox = null;
                repaint();
            }
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
                && isOnItemRotateHandle(
                        selectedItem,
                        mouseX,
                        mouseY)) {

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

        if (selectedLine != null
                && (isNearLineStart(selectedLine, mouseX, mouseY)
                || isNearLineEnd(selectedLine, mouseX, mouseY))) {

            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            return;
        }

        if (selectedTextBox != null
                && isOnTextBoxResizeHandle(selectedTextBox, mouseX, mouseY)) {

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

        DrawLine line = findLine(mouseX, mouseY);

        if (line != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }

        TextBoxItem textBox = findTextBox(mouseX, mouseY);

        if (hoveredTextBox != textBox) {
            hoveredTextBox = textBox;
            repaint();
        }

        if (textBox != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }

        if (isEditableBackgroundAt(mouseX, mouseY)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
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

        if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedLine != null) {

            deleteSelectedItem();

            return;
        }

        int moveAmount = e.isShiftDown() ? 1 : (snapToGrid ? GRID_SIZE : 5);

        if (selectedLine != null) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                moveSelectedLineGroup(0, -moveAmount);
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                moveSelectedLineGroup(0, moveAmount);
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                moveSelectedLineGroup(-moveAmount, 0);
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                moveSelectedLineGroup(moveAmount, 0);
                return;
            }
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

        if (selectedTextBox != null) {
            textBoxes.remove(selectedTextBox);
            selectedTextBox = null;
            draggingTextBox = false;

            notifyChanged();

            repaint();

            return;
        }

        if (backgroundSelected && backgroundMap != null) {
            backgroundMap = null;
            backgroundImage = null;
            backgroundSelected = false;
            draggingBackground = false;

            notifyChanged();

            repaint();

            return;
        }

        if (selectedLine != null) {

            drawLines.remove(selectedLine);

            selectedLine = null;
            draggingLineStart = false;
            draggingLineEnd = false;
            draggingLineGroup = false;

            notifyChanged();

            repaint();

            return;
        }
		
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

        pasteCopiedItemAt(
                copiedItem.getX() + 30,
                copiedItem.getY() + 30);
	}

    private void pasteCopiedItemAt(int x, int y) {

        if (copiedItem == null) {
            return;
        }

		Equipment equipment =
				EquipmentFactory.create(
						copiedItem.getEquipment().getName());
		
		LayoutItem item =
				new LayoutItem(
						equipment,
						x,
						y);
		
		item.setSize(
		        copiedItem.getWidth(),
		        copiedItem.getHeight());
		
		item.setRotation(copiedItem.getRotation());
		
		item.setMemo(copiedItem.getMemo());
		
		item.setQuantity(copiedItem.getQuantity());
		
		item.setLabel(copiedItem.getLabel());

        item.setShowLabel(copiedItem.isShowLabel());
		
		items.add(item);
		
		selectedItem =item;
		
		refreshPanels();
		
		notifyChanged();
		
		repaint();
	}

    private void editSelectedItemLabel() {

        if (selectedItem == null) {
            return;
        }

        String currentLabel = selectedItem.getLabel();

        if (currentLabel == null || currentLabel.isBlank()) {
            currentLabel = selectedItem.getEquipment().getName();
        }

        String label =
                JOptionPane.showInputDialog(
                        this,
                        "表示名を入力してください",
                        currentLabel);

        if (label == null) {
            return;
        }

        selectedItem.setLabel(label.trim());
        refreshPanels();
        notifyChanged();
        repaint();
    }

    private void toggleSelectedItemLabel() {

        if (selectedItem == null) {
            return;
        }

        selectedItem.setShowLabel(!selectedItem.isShowLabel());
        refreshPanels();
        notifyChanged();
        repaint();
    }

    private void bringSelectedItemToFront() {

        if (selectedItem == null || !items.remove(selectedItem)) {
            return;
        }

        items.add(selectedItem);
        notifyChanged();
        repaint();
    }

    private void sendSelectedItemToBack() {

        if (selectedItem == null || !items.remove(selectedItem)) {
            return;
        }

        items.add(0, selectedItem);
        notifyChanged();
        repaint();
    }

    private void copySelectedTextBox() {

        if (selectedTextBox == null) {
            return;
        }

        copiedTextBox = copyTextBox(selectedTextBox);
    }

    private void pasteCopiedTextBox() {

        if (copiedTextBox == null) {
            return;
        }

        pasteCopiedTextBoxAt(copiedTextBox.getX() + 30, copiedTextBox.getY() + 30);
    }

    private void pasteCopiedTextBoxAt(int x, int y) {

        if (copiedTextBox == null) {
            return;
        }

        TextBoxItem textBox = copyTextBox(copiedTextBox);

        textBox.setX(x);
        textBox.setY(y);
        textBoxes.add(textBox);
        selectedTextBox = textBox;
        selectedItem = null;
        selectedRoomObject = null;
        selectedLine = null;
        backgroundSelected = false;

        notifyChanged();
        repaint();
    }

    private TextBoxItem copyTextBox(TextBoxItem source) {

        TextBoxItem copy = new TextBoxItem(
                source.getText(),
                source.getX(),
                source.getY());

        copy.setWidth(source.getWidth());
        copy.setHeight(source.getHeight());
        copy.setFontSize(source.getFontSize());
        copy.setTextColor(source.getTextColor());
        copy.setBackgroundColor(source.getBackgroundColor());
        copy.setShowBackground(source.isShowBackground());
        copy.setShowBorder(source.isShowBorder());

        return copy;
    }

    private void copySelectedRoomObject() {

        if (selectedRoomObject == null) {
            return;
        }

        copiedRoomObject = copyRoomObject(selectedRoomObject);
    }

    private void pasteCopiedRoomObject() {

        if (copiedRoomObject == null) {
            return;
        }

        pasteCopiedRoomObjectAt(
                copiedRoomObject.getX() + 30,
                copiedRoomObject.getY() + 30);
    }

    private void pasteCopiedRoomObjectAt(int x, int y) {

        if (copiedRoomObject == null) {
            return;
        }

        RoomObject object = copyRoomObject(copiedRoomObject);
        int dx = x - object.getX();
        int dy = y - object.getY();

        object.setX(object.getX() + dx);
        object.setY(object.getY() + dy);

        customRoomObjects.add(object);
        selectedRoomObject = object;
        selectedItem = null;
        selectedLine = null;
        selectedTextBox = null;
        backgroundSelected = false;

        notifyChanged();
        repaint();
    }

    private void bringSelectedRoomObjectToFront() {

        if (selectedRoomObject == null
                || isLockedRoomObject(selectedRoomObject)
                || !customRoomObjects.remove(selectedRoomObject)) {
            return;
        }

        customRoomObjects.add(selectedRoomObject);
        notifyChanged();
        repaint();
    }

    private void sendSelectedRoomObjectToBack() {

        if (selectedRoomObject == null
                || isLockedRoomObject(selectedRoomObject)
                || !customRoomObjects.remove(selectedRoomObject)) {
            return;
        }

        customRoomObjects.add(0, selectedRoomObject);
        notifyChanged();
        repaint();
    }

    private void pasteBestCopiedObjectAt(int x, int y) {

        if (copiedItem != null) {
            pasteCopiedItemAt(x, y);
            return;
        }

        if (copiedTextBox != null) {
            pasteCopiedTextBoxAt(x, y);
            return;
        }

        if (copiedRoomObject != null) {
            pasteCopiedRoomObjectAt(x, y);
        }
    }
	public List<LayoutItem> getItems(){

	    return items;
	}
	
	
	public void setItems(List<LayoutItem> items) {

	    this.items = items;

	    selectedItem = null;
        selectedLine = null;

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
        textBoxes.clear();

	    selectedItem = null;
	    selectedRoomObject = null;
        selectedLine = null;
        selectedTextBox = null;
        backgroundMap = null;
        backgroundImage = null;
        backgroundSelected = false;
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
        repaint();
	}

    public void setAlignmentGuideEnabled(boolean alignmentGuideEnabled) {

        this.alignmentGuideEnabled = alignmentGuideEnabled;
        clearAlignmentGuides();
        repaint();
    }

    public boolean isAlignmentGuideEnabled() {

        return alignmentGuideEnabled;
    }

    private void clearAlignmentGuides() {

        activeAlignmentGuideX = null;
        activeAlignmentGuideY = null;
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

    private DrawLine findLine(int x, int y) {

        for (int i = drawLines.size() - 1; i >= 0; i--) {

            DrawLine line = drawLines.get(i);

            if (isNearLineStart(line, x, y)
                    || isNearLineEnd(line, x, y)
                    || distanceToLineSegment(line, x, y) <= 8.0) {
                return line;
            }
        }

        return null;
    }

    private boolean isNearLineStart(DrawLine line, int x, int y) {

        return distance(line.getStartX(), line.getStartY(), x, y) <= 8.0;
    }

    private boolean isNearLineEnd(DrawLine line, int x, int y) {

        return distance(line.getEndX(), line.getEndY(), x, y) <= 8.0;
    }

    private double distance(int x1, int y1, int x2, int y2) {

        int dx = x1 - x2;
        int dy = y1 - y2;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private double distanceToLineSegment(DrawLine line, int x, int y) {

        double x1 = line.getStartX();
        double y1 = line.getStartY();
        double x2 = line.getEndX();
        double y2 = line.getEndY();

        double dx = x2 - x1;
        double dy = y2 - y1;

        double lengthSquared = dx * dx + dy * dy;

        if (lengthSquared == 0) {
            return distance((int) x1, (int) y1, x, y);
        }

        double t =
                ((x - x1) * dx + (y - y1) * dy) / lengthSquared;

        t = Math.max(0.0, Math.min(1.0, t));

        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;

        double closestDx = x - closestX;
        double closestDy = y - closestY;

        return Math.sqrt(closestDx * closestDx + closestDy * closestDy);
    }

    private TextBoxItem findTextBox(int x, int y) {

        for (int i = textBoxes.size() - 1; i >= 0; i--) {
            TextBoxItem textBox = textBoxes.get(i);
            Rectangle rect =
                    new Rectangle(
                            textBox.getX(),
                            textBox.getY(),
                            textBox.getWidth(),
                            textBox.getHeight());

            if (rect.contains(x, y)) {
                return textBox;
            }
        }

        return null;
    }

    private Rectangle getTextBoxResizeHandle(TextBoxItem textBox) {

        return new Rectangle(
                textBox.getX() + textBox.getWidth() - RESIZE_HANDLE_SIZE,
                textBox.getY() + textBox.getHeight() - RESIZE_HANDLE_SIZE,
                RESIZE_HANDLE_SIZE,
                RESIZE_HANDLE_SIZE);
    }

    private boolean isOnTextBoxResizeHandle(
            TextBoxItem textBox,
            int x,
            int y) {

        return textBox != null && getTextBoxResizeHandle(textBox).contains(x, y);
    }

    private boolean isEditableBackgroundAt(int x, int y) {

        if (backgroundMap == null
                || backgroundImage == null
                || !backgroundMap.isVisible()
                || backgroundMap.isLocked()) {
            return false;
        }

        return isBackgroundAt(x, y);
    }

    private boolean isBackgroundAt(int x, int y) {

        if (backgroundMap == null
                || backgroundImage == null
                || !backgroundMap.isVisible()) {
            return false;
        }

        Rectangle rect =
                new Rectangle(
                        backgroundMap.getX(),
                        backgroundMap.getY(),
                        backgroundMap.getWidth(),
                        backgroundMap.getHeight());

        return rect.contains(x, y);
    }
	
	public void setChangeCallback(Runnable changeCallback) {

	    this.changeCallback = changeCallback;
	}
	//変更通知用メソッド
	private void notifyChanged() {

        // 変更通知は保存ボタンの有効化だけでなく、undo/redo用の履歴記録も兼ねる。
        // 操作を追加した時は、原則ここを通すようにする。
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

        // 履歴は現在のリストをそのまま持つと後続操作で変わってしまう。
        // そのため、機材、会場パーツ、線、背景、文字をコピーして保存する。
        return new LayoutSnapshot(
                copyItems(items),
                copyRoomObjects(customRoomObjects),
                copyDrawLines(drawLines),
                copyBackgroundMap(backgroundMap),
                copyTextBoxes(textBoxes));
    }

    private void restoreSnapshot(LayoutSnapshot snapshot) {

        if (snapshot == null) {
            return;
        }

        restoringSnapshot = true;

        items = copyItems(snapshot.items);
        customRoomObjects = copyRoomObjects(snapshot.roomObjects);
        drawLines = copyDrawLines(snapshot.lines);
        backgroundMap = copyBackgroundMap(snapshot.backgroundMap);
        textBoxes = copyTextBoxes(snapshot.textBoxes);

        if (backgroundMap != null && !backgroundMap.getImagePath().isBlank()) {
            try {
                backgroundImage =
                        BackgroundImageLoader.load(new File(backgroundMap.getImagePath()));
            } catch (IOException ex) {
                backgroundImage = null;
            }
        } else {
            backgroundImage = null;
        }

        selectedItem = null;
        selectedRoomObject = null;
        selectedLine = null;
        selectedTextBox = null;
        backgroundSelected = false;
        dragging = false;
        draggingRoomObject = false;
        resizingItem = false;
        resizingRoomObject = false;
        draggingLineStart = false;
        draggingLineEnd = false;
        draggingLineGroup = false;
        draggingTextBox = false;
        resizingTextBox = false;
        draggingBackground = false;

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
            copiedItem.setShowLabel(item.isShowLabel());

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

        if (isShapeRoomObject(object)) {
            return RoomObject.createShape(
                    object.getType(),
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
            copiedLine.setShowLength(line.isShowLength());
            copiedLine.setLineType(line.getLineType());
            copiedLine.setShowLabel(line.isShowLabel());

            copiedLines.add(copiedLine);
        }

        return copiedLines;
    }

    private BackgroundMap copyBackgroundMap(BackgroundMap source) {

        if (source == null) {
            return null;
        }

        BackgroundMap copy = new BackgroundMap();
        copy.setImagePath(source.getImagePath());
        copy.setOriginalFileName(source.getOriginalFileName());
        copy.setX(source.getX());
        copy.setY(source.getY());
        copy.setWidth(source.getWidth());
        copy.setHeight(source.getHeight());
        copy.setOpacity(source.getOpacity());
        copy.setRotation(source.getRotation());
        copy.setVisible(source.isVisible());
        copy.setLocked(source.isLocked());
        copy.setActualWidthMeters(source.getActualWidthMeters());
        copy.setActualHeightMeters(source.getActualHeightMeters());
        copy.setCropX(source.getCropX());
        copy.setCropY(source.getCropY());
        copy.setCropWidth(source.getCropWidth());
        copy.setCropHeight(source.getCropHeight());
        copy.setPreviewMode(source.getPreviewMode());

        return copy;
    }

    private List<TextBoxItem> copyTextBoxes(List<TextBoxItem> sourceTextBoxes) {

        List<TextBoxItem> copiedTextBoxes = new ArrayList<>();

        if (sourceTextBoxes == null) {
            return copiedTextBoxes;
        }

        for (TextBoxItem textBox : sourceTextBoxes) {
            TextBoxItem copy =
                    new TextBoxItem(
                            textBox.getText(),
                            textBox.getX(),
                            textBox.getY());
            copy.setWidth(textBox.getWidth());
            copy.setHeight(textBox.getHeight());
            copy.setFontSize(textBox.getFontSize());
            copy.setTextColor(textBox.getTextColor());
            copy.setBackgroundColor(textBox.getBackgroundColor());
            copy.setShowBackground(textBox.isShowBackground());
            copy.setShowBorder(textBox.isShowBorder());
            copiedTextBoxes.add(copy);
        }

        return copiedTextBoxes;
    }

    private static class LayoutSnapshot {

        private final List<LayoutItem> items;

        private final List<RoomObject> roomObjects;

        private final List<DrawLine> lines;

        private final BackgroundMap backgroundMap;

        private final List<TextBoxItem> textBoxes;

        LayoutSnapshot(
                List<LayoutItem> items,
                List<RoomObject> roomObjects,
                List<DrawLine> lines,
                BackgroundMap backgroundMap,
                List<TextBoxItem> textBoxes) {

            this.items = items;
            this.roomObjects = roomObjects;
            this.lines = lines;
            this.backgroundMap = backgroundMap;
            this.textBoxes = textBoxes;
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

        clearAllModes();
	    this.drawLineMode = drawLineMode;
        currentLineLabel = "";
	    
	    if (drawLineMode) {
	        roomObjectAddMode = null;
	    }

	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;
	    selectedRoomObject = null;
        selectedLine = null;

	    refreshPanels();

	    repaint();
	}

    public void setTextMode(boolean textMode) {

        clearAllModes();
        this.textMode = textMode;
        lineStartX = null;
        lineStartY = null;
        selectedItem = null;
        selectedRoomObject = null;
        selectedLine = null;
        backgroundSelected = false;

        refreshPanels();
        repaint();
    }

    public void switchToItemPlacementMode() {

        clearAllModes();

        // 機材パレットを押した時は、線や文字を描く作業が終わったと判断する。
        drawLineMode = false;
        textMode = false;
        roomObjectAddMode = null;
        lineStartX = null;
        lineStartY = null;
        selectedLine = null;
        selectedRoomObject = null;
        backgroundSelected = false;

        repaint();
    }

    private void clearAllModes() {

        textMode = false;
        drawLineMode = false;
        roomObjectAddMode = null;

        lineStartX = null;
        lineStartY = null;

        selectedTextBox = null;
        selectedItem = null;
        selectedRoomObject = null;
        selectedLine = null;
        backgroundSelected = false;

        dragging = false;
        draggingRoomObject = false;
        resizingRoomObject = false;
        resizingItem = false;
        rotatingItem = false;
        draggingLineStart = false;
        draggingLineEnd = false;
        draggingLineGroup = false;
        draggingTextBox = false;
        resizingTextBox = false;
        draggingBackground = false;
        clearAlignmentGuides();
    }

    private void addTextBoxAt(int x, int y) {

        String text =
                JOptionPane.showInputDialog(
                        this,
                        "表示する文字を入力してください");

        if (text == null || text.isBlank()) {
            return;
        }

        TextBoxItem textBox = new TextBoxItem(text, x, y);
        textBoxes.add(textBox);
        selectedTextBox = textBox;

        notifyChanged();
        repaint();
    }

    private void editSelectedTextBox() {

        if (selectedTextBox == null) {
            return;
        }

        if (showTextBoxEditor()) {
            return;
        }

        String text =
                JOptionPane.showInputDialog(
                        this,
                        "表示する文字を入力してください",
                        selectedTextBox.getText());

        if (text == null) {
            return;
        }

        selectedTextBox.setText(text);
        notifyChanged();
        repaint();
    }

    private boolean showTextBoxEditor() {

        JTextArea textArea = new JTextArea(selectedTextBox.getText(), 4, 24);
        JTextField widthField =
                new JTextField(String.valueOf(selectedTextBox.getWidth()), 6);
        JTextField heightField =
                new JTextField(String.valueOf(selectedTextBox.getHeight()), 6);
        JTextField fontSizeField =
                new JTextField(String.valueOf(selectedTextBox.getFontSize()), 6);
        JComboBox<String> backgroundColorBox =
                new JComboBox<>(new String[] {"white", "lightGray", "yellow", "none"});
        backgroundColorBox.setSelectedItem(
                selectedTextBox.isShowBackground()
                        ? colorName(selectedTextBox.getBackgroundColor())
                        : "none");
        JCheckBox borderCheckBox =
                new JCheckBox("border", selectedTextBox.isShowBorder());

        JPanel panel = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("text"));
        panel.add(new JScrollPane(textArea));
        panel.add(new JLabel("width"));
        panel.add(widthField);
        panel.add(new JLabel("height"));
        panel.add(heightField);
        panel.add(new JLabel("font size"));
        panel.add(fontSizeField);
        panel.add(new JLabel("background"));
        panel.add(backgroundColorBox);
        panel.add(new JLabel("border"));
        panel.add(borderCheckBox);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Text Box",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return true;
        }

        selectedTextBox.setText(textArea.getText());
        selectedTextBox.setWidth(parsePositiveInt(widthField.getText(), selectedTextBox.getWidth()));
        selectedTextBox.setHeight(parsePositiveInt(heightField.getText(), selectedTextBox.getHeight()));
        selectedTextBox.setFontSize(parsePositiveInt(fontSizeField.getText(), selectedTextBox.getFontSize()));
        selectedTextBox.setShowBorder(borderCheckBox.isSelected());

        String backgroundName = String.valueOf(backgroundColorBox.getSelectedItem());
        selectedTextBox.setShowBackground(!"none".equals(backgroundName));
        selectedTextBox.setBackgroundColor(colorFromName(backgroundName));

        notifyChanged();
        repaint();
        return true;
    }

    private int parsePositiveInt(String text, int defaultValue) {

        try {
            return Math.max(1, Integer.parseInt(text.trim()));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private String colorName(Color color) {

        if (Color.LIGHT_GRAY.equals(color)) {
            return "lightGray";
        }

        if (Color.YELLOW.equals(color)) {
            return "yellow";
        }

        return "white";
    }

    private Color colorFromName(String name) {

        if ("lightGray".equals(name)) {
            return Color.LIGHT_GRAY;
        }

        if ("yellow".equals(name)) {
            return Color.YELLOW;
        }

        return Color.WHITE;
    }

    public void loadBackgroundMap(File file) {

        if (file == null) {
            return;
        }

        try {
            // PDFは提出用の下絵として使いやすいよう、先頭ページだけ画像化する。
            BufferedImage image = BackgroundImageLoader.load(file);

            backgroundImage = image;
            backgroundMap = new BackgroundMap();
            backgroundMap.setImagePath(file.getAbsolutePath());
            backgroundMap.setOriginalFileName(file.getName());
            backgroundMap.setX(0);
            backgroundMap.setY(0);
            backgroundMap.setWidth(image.getWidth());
            backgroundMap.setHeight(image.getHeight());
            backgroundMap.setCropWidth(image.getWidth());
            backgroundMap.setCropHeight(image.getHeight());
            backgroundMap.setPreviewMode(SheetPreviewPanel.PREVIEW_BACKGROUND);
            backgroundMap.setLocked(true);
            backgroundMap.setVisible(true);
            backgroundSelected = false;

            double[] actualSizeMeters = askBackgroundActualSizeMeters(image);

            // 実寸幅が分かる場合はメートルからピクセルへ変換し、
            // 分からない場合は作業シート幅に合わせて扱いやすい初期サイズにする。
            if (actualSizeMeters == null) {
                fitBackgroundToSheetWidth();
            } else {
                fitBackgroundToActualSize(actualSizeMeters[0], actualSizeMeters[1]);
            }

            notifyChanged();
            repaint();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "背景図面の読み込みに失敗しました。\n"
                            + ex.getMessage()
                            + "\n\nPDFの場合は、破損していないファイルか、ページがあるPDFかを確認してください。");
        }
    }

    private double[] askBackgroundActualSizeMeters(BufferedImage image) {

        JTextField widthField = new JTextField();
        JTextField heightField = new JTextField();

        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("横幅 m"));
        panel.add(widthField);
        panel.add(new JLabel("高さ m（空欄なら比率で自動）"));
        panel.add(heightField);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "背景図面の実寸幅",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        try {
            double widthMeters = Double.parseDouble(widthField.getText().trim());
            double heightMeters = 0.0;

            if (!heightField.getText().trim().isBlank()) {
                heightMeters = Double.parseDouble(heightField.getText().trim());
            } else if (image != null && image.getWidth() > 0) {
                heightMeters = widthMeters * image.getHeight() / image.getWidth();
            }

            if (widthMeters <= 0.0 || heightMeters <= 0.0) {
                throw new NumberFormatException();
            }

            return new double[] {widthMeters, heightMeters};

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "実寸幅は 20 や 17.5 のような数字で入力してください。\n"
                            + "今回はシート幅に合わせて読み込みます。");
            return null;
        }
    }

    public void setBackgroundMap(BackgroundMap backgroundMap) {

        this.backgroundMap = backgroundMap;
        backgroundImage = null;
        backgroundSelected = false;

        if (backgroundMap != null && !backgroundMap.getImagePath().isBlank()) {
            try {
                backgroundImage =
                        BackgroundImageLoader.load(new File(backgroundMap.getImagePath()));
            } catch (IOException ex) {
                backgroundImage = null;
            }
        }

        repaint();
    }

    public BackgroundMap getBackgroundMap() {
        return backgroundMap;
    }

    public void toggleBackgroundVisible() {

        if (backgroundMap == null) {
            return;
        }

        backgroundMap.setVisible(!backgroundMap.isVisible());
        notifyChanged();
        repaint();
    }

    public void toggleBackgroundLocked() {

        if (backgroundMap == null) {
            return;
        }

        backgroundMap.setLocked(!backgroundMap.isLocked());
        backgroundSelected = false;
        notifyChanged();
        repaint();
    }

    public void setBackgroundOpacity(float opacity) {

        if (backgroundMap == null) {
            return;
        }

        backgroundMap.setOpacity(opacity);
        notifyChanged();
        repaint();
    }

    public void clearBackgroundMap() {

        backgroundMap = null;
        backgroundImage = null;
        backgroundSelected = false;
        notifyChanged();
        repaint();
    }

    public void fitBackgroundToSheetWidth() {

        if (backgroundMap == null || backgroundImage == null) {
            return;
        }

        double scale = getSheetContentWidth() / (double) backgroundImage.getWidth();
        backgroundMap.setWidth(getSheetContentWidth());
        backgroundMap.setHeight((int) Math.round(backgroundImage.getHeight() * scale));
        centerBackgroundMap();
    }

    public void fitBackgroundToSheetHeight() {

        if (backgroundMap == null || backgroundImage == null) {
            return;
        }

        double scale = getSheetContentHeight() / (double) backgroundImage.getHeight();
        backgroundMap.setHeight(getSheetContentHeight());
        backgroundMap.setWidth((int) Math.round(backgroundImage.getWidth() * scale));
        centerBackgroundMap();
    }

    public void fitBackgroundToActualSize(double actualWidthMeters, double actualHeightMeters) {

        if (backgroundMap == null) {
            return;
        }

        if (actualWidthMeters <= 0.0) {
            return;
        }

        int widthPx = (int) Math.round(actualWidthMeters * getPixelsPerMeter());
        int heightPx;

        if (actualHeightMeters > 0.0) {
            heightPx = (int) Math.round(actualHeightMeters * getPixelsPerMeter());
        } else if (backgroundImage != null && backgroundImage.getWidth() > 0) {
            heightPx =
                    (int) Math.round(
                            widthPx * backgroundImage.getHeight()
                                    / (double) backgroundImage.getWidth());
        } else {
            heightPx = backgroundMap.getHeight();
        }

        backgroundMap.setActualWidthMeters(actualWidthMeters);
        backgroundMap.setActualHeightMeters(actualHeightMeters);
        backgroundMap.setWidth(widthPx);
        backgroundMap.setHeight(heightPx);

        if (widthPx > getSheetContentWidth() || heightPx > getSheetContentHeight()) {
            setSheetSizeMeters(
                    Math.max(2.0, actualWidthMeters + 2.0),
                    Math.max(2.0, actualHeightMeters + 2.0));
        }

        centerBackgroundMap();
    }

    public void askAndFitBackgroundToActualSize() {

        if (backgroundMap == null) {
            return;
        }

        double[] actualSizeMeters = askBackgroundActualSizeMeters(backgroundImage);

        if (actualSizeMeters == null) {
            return;
        }

        fitBackgroundToActualSize(actualSizeMeters[0], actualSizeMeters[1]);
    }

    public void centerBackgroundMap() {

        if (backgroundMap == null) {
            return;
        }

        backgroundMap.setX((getSheetContentWidth() - backgroundMap.getWidth()) / 2);
        backgroundMap.setY((getSheetContentHeight() - backgroundMap.getHeight()) / 2);
        notifyChanged();
        repaint();
    }

    public List<TextBoxItem> getTextBoxes() {
        return textBoxes;
    }

    public void setTextBoxes(List<TextBoxItem> textBoxes) {
        this.textBoxes = textBoxes == null ? new ArrayList<>() : textBoxes;
        selectedTextBox = null;
        repaint();
    }

	public boolean isDrawLineMode() {
	    return drawLineMode;
	}

	public void setCurrentLineColor(Color color) {

	    this.currentLineColor = color;

	    repaint();
	}

    public void setCurrentLineType(String lineType) {

        if (lineType == null || lineType.isBlank()) {
            lineType = DrawLine.TYPE_NORMAL;
        }

        this.currentLineType = lineType;

        if (DrawLine.TYPE_CABLE.equals(lineType)) {
            currentLineColor = Color.BLACK;
            currentLineLabel = "ケーブル";
        } else if (DrawLine.TYPE_FLOW.equals(lineType)) {
            currentLineColor = new Color(40, 110, 210);
            currentLineLabel = "導線";
        } else if (DrawLine.TYPE_NORMAL.equals(lineType)) {
            currentLineLabel = "";
        }

        if (selectedLine != null) {
            selectedLine.setLineType(lineType);
            selectedLine.setLabel(currentLineLabel);
            selectedLine.setShowLength(!DrawLine.TYPE_BAMIRI.equals(lineType));
            notifyChanged();
        }

        repaint();
    }

    private void changeSelectedLineType(String lineType) {

        if (selectedLine == null) {
            return;
        }

        if (lineType == null || lineType.isBlank()) {
            lineType = DrawLine.TYPE_NORMAL;
        }

        for (DrawLine line : getConnectedLines(selectedLine, false)) {
            line.setLineType(lineType);

            if (DrawLine.TYPE_CABLE.equals(lineType)) {
                line.setColor(Color.BLACK);
                line.setLabel("ケーブル");
                line.setShowLength(true);
            } else if (DrawLine.TYPE_FLOW.equals(lineType)) {
                line.setColor(new Color(40, 110, 210));
                line.setLabel("導線");
                line.setShowLength(true);
            } else if (DrawLine.TYPE_BAMIRI.equals(lineType)) {
                line.setColor(Color.RED);
                line.setLabel("バミリ");
                line.setShowLength(false);
            } else {
                line.setLabel("");
                line.setShowLength(true);
            }
        }

        currentLineType = lineType;
        notifyChanged();
        repaint();
    }

    private void moveSelectedLineGroup(int dx, int dy) {

        moveSelectedLineGroup(dx, dy, true);
    }

    private void moveSelectedLineGroup(int dx, int dy, boolean recordChange) {

        if (selectedLine == null) {
            return;
        }

        for (DrawLine line : getConnectedLines(selectedLine, true)) {
            line.setStart(
                    line.getStartX() + dx,
                    line.getStartY() + dy);
            line.setEnd(
                    line.getEndX() + dx,
                    line.getEndY() + dy);
        }

        if (recordChange) {
            notifyChanged();
        }

        repaint();
    }

    private List<DrawLine> getConnectedLines(DrawLine source, boolean sameTypeOnly) {

        List<DrawLine> connected = new ArrayList<>();

        if (source == null) {
            return connected;
        }

        String groupId = source.getGroupId();

        if (groupId == null || groupId.isBlank()) {
            connected.add(source);
            return connected;
        }

        for (DrawLine candidate : drawLines) {
            if (!groupId.equals(candidate.getGroupId())) {
                continue;
            }

            if (sameTypeOnly
                    && !source.getLineType().equals(candidate.getLineType())) {
                continue;
            }

            connected.add(candidate);
        }

        return connected;
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
        activeLineGroupId = "";

	    repaint();
	}
	
	public void finishCurrentLine() {

	    lineStartX = null;
	    lineStartY = null;
        activeLineGroupId = "";

	    repaint();
	}
	
	public void setCurrentLineStrokeWidth(int strokeWidth) {

	    if (strokeWidth < 1) {
	        strokeWidth = 1;
	    }

	    this.currentLineStrokeWidth = strokeWidth;

        if (selectedLine != null) {
            selectedLine.setStrokeWidth(strokeWidth);
            notifyChanged();
        }

	    repaint();
	}

    public void setBamiriLineMode() {

        clearAllModes();
        drawLineMode = false;
        if (equipmentPanel != null) {
            equipmentPanel.selectEquipmentByName("バミリ 横");
        }
        roomObjectAddMode = null;
        lineStartX = null;
        lineStartY = null;
        selectedItem = null;
        selectedRoomObject = null;
        selectedLine = null;
        selectedTextBox = null;
        backgroundSelected = false;
        currentLineColor = Color.RED;
        currentLineStrokeWidth = 5;
        currentLineLabel = "バミリ";
        currentLineType = DrawLine.TYPE_BAMIRI;

        refreshPanels();

        repaint();
    }

    public void setBamiriFreeLineMode() {

        clearAllModes();
        drawLineMode = true;
        currentLineColor = Color.RED;
        currentLineStrokeWidth = 5;
        currentLineLabel = "バミリ";
        currentLineType = DrawLine.TYPE_BAMIRI;

        refreshPanels();
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

    public int getPreviewSheetWidth() {
        return getSheetContentWidth();
    }

    private int getSheetContentHeight() {

        if (roomTemplate != null) {
            return roomTemplate.getHeight();
        }

        return sheetHeight;
    }

    public int getPreviewSheetHeight() {
        return getSheetContentHeight();
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
        textMode = false;
	    
	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;
	    selectedRoomObject = null;
        selectedLine = null;
        selectedTextBox = null;
	    
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

    public void addRoomShapeFromMeters(
            String shapeType,
            String name,
            double widthMeters,
            double heightMeters) {

        if (stageLocked) {
            return;
        }

        int width = Math.max(GRID_SIZE, metersToCurrentGridPixels(widthMeters));
        int height = Math.max(GRID_SIZE, metersToCurrentGridPixels(heightMeters));

        // 図形テンプレートは作成直後に見失わないよう、作業シートの中央へ置く。
        // その後は通常の会場パーツと同じ移動・リサイズ処理で調整する。
        int x = Math.max(0, (sheetWidth - width) / 2);
        int y = Math.max(0, (sheetHeight - height) / 2);

        if (snapToGrid) {
            x = snapValue(x);
            y = snapValue(y);
        }

        RoomObject object =
                RoomObject.createShape(
                        shapeType,
                        name,
                        x,
                        y,
                        width,
                        height);

        clearAllModes();
        customRoomObjects.add(object);
        selectedRoomObject = object;

        notifyChanged();
        repaint();
    }

    private int metersToCurrentGridPixels(double meters) {
        return (int) Math.round((meters / metersPerGrid) * GRID_SIZE);
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
	                    ? SELECTION_COLOR
	                    : getRoomObjectBorderColor(object));
	            g2.setStroke(new BasicStroke(selected ? 2.5f : 1));
	            g2.drawOval(
	                    object.getX(),
	                    object.getY(),
	                    object.getWidth(),
	                    object.getHeight());

	        } else if (isShapeRoomObject(object)) {

                Shape shape = createRoomObjectShape(object);

	            g2.setColor(fillColor);
	            g2.fill(shape);

	            g2.setColor(selected
	                    ? SELECTION_COLOR
	                    : getRoomObjectBorderColor(object));
	            g2.setStroke(new BasicStroke(selected ? 2.5f : 1));
	            g2.draw(shape);

	        } else {

	            g2.setColor(fillColor);
	            g2.fillRect(
	                    object.getX(),
	                    object.getY(),
	                    object.getWidth(),
	                    object.getHeight());

	            g2.setColor(selected
	                    ? SELECTION_COLOR
	                    : getRoomObjectBorderColor(object));
	            g2.setStroke(new BasicStroke(selected ? 2.5f : 1));
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
	        g2.setColor(SELECTION_COLOR);
	        g2.setStroke(new BasicStroke(2.5f));
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

        if (isShapeRoomObject(object)) {
            return new Color(226, 234, 240);
        }

	    String name = object.getName();

	    if (name != null
	            && (name.contains("客席") || name.contains("Audience"))) {
	        return SEAT_AREA_FILL_COLOR;
	    }

	    return STAGE_FILL_COLOR;
	}

    private Color getRoomObjectBorderColor(RoomObject object) {

        if (isShapeRoomObject(object)) {
            return new Color(158, 170, 180);
        }

        return ROOM_OBJECT_BORDER_COLOR;
    }

    private boolean isShapeRoomObject(RoomObject object) {

        if (object == null) {
            return false;
        }

        String type = object.getType();

        return RoomObject.TYPE_OVAL.equals(type)
                || RoomObject.TYPE_TRACK.equals(type)
                || RoomObject.TYPE_FRONT_ARC.equals(type)
                || RoomObject.TYPE_ROUNDED_RECT.equals(type);
    }

    private Shape createRoomObjectShape(RoomObject object) {

        int x = object.getX();
        int y = object.getY();
        int w = Math.max(1, object.getWidth());
        int h = Math.max(1, object.getHeight());

        if (RoomObject.TYPE_OVAL.equals(object.getType())) {
            return new Ellipse2D.Double(x, y, w, h);
        }

        if (RoomObject.TYPE_TRACK.equals(object.getType())) {
            return new RoundRectangle2D.Double(x, y, w, h, h, h);
        }

        if (RoomObject.TYPE_FRONT_ARC.equals(object.getType())) {
            Path2D path = new Path2D.Double();
            double arcHeight = Math.max(12, h * 0.35);

            path.moveTo(x, y);
            path.lineTo(x + w, y);
            path.lineTo(x + w, y + h - arcHeight);
            path.quadTo(x + w / 2.0, y + h, x, y + h - arcHeight);
            path.closePath();

            return path;
        }

        return new RoundRectangle2D.Double(x, y, w, h, Math.min(w, h) / 3.0, Math.min(w, h) / 3.0);
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

	    drawResizeHandle(g2, handleX, handleY);
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
        textMode = false;

	    roomObjectAddMode = null;

	    lineStartX = null;
	    lineStartY = null;

	    selectedItem = null;
	    selectedRoomObject = null;
        selectedLine = null;
        selectedTextBox = null;
        backgroundSelected = false;

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
        selectedLine = null;
        draggingLineStart = false;
        draggingLineEnd = false;
        draggingLineGroup = false;

	    repaint();
	}
	
	private void drawItemResizeHandle(Graphics2D g2, LayoutItem item) {

        Rectangle handle = getItemResizeHandle(item);

	    drawResizeHandle(g2, handle.x, handle.y);
	}

    private void drawItemRotateHandle(Graphics2D g2, LayoutItem item) {

        Rectangle handle = getItemRotateHandle(item);
        int centerX = item.getX() + item.getWidth() / 2;
        int itemTopY = item.getY();
        int handleCenterX = handle.x + handle.width / 2;
        int handleCenterY = handle.y + handle.height / 2;

        g2.setColor(SELECTION_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(centerX, itemTopY, handleCenterX, handleCenterY);

        g2.setColor(Color.WHITE);
        g2.fillOval(handle.x, handle.y, handle.width, handle.height);

        g2.setColor(SELECTION_HANDLE_BORDER_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(handle.x, handle.y, handle.width, handle.height);
    }

    private void drawResizeHandle(Graphics2D g2, int x, int y) {

        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);

        g2.setColor(SELECTION_HANDLE_BORDER_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);
    }

}
