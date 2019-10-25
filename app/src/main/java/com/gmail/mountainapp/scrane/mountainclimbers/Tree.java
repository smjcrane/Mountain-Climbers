package com.gmail.mountainapp.scrane.mountainclimbers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.util.List;
import java.util.Random;

import static com.gmail.mountainapp.scrane.mountainclimbers.MountainView.PADDING;
import static com.gmail.mountainapp.scrane.mountainclimbers.MountainView.PADDING_TOP;

public class Tree {
    private Path path;
    private Paint paint;
    private Point p;

    public Tree(Random random, int maxWidth, int maxHeight, Mountain mountain, List<Tree> others){
        boolean good = false;
        int parentHeight = maxHeight - PADDING - PADDING_TOP;
        int parentWidth = maxWidth - 2 * PADDING;
        while(!good){
            int x = random.nextInt(mountain.getWidth());
            int y = mountain.getHeightAt(x) * parentHeight / mountain.getMaxHeight() + 1;
            p = new Point(PADDING + x * parentWidth / mountain.getWidth(), random.nextInt(maxHeight));
            if (p.y > parentHeight + PADDING_TOP + 50 - y && p.y < parentHeight + PADDING){
                good = true;
            }
            for (Tree other : others){
                if (Math.abs(other.getPoint().x - p.x) < 30 && Math.abs(other.getPoint().y - p.y) < 80){
                    good = false;
                }
            }
        }
        paint = new Paint();
        paint.setColor(Color.rgb(random.nextInt(120), random.nextInt(70) + 60, random.nextInt(10) + 10));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(p.x - 5, p.y);
        path.lineTo(p.x + 5, p.y);
        path.lineTo(p.x + 3, p.y - 20);
        int numBranches = random.nextInt(2) + 3;
        for (int i = 1; i <= numBranches; i++) {
            path.lineTo(p.x + 25 - random.nextInt(5) - 2 * i, p.y - 15 * i + random.nextInt(4));
            path.lineTo(p.x + 3, p.y - 15 * i - 15);
        }
        for (int i = numBranches; i > 0; i--){
            path.lineTo(p.x - 3, p.y - 15 * i - 15);
            path.lineTo(p.x - 25 + random.nextInt(5) + 2 * i, p.y - 15 * i + random.nextInt(4));
        }
        path.lineTo(p.x - 3, p.y - 20);
        path.close();    }

    public void draw(Canvas canvas){
        canvas.drawPath(path, paint);
    }

    public Point getPoint(){
        return p;
    }
}
