package com.example.mountainclimbers;

import android.telephony.CellSignalStrengthGsm;
import android.util.Log;

public class Mountain {

    private MountainSegment[] segments;
    private int width, maxHeight;
    private int[] turningPoints;

    public Mountain(int[] heights){
        this.segments = new MountainSegment[heights.length - 1];
        this.width = 0;
        this.maxHeight = 0;
        this.turningPoints = new int[heights.length];
        this.turningPoints[0] = 0;
        for (int i = 0; i < heights.length - 1; i++) {
            int segWidth = Math.abs(heights[i] - heights[i + 1]);
            this.segments[i] = new MountainSegment(segWidth, heights[i], heights[i + 1]);
            this.width = this.width + segWidth;
            if (heights[i] > this.maxHeight){
                this.maxHeight = heights[i];
            }
            this.turningPoints[i + 1] = this.width;
        }
        if (heights[heights.length - 1] > this.maxHeight){
            this.maxHeight = heights[heights.length - 1];
        }
    }

    public int getWidth(){
        return width;
    }

    public int getMaxHeight(){
        return maxHeight;
    }

    public int getHeightAt(int x){
        if (x > this.width || x < 0){
            throw new IndexOutOfBoundsException("The mountain is only "
                    + Integer.toString(this.width)
                    + " wide but you tried to access "
                    + Integer.toString(x));
        }
        int w = 0;
        for (MountainSegment s : segments){
            if (w + s.getWidth() >= x){
                return s.getHeightAt(x - w);
            }
            w = w + s.getWidth();
        }
        throw new IndexOutOfBoundsException("Couldn't get height and I don't know why");
    }

    public Slope getTypeAt(int x){
        if (x > this.width || x < 0){
            throw new IndexOutOfBoundsException("The mountain is only "
                    + Integer.toString(this.width)
                    + " wide but you tried to access "
                    + Integer.toString(x));
        }
        int w = 0;
        for (MountainSegment s : segments){
            if (w + s.getWidth() > x){
                return s.goesUp() ? Slope.UP : Slope.DOWN;
            } else if (w + s.getWidth() == x){
                return s.goesUp() ? Slope.MAX : Slope.MIN; //IMPORTANT: this relies on alternating signs
            }
            w = w + s.getWidth();
        }
        throw new IndexOutOfBoundsException("Couldn't get slope and I don't know why");
    }

    public int[] getTurningPoints(){
        return turningPoints;
    }

    public String toString(){
        String s = "Mountain";
        for (int i : turningPoints){
            s = s + " " + getHeightAt(i);
        }
        return s;
    }

    public enum Slope {
        UP, DOWN, MAX, MIN
    }
}
