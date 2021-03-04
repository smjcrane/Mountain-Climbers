package com.gmail.mountainapp.scrane.mountainclimbers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TutorialSelectActivity extends AppCompatActivity {

    private ListView listView;
    private TutorialListAdapter adapter;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        editor = preferences.edit();

        listView = findViewById(R.id.levelList);
        adapter = new TutorialListAdapter(this);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        TextView titleText = findViewById(R.id.listHeader);
        titleText.setText(getString(R.string.tutorial).toUpperCase());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= Levels.Tutorial.getLength()) {
                    return;
                }
                editor.putInt(getString(R.string.tutorial), position);
                editor.apply();
                Intent doTut = new Intent();
                doTut.setClass(TutorialSelectActivity.this, TutorialActivity.class);
                startActivity(doTut);
            }
        });

        Button backButton = findViewById(R.id.levelSelectBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}