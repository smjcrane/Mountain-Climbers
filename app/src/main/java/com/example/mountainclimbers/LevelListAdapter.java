package com.example.mountainclimbers;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
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
    private int mode;
    private View[] views;

    public LevelListAdapter(Context context, int layoutID, Integer[] levelIDs, int mode){
        super(context, layoutID, levelIDs);
        this.context = context;
        this.db = new DataBaseHandler(context);
        this.levelIDs = levelIDs;
        this.mode = mode;
        this.views = new View[levelIDs.length];
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        v = views[position];

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

        TextView timeText = v.findViewById(R.id.listItemLevelTime);

        boolean completed = db.isCompleted(levelID);
        Log.d("LIST", position + " " + levelID + " " + db.isLocked(levelID));

        if (db.isLocked(levelID)) {
            completedImage.setImageDrawable(context.getDrawable(R.drawable.padlock));
            timeText.setVisibility(View.INVISIBLE);
        } else if (mode == MainActivity.MODE_TIMED) {
            completedImage.setVisibility(View.INVISIBLE);
            timeText.setVisibility(View.VISIBLE);
            int time = db.getBestTimeSeconds(levelID);
            timeText.setText(formatTimeSeconds(time));
        } else if (completed){
            completedImage.setImageDrawable(context.getDrawable(R.drawable.tick));
        } else {
            completedImage.setImageDrawable(null);
        }

        return v;
    }

    private String formatTimeSeconds(int seconds){
        if (seconds < 0){
            return "-";
        }
        String s = Integer.toString(seconds / 60);
        int r = seconds % 60;
        return s + ":" + (r < 10 ? "0" : "") + Integer.toString(r);
    }
}
