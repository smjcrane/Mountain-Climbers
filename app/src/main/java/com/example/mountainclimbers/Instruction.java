package com.example.mountainclimbers;

import android.util.Log;

import static com.example.mountainclimbers.MountainView.PADDING;

public class Instruction {
    public static final int GO_BUTTON = -1;
    public static final int ANYWHERE = -2;

    private int objectID;
    private String text;
    private MountainClimber.Direction direction;
    private boolean done;
    private MountainClimber climber;

    public Instruction(String text, int objectID, MountainClimber.Direction direction){
        this.text = text;
        this.objectID = objectID;
        this.direction = direction;
        this.done = false;
    }

    public void setClimber(MountainClimber climber){
        this.climber = climber;
    }

    public boolean isDone(){
        if (objectID == ANYWHERE || objectID == GO_BUTTON){
            return done;
        } else {
            Log.d("TUT", "I want " + direction + " and it is " + climber.getDirection());
            return climber.getDirection() == direction;
        }
    }

    public void markAsDone(){
        if (objectID == ANYWHERE || objectID == GO_BUTTON){
            done = true;
        }
    }

    public String getText(){
        return text;
    }

    public int getObjectID(){
        return objectID;
    }
}
