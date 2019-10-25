package com.gmail.mountainapp.scrane.mountainclimbers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Date;

public class PlayDailyLevel extends PlayGameActivity {
    long daysSinceEpoch;

    @Override
    public void loadLevel(Bundle savedInstanceState){
        victoryText.setText("");
        final int[] positions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_POSITIONS);
        int[] directions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_DIRECTIONS);

        buttonBack.setVisibility(View.INVISIBLE);
        buttonNextLevel.setVisibility(View.INVISIBLE);
        goButton.setVisibility(View.VISIBLE);
        buttonHint.setVisibility(View.VISIBLE);
        levelNumberText.setText(getString(R.string.daily_puzzle));

        Date date = new Date();
        daysSinceEpoch = date.getTime() / (60 * 60 * 24 * 1000);
        final Mountain mountain = Mountain.generateRandomMountain(daysSinceEpoch);
        game = new Game(mountain);
        game.setOnVictoryListener(this);

        mountainView.setGame(game);
        String[] climberString = new String[] {"0", Integer.toString(mountain.getWidth())};

        for (int i = 0; i < climberString.length; i++) {
            MountainClimber climber = new MountainClimber();
            if (positions == null){
                climber.setPosition(Integer.parseInt(climberString[i]));
            } else if (positions.length > i ){
                climber.setPosition(positions[i]);
            }
            if (directions != null && directions.length > i){
                climber.setDirection(Common.DIRECTIONS[directions[i]]);
            }
            if (savedInstanceState == null || positions != null && positions.length > i){
                mountainView.addClimber(climber);
            }
        }

        while(game.removeClimbers() != null){}
        game.updateVictory();
        game.setUpSolver();
    }

    @Override
    public void onVictory(){
        Log.d("DAILY", "victory!");
        goButton.setVisibility(View.INVISIBLE);
        buttonHint.setVisibility(View.INVISIBLE);
        mountainView.invalidate();
        victoryText.setText(getString(R.string.youwin) + "!");
        DataBaseHandler db = new DataBaseHandler(this);
        db.markDailyCompleted(daysSinceEpoch);
        db.close();
        if (shouldUpdateAchievements) {
            client.setSteps(getString(R.string.achievement_addicted),
                    db.getDailyStreak(daysSinceEpoch));
        }
    }
}
