package model;

// キャンバス上に配置された1つの機材や人物を表す。
// Equipmentは機材の種類、LayoutItemは実際に置かれた位置やサイズを持つ個体。
public class LayoutItem {

    private Equipment equipment;

    private int x;

    private int y;

    private int width;

    private int height;

    private int quantity = 1;
    
    private int rotation = 0;

    private String memo = "";
    
    private String label = "";

    private boolean showLabel = true;

    public LayoutItem(Equipment equipment, int x, int y) {

        this.equipment = equipment;
        this.x = x;
        this.y = y;

        this.width = equipment.getWidth();
        this.height = equipment.getHeight();
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
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
    
    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }
    
    
    public void rotateBy(int degree) {

        rotation = (rotation + degree) % 360;

        if (rotation < 0) {
            rotation += 360;
        }
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public String getDisplayName() {

        if (label != null && !label.isBlank()) {
            return label;
        }

        return equipment.getName();
    }
}
