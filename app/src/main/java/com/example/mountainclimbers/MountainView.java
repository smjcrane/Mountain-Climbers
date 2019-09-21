package com.example.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import java.lang.Math;
import java.util.Random;

public class MountainView extends View {

    public static int PADDING = 150;
    private static int TEXT_SIZE = 1000;
    private static int HINT_FLASH_TIME = 500;

    private Paint mountainPaint, skyPaint, cloudPaint;
    protected Paint victoryTextPaint;
    private ColorFilter arrowFilter;
    private ColorFilter hintFilter;
    private Solver.Move hint;
    private boolean hintFlashOn;
    protected Map<MountainClimber, Paint> climberPaints;
    protected Context context;
    protected MountainClimber selectedClimber;
    protected Rect r = new Rect();
    protected Random random;
    protected long seed;
    Game game;

    public MountainView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        this.random = new Random();

        this.mountainPaint = new Paint();
        this.mountainPaint.setColor(context.getColor(R.color.mountainGrey));
        this.mountainPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mountainPaint.setAntiAlias(true);
        this.mountainPaint.setStrokeWidth(2);
        this.skyPaint = new Paint();
        this.skyPaint.setColor(context.getColor(R.color.skyBlue));
        this.cloudPaint = new Paint();
        this.cloudPaint.setColor(context.getColor(R.color.cloudWhite));
        this.cloudPaint.setStrokeWidth(10);
        this.cloudPaint.setStrokeCap(Paint.Cap.ROUND);
        this.victoryTextPaint = new Paint();
        this.victoryTextPaint.setColor(context.getColor(R.color.victoryGold));
        this.victoryTextPaint.setTextSize(TEXT_SIZE);
        this.arrowFilter = new PorterDuffColorFilter(context.getColor(R.color.highlightedArrow), PorterDuff.Mode.SRC_ATOP);
        this.hintFilter = new PorterDuffColorFilter(context.getColor(R.color.hintingArrow), PorterDuff.Mode.SRC_ATOP);

