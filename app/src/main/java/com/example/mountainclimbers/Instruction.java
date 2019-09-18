package com.example.mountainclimbers;

import static com.example.mountainclimbers.MountainView.PADDING;

public class Instruction {
    public static final int GO_BUTTON = -1;
    public static final int ANYWHERE = -2;

    private int objectID;
    private String text;
    private MountainView mountainView;
    private MountainClimber.Direction direction;
    private boolean done;

    public Instruction(String text, int objectID, MountainClimber.Direction direction, TutorialMountainView mountainView){
        this.text = text;
        this.objectID = objectID;
        this.mountainView = mountainView;
        this.direction = direction;
        done = false;
    }

    public boolean isDone(){
        if (objectID == ANYWHERE || objectID == GO_BUTTON){
            return done;
        } else {
            return mountainView.climbers.get(objectID).getDirection() == direction;
        }
    }

    public void markAsDone(){
        done = true;
    }

    public String getText(){
        return text;
    }

    public int getObjectID(){
        return objectID;
    }
}
