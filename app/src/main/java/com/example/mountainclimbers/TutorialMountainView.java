package com.example.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialMountainView extends MountainView {

    private List<Instruction> instructionList;
    private int instructionIndex;
    private boolean actionInProgress;
    protected Paint textHintPaint;

    public TutorialMountainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        instructionIndex = 0;
        actionInProgress = false;
        textHintPaint = new Paint();
        textHintPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        textHintPaint.setTextSize(100);
    }

    public void setInstructionList(List<Instruction> instructionList){
        Log.d("TUT", "there are " + instructionList.size() + " instructions");
        this.instructionList = instructionList;
        this.instructionIndex = 0;
    }

    public void go(){
        if (victory) {
            return;
        }
        if (instructionList.get(instructionIndex).getObjectID() != Instruction.GO_BUTTON){
            return;
        }
        super.go();
        instructionList.get(instructionIndex).markAsDone();
        instructionIndex = instructionIndex + 1;
        if (instructionIndex == instructionList.size()){
            victory = true;
        }
    }

    protected boolean removeClimbers(){
        for (MountainClimber climber : climbers){
            for (MountainClimber c2 : climbers) {
                if (c2 != climber && Math.abs(c2.getPosition() - climber.getPosition()) < 1.5) {
                    this.climbers.remove(c2);
                    climber.setDirection(null);
                    return true;
                }
            }
        }
        return false;
    }

    protected void drawDirections(Canvas canvas){
        if (victory){
            return;
        }
        super.drawDirections(canvas);
    }

    protected void drawTextHint(Canvas canvas, String text) {
        int width = getWidth() - 2 * PADDING;
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
            canvas.drawText(s, PADDING, y, textHintPaint);
            y = y + textHintPaint.descent() - textHintPaint.ascent();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (moving != Moving.NONE){
            return;
        }
        if (victory) {
            drawTextHint(canvas, "Good!");
        } else {
            Instruction instruction = instructionList.get(instructionIndex);
            drawTextHint(canvas, instruction.getText());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (victory) {
            return true;
        }

        float x = e.getX();
        float y = e.getY();

        Instruction instruction = instructionList.get(instructionIndex);

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("TUT", instructionIndex + " " + instruction.isDone());
                if (actionInProgress){
                    return false;
                }
                if (selectedClimber == null){
                    int width = getWidth() - 2 * PADDING;
                    int height = getHeight() - 2 * PADDING;

                    MountainClimber bestClimber = null;
                    int bestDistance = 200;
                    for (MountainClimber climber : climbers){
                        int cx = climber.getPosition() * width / mountain.getWidth() + PADDING;
                        int cy = getHeight() - PADDING - mountain.getHeightAt(climber.getPosition()) * height / mountain.getMaxHeight();
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
                int cx = selectedClimber.getPosition() * getWidth() / mountain.getWidth();
                int cy = getHeight() - mountain.getHeightAt(selectedClimber.getPosition()) * getHeight() / mountain.getMaxHeight();
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
                    instruction.markAsDone();
                }
                while (instruction.isDone()){
                    instructionIndex = instructionIndex + 1;
                    if (instructionIndex == instructionList.size()){
                        victory = true;
                        return true;
                    }
                    instruction = instructionList.get(instructionIndex);
                }
                return true;
        }
        return false;
    }
}


