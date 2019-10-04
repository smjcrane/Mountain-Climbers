package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

        String packName = Levels.packs[position].getName();

        TextView packNameText = v.findViewById(R.id.listItemPackText);

        ImageView completedImage = v.findViewById(R.id.listItemCompletedImage);

        boolean completed = db.isCompleted(db.getId(position, Levels.packs[position].getLength() - 1));

        if (completed && mode == Common.MODE_DEFAULT){
            completedImage.setVisibility(View.VISIBLE);
        } else {
            completedImage.setVisibility(View.INVISIBLE);
        }

        packNameText.setText(packName);

        return v;
    }
}
