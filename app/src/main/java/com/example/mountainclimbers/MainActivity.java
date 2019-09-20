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
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                startActivity(selectPack);

                Intent selectLevel = new Intent();
                selectLevel.setClass(MainActivity.this, LevelSelectActivity.class);
                Intent playGame = new Intent();
                playGame.setClass(MainActivity.this, SeeMountainActivity.class);

                int packPos = 0;
                int levelPos = 0;
                Integer[] levelIDs = Levels.packs[packPos].getLevelIDs();
                int levelID = levelIDs[levelPos];

                DataBaseHandler db = new DataBaseHandler(MainActivity.this);
                while (db.isCompleted(levelID) && packPos < Levels.packs.length - 1){
                    if (levelPos == levelIDs.length - 1){
                        packPos ++;
                        levelIDs = Levels.packs[packPos].getLevelIDs();
                        levelPos = 0;
                    } else {
                        levelPos ++;
                    }
                    levelID = levelIDs[levelPos];
                }

                selectLevel.putExtra(Levels.PACK_POS, packPos);
                startActivity(selectLevel);

                playGame.putExtra(Levels.PACK_POS, packPos);
                playGame.putExtra(Levels.LEVEL_POS, levelPos);
                startActivity(playGame);
            }
        });

        levelSelectButton = findViewById(R.id.mainLevelSelectButton);
        levelSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                startActivity(selectPack);
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
