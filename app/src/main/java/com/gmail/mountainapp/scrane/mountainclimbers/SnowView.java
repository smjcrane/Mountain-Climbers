package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnowView extends View {

    public static int FPS = 24;
    static int NUM_SNOWFLAKES = 100;
    static int RADIUS = 7;
    static int GRAVITY = 5;
    static int WIND = 1;

    private Random random;
    private CountUpTimer timer;
    private List<Point> snowFlakePositions, snowFlakeVelocities;
    private int width;
    private int height;
    private Paint snowPaint, skyPaint;


    public SnowView(Context context, AttributeSet attrs){
        super(context, attrs);
        random = new Random();
        snowPaint = new Paint();
        snowPaint.setColor(context.getColor(R.color.cloudWhite));
        this.skyPaint = new Paint();
        this.skyPaint.setColor(context.getColor(R.color.skyBlue));
        snowFlakePositions = new ArrayList<>();
        snowFlakeVelocities = new ArrayList<>();
        timer = new CountUpTimer(1000 / FPS) {
            @Override
            public void onTick(long millisElapsed) {
                moveAllSnowflakes();
                invalidate();
            }
        };
        timer.start();
    }

    private void moveAllSnowflakes(){
        if (snowFlakePositions.size() == 0){
            return;
        }
        for (int i = 0; i < NUM_SNOWFLAKES; i++){
            Point point = snowFlakePositions.get(i);
            Point velocity = snowFlakeVelocities.get(i);
            point.y += velocity.y;
            float f = random.nextFloat();
            if (f < 0.01 && velocity.x > -2){
                velocity.x--;
            } else if (f > 0.99 && velocity.x < 2){
                velocity.x++;
            }
            point.x += velocity.x + WIND;
            if (point.y > height){
                point.y -= height;
            }
            if (point.x > width){
                point.x -= width;
            } else if (point.x < 0){
                point.x += width;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        width = canvas.getWidth();
        height = canvas.getHeight();
        canvas.drawRect(0,  getHeight(), getWidth(), 0, skyPaint);
        if (snowFlakePositions.size() == 0){
            for (int i = 0; i < NUM_SNOWFLAKES; i++){
                snowFlakePositions.add(new Point(random.nextInt(width), random.nextInt(height)));
                snowFlakeVelocities.add(new Point(0, GRAVITY));
            }
        }
        for (Point point : snowFlakePositions){
            canvas.drawCircle(point.x, point.y, RADIUS, snowPaint);
        }
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        timer.cancel();
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        timer.start();
    }
}
