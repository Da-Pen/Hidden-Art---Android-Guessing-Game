package me.dpeng.clickdots;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Created by Daniel Peng on 7/22/2018.
 *
 */

public class GameView extends View {

    // since the source image is too large to calculate the average color of, we instead load
    // a compressed version of it (1/RESOLUTION_RATIO of the size) and use that to do calculations
    public final static int RESOLUTION_RATIO = 10;
    // minimum diameter for a dot (in pixels). Once the dots are about to become smaller than this,
    // clicking on the dots does nothing.
    public final static int MIN_DIAMETER = 10;

    // in square mode, squares are displayed instead of circles
    private boolean squareMode = false;
    // whether or not the user has revealed the source image
    private boolean revealed = false;
    // a bitmap of the source image that has dimensions equal to the screen width (i.e width x width)
    private Bitmap screenSizeBmp;
    // the source image except scaled down to 1/RESOLUTION_RATIOth the size of screenSizeBmp. This
    // is used to calculate average colors.
    private Bitmap srcBmp;
    private Paint paint = new Paint();
    // height of this view, in px
    private int gameHeight;

    private float lastTouchX = -1;
    private float lastTouchY = -1;

    // list of all the dots
    ArrayList<Dot> dots;

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public GameView(Context context, ConstraintLayout constraintLayout, Bitmap srcBmp) {
        this(context);
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        this.srcBmp = Bitmap.createScaledBitmap(srcBmp, width / RESOLUTION_RATIO, width / RESOLUTION_RATIO, false);
        this.screenSizeBmp = Bitmap.createScaledBitmap(srcBmp, width, width, false);
        init(constraintLayout);
    }


    private void init(ConstraintLayout constraintLayout) {
        // get screen dimensions
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        gameHeight = dm.widthPixels;

        // Set the height of this View to be equal to its width
        final GameView thisView = this;
        this.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams mParams = thisView.getLayoutParams();
                mParams.height = thisView.getWidth();
                thisView.setLayoutParams(mParams);
                thisView.postInvalidate();
            }
        });


        paint.setStyle(Paint.Style.FILL);

        resetGame();


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                splitSelected(touchX, touchY, false);
                break;
            case MotionEvent.ACTION_MOVE:
                splitSelected(touchX, touchY, true);
                break;
            case MotionEvent.ACTION_UP:
                lastTouchX = -1;
                lastTouchY = -1;
                break;
        }
        lastTouchX = touchX;
        lastTouchY = touchY;

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!revealed) {
            for (Dot d : dots) {
                d.draw(canvas, paint, squareMode);
            }
        } else {
            canvas.drawBitmap(screenSizeBmp, 0, 0, null);
        }



    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public Bitmap getSrcBmp() {
        return srcBmp;
    }

    public void setSrcBmp(Bitmap srcBmp) {
        this.srcBmp = srcBmp;
    }

    // Gets the average color of the rectangle defined by the parameters (in pixels, inclusive) of the srcBmp
    private int getAvgColor(int startX, int startY, int endX, int endY) {

        int totalPixels = (endY - startY)*(endX - startX);


        int totalDots = 0;
        startX /= RESOLUTION_RATIO;
        startY /= RESOLUTION_RATIO;
        endX /= RESOLUTION_RATIO;
        endY /= RESOLUTION_RATIO;
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        for(int x = startX; x < endX; ++x) {
            for(int y = startY; y < endY; ++y) {
                totalDots++;
                int pix = srcBmp.getPixel(x, y);
                sumR += Color.red(pix);
                sumG += Color.green(pix);
                sumB += Color.blue(pix);
            }
        }

        if(totalDots == 0) {
            return -1;
        }

        return Color.rgb(Math.min(sumR/totalDots, 255), Math.min(sumG/totalDots, 255), Math.min(sumB/totalDots, 255));
    }

    // index is not used for now but may be used later
    private void splitDot(Dot dot, int index) {
        if(dot.getDiameter() / 2 > MIN_DIAMETER) {
            // REMOVE THE OLD DOT
            dots.remove(index);
            // SPLIT THE DOT INTO FOUR DOTS
            // Top left dot
            int rad = (int) Math.round(((float) dot.getDiameter()) / 2.0);
            int c = getAvgColor(dot.getX(), dot.getY(), dot.getX() + rad, dot.getY() + rad);
            dots.add(new Dot(dot.getX(), dot.getY(), rad, c));

            // Top right dot
            c = getAvgColor(dot.getX() + rad, dot.getY(), dot.getX() + dot.getDiameter(), dot.getY() + rad);
            dots.add(new Dot(dot.getX() + rad, dot.getY(), rad, c));
            // Bottom left dot
            c = getAvgColor(dot.getX(), dot.getY() + rad, dot.getX() + rad, dot.getY() + dot.getDiameter());
            dots.add(new Dot(dot.getX(), dot.getY() + rad, rad, c));
            // Bottom right dot
            c = getAvgColor(dot.getX() + rad, dot.getY() + rad, dot.getX() + dot.getDiameter(), dot.getY() + dot.getDiameter());
            dots.add(new Dot(dot.getX() + rad, dot.getY() + rad, rad, c));
        }
    }

    private void splitSelected(float touchX, float touchY, boolean moving) {

        int len = dots.size();
        for(int i = 0; i < len; ++i) {
            Dot dot = dots.get(i);
            // if the user pressed in that dot's bounding square
            if(dot.inside(touchX, touchY)) {
                if(moving) {
                    // if the user did not "move into" this Dot (i.e their finger was already
                    // on the Dot) then do not split it.
                    if(dot.inside(lastTouchX, lastTouchY)) {
                        return;
                    }
                }
                splitDot(dot, i);
                break;
            }
        }
    }

    public void splitAll() {
        int i = 0;
        while(i < dots.size()) {
            while (dots.get(i).getDiameter() / 2 > MIN_DIAMETER) { //while splittable
                splitDot(dots.get(i), i);
            }
            i++;
        }
        invalidate();
    }

    public void revealOrHideSourceImage() {
        revealed = !revealed;
        invalidate();
    }

    public void resetGame() {
        paint.setColor(getResources().getColor(R.color.game_background));
        dots = new ArrayList<>();
        int avgColor = getAvgColor(0, 0, gameHeight, gameHeight);
        // add first dot
        dots.add(new Dot(0, 0, gameHeight, avgColor));
        invalidate();

    }

}
