package com.example.mountainclimbers;

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

    public static int[] levelIDs = new int[] {R.raw.tutorial0, R.raw.tutorial1, R.raw.tutorial2, R.raw.tutorial3};

    private TutorialMountainView mountainView;
    private TextView goButton;
    private Button buttonBack, buttonNextLevel, buttonReset;
    private int levelID;
    private int levelPos;

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

        mountainView.setOnVictoryListener(new MountainView.OnVictoryListener() {
            @Override
            public void onVictory() {
                buttonBack.setVisibility(View.VISIBLE);
                buttonReset.setVisibility(View.VISIBLE);

                if (levelPos < levelIDs.length - 1){
                    buttonNextLevel.setVisibility(View.VISIBLE);
                }

                goButton.setVisibility(View.INVISIBLE);            }
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
            mountainView.setMountain(mountain);

            for (int i = 0; i < climberStrings.length; i++) {
                MountainClimber climber = new MountainClimber();
                climber.setPosition(Integer.parseInt(climberStrings[i]));
                mountainView.addClimber(climber, SeeMountainActivity.colorIDs[i]);
            }

            List<Instruction> instructionList = new ArrayList<>();
            while (br.ready()){
                String s = br.readLine();
                String type = s.substring(0, 1);
                if (type.equals("N")){
                    instructionList.add(new Instruction(
                            s.substring(2), Instruction.ANYWHERE, null, mountainView
                    ));
                    //set text and wait for any tap
                } else if (type.equals("G")){
                    instructionList.add(new Instruction(
                            s.substring(2), Instruction.GO_BUTTON, null, mountainView
                    ));
                    //set text and wait for go button
                } else {
                    int startOfText = s.indexOf(" ");
                    instructionList.add(new Instruction(s.substring(startOfText + 1),
                            Integer.parseInt(s.substring(1, startOfText)),
                            type.equals("R") ? MountainClimber.Direction.RIGHT : MountainClimber.Direction.LEFT,
                            mountainView
                    ));
                    //set text and wait to set climber direction
                }
            }
            mountainView.setInstructionList(instructionList);

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
