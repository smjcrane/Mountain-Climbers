package com.example.mountainclimbers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.logging.Level;

public class LevelListAdapter extends ArrayAdapter<Integer> {

    private Context context;
    private DataBaseHandler db;
    private Levels.Pack pack;
    private Drawable completedDrawable;
    private Drawable lockedDrawable;

    public LevelListAdapter(Context context, int layoutID){
        super(context, layoutID, new Integer[Levels.packs[Common.PACK_POS].getLength() + Levels.packs[Common.PACK_POS].getNumTutorials()]);
        this.pack = Levels.packs[Common.PACK_POS];
        this.context = context;
        this.db = new DataBaseHandler(context);
        completedDrawable = context.getDrawable(R.drawable.tick);
        lockedDrawable = context.getDrawable(R.drawable.padlock);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_level_select, null);
        }

        TextView nameText = v.findViewById(R.id.listItemLevelText);
        ImageView completedImage = v.findViewById(R.id.listItemCompletedImage);
        TextView timeText = v.findViewById(R.id.listItemLevelTime);

        String displayName;
        if (position < pack.getNumTutorials()){
            displayName = "Tutorial " + (position + 1);
            completedImage.setImageDrawable(null);
            timeText.setText("");
        } else {
            displayName = "Level " + (position - pack.getNumTutorials() + 1);
            int levelID = db.getId(Common.PACK_POS, position - pack.getNumTutorials());
            switch (Common.MODE){
                case Common.MODE_DEFAULT:
                    timeText.setText("");
                    if (db.isCompleted(levelID)){
                        completedImage.setImageDrawable(completedDrawable);
                    } else if (db.isLocked(levelID)) {
                        completedImage.setImageDrawable(lockedDrawable);
                    } else {
                        completedImage.setImageDrawable(null);
                    }
                    break;
                case Common.MODE_TIMED:
                    if (db.isLocked(levelID)){
                        completedImage.setImageDrawable(lockedDrawable);
                        timeText.setText("");
                    } else {
                        completedImage.setImageDrawable(null);
                        timeText.setText(formatTimeSeconds(db.getBestTimeSeconds(levelID)));
                    }
            }
        }

        nameText.setText(displayName);
        return v;
    }

    private String formatTimeSeconds(int seconds){
        if (seconds < 0){
            return "-";
        }
        String s = Integer.toString(seconds / 60);
        int r = seconds % 60;
        return s + ":" + (r < 10 ? "0" : "") + r;
    }
}
