package com.gmail.mountainapp.scrane.mountainclimbers;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public abstract class CountUpTimer {

    boolean cancelled = false;
    private long millisAtStart;
    private long interval;
    private static final int MSG = 1;

    public CountUpTimer(long interval) {
        this.interval = interval;
    }

    public CountUpTimer(long interval, long millisAtStart){
        this.interval = interval;
        this.millisAtStart = millisAtStart;
        handler.sendMessage(handler.obtainMessage(MSG));
    }

    public long getMillisAtStart(){
        return millisAtStart;
    }

    public synchronized final void cancel() {
        cancelled = true;
    }

    public synchronized final void start() {
        millisAtStart = SystemClock.elapsedRealtime();
        handler.sendMessage(handler.obtainMessage(MSG));
    }

    public abstract void onTick(long millisElapsed);

    // handles counting down
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CountUpTimer.this) {
                if (cancelled) {
                    return;
                }

                final long millisElapsed = SystemClock.elapsedRealtime() - millisAtStart;

                long lastTickStart = SystemClock.elapsedRealtime();
                onTick(millisElapsed);

                // take into account user's onTick taking time to execute
                long lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart;
                long delay = interval - lastTickDuration;

                sendMessageDelayed(obtainMessage(MSG), delay);
            }
        }
    };
}