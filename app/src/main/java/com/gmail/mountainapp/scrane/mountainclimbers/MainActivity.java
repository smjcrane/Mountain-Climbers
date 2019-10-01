package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private Button playButton, levelSelectButton, timedButton, puzzleButton;
    private ImageView settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.mainPlayButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.MODE = Common.MODE_DEFAULT;

                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);

                Intent selectLevel = new Intent();
                selectLevel.setClass(MainActivity.this, LevelSelectActivity.class);
                Intent playGame = new Intent();
                playGame.setClass(MainActivity.this, PlayGameActivity.class);

                int length = Levels.packs[Common.PACK_POS].getLength();

                DataBaseHandler db = new DataBaseHandler(MainActivity.this);
                while (db.isCompleted(db.getId(Common.PACK_POS, Common.LEVEL_POS)) && Common.PACK_POS < Levels.packs.length - 1){
                    if (Common.LEVEL_POS == length - 1){
                        Common.PACK_POS ++;length = Levels.packs[Common.PACK_POS].getLength();
                        Common.LEVEL_POS = 0;
                    } else {
                        Common.LEVEL_POS ++;
                    }
                }

                startActivity(selectPack);
                startActivity(selectLevel);
                startActivity(playGame);
            }
        });

        levelSelectButton = findViewById(R.id.mainLevelSelectButton);
        levelSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                Common.MODE = Common.MODE_DEFAULT;
                startActivity(selectPack);
            }
        });

        timedButton = findViewById(R.id.mainTimedModeButton);
        timedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                Common.MODE = Common.MODE_TIMED;
                startActivity(selectPack);
            }
        });

        puzzleButton = findViewById(R.id.mainPuzzleModeButton);
        puzzleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                Common.MODE = Common.MODE_PUZZLE;
                startActivity(selectPack);
            }
        });

        settingsButton = findViewById(R.id.mainSettingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent();
                settings.setClass(MainActivity.this, SettingsActivity.class);
                startActivity(settings);
            }
        });
    }
}
