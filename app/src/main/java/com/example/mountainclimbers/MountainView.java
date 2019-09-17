package com.example.mountainclimbers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.lang.Math;

public class MountainView extends View {

    public static int STEP_NUMBER = 1000;

    private Mountain mountain;
    private Paint mountainPaint, skyPaint;
    private ColorFilter arrowFilter, highlightedArrowFilter;
    private List<MountainClimber> climbers;
    private Map<MountainClimber, Paint> climberPaints;
    private Context context;
    private MountainClimber selectedClimber;
    private Moving moving;
    private int padding = 100;

    public MountainView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        Resources r = getResources();
        this.mountainPaint = new Paint();
        this.mountainPaint.setColor(r.getColor(R.color.mountainGrey));
        this.skyPaint = new Paint();
        this.skyPaint.setColor(r.getColor(R.color.skyBlue));
        this.arrowFilter = new PorterDuffColorFilter(r.getColor(R.color.guideArrows), PorterDuff.Mode.SRC_ATOP);
        this.highlightedArrowFilter = new PorterDuffColorFilter(r.getColor(R.color.highlightedArrow), PorterDuff.Mode.SRC_ATOP);
        this.climbers = new ArrayList<>();
        this.climberPaints = new HashMap<>();
        this.selectedClimber = null;
        this.moving = Moving.NONE;
    }

    public void addClimber(MountainClimber climber, int colorId){
        this.climbers.add(climber);
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(colorId));
        this.climberPaints.put(climber, paint);
    }

    public void setMountain(Mountain mountain){
        this.mountain = mountain;
    }

    public void drawClimbers(Canvas canvas){
        int width = getWidth() - 2 * padding;
        for (MountainClimber climber : climbers){
            canvas.drawCircle(climber.getPosition() * width / mountain.getWidth() + padding,
                    getHeight() - padding - mountain.getHeightAt(climber.getPosition()) * getHeight() / mountain.getViewHeight(),
                    30, climberPaints.get(climber));
        }
    }

    public void drawDirections(Canvas canvas){
        if (moving == Moving.UP || moving == Moving.DOWN){
            return;
        }
        int width = getWidth() - 2 * padding;
        for (MountainClimber climber : climbers){
            int cx = climber.getPosition() * width / mountain.getWidth() + padding;
            int cy = getHeight() - padding - this.mountain.getHeightAt(climber.getPosition())  * getHeight() / mountain.getViewHeight();
            Drawable d = ContextCompat.getDrawable(this.context, R.drawable.arrow_left);
            d.setColorFilter(arrowFilter);

            MountainClimber.Direction direction = climber.getDirection();

            d.setBounds(cx - 80, cy - 30, cx - 35, cy + 30);
            if (direction == MountainClimber.Direction.LEFT){
                d.setColorFilter(highlightedArrowFilter);
                d.draw(canvas);
                d.setColorFilter(arrowFilter);
            } else if (climber == selectedClimber){
                d.draw(canvas);
            }
            d.setBounds(cx + 80, cy - 30, cx + 35, cy + 30);
            if (direction == MountainClimber.Direction.RIGHT){
                d.setColorFilter(highlightedArrowFilter);
                d.draw(canvas);
                d.setColorFilter(arrowFilter);
            } else if (climber == selectedClimber){
                d.draw(canvas);
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = 0;
        int height = getHeight();
        int width = getWidth() - 2 * padding;
        canvas.drawRect(0,  height, width + 2 * padding, 0, skyPaint);
        canvas.drawRect(0, height, padding, height - padding, mountainPaint);
        canvas.drawRect(width + padding, height, width + 2 * padding, height - padding, mountainPaint);
        for (int i = 0; i < STEP_NUMBER; i++){
            int y = mountain.getHeightAt(
                    mountain.getWidth() * i / STEP_NUMBER) * height / mountain.getViewHeight();
            canvas.drawRect(i * width / STEP_NUMBER + padding,
                    height,
                    (i + 1) * width / STEP_NUMBER + padding,
                    height - y - padding, mountainPaint);
        }
        drawClimbers(canvas);
        drawDirections(canvas);
        if (moving != Moving.NONE){
            boolean canGoUp = (moving == Moving.UP);
            for (MountainClimber climber : climbers){
                canGoUp = canGoUp && climber.canMoveUp(mountain);
            }
            boolean canGoDown = (moving == Moving.DOWN);
            for (MountainClimber climber : climbers){
                canGoDown = canGoDown && climber.canMoveDown(mountain);
            }
            if (canGoUp || canGoDown){
                for (MountainClimber climber : climbers){
                    climber.move();
                }
                postInvalidateDelayed(2);
                return;
            } else {
                moving = Moving.NONE;
                //TODO collision checking
                invalidate();
            }
        }
    }

    public void go(){
        boolean allSelected = true;
        for (MountainClimber climber : climbers){
            if (climber.getDirection() == null){
                allSelected = false;
            }
        }
        if (allSelected == false){
            return;
        }
        boolean canGoUp = true;
        for (MountainClimber climber : climbers){
            canGoUp = canGoUp && climber.canMoveUp(mountain);
        }
        if (canGoUp){
            Log.d("MVIEW", "Going up");
            moving = Moving.UP;
            invalidate();
        }
        boolean canGoDown = true;
        for (MountainClimber climber : climbers){
            canGoDown = canGoDown && climber.canMoveDown(mountain);
        }
        if (canGoDown){
            Log.d("MVIEW", "Going down");
            moving = Moving.DOWN;
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (selectedClimber == null){
                    MountainClimber bestClimber = null;
                    int bestDistance = Integer.MAX_VALUE;
                    for (MountainClimber climber : climbers){
                        int cx = climber.getPosition() * getWidth() / mountain.getWidth();
                        int cy = getHeight() - mountain.getHeightAt(climber.getPosition()) * getHeight() / mountain.getViewHeight();
                        if (Math.abs(cx - x) + Math.abs(cy - y) < bestDistance){
                            bestClimber = climber;
                            bestDistance = (int) (Math.abs(cx - x) + Math.abs(cy - y));
                        }
                    }
                    selectedClimber = bestClimber;
                    invalidate();
                    return true;
                } else {
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (selectedClimber == null){
                    return false;
                }
                int cx = selectedClimber.getPosition() * getWidth() / mountain.getWidth();
                int cy = getHeight() - mountain.getHeightAt(selectedClimber.getPosition()) * getHeight() / mountain.getViewHeight();
                Log.d("MVIEW", cx + " " + cy + ", " + x + " " + y);
                if (x > cx){
                    selectedClimber.setDirection(MountainClimber.Direction.RIGHT);
                } else {
                    selectedClimber.setDirection(MountainClimber.Direction.LEFT);
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (selectedClimber != null){
                    selectedClimber = null;
                    invalidate();
                    return true;
                }
                return false;
        }
        return false;
    }

    public enum Moving{
        UP, DOWN, NONE
    }
}
