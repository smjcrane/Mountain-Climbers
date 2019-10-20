package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.logging.Level;

public class PackListAdapter extends ArrayAdapter<Integer> {

    private Context context;
    private DataBaseHandler db;
    private int mode;

    public PackListAdapter(Context context, int layoutID, int mode) {
        super(context, layoutID, new Integer[Levels.packs.length]);
        this.context = context;
        this.db = new DataBaseHandler(context);
        this.mode = mode;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_pack_select, null);
        }

        Levels.Pack pack = Levels.packs[position];

        String packName = pack.getName(context);

        TextView packNameText = v.findViewById(R.id.listItemPackText);

        ImageView completedImage = v.findViewById(R.id.listItemCompletedImage);

        TextView progressText = v.findViewById(R.id.packProgressText);

        switch (mode){
            case Common.MODE_DEFAULT:
                boolean completed = db.isCompleted(db.getId(position, pack.getLength() - 1));
                if (completed){
                    completedImage.setVisibility(View.VISIBLE);
                    progressText.setVisibility(View.INVISIBLE);
                } else {
                    completedImage.setVisibility(View.INVISIBLE);
                    progressText.setVisibility(View.VISIBLE);
                    progressText.setText(db.howManyCompletedInPack(position) + "/" + pack.getLength());
                }
                break;
            case Common.MODE_PUZZLE:
                completedImage.setVisibility(View.INVISIBLE);
                progressText.setText(db.countStars(position) + "/" + (3 * Levels.packs[position].getLength()));
                break;
            case Common.MODE_TIMED:
                completedImage.setVisibility(View.INVISIBLE);
                int numCompletedTimed = db.howManyCompletedTimedInPack(position);
                if (numCompletedTimed < pack.getLength()){
                    progressText.setVisibility(View.VISIBLE);
                    progressText.setText(numCompletedTimed + "/" + pack.getLength());
                    progressText.setTextColor(context.getColor(R.color.darkTextBlue));
                } else {
                    progressText.setText(LevelListAdapter.formatTimeSeconds(db.getTotalTimeInPack(position)));
                    progressText.setTextColor(Color.BLACK);
                }
                break;
        }




        packNameText.setText(packName);

        return v;
    }
}
