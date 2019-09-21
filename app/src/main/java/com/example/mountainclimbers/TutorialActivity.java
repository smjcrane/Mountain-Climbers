package com.example.mountainclimbers;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {

    public static int[] levelIDs = new int[] {R.raw.tutorial_0, R.raw.tutorial_1, R.raw.tutorial_2, R.raw.tutorial_3, R.raw.tutorial_4};

    private TutorialMountainView mountainView;
    private TextView goButton;
    private Button buttonBack, buttonNextLevel, buttonReset;
    private int levelID;
    private int levelPos;

    private TutorialGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        levelPos = 0;
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
                loadLevel();
            }
        });

        buttonNextLevel = findViewById(R.id.mountainNextLevelButton);
        buttonNextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelPos++;
                levelID = levelIDs[levelPos];
                loadLevel();
            }
        });

        loadLevel();

    }

    private void loadLevel(){
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
                climber.setPosition(Integer.parseInt(climberStrings[i]));
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
                    instructionList.add(new Instruction(s.substring(startOfText + 1),
                            Integer.parseInt(s.substring(1, startOfText)),
                            type.equals("R") ? MountainClimber.Direction.RIGHT : MountainClimber.Direction.LEFT
                    ));
                    //set text and wait to set climber direction
                }
            }

            game = new TutorialGame(mountain, instructionList);

            game.setOnVictoryListener(new Game.OnVictoryListener() {
                @Override
                public void onVictory() {
                    buttonBack.setVisibility(View.VISIBLE);
                    buttonReset.setVisibility(View.VISIBLE);

                    if (levelPos < levelIDs.length - 1){
                        buttonNextLevel.setVisibility(View.VISIBLE);
                    }

                    goButton.setVisibility(View.INVISIBLE);            }
            });

            mountainView.setGame(game);

            for (int i = 0; i < climbers.size(); i++){
                mountainView.addClimber(climbers.get(i), SeeMountainActivity.colorIDs[i]);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
