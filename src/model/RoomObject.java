package model;

public class RoomObject {
	
	public static final String TYPE_LINE = "LINE";
	public static final String TYPE_RECT = "RECT";
	public static final String TYPE_CIRCLE = "CIRCLE";
	public static final String TYPE_ARC = "ARC";
	public static final String TYPE_TEXT = "TEXT";
	
	private String type;
	
	private String name;
	
	private int x;
	
	private int y;
	
	private int width;

	private int height;
	
	private int endX;
	
	private int endY;
	
	public RoomObject(
			String name,
			int x,
			int y,
			int width,
			int height) {
		
		this.type = TYPE_RECT;
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public static RoomObject createLine(
			String name,
			int x,
			int y,
			int endX,
			int endY) {
		
		RoomObject object =
				new RoomObject(name, x, y, 0, 0);
		
		object.type = TYPE_LINE;
		object.endX = endX;
		object.endY = endY;
		
		return object;
	}
	
	public static RoomObject createCircle(
	        String name,
	        int x,
	        int y,
	        int width,
	        int height) {

	    RoomObject object =
	            new RoomObject(name, x, y, width, height);

	    object.type = TYPE_CIRCLE;

	    return object;
	}

	public static RoomObject createArc(
	        String name,
	        int x,
	        int y,
	        int width,
	        int height) {

	    RoomObject object =
	            new RoomObject(name, x, y, width, height);

	    object.type = TYPE_ARC;

	    return object;
	}

	public static RoomObject createText(
	        String name,
	        int x,
	        int y) {

	    RoomObject object =
	            new RoomObject(name, x, y, 0, 0);

	    object.type = TYPE_TEXT;

	    return object;
	}
	
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getEndX() {
		return endX;
	}
	
	public int getEndY() {
		return endY;
	}
	
	public void setX(int x) {
	    this.x = x;
	}

	public void setY(int y) {
	    this.y = y;
	}

	public void setWidth(int width) {
	    this.width = width;
	}

	public void setHeight(int height) {
	    this.height = height;
	}

}
