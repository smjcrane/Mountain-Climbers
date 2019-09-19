package com.example.mountainclimbers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button playButton, levelSelectButton, tutorialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.mainPlayButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectLevel = new Intent();
                selectLevel.setClass(MainActivity.this, LevelSelectActivity.class);
                startActivity(selectLevel);
                Intent playGame = new Intent();
                int levelPos = 0;
                int levelID = LevelSelectActivity.levelIDs[levelPos];
                DataBaseHandler db = new DataBaseHandler(MainActivity.this);
                while (db.isCompleted(levelID) && levelPos < LevelSelectActivity.levelIDs.length - 1){
                    levelPos ++;
                    levelID = LevelSelectActivity.levelIDs[levelPos];
                }
                playGame.putExtra(LevelSelectActivity.LEVEL_POS, levelPos);
                playGame.putExtra(LevelSelectActivity.LEVELID, levelID);
                playGame.setClass(MainActivity.this, SeeMountainActivity.class);
                startActivity(playGame);
            }
        });

        levelSelectButton = findViewById(R.id.mainLevelSelectButton);
        levelSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectLevel = new Intent();
                selectLevel.setClass(MainActivity.this, LevelSelectActivity.class);
                startActivity(selectLevel);
            }
        });

        tutorialButton = findViewById(R.id.mainTutorialButton);
        tutorialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorial = new Intent();
                tutorial.setClass(MainActivity.this, TutorialActivity.class);
                startActivity(tutorial);
            }
        });
    }
}
