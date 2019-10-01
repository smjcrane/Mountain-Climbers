package com.example.mountainclimbers;

public class MountainSegment {

    private int width, leftHeight, rightHeight;
    private boolean goesUp;

    public MountainSegment(int width, int leftHeight, int rightHeight){
        this.width = width;
        this.leftHeight = leftHeight;
        this.rightHeight = rightHeight;
        this.goesUp = rightHeight > leftHeight;
    }

    public int getHeightAt(int x){
        if (x > this.width || x < 0){
            throw new IndexOutOfBoundsException("The segment is only "
                    + Integer.toString(this.width)
                    + " wide but you tried to access "
                    + Integer.toString(x));
        }
        return (int) (this.leftHeight + (this.rightHeight - this.leftHeight) * x / width);
    }

    public int getWidth(){
        return this.width;
    }

    public boolean goesUp(){
        return goesUp;
    }

    @Override
    public String toString() {
        return "Segment " + (this.goesUp ? "up" : "down") + " from "
                + Integer.toString(this.leftHeight) + " to "
                + Integer.toString(this.rightHeight);
    }
}
