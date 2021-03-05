package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.List;
import java.util.Random;

import static com.gmail.mountainapp.scrane.mountainclimbers.MountainView.PADDING;
import static com.gmail.mountainapp.scrane.mountainclimbers.MountainView.PADDING_TOP;

public class Tree {
    private Drawable leafDrawable, trunkDrawable, shadowDrawable;
    private Point p;

    public Tree(Context context, Random random, int maxWidth, int maxHeight, Mountain mountain, List<Tree> others) {
        boolean good = false;
        int parentHeight = maxHeight - PADDING - PADDING_TOP;
        int parentWidth = maxWidth - 2 * PADDING;
        double scaleFactor = context.getResources().getDisplayMetrics().density / 2.75;
        while (!good) {
            int x = random.nextInt(mountain.getWidth());
            int y = mountain.getHeightAt(x) * parentHeight / mountain.getMaxHeight() + 1;
            p = new Point(PADDING + x * parentWidth / mountain.getWidth(), random.nextInt(maxHeight));
            if (p.y > parentHeight + PADDING_TOP + 50 * scaleFactor - y && p.y < parentHeight + PADDING) {
                good = true;
            }
            for (Tree other : others) {
                if (Math.abs(other.getPoint().x - p.x) < 20 * scaleFactor && Math.abs(other.getPoint().y - p.y) < 40 * scaleFactor) {
                    good = false;
                }
            }
        }
        leafDrawable = context.getDrawable(R.drawable.treetop).getConstantState().newDrawable().mutate();
        int green = random.nextInt(70) + 60;
        int leafColor = Color.rgb(green + random.nextInt(40) - 60, green, random.nextInt(10) + 10);
        leafDrawable.setColorFilter(new PorterDuffColorFilter(leafColor, PorterDuff.Mode.SRC_ATOP));
        leafDrawable.setBounds(p.x - (int) (50*scaleFactor), p.y - (int)(100*scaleFactor), p.x + (int)(50*scaleFactor), p.y);

        trunkDrawable = context.getDrawable(R.drawable.treetrunk).getConstantState().newDrawable().mutate();
        int trunkColor = Color.rgb(120 + random.nextInt(20), 50 + random.nextInt(20), random.nextInt(10));
        trunkDrawable.setColorFilter(new PorterDuffColorFilter(trunkColor, PorterDuff.Mode.SRC_ATOP));
        trunkDrawable.setBounds(p.x - (int) (50*scaleFactor), p.y - (int)(100*scaleFactor), p.x + (int)(50*scaleFactor), p.y);
    }

    public void draw(Canvas canvas){
        trunkDrawable.draw(canvas);
        leafDrawable.draw(canvas);
    }

    public Point getPoint(){
        return p;
    }
}
