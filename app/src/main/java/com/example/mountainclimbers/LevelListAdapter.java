package com.example.mountainclimbers;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LevelListAdapter extends ArrayAdapter<Integer> {

    private Context context;
    private DataBaseHandler db;
    private Integer[] levelIDs;

    public LevelListAdapter(Context context, int layoutID, Integer[] levelIDs){
        super(context, layoutID, levelIDs);
        this.context = context;
        this.db = new DataBaseHandler(context);
        this.levelIDs = levelIDs;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_level_select, null);
        }

        int levelID = levelIDs[position];

        Resources resources = v.getResources();

        TextView nameText = v.findViewById(R.id.listItemLevelText);

        nameText.setText("Level " + Integer.toString(position + 1));

        ImageView completedImage = v.findViewById(R.id.listItemCompletedImage);

        boolean completed = db.isCompleted(levelID);
        boolean locked = db.isLocked(levelID);

        if (completed){
            completedImage.setImageDrawable(resources.getDrawable(R.drawable.tick));
        } else if (locked) {
            completedImage.setImageDrawable(resources.getDrawable(R.drawable.padlock));
        } else {
            completedImage.setImageDrawable(null);
        }

        return v;
    }
}
