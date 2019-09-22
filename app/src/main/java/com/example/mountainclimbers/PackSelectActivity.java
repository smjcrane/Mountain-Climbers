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

public class PackSelectActivity extends AppCompatActivity {

    private ListView listView;
    private PackListAdapter adapter;
    private Button backButton;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        backButton = findViewById(R.id.levelSelectBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mode = Common.MODE;

        listView = findViewById(R.id.levelList);
        adapter = new PackListAdapter(this, R.layout.list_item_level_select);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent selectLevel = new Intent();
                selectLevel.setClass(PackSelectActivity.this, LevelSelectActivity.class);
                Common.PACK_POS = position;
                startActivity(selectLevel);
            }
        });
    }
}
