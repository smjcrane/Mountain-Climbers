package com.gmail.mountainapp.scrane.mountainclimbers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

public class PlayPuzzleModeActivity extends PlayGameActivity {

    public static final String SAVED_MOVES = "savedmoves";
    public static final int[] MESSAGES = new int[] {0, R.string.youwin, R.string.stars2, R.string.stars3};

    private TextView movesText;
    private Future<Integer> optimalMoves;
    private RelativeLayout starBlock;
    private ImageView[] starFills;

    @Override
    protected void setup(){
        super.setup();
        movesText = findViewById(R.id.mountainTimerText);
        movesText.setVisibility(View.VISIBLE);
        starBlock = findViewById(R.id.starBlock);
        starFills = new ImageView[] {findViewById(R.id.starFill1), findViewById(R.id.starFill2), findViewById(R.id.starFill3)};
    }

    @Override
    public void onVictory() {
        super.onVictory();
        buttonHint.setVisibility(View.INVISIBLE);
        int levelPos = preferences.getInt(getString(R.string.LEVELPOS),0);
        int levelDBID = db.getId(packPos, levelPos);
        int previousBest = db.getBestMoves(levelDBID);
        int moves = game.getMovesTaken();
        if (moves < previousBest || previousBest <=0) {
            db.setBestMoves(levelDBID, moves);
        }
        try {
            if (optimalMoves == null){
                optimalMoves = db.getOptimalMoves(db.getId(packPos, levelPos), PlayPuzzleModeActivity.this);
            }
            if (optimalMoves.isDone() && optimalMoves.get() != -1) {
                int stars = LevelListAdapter.howManyStars(moves, optimalMoves.get());
                starBlock.setVisibility(View.VISIBLE);
                AnimatorSet animatorSet = new AnimatorSet();
                List<Animator> animators = new ArrayList<>();
                PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(SCALE_X, 0, 1);
                PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(SCALE_Y, 0, 1);
                for (int i = 0; i < 3; i++){
                    if (i < stars){
                        starFills[i].setVisibility(View.VISIBLE);
                    } else {
                        starFills[i].setVisibility(View.INVISIBLE);
                    }
                    Rect bounds = new Rect();
                    Point globalOffset = new Point();

                    starFills[i].getGlobalVisibleRect(bounds, globalOffset);
                    bounds.offset(-globalOffset.x, -globalOffset.y);
                    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(starFills[i], pvhX, pvhY);
                    animator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
                    animators.add(animator);
                }
                animatorSet.playTogether(animators);
                animatorSet.start();
                victoryMessage = PlayPuzzleModeActivity.this.getString(MESSAGES[stars]);
                victoryText.setText(victoryMessage);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mountainView.invalidate();
        if (shouldUpdateAchievements){
            client.setSteps(getString(R.string.achievement_perfect_10), db.howManyPerfect());
            client.setSteps(getString(R.string.achievement_perfect_100), db.howManyPerfect());
        }
    }

    @Override
    protected void loadLevel(Bundle savedInstanceState){
        super.loadLevel(savedInstanceState);
        buttonHint.setVisibility(View.INVISIBLE);
        starBlock.setVisibility(View.INVISIBLE);
        int moves = 0;
        if (savedInstanceState != null){
            moves = savedInstanceState.getInt(SAVED_MOVES);
        }
        movesText.setText("Moves: " + moves);
        game.setMoves(moves);
        game.setOnGo(new Game.OnGo() {
            @Override
            public void onGo() {
                movesText.setText(getString(R.string.moves) + ": " + game.getMovesTaken());
            }
        });
        int levelPos = preferences.getInt(getString(R.string.LEVELPOS),0);
        optimalMoves = db.getOptimalMoves(db.getId(packPos, levelPos), PlayPuzzleModeActivity.this);
        if (game.victory){
            game.callOnVictoryListener();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_MOVES, game.getMovesTaken());
    }
}
