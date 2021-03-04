package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.core.util.Pair;

import com.google.common.base.Ticker;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Game {

    public static int speed;

    Mountain mountain;
    List<MountainClimber> climbers;
    boolean victory;
    Moving moving;
    private OnVictoryListener victoryListener;
    private Future<Solver> solver;
    private int movesTaken;
    private OnGo onGo;
    private Runnable onStopMoving;
    private CountUpTimer movingTimer;

    public Game(final Mountain mountain){
        if (speed < 1){
            speed = 1;
        }
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
        this.solver = null;
        this.movesTaken = 0;
        this.onGo = new OnGo() {
            @Override
            public void onGo() {
                return;
            }
        };
        movingTimer = new CountUpTimer(1000 / 48, new CountUpTimer.Ticker() {
            public void onTick(long millisElapsed) {
                moveStep();
            }
        });
    }

    public void setUpSolver(){
        solver = Executors.newSingleThreadExecutor().submit(new Callable<Solver>() {
            @Override
            public Solver call(){
                return new Solver(mountain, climbers.size());
            }
        });
    }

    public void setOnGo(OnGo onGo){
        this.onGo = onGo;
    }
    public void setOnStopMoving(Runnable r){
        this.onStopMoving = r;
    }

    public int getMovesTaken(){
        return movesTaken;
    }

    public void setMoves(int moves){
        movesTaken = moves;
    }

    public void setOnVictoryListener(OnVictoryListener v){
        this.victoryListener = v;
    }

    public void callOnVictoryListener(){
        if (victoryListener != null){
            victoryListener.onVictory();
        }
    }

    protected Pair<MountainClimber, MountainClimber> removeClimbers(){
        if (climbers.size() == 1){
            return null;
        }
        for (MountainClimber climber : climbers){
            for (MountainClimber c2 : climbers) {
                if (c2 != climber && Math.abs(c2.getPosition() - climber.getPosition()) < 1.5) {
                    this.climbers.remove(c2);
                    climber.setDirection(null);
                    return new Pair<>(climber, c2);
                }
            }
        }
        return null;
    }

    protected void updateVictory(){
        if (climbers.size() == 1 && !victory){
            victory = true;
            callOnVictoryListener();
        }
    }

    public boolean go() {
        if (victory || moving != Moving.NONE) {
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
            movesTaken ++;
            onGo.onGo();
            movingTimer.start();
            return true;
        }
        boolean canGoDown = true;
        for (MountainClimber climber : climbers) {
            canGoDown = canGoDown && climber.canMoveDown(mountain);
        }
        if (canGoDown) {
            Log.d("GAME", "Going down");
            moving = Moving.DOWN;
            movesTaken ++;
            onGo.onGo();
            movingTimer.start();
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
                movingTimer.cancel();
                onStopMoving.run();
                updateVictory();
            }
            moved = true;
        }
        return moved;
    }

    public int[] getPositions(){
        int[] positions = new int[climbers.size()];
        for (int i = 0; i < climbers.size(); i++){
            positions[i] = climbers.get(i).getPosition();
        }
        return positions;
    }

    public Future<Solver.Move> getHint(Context context){
        if (solver == null || !solver.isDone()){
            if (climbers.size() >= 5){
                Log.d("GAME", "Lots of climbers");
                Toast.makeText(context, "Calculating...", Toast.LENGTH_SHORT).show();
            }
            if (solver == null){
                setUpSolver();
            }
        }
        return Executors.newSingleThreadExecutor().submit(new Callable<Solver.Move>() {
            @Override
            public Solver.Move call() throws Exception {
                return solver.get().solve(getPositions()).get(0);
            }
        });
    }

    public enum Moving{
        UP, DOWN, NONE
    }

    public interface OnVictoryListener {
        void onVictory();
    }

    public interface OnGo {
        void onGo();
    }

}
