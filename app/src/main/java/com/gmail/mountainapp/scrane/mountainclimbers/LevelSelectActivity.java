package com.example.mountainclimbers;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import static com.example.mountainclimbers.Common.MODE_DEFAULT;
import static com.example.mountainclimbers.Common.MODE_PUZZLE;
import static com.example.mountainclimbers.Common.MODE_TIMED;


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

        TextView titleText = findViewById(R.id.listHeader);
        titleText.setText(Levels.packs[Common.PACK_POS].getName().toUpperCase());

        View footer = new ImageView(this);
        footer.setMinimumHeight(500);
        listView.addFooterView(footer);

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
                        switch (mode){
                            case MODE_DEFAULT:
                                playLevel.setClass(LevelSelectActivity.this, PlayGameActivity.class);
                                break;
                            case MODE_TIMED:
                                playLevel.setClass(LevelSelectActivity.this, PlayTimedModeActivity.class);
                                break;
                            case MODE_PUZZLE:
                                playLevel.setClass(LevelSelectActivity.this, PlayPuzzleModeActivity.class);
                                break;
                        }
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
