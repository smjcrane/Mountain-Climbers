package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.util.Pair;

import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.lang.Math;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MountainView extends View {
    public static int PADDING_TOP = 200;
    public static int PADDING = 150;
    private static int TEXT_SIZE = 1000;
    private static int HINT_FLASH_TIME = 500;
    private static int NUM_TREES = 30;
    protected Paint mountainPaint, victoryTextPaint;
    private ColorFilter hintFilter;
    protected Future<Solver.Move> hint;
    protected boolean hintFlashOn;
    protected Map<MountainClimber, PorterDuffColorFilter> climberFilters;
    protected Map<MountainClimber, PorterDuffColorFilter> arrowFilters;
    protected Map<MountainClimber, PorterDuffColorFilter> highlightedArrowFilters;
    protected Context context;
    protected MountainClimber selectedClimber;
    protected Rect r = new Rect();
    protected CountUpTimer hintTimer;
    Game game;
    private boolean clickable;
    private Map<MountainClimber, Integer> coloursInUse;
    private Random random;
    private Set<Integer> colorsAvailable;
    private List<Tree> trees;
    private Drawable climberDrawableRight, climberDrawableLeft;
    private boolean colorClimbers;
    static final int[] climberDrawableRightIDs = new int[] {R.drawable.circle, R.drawable.peg_person, R.drawable.hollow, R.drawable.climber_steampunk_right};
    static final int[] climberDrawableLeftIDs = new int[] {R.drawable.circle, R.drawable.peg_person, R.drawable.hollow, R.drawable.climber_steampunk_left};

    public MountainView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        this.clickable = true;

        this.mountainPaint = new Paint();
        this.mountainPaint.setColor(context.getColor(R.color.mountainGrey));
        this.mountainPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mountainPaint.setAntiAlias(true);
        this.mountainPaint.setStrokeWidth(2);
        this.victoryTextPaint = new Paint();
        this.victoryTextPaint.setColor(context.getColor(R.color.victoryGold));
        this.victoryTextPaint.setTextSize(TEXT_SIZE);
        this.hintFilter = new PorterDuffColorFilter(context.getColor(R.color.hintingArrow), PorterDuff.Mode.SRC_ATOP);

        this.climberFilters = new HashMap<>();
        this.arrowFilters = new HashMap<>();
        this.highlightedArrowFilters = new HashMap<>();
        this.selectedClimber = null;
        this.hintTimer = new CountUpTimer(HINT_FLASH_TIME, new CountUpTimer.Ticker() {
            @Override
            public void onTick(long millisElapsed) {
                if (hintFlashOn){
                    hintFlashOn = false;
                } else {
                    hintFlashOn = true;
                }
                invalidate();
            }
        });
        updateClimberDrawable();
        coloursInUse = new HashMap<>();
        random = new Random(SystemClock.elapsedRealtime());
    }

    public void deActivate(){
        clickable = false;
    }

    public void activate() {
        clickable = true;
    }

    public void cancelHint(){
        hintTimer.cancel();
        this.hintFlashOn = false;
    }

    public void addClimber(MountainClimber climber){
        int i = new ArrayList<>(colorsAvailable).get(random.nextInt(colorsAvailable.size()));
        game.climbers.add(climber);
        PorterDuffColorFilter filter = new PorterDuffColorFilter(getClimberColor(i), PorterDuff.Mode.SRC_ATOP);
        climberFilters.put(climber, filter);
        PorterDuffColorFilter arrowFilter = new PorterDuffColorFilter(getArrowColor(i), PorterDuff.Mode.SRC_ATOP);
        this.arrowFilters.put(climber, arrowFilter);
        PorterDuffColorFilter highlightedFilter = new PorterDuffColorFilter(getHighlightedColor(i), PorterDuff.Mode.SRC_ATOP);
        this.highlightedArrowFilters.put(climber, highlightedFilter);
        coloursInUse.put(climber, i);
        colorsAvailable.remove(i);
    }

    public void setGame(Game game){
        this.game = game;
        this.selectedClimber = null;
        this.climberFilters = new HashMap<>();
        this.arrowFilters = new HashMap<>();
        this.highlightedArrowFilters = new HashMap<>();
        this.hint = null;
        this.hintFlashOn = false;
        colorsAvailable = new HashSet<>(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5}));
        trees = null;
        invalidate();
    }

    private void drawClimbers(Canvas canvas){
        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - PADDING - PADDING_TOP;
        int climberSize = Math.max(40, Math.max(width, height) / 30);
        for (MountainClimber climber : game.climbers){
            int cx = climber.getPosition() * width / game.mountain.getWidth() + PADDING;
            int cy = getHeight() - PADDING - game.mountain.getHeightAt(climber.getPosition()) *
                    height / game.mountain.getMaxHeight();
            Drawable d;
            if (climber.getDirection() == MountainClimber.Direction.LEFT){
                d = climberDrawableLeft.getConstantState().newDrawable().mutate();
            } else {
                d = climberDrawableRight.getConstantState().newDrawable().mutate();
            }
            if (colorClimbers){
                d.setColorFilter(climberFilters.get(climber));
            }
            d.setBounds(cx - climberSize, cy - climberSize, cx + climberSize, cy + climberSize);
            d.draw(canvas);
        }
    }

    public void showHint(){
        if (hint == null){
            hintFlashOn = true;
            hintTimer.start();
            hint = game.getHint(context);
            invalidate();
        }
    }

    protected void drawDirections(Canvas canvas){
        if (game.moving == Game.Moving.UP || game.moving == Game.Moving.DOWN || game.victory){
            return;
        }
        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - PADDING - PADDING_TOP;
        int arrowSize = Math.max(20, Math.max(width, height) / 35);
        for (MountainClimber climber : game.climbers) {
            int cx = climber.getPosition() * width / game.mountain.getWidth() + PADDING;
            int cy = getHeight() - PADDING - game.mountain.getHeightAt(climber.getPosition()) *
                    height / game.mountain.getMaxHeight();

            MountainClimber.Direction direction = climber.getDirection();

            if (direction == MountainClimber.Direction.LEFT || climber == selectedClimber) {
                Drawable leftArrow = ContextCompat.getDrawable(this.context, R.drawable.arrow_left);
                leftArrow.setBounds(cx - (int) (arrowSize * 2), cy - arrowSize, cx - arrowSize, cy + arrowSize);
                if (direction == MountainClimber.Direction.LEFT) {
                    leftArrow.setColorFilter(highlightedArrowFilters.get(climber));
                } else {
                    leftArrow.setColorFilter(arrowFilters.get(climber));
                }
                leftArrow.draw(canvas);
            }
            if (direction == MountainClimber.Direction.RIGHT || climber == selectedClimber) {
                Drawable rightArrow = ContextCompat.getDrawable(this.context, R.drawable.arrow_right);
                rightArrow.setBounds(cx + arrowSize, cy - arrowSize, cx + (int) (arrowSize * 2), cy + arrowSize);
                if (direction == MountainClimber.Direction.RIGHT) {
                    rightArrow.setColorFilter(highlightedArrowFilters.get(climber));
                } else {
                    rightArrow.setColorFilter(arrowFilters.get(climber));
                }
                rightArrow.draw(canvas);
            }
        }
    }

    protected void drawHint(Canvas canvas){
        if (hint == null || !hintFlashOn || !hint.isDone()){
            return;
        }
        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - PADDING - PADDING_TOP;
        MountainClimber.Direction[] directions;
        try {
            directions = hint.get().getDirections();
        } catch (InterruptedException| ExecutionException e){
            e.printStackTrace();
            return;
        }
        int arrowSize = Math.max(20, Math.max(width, height) / 35);
        for (int i = 0; i < game.climbers.size(); i++) {
            MountainClimber climber = game.climbers.get(i);
            int cx = climber.getPosition() * width / game.mountain.getWidth() + PADDING;
            int cy = getHeight() - PADDING - game.mountain.getHeightAt(climber.getPosition()) *
                    height / game.mountain.getMaxHeight();
            MountainClimber.Direction direction = directions[i];
            if (direction == MountainClimber.Direction.LEFT && (climber.getDirection() != MountainClimber.Direction.LEFT)) {
                Drawable d = ContextCompat.getDrawable(this.context, R.drawable.arrow_left);
                d.setColorFilter(hintFilter);
                d.setAlpha(150);
                d.setBounds(cx - (int) (arrowSize * 2), cy - arrowSize, cx - arrowSize, cy + arrowSize);
                d.draw(canvas);
            } else if (direction == MountainClimber.Direction.RIGHT && (climber.getDirection() != MountainClimber.Direction.RIGHT)){
                Drawable d = ContextCompat.getDrawable(this.context, R.drawable.arrow_right);
                d.setColorFilter(hintFilter);
                d.setAlpha(150);
                d.setBounds(cx + arrowSize, cy - arrowSize, cx + (int) (arrowSize * 2), cy + arrowSize);
                d.draw(canvas);
            }
        }
        }

    protected void drawMountain(Canvas canvas){
        int height = getHeight() - PADDING - PADDING_TOP;
        int width = getWidth() - 2 * PADDING;

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(PADDING, height + PADDING_TOP);
        for (int x : game.mountain.getTurningPoints()) {
            int y = -PADDING_TOP + game.mountain.getHeightAt(x) * height / game.mountain.getMaxHeight();
            path.lineTo(PADDING + x * width / game.mountain.getWidth(), height - y);
        }
        path.lineTo(width + PADDING, height + PADDING_TOP);
        path.close();
        canvas.drawPath(path, mountainPaint);
        canvas.drawRect(0, getHeight(), getWidth(), getHeight() - PADDING, mountainPaint);
        canvas.drawRect(0, getHeight(), PADDING,
                 height + PADDING_TOP - mountainPaint.getStrokeWidth() -
                         game.mountain.getHeightAt(0) * height / game.mountain.getMaxHeight(),
                mountainPaint);
        canvas.drawRect(width + PADDING, getHeight(), getWidth(),
                height + PADDING_TOP - mountainPaint.getStrokeWidth() -
                        game.mountain.getHeightAt(game.mountain.getWidth()) * height / game.mountain.getMaxHeight(),
                mountainPaint);
    }

    protected void drawTrees(Canvas canvas){
        if (trees == null){
            trees = new ArrayList<>();
            for (int i = 0; i < NUM_TREES; i++){
                trees.add(new Tree(this.context, random, getWidth(), getHeight(), game.mountain, trees));
            }
        }
        for (int i = 0; i < trees.size(); i++){
            trees.get(i).draw(canvas);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMountain(canvas);
        drawTrees(canvas);
        drawDirections(canvas);
        drawHint(canvas);
        drawClimbers(canvas);

        if (game.moving != Game.Moving.NONE){
            postInvalidateDelayed(5);
        }

        Pair<MountainClimber, MountainClimber> climbers = game.removeClimbers();
        if (!game.victory){
            game.updateVictory();
        }
        while (climbers != null){
            MountainClimber climber = climbers.first;
            MountainClimber gone = climbers.second;
            int c;
            if (colorsAvailable.size() == 0){
                colorsAvailable.add(coloursInUse.get(climber));;
                colorsAvailable.add(coloursInUse.get(gone));
                c = new ArrayList<>(colorsAvailable).get(random.nextInt(colorsAvailable.size()));
            } else {
                c = new ArrayList<>(colorsAvailable).get(random.nextInt(colorsAvailable.size()));
                colorsAvailable.add(coloursInUse.get(climber));;
                colorsAvailable.add(coloursInUse.get(gone));
            }
            climberFilters.put(climber, new PorterDuffColorFilter(getClimberColor(c), PorterDuff.Mode.SRC_ATOP));
            arrowFilters.put(climber, new PorterDuffColorFilter(getArrowColor(c), PorterDuff.Mode.SRC_ATOP));
            highlightedArrowFilters.put(climber, new PorterDuffColorFilter(getHighlightedColor(c), PorterDuff.Mode.SRC_ATOP));
            climbers = game.removeClimbers();
            colorsAvailable.remove(c);
            coloursInUse.put(climber, c);
            coloursInUse.remove(gone);
        }
        invalidate();
    }

    public boolean go(){
        boolean gone = game.go();
        if (gone){
            hint = null;
            hintFlashOn = false;
            hintTimer.cancel();
            invalidate();
        }
        return gone;
    }

    public void updateClimberDrawable(){
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.PREFERENCES), Context.MODE_PRIVATE);
        int climberAppearance = preferences.getInt(context.getString(R.string.CLIMBER_APPEARANCE), SettingsActivity.CLIMBER_CIRCLE);
        // climberAppearance = 3;
        colorClimbers = climberAppearance < 3;
        this.climberDrawableRight = context.getDrawable(climberDrawableRightIDs[climberAppearance]);
        this.climberDrawableLeft = context.getDrawable(climberDrawableLeftIDs[climberAppearance]);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        if (game.victory || !clickable || game.moving != Game.Moving.NONE){
            return true;
        }

        float x = e.getX();
        float y = e.getY();

        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - PADDING - PADDING_TOP;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //hint = null;
                //hintFlashOn = false;
                //hintTimer.cancel();
                if (selectedClimber == null){
                    MountainClimber bestClimber = null;
                    int bestDistance = Math.max(width, height) / 10;
                    MountainClimber.Direction direction = null;
                    for (MountainClimber climber : game.climbers){
                        int cx = climber.getPosition() * width / game.mountain.getWidth() + PADDING;
                        int cy = getHeight() - PADDING -
                                game.mountain.getHeightAt(climber.getPosition()) * height / game.mountain.getMaxHeight();
                        if (Math.abs(cx - x) + Math.abs(cy - y) < bestDistance){
                            bestClimber = climber;
                            bestDistance = (int) (Math.abs(cx - x) + Math.abs(cy - y));
                            direction = x > cx ? MountainClimber.Direction.RIGHT : MountainClimber.Direction.LEFT;
                        }
                    }
                    selectedClimber = bestClimber;
                    if (selectedClimber != null){
                        selectedClimber.setDirection(direction);
                    }
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

    private int getClimberColor(int i){
        return ColorUtils.HSLToColor(new float[]{Common.climberHues[i], 0.9f, 0.5f});
    }

    private int getArrowColor(int i){
        return ColorUtils.HSLToColor(new float[]{Common.climberHues[i], 0.7f, 0.7f});
    }

    private int getHighlightedColor(int i){
        return ColorUtils.HSLToColor(new float[]{Common.climberHues[i], 0.7f, 0.85f});
    }
}
