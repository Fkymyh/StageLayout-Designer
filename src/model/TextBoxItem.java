package model;

import java.awt.Color;

public class TextBoxItem {

    private String text;
    private int x;
    private int y;
    private int width = 160;
    private int height = 42;
    private int fontSize = 16;
    private Color textColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;
    private boolean showBackground = true;
    private boolean showBorder = true;

    public TextBoxItem(String text, int x, int y) {
        this.text = text == null ? "" : text;
        this.x = x;
        this.y = y;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = Math.max(30, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(20, height);
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = Math.max(8, fontSize);
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor == null ? Color.BLACK : textColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor =
                backgroundColor == null ? Color.WHITE : backgroundColor;
    }

    public boolean isShowBackground() {
        return showBackground;
    }

    public void setShowBackground(boolean showBackground) {
        this.showBackground = showBackground;
    }

    public boolean isShowBorder() {
        return showBorder;
    }

    public void setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
    }
}
