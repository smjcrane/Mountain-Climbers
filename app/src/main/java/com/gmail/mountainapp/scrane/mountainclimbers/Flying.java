package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;

abstract class Flying {
    protected Point pos;
    protected int turns;
    protected Context context;

    public Flying(Context context, Point initialPos){
        this.context = context;
        pos = initialPos;
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

    public abstract void draw(Canvas canvas);
}
