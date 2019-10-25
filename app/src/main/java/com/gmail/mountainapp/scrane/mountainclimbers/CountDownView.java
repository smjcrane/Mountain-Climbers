package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Px;
import androidx.core.widget.TextViewCompat;

import org.w3c.dom.Text;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class CountDownView extends TextView {

    private static int FPS = 24;

    private Context context;
    private TextPaint paint;
    private boolean going;
    private long stopMillis;
    private CountDownTimer timer;
    private Runnable onFinish;


    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setTextColor(context.getColor(R.color.darkTextBlue));
        setAlpha(0.4f);
        setGravity(Gravity.CENTER);
        setEllipsize(TextUtils.TruncateAt.END);
        setSingleLine(true);
    }

    public void setOnFinish(Runnable r){
        onFinish = r;
    }

    public void start(int numberToShow){
        going = true;
        if (timer!= null){
            timer.cancel();
        }
        timer = new CountDownTimer(1000 * (numberToShow + 1), 40) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!going){
                    return;
                }
                if (millisUntilFinished < 1000){
                    setText(context.getString(R.string.go) + "!");
                } else {
                    int seconds = (int) millisUntilFinished / 1000;
                    setText(Integer.toString(seconds));
                }
                int textSize = Math.max(200, getWidth() * (int) (millisUntilFinished % 1000) / 1000);
                setTextSize(COMPLEX_UNIT_DIP, textSize);
            }

            @Override
            public void onFinish() {
                setText("");
                if (onFinish != null){
                    onFinish.run();
                }
            }
        };
        timer.start();
    }

    public void cancel(){
        going = false;
    }
}
