package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import static com.gmail.mountainapp.scrane.mountainclimbers.SnowView.FPS;

public class TutorialFinger {
    private Context context;
    private Drawable drawable;
    private int animationType;
    private Point pos;
    private Point pos2;
    private int percentageSwiped;
    private CountUpTimer timer;
    private Paint paint;

    private static final int INVISIBLE=0;
    private static final int TAPPING=1;
    private static final int SWIPING=2;

    public TutorialFinger(Context context) {
        this.context = context;
        drawable = ContextCompat.getDrawable(context, R.drawable.finger_colour);
        this.animationType = INVISIBLE;
        this.paint = new Paint();
        paint.setColor(Color.BLACK);
        timer = new CountUpTimer(1000 / FPS, new CountUpTimer.Ticker() {
            public void onTick(long millisElapsed) {
                tick();
            }
        });
        timer.start();
    }

    public void disappear(){
        this.pos = null;
        this.pos2 = null;
        this.animationType = INVISIBLE;
        timer.cancel();
    }

    public void tapOnPoint(Point p){
        this.pos = p;
        this.animationType = TAPPING;
        this.percentageSwiped = 0;
        timer.start();
    }

    public void swipeBetweenPoints(Point start, Point end){
        this.pos = start;
        this.pos2 = end;
        this.animationType = SWIPING;
        this.percentageSwiped = 0;
        timer.start();
    }

    private void tick(){
        switch(animationType){
            case INVISIBLE:
                break;
            case TAPPING:
                percentageSwiped += 2;
                if (percentageSwiped > 30){
                    percentageSwiped = 0;
                }
                break;
            case SWIPING:
                percentageSwiped += 5;
                if (percentageSwiped > 150){
                    percentageSwiped = 0;
                }
                break;
            default:
                break;
        }
    }

    public void draw(Canvas canvas){
        int p;
        switch(animationType) {
            case INVISIBLE:
                return;
            case TAPPING:
                p = Math.min(percentageSwiped, 60 - 2 * percentageSwiped);
                drawable.setBounds(pos.x - 20 + p, pos.y - 25 - p, pos.x + 130 + p, pos.y + 225 - p);
                drawable.draw(canvas);
                break;
            case SWIPING:
                p = percentageSwiped - 30;
                if (p < 0){
                    p = 0;
                }
                if (p > 100){
                    p = 100;
                }
                int cx = (pos.x * (100 - p) + pos2.x * p) / 100;
                int cy = (pos.y * (100 - p) + pos2.y * p) / 100;
                drawable.setBounds(cx - 20, cy - 25, cx + 130, cy + 225);
                drawable.draw(canvas);
                break;
            default:
                break;
        }
    }
}
