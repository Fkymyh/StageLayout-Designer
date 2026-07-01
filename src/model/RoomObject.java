package model;

public class RoomObject {
	
	
	
	private String name;
	
	private int x;
	private int y;
	
	
	private int width;
	private int height;
	
	public RoomObject(
			String name,
			int x,
			int y,
			int width,
			int height) {
		
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
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
	
	

}