        this.climberPaints = new HashMap<>();
        this.selectedClimber = null;
    }

    public void setSeed(long seed){
        this.seed = seed;
    }

    public void addClimber(MountainClimber climber, int colorId){
        this.game.climbers.add(climber);
        Paint paint = new Paint();
        paint.setColor(context.getColor(colorId));
        this.climberPaints.put(climber, paint);
    }

    public void setGame(Game game){
        this.game = game;
        this.selectedClimber = null;
        this.climberPaints = new HashMap<>();
        invalidate();
    }

    private void drawClimbers(Canvas canvas){
        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - 2 * PADDING;
        for (MountainClimber climber : game.climbers){
            canvas.drawCircle(climber.getPosition() * width / game.mountain.getWidth() + PADDING,
                    getHeight() - PADDING - game.mountain.getHeightAt(climber.getPosition()) *
                            height / game.mountain.getMaxHeight(),
                    30, climberPaints.get(climber));
        }
    }

    public void showHint(){
        hint = game.getHint();
        hintFlashOn = true;
        invalidate();
    }

    protected void drawDirections(Canvas canvas){
        if (game.moving == Game.Moving.UP || game.moving == Game.Moving.DOWN || game.victory){
            return;
        }
        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - 2 * PADDING;
        for (MountainClimber climber : game.climbers) {
            int cx = climber.getPosition() * width / game.mountain.getWidth() + PADDING;
            int cy = getHeight() - PADDING - game.mountain.getHeightAt(climber.getPosition()) *
                    height / game.mountain.getMaxHeight();

            MountainClimber.Direction direction = climber.getDirection();

            if (direction == MountainClimber.Direction.LEFT || climber == selectedClimber) {
                Drawable leftArrow = ContextCompat.getDrawable(this.context, R.drawable.arrow_left);
                leftArrow.setBounds(cx - 80, cy - 30, cx - 35, cy + 30);
                leftArrow.setColorFilter(arrowFilter);
                leftArrow.setAlpha(100);
                if (direction == MountainClimber.Direction.LEFT) {
                    leftArrow.setAlpha(150);
                }
                leftArrow.setColorFilter(arrowFilter);
                leftArrow.draw(canvas);
            }
            if (direction == MountainClimber.Direction.RIGHT) {
                Drawable rightArrow = ContextCompat.getDrawable(this.context, R.drawable.arrow_right);
                rightArrow.setBounds(cx + 35, cy - 30, cx + 80, cy + 30);
                rightArrow.setColorFilter(arrowFilter);
                rightArrow.setAlpha(100);
                if (direction == MountainClimber.Direction.LEFT) {
                    rightArrow.setAlpha(150);
                }
                rightArrow.draw(canvas);
            }
        }
    }

    protected void drawHint(Canvas canvas){
        if (hint == null){
            return;
        }
        if (!hintFlashOn){
            hintFlashOn = true;
            postInvalidateDelayed(HINT_FLASH_TIME);
            return;
        }
        Log.d("MVIEW", "Drawing hint");
        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - 2 * PADDING;
        MountainClimber.Direction[] directions = hint.getDirections();
        for (int i = 0; i < game.climbers.size(); i++) {
            MountainClimber climber = game.climbers.get(i);
            int cx = climber.getPosition() * width / game.mountain.getWidth() + PADDING;
            int cy = getHeight() - PADDING - game.mountain.getHeightAt(climber.getPosition()) *
                    height / game.mountain.getMaxHeight();


            MountainClimber.Direction direction = directions[i];
            if (direction == MountainClimber.Direction.LEFT) {
                Drawable d = ContextCompat.getDrawable(this.context, R.drawable.arrow_left);
                d.setColorFilter(hintFilter);
                d.setAlpha(150);
                d.setBounds(cx - 80, cy - 30, cx - 35, cy + 30);
                d.draw(canvas);
            } else if (direction == MountainClimber.Direction.RIGHT){
                Drawable d = ContextCompat.getDrawable(this.context, R.drawable.arrow_right);
                d.setColorFilter(hintFilter);
                d.setAlpha(150);
                d.setBounds(cx + 35, cy - 30, cx + 80, cy + 30);
                d.draw(canvas);
            }
        }
        hintFlashOn = false;
        postInvalidateDelayed(HINT_FLASH_TIME);
        }

    protected void drawCenteredText(Canvas canvas, Paint paint, String text) {
        TEXT_SIZE = 1000;
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        while (x < PADDING){
            TEXT_SIZE = TEXT_SIZE - 50;
            paint.setTextSize(TEXT_SIZE);
            canvas.getClipBounds(r);
            cHeight = r.height();
            cWidth = r.width();
            paint.setTextAlign(Paint.Align.LEFT);
            paint.getTextBounds(text, 0, text.length(), r);
            x = cWidth / 2f - r.width() / 2f - r.left;
            y = cHeight / 2f + r.height() / 2f - r.bottom;
        }
        canvas.drawText(text, x, y, paint);
    }

    protected void drawMountain(Canvas canvas){
        int height = getHeight() - 2 * PADDING;
        int width = getWidth() - 2 * PADDING;

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(PADDING, height + PADDING);
        for (int x : game.mountain.getTurningPoints()) {
            int y = -PADDING + game.mountain.getHeightAt(x) * height / game.mountain.getMaxHeight();
            path.lineTo(PADDING + x * width / game.mountain.getWidth(), height - y);
        }
        path.lineTo(width + PADDING, height + PADDING);
        path.close();
        canvas.drawPath(path, mountainPaint);
        canvas.drawRect(0, getHeight(), getWidth(), getHeight() - PADDING, mountainPaint);
        canvas.drawRect(0, getHeight(), PADDING,
                 height + PADDING - mountainPaint.getStrokeWidth() -
                         game.mountain.getHeightAt(0) * height / game.mountain.getMaxHeight(),
                mountainPaint);
        canvas.drawRect(width + PADDING, getHeight(), getWidth(),
                height + PADDING - mountainPaint.getStrokeWidth() -
                        game.mountain.getHeightAt(game.mountain.getWidth()) * height / game.mountain.getMaxHeight(),
                mountainPaint);
    }

    protected void drawClouds(Canvas canvas){
        for (int i = 0; i < 5; i++){
            float cx = random.nextFloat() * getWidth();
            float cy = random.nextFloat() * getHeight() / 2;
            int numBlobs = random.nextInt(3) + 3;
            int cloudWidth = 0;
            float radius = 0;
            for (int j = 0; j < numBlobs; j++){
                radius = random.nextFloat() * 50 + 50;
                canvas.drawCircle(cx + cloudWidth, cy - radius, radius, cloudPaint);
                cloudWidth += radius * 1.4;
            }
            cloudWidth -= radius * 1.4;
            canvas.drawRect(cx, cy, cx + cloudWidth, cy - 50, cloudPaint);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        random.setSeed(seed);

        while (game.removeClimbers()){
            game.updateVictory();
        };

        //sky
        canvas.drawRect(0,  getHeight(), getWidth(), 0, skyPaint);
        drawClouds(canvas);

        drawMountain(canvas);
        drawClimbers(canvas);
        drawDirections(canvas);
        drawHint(canvas);

        if (game.victory && game.moving == Game.Moving.NONE){
            drawCenteredText(canvas, victoryTextPaint, "YOU WIN!");
        }

        boolean moved = game.moveStep();
        if (moved){
            postInvalidateDelayed(2);
        }
    }

    public boolean go(){
        boolean gone = game.go();
        if (gone){
            hint = null;
            hintFlashOn = false;
            invalidate();
        }
        return gone;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        if (game.victory){
            return true;
        }

        float x = e.getX();
        float y = e.getY();

        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - 2 * PADDING;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hint = null;
                hintFlashOn = false;
                if (selectedClimber == null){
                    MountainClimber bestClimber = null;
                    int bestDistance = 50;
                    for (MountainClimber climber : game.climbers){
                        int cx = climber.getPosition() * width / game.mountain.getWidth() + PADDING;
                        int cy = getHeight() - PADDING -
                                game.mountain.getHeightAt(climber.getPosition()) * height / game.mountain.getMaxHeight();
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
                int cx = selectedClimber.getPosition() * width / game.mountain.getWidth() + PADDING;
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
}
