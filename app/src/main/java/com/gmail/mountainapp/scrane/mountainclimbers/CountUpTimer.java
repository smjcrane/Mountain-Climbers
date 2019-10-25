package com.gmail.mountainapp.scrane.mountainclimbers;

import android.os.Handler;
import android.os.SystemClock;

import java.util.Timer;
import java.util.TimerTask;

public class CountUpTimer {

    boolean cancelled = false;
    private long millisAtStart;
    private long interval;
    private Handler handler;
    private Timer timer;
    private Runnable runnable;
    private Ticker ticker;

    public CountUpTimer(long interval, Ticker ticker) {
        this.interval = interval;
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                onTick(SystemClock.elapsedRealtime() - millisAtStart);
            }
        };
        this.ticker = ticker;
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

    public void onTick(long millisElapsed){
        ticker.onTick(millisElapsed);
    };

    public long getMillisCounted() {
        return SystemClock.elapsedRealtime() - millisAtStart;
    }

    public interface Ticker {
        public void onTick(long millisElapsed);
    }
}