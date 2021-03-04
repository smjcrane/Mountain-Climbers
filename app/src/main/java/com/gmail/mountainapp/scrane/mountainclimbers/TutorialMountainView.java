package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialMountainView extends MountainView {

    private boolean actionInProgress;
    protected TextPaint textHintPaint;
    TutorialGame game;
    private TutorialFinger finger;

    public TutorialMountainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        actionInProgress = false;
        textHintPaint = new TextPaint();
        textHintPaint.setColor(context.getColor(R.color.darkTextGrey));
        textHintPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        victoryTextPaint.setAlpha(0);
        finger = new TutorialFinger(context);
    }

    public void setGame(TutorialGame game){
        super.setGame(game);
        this.game = game;
    }

    public boolean go(){
        if (game.getInstruction().getObjectID() == TutorialInstruction.GO_BUTTON){
            if (super.go()){
                game.markAsDone();
                initialiseFinger();
                return true;
            }
            return false;
        } else if (game.getInstruction().getObjectID() == TutorialInstruction.ANYWHERE){
            game.markAsDone();
            initialiseFinger();
            invalidate();
            return true;
        }
        return false;
    }

    protected void drawDirections(Canvas canvas){
        super.drawDirections(canvas);
    }

    protected void drawTextHint(Canvas canvas, String text) {
        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - 2 * PADDING;
        textHintPaint.setTextSize(Math.max(width, height) / 20);
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split(" ")));
        if (words.size() == 0){
            return;
        }
        List<String> lines = new ArrayList<>();
        String line = words.get(0);
        words.remove(line);
        for (String word : words) {
            if (textHintPaint.measureText(line + " " + word) < width) {
                line = line + " " + word;
            } else {
                lines.add(line);
                line = word;
            }
        }
        lines.add(line);
        float y = PADDING;
        for (String s : lines){
            canvas.drawText(s, 100, y, textHintPaint);
            y = y + textHintPaint.descent() - textHintPaint.ascent();
        }
    }

    public void initialiseFinger() {
        TutorialInstruction instruction = game.getInstruction();
        if (instruction.getObjectID() == TutorialInstruction.GO_BUTTON){
            finger.tapOnPoint(new Point(getWidth() / 2 + 50, getHeight() - 100));
            return;
        }
        if (instruction.getObjectID() == TutorialInstruction.ANYWHERE) {
            finger.disappear();
            return;
        }
        if (game.victory) {
            finger.disappear();
            return;
        }
        MountainClimber.Direction dir = instruction.getDirection();
        if (dir == null){
            finger.disappear();
        } else {
            int width = getWidth() - 2 * PADDING;
            int height = getHeight() - PADDING - PADDING_TOP;
            int climberPos = instruction.getClimber().getPosition();
            int cx = climberPos * width / game.mountain.getWidth() + PADDING;
            int cy = getHeight() - PADDING - game.mountain.getHeightAt(climberPos) * height / game.mountain.getMaxHeight();
            Point start = new Point(cx, cy);
            int endx;
            if (dir == MountainClimber.Direction.LEFT){
                endx = cx - 200;
            } else {
                endx = cx + 200;
            }
            finger.swipeBetweenPoints(start, new Point(endx, cy));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initialiseFinger();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game.moving != Game.Moving.NONE){
            return;
        }
        game.updateVictory();
        if (game.victory) {
            drawTextHint(canvas, "Good!");
            game.callOnVictoryListener();
        } else {
            TutorialInstruction instruction = game.getInstruction();
            drawTextHint(canvas, instruction.getText());
            finger.draw(canvas);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        game.updateVictory();
        if (game.victory) {
            return true;
        }

        float x = e.getX();
        float y = e.getY();

        TutorialInstruction instruction = game.getInstruction();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (actionInProgress){
                    return false;
                }
                if (selectedClimber == null){
                    int width = getWidth() - 2 * PADDING;
                    int height = getHeight() - 2 * PADDING;

                    MountainClimber bestClimber = null;
                    int bestDistance = 200;
                    for (MountainClimber climber : game.climbers){
                        int cx = climber.getPosition() * width / game.mountain.getWidth() + PADDING;
                        int cy = getHeight() - PADDING -
                                game.mountain.getHeightAt(climber.getPosition()) *
                                        height / game.mountain.getMaxHeight();
                        if (Math.abs(cx - x) + Math.abs(cy - y) < bestDistance){
                            bestClimber = climber;
                            bestDistance = (int) (Math.abs(cx - x) + Math.abs(cy - y));
                        }
                    }
                    selectedClimber = bestClimber;
                    actionInProgress = true;
                    invalidate();
                    return true;
                } else {
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (selectedClimber == null){
                    return false;
                }
                int cx = selectedClimber.getPosition() * getWidth() / game.mountain.getWidth();
                int cy = getHeight() - game.mountain.getHeightAt(selectedClimber.getPosition()) *
                        getHeight() / game.mountain.getMaxHeight();
                if (x > cx){
                    selectedClimber.setDirection(MountainClimber.Direction.RIGHT);
                } else {
                    selectedClimber.setDirection(MountainClimber.Direction.LEFT);
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                actionInProgress = false;
                if (selectedClimber != null){
                    selectedClimber = null;
                    invalidate();
                }
                if (instruction.getObjectID() == TutorialInstruction.ANYWHERE) {
                    game.markAsDone();
                    initialiseFinger();
                }
                while (game.getInstruction().isDone()){
                    game.markAsDone();
                    initialiseFinger();
                }
                if (game.getInstruction().isHint() && hint == null){
                    hint = game.getInstruction().getHint();
                    hintFlashOn = true;
                    hintTimer.start();
                } else if (!game.getInstruction().isHint()) {
                    hint = null;
                    hintTimer.cancel();
                }
                invalidate();
                return true;
        }
        return false;
    }
}


