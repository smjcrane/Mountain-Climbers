package com.example.mountainclimbers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button tutorialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.mainStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
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
