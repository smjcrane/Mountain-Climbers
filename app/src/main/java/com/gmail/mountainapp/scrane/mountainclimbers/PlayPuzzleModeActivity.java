package com.gmail.mountainapp.scrane.mountainclimbers;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

public class PlayPuzzleModeActivity extends PlayGameActivity {

    public static final String SAVED_MOVES = "savedmoves";
    public static final String[] MESSAGES = new String[] {"", "YOU WIN!", "GREAT!", "PERFECT!"};

    private TextView movesText;
    private int optimalMoves;

    @Override
    protected void setup(){
        super.setup();
        movesText = findViewById(R.id.mountainTimerText);
        movesText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVictory() {
        super.onVictory();
        buttonHint.setVisibility(View.INVISIBLE);
        int levelPos = preferences.getInt(getString(R.string.LEVELPOS),0);
        int levelDBID = db.getId(packPos, levelPos);
        int previousBest = db.getBestMoves(levelDBID);
        int moves = game.getMovesTaken();
        if (moves < previousBest || previousBest == -1) {
            db.setBestMoves(levelDBID, moves);
        }
        if (optimalMoves != -1){
            int stars = LevelListAdapter.howManyStars(moves, optimalMoves);
            if (moves < previousBest || previousBest == -1){
                MountainView.victoryMessage = MESSAGES[stars];
            } else {
                MountainView.victoryMessage = "YOU WIN!";
            }
        } else {
            MountainView.victoryMessage = "YOU WIN!";
        }
        mountainView.invalidate();
        if (shouldUpdateAchievements){
            AchievementsClient client = Games.getAchievementsClient(PlayPuzzleModeActivity.this, account);
            client.setSteps(getString(R.string.achievement_perfect_score), db.howManyPerfect());
        }
    }

    @Override
    protected void loadLevel(Bundle savedInstanceState){
        super.loadLevel(savedInstanceState);
        buttonHint.setVisibility(View.INVISIBLE);
        optimalMoves = -1;
        int moves = 0;
        if (savedInstanceState != null){
            moves = savedInstanceState.getInt(SAVED_MOVES);
        }
        movesText.setText("Moves: " + moves);
        game.setMoves(moves);
        game.setOnGo(new Game.OnGo() {
            @Override
            public void onGo() {
                movesText.setText("Moves: " + game.getMovesTaken());
            }
        });
        new Thread(new Runnable() {
            public void run() {
                int levelPos = preferences.getInt(getString(R.string.LEVELPOS),0);
                optimalMoves = db.getOptimalMoves(db.getId(packPos, levelPos), PlayPuzzleModeActivity.this);
            }
        }).start();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_MOVES, game.getMovesTaken());
    }
}
