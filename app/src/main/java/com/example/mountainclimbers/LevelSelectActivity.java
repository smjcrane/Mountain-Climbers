package com.example.mountainclimbers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class LevelSelectActivity extends AppCompatActivity {

    public static final Integer[] levelIDs = new Integer[] {R.raw.lvl00, R.raw.lvl01};
    public static final String LEVELID = "levelID";

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
                playLevel.setClass(LevelSelectActivity.this, SeeMountain.class);
                playLevel.putExtra(LEVELID, levelIDs[position]);
                startActivity(playLevel);
            }
        });
    }
}
