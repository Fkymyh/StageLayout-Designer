package model;

public class BackgroundMap {

    private String imagePath = "";
    private int x;
    private int y;
    private int width;
    private int height;
    private float opacity = 0.65f;
    private int rotation;
    private boolean visible = true;
    private boolean locked = true;
    private String originalFileName = "";
    private double actualWidthMeters;
    private double actualHeightMeters;
    private int cropX;
    private int cropY;
    private int cropWidth;
    private int cropHeight;
    private String previewMode = "CONTENT";

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath == null ? "" : imagePath;
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
        this.width = Math.max(20, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(20, height);
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.1f, Math.min(1.0f, opacity));
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName == null ? "" : originalFileName;
    }

    public double getActualWidthMeters() {
        return actualWidthMeters;
    }

    public void setActualWidthMeters(double actualWidthMeters) {
        this.actualWidthMeters = Math.max(0.0, actualWidthMeters);
    }

    public double getActualHeightMeters() {
        return actualHeightMeters;
    }

    public void setActualHeightMeters(double actualHeightMeters) {
        this.actualHeightMeters = Math.max(0.0, actualHeightMeters);
    }

    public int getCropX() {
        return cropX;
    }

    public void setCropX(int cropX) {
        this.cropX = Math.max(0, cropX);
    }

    public int getCropY() {
        return cropY;
    }

    public void setCropY(int cropY) {
        this.cropY = Math.max(0, cropY);
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public void setCropWidth(int cropWidth) {
        this.cropWidth = Math.max(0, cropWidth);
    }

    public int getCropHeight() {
        return cropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.cropHeight = Math.max(0, cropHeight);
    }

    public String getPreviewMode() {
        return previewMode;
    }

    public void setPreviewMode(String previewMode) {
        this.previewMode = previewMode == null || previewMode.isBlank()
                ? "CONTENT"
                : previewMode;
    }
}
