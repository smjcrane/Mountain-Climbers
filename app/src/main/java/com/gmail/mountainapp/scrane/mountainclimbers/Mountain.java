package com.gmail.mountainapp.scrane.mountainclimbers;

import android.telephony.CellSignalStrengthGsm;
import android.util.Log;

import java.util.Random;

public class Mountain {

    private MountainSegment[] segments;
    private int width, maxHeight;
    private Integer[] turningPoints;

    public Mountain(int[] heights){
        this.segments = new MountainSegment[heights.length - 1];
        this.width = 0;
        this.maxHeight = 0;
        this.turningPoints = new Integer[heights.length];
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

    public static Mountain generateRandomMountain(long seed){
        Random random = new Random(seed);
        int numPeaks = random.nextInt(4) + 4;
        int[] heights = new int[numPeaks * 2 + 1];
        heights[0] = 0;
        int valley = 0;
        int peak = 100;
        boolean got100 = false;
        for (int i = 0; i < numPeaks; i++){
            if (valley < 80){
                peak = (random.nextInt(8 - valley / 10) + valley / 10 + 2) * 10;
            } else {
                peak = 100;
            }
            Log.d("DAILY", "peak: " + peak);
            if (peak > 20){
                valley = random.nextInt(peak / 10 - 2) * 10;
            } else {
                valley = 0;
            }
            Log.d("DAILY", "peak: " + peak + " valley: " + valley);
            heights[2 * i + 1] = peak;
            heights[2 * i + 2] = valley;
            if (peak == 100){
                got100 = true;
            }
        }
        heights[2 * numPeaks] = 0;
        String s = "";
        for (int i : heights){
            s = s + " " + i;
        }
        Log.d("DAILY", "Mountain is " + s);
        if (!got100){
            int tallest = random.nextInt(numPeaks - 3) + 1;
            heights[2 * tallest + 1] = 100;
        }
        return new Mountain(heights);
    }

    public int getWidth(){
        return width;
    }

    public int getMaxHeight(){
        return maxHeight;
    }

    public int getHeightAt(int x){
        if (x > this.width || x < 0){
            throw new IndexOutOfBoundsException("The mountain is only " + this.width +
                    " wide but you tried to access " + x);
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
            throw new IndexOutOfBoundsException("The mountain is only " + this.width +
                    " wide but you tried to access " + x);
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

    public Integer[] getTurningPoints(){
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
