package model;

public class LayoutItem {
	
	private Equipment equipment;
	
	private int x;
	
	private int y;
	
	public LayoutItem(Equipment equipment, int x, int y) {
		
		this.equipment = equipment;
		this.x = x;
		this.y = y;
	}
	
	public Equipment getEquipment() {
		return equipment;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}

}
