package com.example.mountainclimbers;

import java.util.zip.DeflaterInputStream;

public class MountainClimber {

    private int position;
    private Direction direction;

    public MountainClimber(){
        position = 0;
        direction = null;
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setDirection(Direction direction){
        this.direction = direction;
    }

    public Direction getDirection(){
        return direction;
    }

    public boolean canMoveUp(Mountain mountain){
        Mountain.Slope slope = mountain.getTypeAt(position);
        switch (slope){
            case MIN:
                return true;
            case MAX:
                return false;
            case UP:
                return direction == Direction.RIGHT;
            case DOWN:
                return direction == Direction.LEFT;
        }
        return false;
    }

    public boolean canMoveDown(Mountain mountain){
        if (position == 0 || position == mountain.getWidth()){
            return false;
        }
        Mountain.Slope slope = mountain.getTypeAt(position);
        switch (slope){
            case MIN:
                return false;
            case MAX:
                return true;
            case UP:
                return direction == Direction.LEFT;
            case DOWN:
                return direction == Direction.RIGHT;
        }
        return false;
    }

    public void move(){
        if (direction == Direction.LEFT){
            position--;
        } else if (direction == Direction.RIGHT) {
            position ++;
        } else {
            throw new NullPointerException("Cannot move without a direction");
        }
    }

    public String toString(){
        return "Climber at " + Integer.toString(position);
    }

    public enum Direction{
        LEFT, RIGHT
    }
}
