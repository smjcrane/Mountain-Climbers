package com.gmail.mountainapp.scrane.mountainclimbers;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

public class PlayTimedModeActivity extends PlayGameActivity {

    private static final String SAVED_TIME = "savedtime";

    private TextView timerText;
    private CountDownView countDownView;
    private Game.OnVictoryListener onTimedVictoryListener;
    private CountUpTimer timer;
    private int seconds;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){
            mountainView.deActivate();
        }

        timerText = findViewById(R.id.mountainTimerText);
        timerText.setVisibility(View.VISIBLE);

        countDownView = findViewById(R.id.mountainCountdown);
        countDownView.setVisibility(View.VISIBLE);

        buttonNextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.LEVEL_POS++;
                loadLevel(null);
                loadTimers(null);
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLevel(null);
                loadTimers(null);
            }
        });

        onTimedVictoryListener = new Game.OnVictoryListener() {
            @Override
            public void onVictory() {
                onVictoryListener.onVictory();
                timer.cancel();
                int levelDBID = db.getId(Common.PACK_POS, Common.LEVEL_POS);
                db.markCompleted(levelDBID);
                int previousBest = db.getBestTimeSeconds(levelDBID);
                if (seconds < previousBest || previousBest == -1){
                    db.setBestTimeSeconds(levelDBID, seconds);
                    MountainView.victoryMessage = "NEW RECORD!";
                } else {
                    MountainView.victoryMessage = "YOU WIN!";
                }
                if (shouldUpdateAchievements){
                    AchievementsClient client = Games.getAchievementsClient(PlayTimedModeActivity.this, account);
                    client.setSteps(getString(R.string.achievement_speed_demon), db.howManyInUnder10Seconds());
                }
            }
        };

        loadTimers(savedInstanceState);
    }

    protected void loadTimers(Bundle savedInstanceState){
        mountainView.deActivate();
        buttonHint.setVisibility(View.INVISIBLE);
        timerText.setText("0:00");
        game.setOnVictoryListener(onTimedVictoryListener);
        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVED_TIME)){
            if (timer != null){
                timer.cancel();
            }
            countDownView.setOnCounted(new CountDownView.OnCounted() {
                @Override
                public void onCounted() {
                    if (timer != null){
                        timer.cancel();
                    }
                    timer = new CountUpTimer(1000) {
                        public void onTick(long millis) {
                            int second = (int) millis / 1000;
                            timerText.setText(LevelListAdapter.formatTimeSeconds(second));
                            seconds = second;
                            Log.d("TIME", Integer.toString(second));
                        }
                    };
                    mountainView.activate();
                    timer.start();
                }
            });
            countDownView.start(3);
        } else {
            timer.setMillisAtStart(savedInstanceState.getLong(SAVED_TIME));
            timer.start();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        if (timer!=null && !timer.cancelled) {
            outState.putLong(SAVED_TIME, timer.getMillisAtStart());
            timer.cancel();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (timer != null){
            timer.cancel();
        }
    }
}
