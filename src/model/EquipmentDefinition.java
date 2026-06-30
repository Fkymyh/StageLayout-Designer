package model;

import java.awt.Color;

public class EquipmentDefinition {

    private String name;

    private Color color;

    private int width;

    private int height;

    private String imagePath;

    public EquipmentDefinition(
            String name,
            Color color,
            int width,
            int height,
            String imagePath) {

        this.name = name;
        this.color = color;
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
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
