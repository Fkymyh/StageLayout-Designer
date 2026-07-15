package model;

import java.awt.Color;

// キャンバス上に引いた線を表す。
// 通常線は長さ表示に使い、BAMIRI線は赤テープ位置として長さ表示を出さない。
public class DrawLine {

    public static final String TYPE_NORMAL = "NORMAL";
    public static final String TYPE_CABLE = "CABLE";
    public static final String TYPE_FLOW = "FLOW";
    public static final String TYPE_BAMIRI = "BAMIRI";

    private int startX;
    private int startY;

    private int endX;
    private int endY;
    
    private int strokeWidth = 2;

    private Color color = Color.BLACK;

    private String label = "";

    private boolean showLength = true;

    private String lineType = TYPE_NORMAL;

    private boolean showLabel = true;

    private String groupId = "";

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

    public void setStart(int startX, int startY) {
        this.startX = startX;
        this.startY = startY;
    }

    public void setEnd(int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
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

    public boolean isShowLength() {
        return showLength;
    }

    public void setShowLength(boolean showLength) {
        this.showLength = showLength;
    }

    public String getLineType() {
        return lineType;
    }

    public void setLineType(String lineType) {
        if (lineType == null || lineType.isBlank()) {
            this.lineType = TYPE_NORMAL;
            return;
        }

        this.lineType = lineType;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        if (groupId == null) {
            this.groupId = "";
            return;
        }

        this.groupId = groupId;
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
