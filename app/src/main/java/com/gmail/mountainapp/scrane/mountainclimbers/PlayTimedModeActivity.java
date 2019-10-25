package com.gmail.mountainapp.scrane.mountainclimbers;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

public class PlayTimedModeActivity extends PlayGameActivity implements CountUpTimer.Ticker {

    private static final String SAVED_TIME = "savedtime";
    private static final String VICTORY = "savedvictory";
    private static final String WAS_RECORD = "savedrecord";

    private TextView timerText;
    private CountDownView countDownView;
    private CountUpTimer timer;
    private int seconds;
    private long millisCounted;
    private boolean wonBefore, wasRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        timer = new CountUpTimer(1000, this);
    }

    @Override
    protected void setup() {
        super.setup();
        timerText = findViewById(R.id.mountainTimerText);
        timerText.setVisibility(View.VISIBLE);

        countDownView = findViewById(R.id.mountainCountdown);
        countDownView.setVisibility(View.VISIBLE);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownView.cancel();
                loadLevel(null);
            }
        });
    }

    @Override
    public void onVictory() {
        super.onVictory();
        if (timer != null){
            timer.cancel();
        }
        int levelPos = preferences.getInt(getString(R.string.LEVELPOS),0);
        int levelDBID = db.getId(packPos, levelPos);
        int previousBest = db.getBestTimeSeconds(levelDBID);
        seconds = (int) millisCounted / 1000;
        if (wonBefore) {
            if (wasRecord) {
                victoryText.setText(getString(R.string.newrecord));
            } else {
                victoryText.setText(getString(R.string.youwin));
            }
        } else {
            if (seconds < previousBest || previousBest == -1) {
                db.setBestTimeSeconds(levelDBID, seconds);
                wasRecord = true;
                victoryText.setText(getString(R.string.newrecord));
            } else {
                victoryText.setText(getString(R.string.youwin));
            }
        }
        if (shouldUpdateAchievements){
            client.setSteps(getString(R.string.achievement_quick_10), db.howManyInUnder10Seconds());
            client.setSteps(getString(R.string.achievement_quick_100), db.howManyInUnder10Seconds());
        }
    }

    public void onTick(long millis){
        millisCounted = millis;
        seconds = (int) millis / 1000;
        timerText.setText(LevelListAdapter.formatTimeSeconds(seconds));
        Log.d("TIME", Integer.toString(seconds));
    }

    protected void loadLevel(Bundle savedInstanceState){
        if (timer != null){
            timer.cancel();
        }
        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVED_TIME)) {
            millisCounted = 0;
        } else {
            millisCounted = savedInstanceState.getLong(SAVED_TIME);
        }
        wonBefore = (savedInstanceState == null) ? false : savedInstanceState.getBoolean(VICTORY);
        wasRecord = (savedInstanceState == null) ? false : savedInstanceState.getBoolean(WAS_RECORD);
        super.loadLevel(savedInstanceState);
        buttonHint.setVisibility(View.INVISIBLE);
        timerText.setText(LevelListAdapter.formatTimeSeconds((int) millisCounted / 1000));
        if (game.victory){
            return;
        }
        mountainView.deActivate();
        countDownView.setOnFinish(new Runnable() {
            @Override
            public void run() {
                timer = new CountUpTimer(1000, PlayTimedModeActivity.this);
                mountainView.activate();
                timer.start();
                timer.setMillisAtStart(SystemClock.elapsedRealtime() - millisCounted);
            }
        });
        countDownView.start(3);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        if (timer!=null && !timer.cancelled) {
            outState.putLong(SAVED_TIME, millisCounted);
            timer.cancel();
        }
        outState.putBoolean(VICTORY, game.victory);
        outState.putBoolean(WAS_RECORD, wasRecord);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (timer != null){
            timer.cancel();
        }
        if (countDownView != null){
            countDownView.cancel();
        }
    }

    @Override
    public void onBackPressed(){
        if (timer != null){
            millisCounted = timer.getMillisCounted();
            timer.cancel();
        }
        if (countDownView != null) {
            countDownView.cancel();
        }
        super.onBackPressed();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (game.victory){
            return;
        }
        countDownView.start(3);
        countDownView.setOnFinish(new Runnable() {
            @Override
            public void run() {
                if (timer != null){
                    timer.cancel();
                }
                timer = new CountUpTimer(1000, PlayTimedModeActivity.this);
                mountainView.activate();
                timer.start();
                timer.setMillisAtStart(SystemClock.elapsedRealtime() - millisCounted);
            }
        });
    }
}
