package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.gmail.mountainapp.scrane.mountainclimbers.Common.DIRECTIONS;
import static com.gmail.mountainapp.scrane.mountainclimbers.PlayGameActivity.SAVED_DIRECTIONS;
import static com.gmail.mountainapp.scrane.mountainclimbers.PlayGameActivity.SAVED_POSITIONS;

public class TutorialActivity extends DriveActivity {

    static final String SAVED_INDEX = "savedindex";

    private Integer[] levelIDs;
    private TutorialMountainView mountainView;
    private TextView goButton;
    private Button buttonBack, buttonNextLevel, buttonReset;
    private static int levelID;

    private DataBaseHandler db;

    private TutorialGame game;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    AchievementsClient client;


    int levelPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        editor = preferences.edit();
        editor.putBoolean(getString(R.string.TUTORIAL), true);
        editor.apply();
        int packPos = preferences.getInt(getString(R.string.PACKPOS), 0);
        levelPos = preferences.getInt(getString(R.string.LEVELPOS), 0);

        db = new DataBaseHandler(this);

        levelIDs = Levels.packs[packPos].getTutorialLevelIDs();
        levelID = levelIDs[levelPos];

        mountainView = findViewById(R.id.tutorialMountainView);

        goButton = findViewById(R.id.tutorialGoButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TutorialActivity.this.mountainView.go();
            }
        });

        buttonBack = findViewById(R.id.mountainBackButton);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonReset = findViewById(R.id.mountainReplayButton);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLevel(null);
            }
        });

        buttonNextLevel = findViewById(R.id.mountainNextLevelButton);
        buttonNextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelPos++;
                editor.putInt(getString(R.string.LEVELPOS), levelPos);
                editor.apply();
                levelID = levelIDs[levelPos];
                loadLevel(null);
            }
        });

        loadLevel(savedInstanceState);
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
        outState.putInt(SAVED_INDEX, game.instructionIndex);
        super.onSaveInstanceState(outState);
    }


    private void loadLevel(Bundle savedInstanceState){
        int[] positions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_POSITIONS);
        int[] directions = savedInstanceState == null ? null : savedInstanceState.getIntArray(SAVED_DIRECTIONS);

        buttonBack.setVisibility(View.INVISIBLE);
        buttonReset.setVisibility(View.INVISIBLE);
        buttonNextLevel.setVisibility(View.INVISIBLE);
        goButton.setVisibility(View.VISIBLE);


        try {
            InputStream stream = getResources().openRawResource(levelID);

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String[] heightStrings = br.readLine().split(" ");
            String[] climberStrings = br.readLine().split(" ");

            int[] heights = new int[heightStrings.length];
            for (int i = 0; i < heightStrings.length; i++) {
                heights[i] = Integer.parseInt(heightStrings[i]);
            }

            Mountain mountain = new Mountain(heights);

            List<MountainClimber> climbers = new ArrayList<>();
            for (int i = 0; i < climberStrings.length; i++) {
                MountainClimber climber = new MountainClimber();
                if (positions == null){
                    climber.setPosition(Integer.parseInt(climberStrings[i]));
                } else {
                    climber.setPosition(positions[i]);
                    climber.setDirection(DIRECTIONS[directions[i]]);
                }
                climbers.add(climber);
            }

            List<Instruction> instructionList = new ArrayList<>();
            while (br.ready()){
                String s = br.readLine();
                String type = s.substring(0, 1);
                if (type.equals("N")){
                    instructionList.add(new Instruction(
                            s.substring(2), Instruction.ANYWHERE, null
                    ));
                    //set text and wait for any tap
                } else if (type.equals("G")){
                    instructionList.add(new Instruction(
                            s.substring(2), Instruction.GO_BUTTON, null
                    ));
                    //set text and wait for go button
                } else {
                    int startOfText = s.indexOf(" ");
                    String text = s.substring(startOfText + 1);
                    boolean isHint = s.substring(startOfText - 1, startOfText).equals("H");
                    Log.d("TUT", "The instruction is " + (isHint ? "" : "not ") + "a hint");
                    MountainClimber.Direction d = type.equals("R") ? MountainClimber.Direction.RIGHT : MountainClimber.Direction.LEFT;
                    int objectID;
                    if (isHint) {
                        objectID = Integer.parseInt(s.substring(1, startOfText - 1));
                    } else {
                        objectID = Integer.parseInt(s.substring(1, startOfText));
                    }
                    instructionList.add(new Instruction(text, objectID, d, isHint));
                    //set text and wait to set climber direction
                }
            }

            game = new TutorialGame(mountain, instructionList);
            game.instructionIndex = savedInstanceState == null ? 0 : savedInstanceState.getInt(SAVED_INDEX);
            game.updateVictory();

            game.setOnVictoryListener(new Game.OnVictoryListener() {
                @Override
                public void onVictory() {
                    buttonBack.setVisibility(View.VISIBLE);
                    buttonReset.setVisibility(View.VISIBLE);
                    int packPos = preferences.getInt(getString(R.string.PACKPOS), 0);
                    int levelPos = preferences.getInt(getString(R.string.LEVELPOS), 0);
                    db.markCompletedTutorial(db.getId(packPos, levelPos));
                    if (signedIn){
                        client.setSteps(getString(R.string.achievement_learning_the_ropes), db.howManyTutorialCompletedInPack(packPos));
                    }
                    if (levelPos < levelIDs.length - 1){
                        buttonNextLevel.setVisibility(View.VISIBLE);
                    }

                    goButton.setVisibility(View.INVISIBLE);            }
            });

            mountainView.setGame(game);
            if (game.victory){
                game.callOnVictoryListener();
            }

            for (int i = 0; i < climbers.size(); i++){
                mountainView.addClimber(climbers.get(i));
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        boolean isLandscapeLocked = preferences.getBoolean(getString(R.string.LANDSCAPE_LOCKED), true);
        if (isLandscapeLocked){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    @Override
    public void onBackPressed() {
        if (game.victory){
            finish();
            return;
        }
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
            }
        });
        dialog.show();
    }

    @Override
    protected void onSignIn(GoogleSignInAccount account){
        if (signedIn){
            Log.d("TUT", "Setting pop up view");
            gamesClient = Games.getGamesClient(this, account);
            gamesClient.setViewForPopups(findViewById(R.id.container_pop_up));
            client = Games.getAchievementsClient(TutorialActivity.this, account);
        }
        else {
            gamesClient = null;
            client = null;
        }
    }

}
