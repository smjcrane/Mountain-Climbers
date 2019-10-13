package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

public class PackSelectActivity extends AppCompatActivity {

    private ListView listView;
    private PackListAdapter adapter;
    private Button backButton;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        editor = preferences.edit();
        int mode = preferences.getInt(getString(R.string.MODE), Common.MODE_DEFAULT);

        backButton = findViewById(R.id.levelSelectBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView = findViewById(R.id.levelList);
        adapter = new PackListAdapter(this, R.layout.list_item_level_select, mode);
        listView.setAdapter(adapter);

        View footer = new ImageView(this);
        footer.setMinimumHeight(500);
        listView.addFooterView(footer);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < Levels.packs.length){
                    Intent selectLevel = new Intent();
                    selectLevel.setClass(PackSelectActivity.this, LevelSelectActivity.class);
                    editor.putInt(getString(R.string.PACKPOS), position);
                    editor.apply();
                    startActivity(selectLevel);
                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        editor.putInt(getString(R.string.LEVELPOS), 0);
        editor.putBoolean(getString(R.string.TUTORIAL), true);
        editor.apply();
        adapter.notifyDataSetInvalidated();
    }
}
