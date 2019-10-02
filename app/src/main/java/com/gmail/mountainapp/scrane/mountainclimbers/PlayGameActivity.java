package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.games.Games;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PlayGameActivity extends SignedInActivity {

    static final String SAVED_POSITIONS = "savedpositions";
    static final String SAVED_DIRECTIONS = "saveddirections";

    protected MountainView mountainView;
    protected Game game;
    protected Button buttonBack, buttonNextLevel;
    protected ImageView buttonReset, buttonHint, settingsButton;
    protected TextView goButton, levelNumberText;
    protected DataBaseHandler db;

    protected static Game.OnVictoryListener onVictoryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_mountain);

        Common.tutorial = false;

        mountainView = findViewById(R.id.mountainView);
        buttonHint = findViewById(R.id.mountainHintButton);
        levelNumberText = findViewById(R.id.mountainLevelNumber);
        goButton = findViewById(R.id.mountainGoButton);
        buttonBack = findViewById(R.id.mountainBackButton);
        buttonReset = findViewById(R.id.mountainResetButton);
        buttonNextLevel = findViewById(R.id.mountainNextLevelButton);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayGameActivity.this.mountainView.go();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLevel(null);
            }
        });

        buttonNextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.LEVEL_POS++;
                loadLevel(null);
            }
        });

        buttonHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Solver.Move hint = game.getHint();
                Log.d("MTN", hint.toString());
                mountainView.showHint();
            }
        });

        settingsButton = findViewById(R.id.mainSettingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mountainView.resetClimberDrawableColor();
                Intent settings = new Intent();
                settings.setClass(PlayGameActivity.this, SettingsActivity.class);
                startActivity(settings);
            }
        });

        onVictoryListener = new Game.OnVictoryListener() {
            @Override
            public void onVictory() {
                Log.d("PLAY", "victory!");
                goButton.setVisibility(View.INVISIBLE);
                buttonHint.setVisibility(View.INVISIBLE);
                buttonBack.setVisibility(View.VISIBLE);

                db = new DataBaseHandler(PlayGameActivity.this);
                int levelDBID = db.getId(Common.PACK_POS, Common.LEVEL_POS);
                if (!db.isCompleted(levelDBID) && Common.PACK_POS == 0){
                    Games.getAchievementsClient(PlayGameActivity.this, account)
                            .increment(getString(R.string.achievement_getting_started), 1);
                }
                db.markCompleted(levelDBID);
                db.close();

                if (Common.LEVEL_POS < Levels.packs[Common.PACK_POS].getLength() - 1){
                    buttonNextLevel.setVisibility(View.VISIBLE);
                }
            }
        };

        loadLevel(savedInstanceState);
    }

    protected void loadLevel(Bundle savedInstanceState){
        int[] positions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_POSITIONS);
        int[] directions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_DIRECTIONS);

        levelNumberText.setText(Integer.toString(Common.LEVEL_POS + 1));
        int levelID = Levels.packs[Common.PACK_POS].getLevelIDs()[Common.LEVEL_POS];
        mountainView.setSeed((long) levelID);

        buttonBack.setVisibility(View.INVISIBLE);
        buttonNextLevel.setVisibility(View.INVISIBLE);
        goButton.setVisibility(View.VISIBLE);
        buttonHint.setVisibility(View.VISIBLE);
        try {
            InputStream stream = getResources().openRawResource(levelID);

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String[] heightStrings = br.readLine().split(" ");
            String[] climberString = br.readLine().split(" ");

            int[] heights = new int[heightStrings.length];
            for (int i = 0; i < heightStrings.length; i++) {
                heights[i] = Integer.parseInt(heightStrings[i]);
            }

            final Mountain mountain = new Mountain(heights);
            game = new Game(mountain);
            game.setOnVictoryListener(onVictoryListener);

            mountainView.setGame(game);

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

            while(game.removeClimbers()){}
            game.updateVictory();
            if (game.victory){
                game.callOnVictoryListener();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        outState.putIntArray(SAVED_POSITIONS, game.getPositions());
        int[] directions = new int[game.climbers.size()];
        for (int i = 0; i < game.climbers.size(); i++){
            MountainClimber.Direction d = game.climbers.get(i).getDirection();
            directions[i] = (d == null) ? 0 : (d == MountainClimber.Direction.LEFT) ? 1 : 2;
        }
        outState.putIntArray(SAVED_DIRECTIONS, directions);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy(){
        if (db != null){
            db.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(SettingsActivity.PREFERENCES, MODE_PRIVATE);
        boolean isLandscapeLocked = preferences.getBoolean(SettingsActivity.LANDSCAPE_LOCKED, true);
        Game.speed = preferences.getInt(SettingsActivity.SPEED, 1);
        if (isLandscapeLocked){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
        mountainView.updateClimberDrawable();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mountainView.resetClimberDrawableColor();
    }

}
