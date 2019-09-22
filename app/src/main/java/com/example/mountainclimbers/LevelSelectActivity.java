package com.example.mountainclimbers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import static com.example.mountainclimbers.Levels.LEVEL_POS;
import static com.example.mountainclimbers.Levels.PACK_POS;

public class LevelSelectActivity extends AppCompatActivity {

    private DataBaseHandler db;
    private ListView listView;
    private LevelListAdapter adapter;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        Intent caller = getIntent();
        final int packPos = caller.getIntExtra(Levels.PACK_POS, -1);
        final Integer[] levelIDs = Levels.packs[packPos].getLevelIDs();
        mode = caller.getIntExtra(MainActivity.MODE, MainActivity.MODE_DEFAULT);

        listView = findViewById(R.id.levelList);
        adapter = new LevelListAdapter(this, R.layout.list_item_level_select, levelIDs, mode);
        listView.setAdapter(adapter);

        db = new DataBaseHandler(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!db.isLocked(levelIDs[position])) {
                    Intent playLevel = new Intent();
                    playLevel.setClass(LevelSelectActivity.this, SeeMountainActivity.class);
                    playLevel.putExtra(PACK_POS, packPos);
                    SeeMountainActivity.levelPos = position;
                    playLevel.putExtra(MainActivity.MODE, mode);
                    startActivity(playLevel);
                }
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

    @Override
    protected void onDestroy() {
        if (db != null) {
            db.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        adapter.notifyDataSetInvalidated();
    }

}
