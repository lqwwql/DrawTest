package com.main.drawtest;

public class DimensionPoint {

    private float leftTopX;
    private float leftTopY;
    private float rightBottomX;
    private float rightBottomY;
    private String createTime;
    private double proportion;

    public DimensionPoint(float leftTopX, float leftTopY, float rightBottomX, float rightBottomY, double proportion) {
        this.leftTopX = leftTopX;
        this.leftTopY = leftTopY;
        this.rightBottomX = rightBottomX;
        this.rightBottomY = rightBottomY;
        this.proportion = proportion;
    }

    public DimensionPoint(float leftTopX, float leftTopY, float rightBottomX, float rightBottomY, String createTime, double proportion) {
        this.leftTopX = leftTopX;
        this.leftTopY = leftTopY;
        this.rightBottomX = rightBottomX;
        this.rightBottomY = rightBottomY;
        this.createTime = createTime;
        this.proportion = proportion;
    }

    public float getLeftTopX() {
        return leftTopX;
    }

    public void setLeftTopX(float leftTopX) {
        this.leftTopX = leftTopX;
    }

    public float getLeftTopY() {
        return leftTopY;
    }

    public void setLeftTopY(float leftTopY) {
        this.leftTopY = leftTopY;
    }

    public float getRightBottomX() {
        return rightBottomX;
    }

    public void setRightBottomX(float rightBottomX) {
        this.rightBottomX = rightBottomX;
    }

    public float getRightBottomY() {
        return rightBottomY;
    }

    public void setRightBottomY(float rightBottomY) {
        this.rightBottomY = rightBottomY;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public double getProportion() {
        return proportion;
    }

    public void setProportion(double proportion) {
        this.proportion = proportion;
    }
}
