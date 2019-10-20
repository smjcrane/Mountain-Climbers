package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CountDownView extends View {

    private static int FPS = 24;

    private Context context;
    private TextPaint paint;
    private boolean going;
    private long stopMillis;
    private OnCounted onCounted;


    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        paint = new TextPaint();
        paint.setColor(context.getColor(R.color.darkTextBlue));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setAlpha(100);
        going = false;
        onCounted = new OnCounted() {
            @Override
            public void onCounted() {
                return;
            }
        };
    }

    public void setOnCounted(OnCounted onCounted){
        this.onCounted = onCounted;
    }

    public void start(int numberToShow){
        stopMillis = SystemClock.elapsedRealtime() + (numberToShow + 1) * 1000;
        going = true;
        invalidate();
    }

    public void cancel(){
        going = false;
    }

    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        long timeLeft = stopMillis - SystemClock.elapsedRealtime();
        if (timeLeft < 1000 ){
            onCounted.onCounted();
        }
        if (timeLeft < 0){
            going = false;
        }
        if (!going){
            return;
        }
        Rect rect = new Rect();
        String text = timeLeft > 1000? Integer.toString((int) timeLeft / 1000) : context.getString(R.string.go) + "!";
        int textSize = getWidth() * (int) (timeLeft % 1000) / 500;
        if (textSize < 400){
            textSize = 400;
        }
        paint.setTextSize(textSize);
        paint.getTextBounds(text, 0, 1, rect);
        canvas.drawText(text, (getWidth() + rect.left - rect.right) / 2, (getHeight() - rect.top + rect.bottom) / 2, paint);
        if (going){
            postInvalidateDelayed(1000 / FPS);
        }
    }

    public interface OnCounted {
        void onCounted();
    }
}
