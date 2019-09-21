package com.example.mountainclimbers;

import android.os.CountDownTimer;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private static int speed = 1;

    Mountain mountain;
    List<MountainClimber> climbers;
    boolean victory;
    Moving moving;
    private OnVictoryListener victoryListener;
    private Solver solver;

    public Game(Mountain mountain){
        this.mountain = mountain;
        this.moving = Moving.NONE;
        this.victoryListener = new OnVictoryListener() {
            @Override
            public void onVictory() {
                return;
            }
        };
        this.climbers = new ArrayList<>();
        this.victory = false;
        this.solver = new Solver(mountain);
    }

    public void setSpeed(int speed){
        speed = speed;
    }

    public void setOnVictoryListener(OnVictoryListener v){
        this.victoryListener = v;
    }

    protected boolean removeClimbers(){
        if (climbers.size() == 1){
            return false;
        }
        for (MountainClimber climber : climbers){
            for (MountainClimber c2 : climbers) {
                if (c2 != climber && Math.abs(c2.getPosition() - climber.getPosition()) < 1.5) {
                    this.climbers.remove(c2);
                    climber.setDirection(null);
                    return true;
                }
            }
        }
        return false;
    }

    protected void updateVictory(){
        if (climbers.size() == 1){
            victory = true;
        }
    }

    public boolean go() {
        if (victory) {
            return false;
        }

        boolean allSelected = true;
        for (MountainClimber climber : climbers) {
            if (climber.getDirection() == null) {
                allSelected = false;
            }
        }
        if (allSelected == false) {
            return false;
        }
        boolean canGoUp = true;
        for (MountainClimber climber : climbers) {
            canGoUp = canGoUp && climber.canMoveUp(mountain);
        }
        if (canGoUp) {
            Log.d("GAME", "Going up");
            moving = Moving.UP;
            return true;
        }
        boolean canGoDown = true;
        for (MountainClimber climber : climbers) {
            canGoDown = canGoDown && climber.canMoveDown(mountain);
        }
        if (canGoDown) {
            Log.d("GAME", "Going down");
            moving = Moving.DOWN;
            return true;
        }
        return false;
    }

    public boolean moveStep(){
        boolean moved = false;
        for (int i = 0; i < speed && moving != Moving.NONE; i++){
            boolean canGoUp = (moving == Moving.UP);
            for (MountainClimber climber : climbers){
                canGoUp = canGoUp && climber.canMoveUp(mountain);
            }
            boolean canGoDown = (moving == Moving.DOWN);
            for (MountainClimber climber : climbers){
                canGoDown = canGoDown && climber.canMoveDown(mountain);
            }
            if (canGoUp || canGoDown){
                for (MountainClimber climber : climbers){
                    climber.move();
                }
            } else {
                moving = Moving.NONE;
                while (removeClimbers()){};
                updateVictory();
                if (victory) {
                    victoryListener.onVictory();
                }
            }
            moved = true;
        }
        return moved;
    }

    public Solver.Move getHint(){
        int[] positions = new int[climbers.size()];
        for (int i = 0; i < climbers.size(); i++){
            positions[i] = climbers.get(i).getPosition();
        }
        return solver.solve(positions).get(0);
    }

    public enum Moving{
        UP, DOWN, NONE
    }

    public interface OnVictoryListener {
        void onVictory();
    }

}
