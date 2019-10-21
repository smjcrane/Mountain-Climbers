package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;

class Flying {
    private Drawable drawable;
    private Point pos;
    private int yVelocity;
    private int turns;

    public Flying(Context context, Drawable drawable, Point initialPos){
        this.drawable = drawable;
        //this.drawable.setColorFilter(new PorterDuffColorFilter(context.getColor(R.color.streak0), PorterDuff.Mode.SRC_ATOP));
        pos = initialPos;
        yVelocity = 0;
        turns = 0;
    }

    public void move(){
        this.pos.x += 7;
        if ((turns % 20) >= 10 ){
            this.pos.y ++;
        } else {
            this.pos.y --;
        }
        turns++;
    }

    public int getY(){
        return pos.y;
    }

    public int getX(){
        return pos.x;
    }

    public void draw(Canvas canvas){
        drawable.setBounds(pos.x, pos.y - 100, pos.x + 100, pos.y);
        drawable.draw(canvas);
    }
}
