package model;

import java.awt.Color;
import java.awt.Image;


public class Equipment {
	
	private String name;
	
	private Color color;
	
	private int width;
	
	private int height;
	
	private Image image;
	
	
	public Equipment(String name,
					Color color,
					int width,
					int height,
					Image image) {
		
		this.name = name;
		this.color = color;
		this.width = width;
		this.height = height;
		this.image = image;
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
	
	public Image getImage() {
	    return image;
	}
}
