package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import static com.gmail.mountainapp.scrane.mountainclimbers.Common.MODE_DEFAULT;
import static com.gmail.mountainapp.scrane.mountainclimbers.Common.MODE_PUZZLE;
import static com.gmail.mountainapp.scrane.mountainclimbers.Common.MODE_TIMED;

public class LevelSelectActivity extends AppCompatActivity {

    private DataBaseHandler db;
    private ListView listView;
    private LevelListAdapter adapter;
    private int mode, packpos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        mode = preferences.getInt(getString(R.string.MODE), MODE_DEFAULT);
        packpos = preferences.getInt(getString(R.string.PACKPOS), 0);
        final SharedPreferences.Editor editor = preferences.edit();

        listView = findViewById(R.id.levelList);
        adapter = new LevelListAdapter(this);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        TextView titleText = findViewById(R.id.listHeader);
        titleText.setText(Levels.packs[packpos].getName(this).toUpperCase());

        View footer = new ImageView(this);
        footer.setMinimumHeight(500);
        listView.addFooterView(footer);

        db = new DataBaseHandler(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Levels.Pack pack = Levels.packs[packpos];
                if (position < pack.getLength()){
                    int levelPos = position;
                    Log.d("LVL", "you clicked on " + levelPos);
                    if (true || !db.isLocked(db.getId(packpos, levelPos))) {
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
                        editor.putInt(getString(R.string.LEVELPOS), levelPos);
                        editor.apply();
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
        SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        int pos = preferences.getInt(getString(R.string.LEVELPOS), 0);
        Log.d("LEVELS", "Scrolling to " + pos);
        listView.setSelection(pos);
    }

}
