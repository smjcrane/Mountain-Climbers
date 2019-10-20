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
    private CountUpTimer timer;
    private int seconds;
    private boolean paused;
    long millis;

    @Override
    protected void setup() {
        super.setup();
        millis = 0;
        timerText = findViewById(R.id.mountainTimerText);
        timerText.setVisibility(View.VISIBLE);

        countDownView = findViewById(R.id.mountainCountdown);
        countDownView.setVisibility(View.VISIBLE);
        paused = false;
    }

    @Override
    public void onVictory() {
        super.onVictory();
        timer.cancel();
        int levelPos = preferences.getInt(getString(R.string.LEVELPOS),0);
        int levelDBID = db.getId(packPos, levelPos);
        int previousBest = db.getBestTimeSeconds(levelDBID);
        if (seconds < previousBest || previousBest == -1){
            db.setBestTimeSeconds(levelDBID, seconds);
            MountainView.victoryMessage = getString(R.string.newrecord);
        } else {
            MountainView.victoryMessage = getString(R.string.youwin);
        }
        if (shouldUpdateAchievements){
            AchievementsClient client = Games.getAchievementsClient(PlayTimedModeActivity.this, account);
            client.setSteps(getString(R.string.achievement_quick_10), db.howManyInUnder10Seconds());
            client.setSteps(getString(R.string.achievement_quick_100), db.howManyInUnder10Seconds());
        }
    }

    protected void loadLevel(Bundle savedInstanceState){
        super.loadLevel(savedInstanceState);
        millis = 0;
        buttonHint.setVisibility(View.INVISIBLE);
        mountainView.deActivate();
        timerText.setText("0:00");
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

    @Override
    public void onBackPressed(){
        if (timer != null){
            millis = timer.getMillisAtStart();
            timer.cancel();
        } else {
            millis = 0;
        }
        if (countDownView != null) {
                countDownView.cancel();
        }
        super.onBackPressed();
    }

    @Override
    public void onResume(){
        super.onResume();
        countDownView.start(3);
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
                if (millis != 0){
                    timer.setMillisAtStart(millis + 1000 * seconds);
                }
            }
        });
    }
}
