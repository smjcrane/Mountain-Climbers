package com.example.mountainclimbers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class LevelSelectActivity extends AppCompatActivity {

    public static final Integer[] levelIDs = new Integer[] {
            R.raw.lvl_00, R.raw.lvl_01, R.raw.lvl_02, R.raw.lvl_03, R.raw.lvl_04,
            R.raw.lvl_05, R.raw.lvl_06, R.raw.lvl_07, R.raw.lvl_08, R.raw.lvl_09,
            R.raw.lvl_10, R.raw.lvl_11, R.raw.lvl_12, R.raw.lvl_13, R.raw.lvl_14,
            R.raw.lvl_15, R.raw.lvl_16, R.raw.lvl_17, R.raw.lvl_18, R.raw.lvl_19,
            R.raw.lvl_20};
    public static final String LEVELID = "levelID";
    public static final String LEVEL_POS = "levelpos";

    private DataBaseHandler db;
    private ListView listView;
    private LevelListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        listView = findViewById(R.id.levelList);
        adapter = new LevelListAdapter(this, R.layout.list_item_level_select, levelIDs);
        listView.setAdapter(adapter);

        db = new DataBaseHandler(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!db.isLocked(levelIDs[position])){
                    Intent playLevel = new Intent();
                    playLevel.setClass(LevelSelectActivity.this, SeeMountainActivity.class);
                    playLevel.putExtra(LEVELID, levelIDs[position]);
                    playLevel.putExtra(LEVEL_POS, position);
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
    protected void onResume(){
        super.onResume();
        adapter.notifyDataSetInvalidated();
    }

    @Override
    protected void onDestroy(){
        if (db != null){
            db.close();
        }
        super.onDestroy();
    }
}
