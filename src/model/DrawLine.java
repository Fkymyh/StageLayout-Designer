package model;

import java.awt.Color;

public class DrawLine {

    private int startX;
    private int startY;

    private int endX;
    private int endY;
    
    private int strokeWidth = 2;

    private Color color = Color.BLACK;

    private String label = "";

    public DrawLine(int startX, int startY, int endX, int endY) {

        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public Color getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        if (strokeWidth < 1) {
            strokeWidth = 1;
        }

        this.strokeWidth = strokeWidth;
    }
}
