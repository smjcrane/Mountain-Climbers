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

public class TutorialListAdapter extends ArrayAdapter<Integer> {

    private Context context;
    private Drawable completedDrawable;

    public TutorialListAdapter(Context context){
        super(context, R.layout.list_item_level_select);
        this.context = context;
        completedDrawable = context.getDrawable(R.drawable.tick);
    }

    @Override
    public int getCount(){
        return Levels.Tutorial.getLength();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_level_select, null);
        }

        TextView nameText = v.findViewById(R.id.listItemLevelText);
        String displayName = context.getString(Levels.TutorialNames[position]);
        nameText.setText(displayName);

        return v;
    }
}
