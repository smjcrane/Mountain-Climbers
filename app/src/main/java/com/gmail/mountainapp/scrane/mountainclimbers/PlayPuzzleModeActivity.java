package com.gmail.mountainapp.scrane.mountainclimbers;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PlayPuzzleModeActivity extends PlayGameActivity {

    public static final String SAVED_MOVES = "savedmoves";
    public static final String[] MESSAGES = new String[] {"", "YOU WIN!", "GREAT!", "PERFECT!"};

    private TextView movesText;
    private Game.OnVictoryListener onPuzzleVictoryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        movesText = findViewById(R.id.mountainTimerText);
        movesText.setVisibility(View.VISIBLE);

        onPuzzleVictoryListener = new Game.OnVictoryListener() {
            @Override
            public void onVictory() {
                onVictoryListener.onVictory();
                int levelDBID = db.getId(Common.PACK_POS, Common.LEVEL_POS);
                db.markCompleted(levelDBID);
                int previousBest = db.getBestMoves(levelDBID);
                int moves = game.getMovesTaken();
                int bestPossible = db.getOptimalMoves(levelDBID, PlayPuzzleModeActivity.this);
                int stars = LevelListAdapter.howManyStars(moves, bestPossible);
                if (moves < previousBest || previousBest == -1){
                    db.setBestMoves(levelDBID, moves);
                    MountainView.victoryMessage = MESSAGES[stars];
                } else {
                    MountainView.victoryMessage = "YOU WIN!";
                }
            }
        };

        buttonNextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.LEVEL_POS++;
                loadLevel(null);
                loadPuzzle(null);
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLevel(null);
                loadPuzzle(null);
            }
        });

        loadPuzzle(savedInstanceState);

        new Thread(new Runnable() {
            public void run() {
                DataBaseHandler db = new DataBaseHandler(PlayPuzzleModeActivity.this);
                db.getOptimalMoves(db.getId(Common.PACK_POS, Common.LEVEL_POS), PlayPuzzleModeActivity.this);
            }
        }).start();
    }

    protected void loadPuzzle(Bundle savedInstanceState){
        super.loadLevel(savedInstanceState);
        buttonHint.setVisibility(View.INVISIBLE);
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
        game.setOnVictoryListener(onPuzzleVictoryListener);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_MOVES, game.getMovesTaken());
    }
}
