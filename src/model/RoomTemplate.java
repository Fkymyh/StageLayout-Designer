package model;

import java.util.ArrayList;
import java.util.List;

// コード側で用意した標準会場テンプレート。
// 今後はユーザー保存テンプレートも同じ「会場の土台」として扱う方針。
public class RoomTemplate {
	
	private String name;
	
	private int width;
	
	private int height;
	
	private List<RoomObject> objects = new ArrayList<>();
	
	public RoomTemplate(String name, int width, int height) {
		this.name = name;
		this.width = width;
		this.height = height;
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
	
	public List<RoomObject> getObjects(){
		return objects;
	}
	
	public void addObject(RoomObject object) {
	    objects.add(object);
	}

}
