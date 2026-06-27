package model;

import java.awt.Color;


public class Equipment {
	
	private String name;
	
	private Color color;
	
	private int width;
	
	private int height;
	
	
	public Equipment(String name,
					Color color,
					int width,
					int height) {
		
		this.name = name;
		this.color = color;
		this.width = width;
		this.height = height;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

}
