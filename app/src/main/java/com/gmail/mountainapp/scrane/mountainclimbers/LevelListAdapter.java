package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class LevelListAdapter extends ArrayAdapter<Integer> {

    private Context context;
    private DataBaseHandler db;
    private Levels.Pack pack;
    private Drawable completedDrawable;
    private Drawable lockedDrawable;
    private SharedPreferences preferences;
    private int packPos, mode;
    private boolean[] askedForOptimalMoves;

    public LevelListAdapter(Context context){
        super(context, R.layout.list_item_level_select);
        preferences = context.getSharedPreferences(context.getString(R.string.PREFERENCES), Context.MODE_PRIVATE);
        packPos = preferences.getInt(context.getString(R.string.PACKPOS), 0);
        this.pack = Levels.packs[packPos];
        this.context = context;
        this.db = new DataBaseHandler(context);
        completedDrawable = context.getDrawable(R.drawable.tick);
        lockedDrawable = context.getDrawable(R.drawable.padlock);
        this.mode = preferences.getInt(context.getString(R.string.MODE), Common.MODE_DEFAULT);
        if (mode == Common.MODE_PUZZLE){
            askedForOptimalMoves = new boolean[getCount()];
        }
    }

    @Override
    public int getCount(){
        return pack.getLength();
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
        RelativeLayout starLayout = v.findViewById(R.id.levelStars);
        ImageView[] starFills = new ImageView[] {
                v.findViewById(R.id.starFill1), v.findViewById(R.id.starFill2), v.findViewById(R.id.starFill3)};

        String displayName;
            displayName = context.getString(R.string.level) + " " + (position + 1);
            int levelPos = position;
            final int levelID = db.getId(packPos, levelPos);
            switch (mode){
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
                    break;
                case Common.MODE_PUZZLE:
                    if (db.isLocked(levelID)){
                        completedImage.setImageDrawable(lockedDrawable);
                        starLayout.setVisibility(View.INVISIBLE);
                    } else {
                        completedImage.setImageDrawable(null);
                        starLayout.setVisibility(View.VISIBLE);
                        int bestMoves = -1;
                        int stars = 0;
                        if (db.knowsOptimalMoves(levelID)){
                            askedForOptimalMoves[position] = true;
                            bestMoves = db.getBestMoves(levelID);
                            try {
                                stars = howManyStars(bestMoves, db.getOptimalMoves(levelID, context).get());
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            stars = 0;
                            if (!askedForOptimalMoves[position]){
                                askedForOptimalMoves[position] = true;
                                new Thread(new Runnable() {
                                    public void run() {
                                        new DataBaseHandler(context).getOptimalMoves(levelID, context);
                                        Log.d("LIST", "Got optimal moves");
                                        //notifyDataSetChanged();
                                    }
                                }).start();
                            }
                        }
                        for (int i = 0; i < 3; i++){
                            if (i < stars){
                                starFills[i].setVisibility(View.VISIBLE);
                            } else {
                                starFills[i].setVisibility(View.INVISIBLE);
                            }
                        }
                    }
            }

        nameText.setText(displayName);
        return v;
    }

    public static String formatTimeSeconds(int seconds){
        if (seconds < 0){
            return "-";
        }
        String s = Integer.toString(seconds / 60);
        int r = seconds % 60;
        return s + ":" + (r < 10 ? "0" : "") + r;
    }

    public static int howManyStars(int moves, int bestPossibleMoves){
        if (moves <= 0){
            return 0;
        } else {
            if (moves == bestPossibleMoves){
                return 3;
            } else if (moves <= Math.max(bestPossibleMoves + 2, (int) bestPossibleMoves * 1.3)) {
                return 2;
            }
        }
        return 1;
    }
}
