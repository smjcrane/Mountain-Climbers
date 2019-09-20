package com.example.mountainclimbers;

import android.os.CountDownTimer;

public abstract class CountUpTimer extends CountDownTimer {
    private static final long INTERVAL_MS = 1000;
    private static final long duration = Long.MAX_VALUE;

    protected CountUpTimer() {
        super(Long.MAX_VALUE, INTERVAL_MS);
    }

    public abstract void onTick(int second);

    @Override
    public void onTick(long msUntilFinished) {
        int second = (int) ((duration - msUntilFinished) / 1000);
        onTick(second);
    }

    @Override
    public void onFinish() {
        onTick(duration / 1000);
    }
}