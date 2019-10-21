package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
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
    private List<Flying> flyings;
    private DrawFilter filter;

    public SnowView(Context context, AttributeSet attrs){
        super(context, attrs);
        random = new Random();
        snowPaint = new Paint();
        snowPaint.setColor(context.getColor(R.color.cloudWhite));
        this.skyPaint = new Paint();
        this.skyPaint.setColor(context.getColor(R.color.skyBlue));
        snowFlakePositions = new ArrayList<>();
        snowFlakeVelocities = new ArrayList<>();
        flyings = new ArrayList<>();
        timer = new CountUpTimer(1000 / FPS) {
            @Override
            public void onTick(long millisElapsed) {
                moveAllSnowflakes();
                moveAllFlyings();
                invalidate();
            }
        };
        timer.start();
        filter = new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG, 1);
    }

    private void moveAllFlyings(){
        for (Flying f: flyings){
            f.move();
        }
        while(removeFlyings()){}
    }

    private boolean removeFlyings(){
        for (Flying f: flyings){
            if (f.getX() > getWidth()){
                flyings.remove(f);
                return true;
            }
        }
        return false;
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
        canvas.setDrawFilter(filter);
        width = canvas.getWidth();
        height = canvas.getHeight();
        canvas.drawRect(0,  getHeight(), getWidth(), 0, skyPaint);
        if (snowFlakePositions.size() == 0){
            for (int i = 0; i < NUM_SNOWFLAKES; i++){
                snowFlakePositions.add(new Point(random.nextInt(width), random.nextInt(height)));
                snowFlakeVelocities.add(new Point(0, GRAVITY));
            }
        }
        float r = random.nextFloat();
        if (r < 0.001){
            flyings.add(new Flying(getContext(), getContext().getDrawable(R.drawable.witch), new Point(-200, 200 + random.nextInt(200))));
        }
        for (Point point : snowFlakePositions){
            canvas.drawCircle(point.x, point.y, RADIUS, snowPaint);
        }
        for (Flying f: flyings){
            f.draw(canvas);
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
