package model;

public class LayoutItem {
	
	private Equipment equipment;
	
	private int x;
	
	private int y;
	
	private int quantity = 1;
	
	private String memo = "";
	
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
	
	public int getQuantity() {
	    return quantity;
	}

	public void setQuantity(int quantity) {
	    this.quantity = quantity;
	}

	public String getMemo() {
	    return memo;
	}

	public void setMemo(String memo) {
	    this.memo = memo;
	}

}
