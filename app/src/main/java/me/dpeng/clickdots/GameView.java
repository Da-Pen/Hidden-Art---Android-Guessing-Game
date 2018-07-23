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

/**
 * Created by Daniel Peng on 7/22/2018.
 *
 */

public class GameView extends View {

    public final static int RESOLUTION_RATIO = 8;
    public final static int MIN_DIAMETER = 20; //minimum diameter for a dot (in pixels)


    private boolean revealed = false;
    private Bitmap screenSizeBmp;
    private Bitmap srcBmp;
    private Bitmap gameBmp;
    private Paint paint = new Paint();
    private int gameHeight; //height of main game view, in px
    private int resWidth; // resWidth = screenWidth / RESOULTION_RATIO
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


        resWidth = gameHeight / RESOLUTION_RATIO;


        gameBmp = Bitmap.createBitmap(gameHeight, gameHeight, Bitmap.Config.ARGB_8888);
        paint.setStyle(Paint.Style.FILL);

        // create the first dot
        dots = new ArrayList<Dot>();
        int avgColor = getAvgColor(0, 0, gameHeight, gameHeight);
        // add first dot
        dots.add(new Dot(0, 0, gameHeight, avgColor));


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if the user selected a point outside of the canvas then ignore it
                if(touchY < 0 || touchY > gameHeight) {
                    break;
                }

                Iterator<Dot> it = dots.iterator();
                while(it.hasNext()) {
                    Dot dot = it.next();
                    // if the user pressed in that dot's bounding square
                    if(touchX > dot.getX() && touchX < dot.getX() + dot.getDiameter() &&
                            touchY > dot.getY() && touchY < dot.getY() + dot.getDiameter()) {
                        if(dot.getDiameter() > MIN_DIAMETER) {
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
                            dots.remove(dot);
                            break;
                        }
                    }
                }


                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!revealed) {
            for (Dot d : dots) {
                d.draw(canvas, paint);
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


        int totalDots = totalPixels / (RESOLUTION_RATIO*RESOLUTION_RATIO);

        if(totalDots == 0) {
            return -1;
        }

        startX /= RESOLUTION_RATIO;
        startY /= RESOLUTION_RATIO;
        endX /= RESOLUTION_RATIO;
        endY /= RESOLUTION_RATIO;
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        for(int x = startX; x < endX; ++x) {
            for(int y = startY; y < endY; ++y) {
                int pix = srcBmp.getPixel(x, y);
                sumR += Color.red(pix);
                sumG += Color.green(pix);
                sumB += Color.blue(pix);
            }
        }



        return Color.rgb(Math.min(sumR/totalDots, 255), Math.min(sumG/totalDots, 255), Math.min(sumB/totalDots, 255));
    }

}
