package me.dpeng.clickdots;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Daniel Peng on 7/22/2018.
 *
 */

public class Dot {
    private int x;
    private int y;
    private int diameter;
    private int color;

    public Dot(int x, int y, int diameter, int color) {
        this.x = x;
        this.y = y;
        this.diameter = diameter;
        this.color = color;
    }

    public void draw(Canvas canvas, Paint paint) {

        paint.setColor(this.color);
        canvas.drawCircle(x + diameter/2, y + diameter/2, diameter/2, paint);
        //canvas.drawRect(x, y, x + diameter, y + diameter, paint);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDiameter() {
        return diameter;
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
