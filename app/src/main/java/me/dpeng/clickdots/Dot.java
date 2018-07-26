package me.dpeng.clickdots;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

/**
 * Created by Daniel Peng on 7/22/2018.
 *
 */

public class Dot implements Comparable<Dot>{
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

    public void draw(Canvas canvas, Paint paint, boolean squareMode) {

        paint.setColor(this.color);

        if(squareMode) {
            canvas.drawRect(x, y, x + diameter, y + diameter, paint);
        } else {
            canvas.drawCircle(x + diameter / 2, y + diameter / 2, diameter / 2, paint);
        }

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

    /**
     * Returns true if position x, y is within the bounding box of this Dot, and false otherwise
     * @param touchX
     * @param touchY
     * @return
     */
    public boolean inside(float touchX, float touchY) {
        return touchX > x && touchX < x + diameter &&
                touchY > y && touchY < y + diameter;
    }

    // sorts based on y position. If they are the same, sorts based on x position.
    @Override
    public int compareTo(@NonNull Dot o) {
        if(this.y > o.getY()) {
            return 1;
        } else if (this.y < o.getY()) {
            return -1;
        } else {
            return this.x > o.getX() ? 1 : (this.x < o.getX() ? -1 : 0);
        }
    }
}
