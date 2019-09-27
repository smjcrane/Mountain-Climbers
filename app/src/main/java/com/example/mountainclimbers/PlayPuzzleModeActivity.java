package com.example.mountainclimbers;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.example.mountainclimbers.Common.MODE_PUZZLE;
import static com.example.mountainclimbers.Common.MODE_TIMED;

public class PlayPuzzleModeActivity extends PlayGameActivity {

    public static final String SAVED_MOVES = "savedmoves";

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
                if (moves < previousBest || previousBest == -1){
                    db.setBestMoves(levelDBID, moves);
                    MountainView.victoryMessage = "NEW RECORD!";
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

        loadPuzzle(savedInstanceState);
    }

    protected void loadPuzzle(Bundle savedInstanceState){
        super.loadLevel(savedInstanceState);
        if (movesText == null){
            return;
        }
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
