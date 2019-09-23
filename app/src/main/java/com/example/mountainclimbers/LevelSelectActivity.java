package com.example.mountainclimbers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


public class LevelSelectActivity extends AppCompatActivity {

    private DataBaseHandler db;
    private ListView listView;
    private LevelListAdapter adapter;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        mode = Common.MODE;

        listView = findViewById(R.id.levelList);
        adapter = new LevelListAdapter(this, R.layout.list_item_level_select);
        listView.setAdapter(adapter);

        db = new DataBaseHandler(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Levels.Pack pack = Levels.packs[Common.PACK_POS];
                if (position < pack.getNumTutorials()){
                    Intent tutorial = new Intent();
                    tutorial.setClass(LevelSelectActivity.this, TutorialActivity.class);
                    Common.TUTORIAL_POS = position;
                    startActivity(tutorial);
                } else {
                    int levelPos = position - pack.getNumTutorials();
                    Log.d("LVL", "you clicked on " + levelPos);
                    if (!db.isLocked(db.getId(Common.PACK_POS, levelPos))) {
                        Intent playLevel = new Intent();
                        playLevel.setClass(LevelSelectActivity.this, SeeMountainActivity.class);
                        Common.LEVEL_POS = levelPos;
                        startActivity(playLevel);
                    }
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
        int pos;
        if (Common.tutorial){
            pos = Common.TUTORIAL_POS;
        } else {
            pos = Common.LEVEL_POS + Levels.packs[Common.PACK_POS].getNumTutorials();
        }
        listView.setSelection(pos);
    }

}
