package com.example.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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

    public TutorialMountainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        actionInProgress = false;
        textHintPaint = new TextPaint();
        textHintPaint.setColor(context.getColor(R.color.darkTextGrey));
        textHintPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        victoryTextPaint.setAlpha(0);
    }

    public void setGame(TutorialGame game){
        super.setGame(game);
        this.game = game;
    }

    public boolean go(){
        if (game.getInstruction().getObjectID() != Instruction.GO_BUTTON){
            return false;
        }
        if (super.go()){
            game.markAsDone();
            return true;
        }
        return false;
    }

    protected void drawDirections(Canvas canvas){
        super.drawDirections(canvas);
    }

    protected void drawTextHint(Canvas canvas, String text) {
        int width = getWidth() - 2 * PADDING;
        textHintPaint.setTextSize(width / 20);
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split(" ")));
        if (words.size() == 0){
            return;
        }
        List<String> lines = new ArrayList<>();
        String line = words.get(0);
        Log.d("HINT", line + line.length());
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
        Log.d("HINT", "A" + lines.get(0).substring(0, 1) + "B");
        float y = PADDING;
        for (String s : lines){
            canvas.drawText(s, 100, y, textHintPaint);
            y = y + textHintPaint.descent() - textHintPaint.ascent();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game.moving != Game.Moving.NONE){
            return;
        }
        if (game.victory) {
            drawTextHint(canvas, "Good!");
        } else {
            Instruction instruction = game.getInstruction();
            drawTextHint(canvas, instruction.getText());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (game.victory) {
            return true;
        }

        float x = e.getX();
        float y = e.getY();

        Instruction instruction = game.getInstruction();

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
                if (instruction.getObjectID() == Instruction.ANYWHERE) {
                    game.markAsDone();
                }
                while (game.getInstruction().isDone()){
                    game.markAsDone();
                    game.updateVictory();
                }
                return true;
        }
        return false;
    }
}


