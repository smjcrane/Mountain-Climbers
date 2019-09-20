package com.example.mountainclimbers;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PackListAdapter extends ArrayAdapter<Integer> {

    private Context context;

    public PackListAdapter(Context context, int layoutID) {
        super(context, layoutID, new Integer[Levels.packs.length]);
        this.context = context;
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

        packNameText.setText(packName);

        return v;
    }
}
