package com.gmail.mountainapp.scrane.mountainclimbers;

import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class TutorialInstruction {
    public static final int GO_BUTTON = -1;
    public static final int ANYWHERE = -2;

    private int objectID;
    private String text;
    private MountainClimber.Direction direction;
    private boolean done;
    private MountainClimber climber;
    private boolean isHint;
    private Solver.Move hint;

    public TutorialInstruction(String text, int objectID, MountainClimber.Direction direction){
        this.text = text;
        this.objectID = objectID;
        this.direction = direction;
        this.done = false;
        this.isHint = false;
        this.hint = null;
    }

    public TutorialInstruction(String text, int objectID, MountainClimber.Direction direction, boolean isHint){
        this(text, objectID, direction);
        this.isHint = isHint;
        Log.d("INST", "I am " + (isHint ? "" : "not ") + "a hint");
        this.hint = new Solver.Move(new MountainClimber.Direction[] {direction});
    }

    public boolean isHint(){
        return isHint;
    }

    public Future<Solver.Move> getHint(){
        return new Future<Solver.Move>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Solver.Move get() throws ExecutionException, InterruptedException {
                return hint;
            }

            @Override
            public Solver.Move get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
                return hint;
            }
        };
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

    public MountainClimber.Direction getDirection() {
        return direction;
    }

    public MountainClimber getClimber(){
        return climber;
    }
}
