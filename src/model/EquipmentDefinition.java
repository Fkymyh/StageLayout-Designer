package model;

import java.awt.Color;

public class EquipmentDefinition {

    private String name;
    
    private String category;

    private Color color;

    private int width;

    private int height;

    private String imagePath;

    public EquipmentDefinition(
            String name,
            String category,
            Color color,
            int width,
            int height,
            String imagePath) {

        this.name = name;
        this.category = category;
        this.color = color;
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }
    
    public String getCategory() {
        return category;
    }

    public Color getColor() {
        return color;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getImagePath() {
        return imagePath;
    }
}
