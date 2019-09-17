package com.example.mountainclimbers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SeeMountain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_mountain);

        Mountain mountain = new Mountain(new int[] {0, 100, 20, 70, 40, 80, 0});
        final MountainView mountainView = findViewById(R.id.mountainView);
        mountainView.setMountain(mountain);
        MountainClimber greenClimber = new MountainClimber();
        greenClimber.setPosition(0);
        mountainView.addClimber(greenClimber, R.color.climberGreen);

        MountainClimber purpleClimber = new MountainClimber();
        purpleClimber.setPosition(mountain.getWidth());
        mountainView.addClimber(purpleClimber, R.color.climberPurple);

        Button goButton = findViewById(R.id.mountainGoButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mountainView.go();
            }
        });
    }
}
