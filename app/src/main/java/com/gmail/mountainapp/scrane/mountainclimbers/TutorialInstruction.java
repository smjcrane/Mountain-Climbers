package com.gmail.mountainapp.scrane.mountainclimbers;

import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class TutorialInstruction {
    public static final int GO_BUTTON = -1;
    public static final int ANYWHERE = -2;
    public static final int HINT_BUTTON = -3;

    private int objectID;
    private String text;
    private MountainClimber.Direction direction;
    private boolean done;
    private MountainClimber climber;
    private Solver.Move hint;

    public TutorialInstruction(String text, int objectID, MountainClimber.Direction direction){
        this.text = text;
        this.objectID = objectID;
        this.direction = direction;
        this.done = false;
        this.hint = null;
    }

    public TutorialInstruction(String text, int objectID, Solver.Move hint){
        this(text, objectID, hint.getDirections()[0]);
        this.hint = hint;
    }

    public Future<Solver.Move> getHint(){
        if (hint == null){
            return null;
        }
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
        if (objectID == ANYWHERE || objectID == GO_BUTTON || objectID == HINT_BUTTON){
            return done;
        } else {
            Log.d("TUT", "I want " + direction + " and it is " + climber.getDirection());
            return climber.getDirection() == direction;
        }
    }

    public void markAsDone(){
        if (objectID == ANYWHERE || objectID == GO_BUTTON || objectID == HINT_BUTTON){
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
