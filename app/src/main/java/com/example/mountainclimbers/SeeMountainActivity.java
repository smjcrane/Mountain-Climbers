package com.example.mountainclimbers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;

import static com.example.mountainclimbers.Common.MODE_TIMED;

public class SeeMountainActivity extends AppCompatActivity {

    static final String SAVED_POSITIONS = "savedpositions";
    static final String SAVED_DIRECTIONS = "saveddirections";
    static final MountainClimber.Direction[] DIRECTIONS =
            new MountainClimber.Direction[] {null, MountainClimber.Direction.LEFT, MountainClimber.Direction.RIGHT
    };
    private static final String SAVED_TIME = "savedtime";
    
    private MountainView mountainView;
    private Game game;
    public static int[] colorIDs = new int[] {R.color.climberGreen, R.color.climberPurple, R.color.climberOrange};
    private Button buttonBack, buttonNextLevel;
    private ImageView buttonReset, buttonHint;
    private TextView goButton, levelNumberText, timerText;
    private Integer[] levelIDs;
    private int levelID;
    private DataBaseHandler db;
    private int speed;
    private int mode;
    private CountUpTimer timer;
    private int seconds;
    private int packPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_mountain);

        Common.tutorial = false;

        SharedPreferences preferences = getSharedPreferences(SettingsActivity.PREFERENCES, MODE_PRIVATE);
        speed = preferences.getInt(SettingsActivity.SPEED, 1);
        Game.speed = speed;

        mountainView = findViewById(R.id.mountainView);
        buttonHint = findViewById(R.id.mountainHintButton);
        levelNumberText = findViewById(R.id.mountainLevelNumber);
        goButton = findViewById(R.id.mountainGoButton);
        buttonBack = findViewById(R.id.mountainBackButton);
        buttonReset = findViewById(R.id.mountainResetButton);
        buttonNextLevel = findViewById(R.id.mountainNextLevelButton);

        packPos = Common.PACK_POS;
        Levels.Pack pack = Levels.packs[packPos];
        levelIDs = pack.getLevelIDs();
        levelID = levelIDs[Common.LEVEL_POS];
        mode = Common.MODE;

        loadLevel(savedInstanceState);

        timerText = findViewById(R.id.mountainTimerText);
        if (mode == MODE_TIMED){
            timerText.setVisibility(View.VISIBLE);
        }

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeeMountainActivity.this.mountainView.go();
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
                levelID = levelIDs[Common.LEVEL_POS];
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

    }

    private void loadLevel(Bundle savedInstanceState){
        int[] positions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_POSITIONS);
        int[] directions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_DIRECTIONS);

        levelNumberText.setText(Integer.toString(Common.LEVEL_POS + 1));
        mountainView.setSeed((long) levelID);

        buttonBack.setVisibility(View.INVISIBLE);
        buttonNextLevel.setVisibility(View.INVISIBLE);
        goButton.setVisibility(View.VISIBLE);
        if (mode == Common.MODE_DEFAULT){
            buttonHint.setVisibility(View.VISIBLE);
        }
        try {
            InputStream stream = getResources().openRawResource(levelID);

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String[] heightStrings = br.readLine().split(" ");
            String[] climberString = br.readLine().split(" ");

            int[] heights = new int[heightStrings.length];
            for (int i = 0; i < heightStrings.length; i++) {
                heights[i] = Integer.parseInt(heightStrings[i]);
            }

            Mountain mountain = new Mountain(heights);
            game = new Game(mountain);
            game.setOnVictoryListener(new Game.OnVictoryListener() {
                @Override
                public void onVictory() {
                    goButton.setVisibility(View.INVISIBLE);
                    buttonHint.setVisibility(View.INVISIBLE);
                    buttonBack.setVisibility(View.VISIBLE);

                    db = new DataBaseHandler(SeeMountainActivity.this);
                    db.markCompleted(db.getId(Common.PACK_POS, Common.LEVEL_POS));
                    db.close();

                    if (Common.LEVEL_POS < levelIDs.length - 1){
                        buttonNextLevel.setVisibility(View.VISIBLE);
                    }
                    if (mode == MODE_TIMED){
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
                    }
                }
            });

            mountainView.setGame(game);

            for (int i = 0; i < climberString.length; i++) {
                MountainClimber climber = new MountainClimber();
                if (positions == null){
                    climber.setPosition(Integer.parseInt(climberString[i]));
                } else if (positions.length > i ){
                    climber.setPosition(positions[i]);
                }
                if (directions != null && directions.length > i){
                    climber.setDirection(DIRECTIONS[directions[i]]);
                }
                if (savedInstanceState == null || positions != null && positions.length > i){
                    mountainView.addClimber(climber, colorIDs[i]);
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
        if (mode == MODE_TIMED){
            if (timer != null){
                timer.cancel();
            }
            if (savedInstanceState == null){
                timer = new CountUpTimer(1000) {
                    public void onTick(long millis) {
                        int second = (int) millis / 1000;
                        timerText.setText(String.valueOf(second));
                        seconds = second;
                    }
                };
                timer.start();
            } else {
                timer = new CountUpTimer(1000, savedInstanceState.getLong(SAVED_TIME)) {
                    @Override
                    public void onTick(long millisElapsed) {
                        int second = (int) millisElapsed / 1000;
                        timerText.setText(String.valueOf(second));
                        seconds = second;
                    }
                };
            }
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
        if (mode == MODE_TIMED){
            outState.putLong(SAVED_TIME, timer.getMillisAtStart());
        }
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
        if (isLandscapeLocked){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

}
