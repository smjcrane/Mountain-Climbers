package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PlayGameActivity extends DriveActivity implements Game.OnVictoryListener{

    static final String SAVED_POSITIONS = "savedpositions";
    static final String SAVED_DIRECTIONS = "saveddirections";

    protected MountainView mountainView;
    protected SnowView snowView;
    protected Game game;
    protected Button buttonBack, buttonNextLevel;
    protected ImageView buttonReset, buttonHint, settingsButton;
    protected TextView goButton, levelNumberText, victoryText;
    protected DataBaseHandler db;
    protected boolean shouldUpdateAchievements;
    protected SharedPreferences preferences;
    protected SharedPreferences.Editor editor;
    protected int packPos;
    protected AchievementsClient client;

    protected String victoryMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_mountain);

        setup();
        loadLevel(savedInstanceState);
        }

    protected void setup() {
        victoryMessage = "YOU WIN!";
        victoryText = findViewById(R.id.victoryText);
        preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        editor = preferences.edit();
        editor.putBoolean(getString(R.string.TUTORIAL), false);
        editor.commit();
        packPos = preferences.getInt(getString(R.string.PACKPOS), 0);

        mountainView = findViewById(R.id.mountainView);
        snowView = findViewById(R.id.snowView);
        buttonHint = findViewById(R.id.mountainHintButton);
        levelNumberText = findViewById(R.id.mountainLevelNumber);
        goButton = findViewById(R.id.mountainGoButton);
        buttonBack = findViewById(R.id.mountainBackButton);
        buttonReset = findViewById(R.id.mountainResetButton);
        buttonNextLevel = findViewById(R.id.mountainNextLevelButton);
        db = new DataBaseHandler(this);

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
                editor.putInt(getString(R.string.LEVELPOS), preferences.getInt(getString(R.string.LEVELPOS), 0) + 1);
                editor.apply();
                loadLevel(null);
            }
        });

        buttonHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game.victory || game.moving != Game.Moving.NONE){
                    return;
                }
                mountainView.showHint();
            }
        });

        settingsButton = findViewById(R.id.mainSettingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent();
                settings.setClass(PlayGameActivity.this, SettingsActivity.class);
                startActivity(settings);
            }
        });
    }

    @Override
    public void onVictory() {
        Log.d("PLAY", "victory!");
        goButton.setVisibility(View.INVISIBLE);
        buttonHint.setVisibility(View.INVISIBLE);
        buttonBack.setVisibility(View.VISIBLE);
        mountainView.invalidate();
        victoryText.setText(victoryMessage);

        int levelPos = preferences.getInt(getString(R.string.LEVELPOS),0);
        db = new DataBaseHandler(PlayGameActivity.this);
        int levelDBID = db.getId(packPos, levelPos);
        db.markCompleted(levelDBID);
        if (signedIn){
            client.setSteps(getString(Common.packCompletedAchievementIDs[packPos]),
                    db.howManyCompletedInPack(packPos));
        }
        db.close();

        if (levelPos < Levels.packs[packPos].getLength() - 1){
            buttonNextLevel.setVisibility(View.VISIBLE);
        }
    }

    protected void loadLevel(final Bundle savedInstanceState){
        victoryText.setText("");
        final int[] positions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_POSITIONS);
        int[] directions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_DIRECTIONS);

        final int levelPos = preferences.getInt(getString(R.string.LEVELPOS),0);
        levelNumberText.setText(Integer.toString(levelPos + 1));
        int levelID = Levels.packs[packPos].getLevelIDs()[levelPos];

        buttonBack.setVisibility(View.INVISIBLE);
        buttonNextLevel.setVisibility(View.INVISIBLE);
        goButton.setVisibility(View.VISIBLE);
        buttonHint.setVisibility(View.VISIBLE);
        try {
            InputStream stream = getResources().openRawResource(levelID);

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String[] heightStrings = br.readLine().split(" ");
            final String[] climberString = br.readLine().split(" ");

            int[] heights = new int[heightStrings.length];
            for (int i = 0; i < heightStrings.length; i++) {
                heights[i] = Integer.parseInt(heightStrings[i]);
            }

            final Mountain mountain = new Mountain(heights);
            game = new Game(mountain);
            game.setOnVictoryListener(this);

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

            while(game.removeClimbers() != null){}
            game.updateVictory();
            if (!game.victory){
                game.setUpSolver();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSignIn(GoogleSignInAccount account){
        super.onSignIn(account);
        client = Games.getAchievementsClient(PlayGameActivity.this, account);
        if (shouldUpdateAchievements){
            gamesClient = Games.getGamesClient(this, account);
            gamesClient.setViewForPopups(findViewById(R.id.container_pop_up));
        } else {
            gamesClient = null;
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
        SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        boolean isLandscapeLocked = preferences.getBoolean(getString(R.string.LANDSCAPE_LOCKED), true);
        Game.speed = preferences.getInt(getString(R.string.SPEED), 1);
        if (isLandscapeLocked){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
        mountainView.updateClimberDrawable();
    }

    @Override
    public void onBackPressed() {
        if (game.victory){
            finish();
            return;
        }
        mountainView.cancelHint();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.are_you_sure_you_want_to_leave, null);
        Button yes = dialogView.findViewById(R.id.leaveYes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        Button no = dialogView.findViewById(R.id.leaveNo);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onResume();
            }
        });
        dialog.show();
    }
}
