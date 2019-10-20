package com.gmail.mountainapp.scrane.mountainclimbers;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public abstract class CountUpTimer {

    boolean cancelled = false;
    private long millisAtStart;
    private long interval;
    private Handler handler;
    private Timer timer;
    private Runnable runnable;

    public CountUpTimer(long interval) {
        this.interval = interval;
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                onTick(SystemClock.elapsedRealtime() - millisAtStart);
            }
        };

    }

    public void cancel() {
        if (timer != null) {
            handler.removeCallbacks(runnable);
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public void start() {
        this.millisAtStart = SystemClock.elapsedRealtime();
        cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        }, 0, interval);
    }

    public long getMillisAtStart(){
        return millisAtStart;
    }

    public void setMillisAtStart(long millisAtStart){
        this.millisAtStart = millisAtStart;
    }

    public abstract void onTick(long millisElapsed);
}