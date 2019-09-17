package com.example.mountainclimbers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LevelListAdapter extends ArrayAdapter<Integer> {

    private Context context;

    public LevelListAdapter(Context context, int layoutID, Integer[] levelIDs){
        super(context, layoutID, levelIDs);
        this.context = context;

    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_level_select, null);
        }

        TextView nameText = (TextView) v.findViewById(R.id.listItemText);

        nameText.setText("Level " + Integer.toString(position));

        return v;
    }
}
