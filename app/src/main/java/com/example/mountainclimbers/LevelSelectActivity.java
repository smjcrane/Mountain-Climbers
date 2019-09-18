package com.example.mountainclimbers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

public class LevelSelectActivity extends AppCompatActivity {

    public static final Integer[] levelIDs = new Integer[] {
            R.raw.lvl00, R.raw.lvl01, R.raw.lvl02,
            R.raw.lvl20};
    public static final String LEVELID = "levelID";
    public static final String LEVEL_POS = "levelpos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        ListView listView = findViewById(R.id.levelList);
        ListAdapter adapter = new LevelListAdapter(this, R.layout.list_item_level_select, levelIDs);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent playLevel = new Intent();
                playLevel.setClass(LevelSelectActivity.this, SeeMountainActivity.class);
                playLevel.putExtra(LEVELID, levelIDs[position]);
                playLevel.putExtra(LEVEL_POS, position);
                startActivity(playLevel);
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
