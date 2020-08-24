package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class Bird extends Flying {
    private static final int[] frames = new int[] {R.drawable.bird_1, R.drawable.bird_2, R.drawable.bird_3, R.drawable.bird_4, R.drawable.bird_3, R.drawable.bird_2};

    public Bird(Context context, Point initialPos){
        super(context, initialPos);
    }

    public void draw(Canvas canvas){
        Drawable drawable = context.getDrawable(frames[turns % frames.length]);
        drawable.setBounds(pos.x - 50, pos.y - 100, pos.x + 50, pos.y);
        drawable.draw(canvas);
    }
}
