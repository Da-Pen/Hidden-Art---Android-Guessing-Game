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
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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

    private GameActivity gameActivity;

    // TOUCH VARIABLES
    private float lastTouchX = -1;
    private float lastTouchY = -1;
    // whether or not the user panned the image
    private boolean moved;
    private static final int INVALID_POINTER_ID = -1;
    // stores the active pointer (finger touch)
    private int activePointerID = INVALID_POINTER_ID;
    // the position of the game view (default: 0, 0)
    private float translateX;
    private float translateY;
    // pinch zoom variables
    private float scaleFactor = 1.0f;
    public final static float MIN_ZOOM = 1.0f;
    public final static float MAX_ZOOM = 5.0f;


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
        init(constraintLayout, srcBmp, context);
    }


    private void init(ConstraintLayout constraintLayout, Bitmap srcBmp, Context context) {

        gameActivity = (GameActivity) context;

        // get screen dimensions
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        gameHeight = dm.widthPixels - GameActivity.SIDE_MARGIN*2;

        // scale bitmaps
        this.srcBmp = Bitmap.createScaledBitmap(srcBmp, gameHeight / RESOLUTION_RATIO, gameHeight / RESOLUTION_RATIO, false);
        this.screenSizeBmp = Bitmap.createScaledBitmap(srcBmp, gameHeight, gameHeight, true);

        this.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams mParams = GameView.this.getLayoutParams();
                mParams.height = GameView.this.getWidth();
                GameView.this.setLayoutParams(mParams);
                GameView.this.postInvalidate();
            }
        });
//
//        // initialize scale detector
//        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        paint.setStyle(Paint.Style.FILL);

        resetGame();


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //the scale gesture detector should inspect all the touch events
        // mScaleDetector.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                //get x and y cords of where we touch the screen
                final float x = event.getX();
                final float y = event.getY();

                //remember where touch event started
                lastTouchX = x;
                lastTouchY = y;
                moved = false;

                //save the ID of this pointer
                activePointerID = event.getPointerId(0);


                break;
            }
            case MotionEvent.ACTION_MOVE: {

                moved = true;

                //find the index of the active pointer and fetch its position
                final int pointerIndex = event.findPointerIndex(activePointerID);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

               // if (!mScaleDetector.isInProgress()) {
                //calculate the distance in x and y directions
                final float distanceX = x - lastTouchX;
                final float distanceY = y - lastTouchY;

                translateX += distanceX;
                translateY += distanceY;

                //redraw canvas (call onDraw method)
                invalidate();

               // }
                //remember this touch position for next move event
                lastTouchX = x;
                lastTouchY = y;

                break;
            }
            case MotionEvent.ACTION_UP:
                if(!moved) {
                    splitSelected(touchX, touchY, false);
                }
//                lastTouchX = -1;
//                lastTouchY = -1;
                activePointerID = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_CANCEL: {
                activePointerID = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: { // idk what most of this does, took it from http://www.northborder-software.com/pinchzoom_and_pan_4.html
                //Extract the index of the pointer that left the screen
                final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerID) {
                    //Our active pointer is going up Choose another active pointer and adjust
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerID = event.getPointerId(newPointerIndex);
                }
                break;
            }


        }

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);


        if(!revealed) {
            canvas.save();
            canvas.scale(scaleFactor, scaleFactor);
            canvas.translate(translateX, translateY);

            for (Dot d : dots) {
                d.draw(canvas, paint, squareMode);
            }
            canvas.restore();
        } else {
            canvas.save();
            canvas.translate(translateX, translateY);
            canvas.scale(scaleFactor, scaleFactor);
            canvas.drawBitmap(screenSizeBmp, 0, 0, null);
            canvas.restore();
        }



    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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

    /**
     * Inserts a Dot into dots. Requires: dots is sorted.
     * @param dot the Dot to insert
     * @param index a hint for where the new dot should be placed. This makes it slightly faster.
     */
    private void insertDotSorted(Dot dot, int index) {
        int len = dots.size();
        if(len == 0) {
            dots.add(dot);
            return;
        }
        while(true) {
            // if dot < dots[index]
            if(dot.compareTo(dots.get(index)) < 0) {
                if(index == 0 || dot.compareTo(dots.get(index - 1)) > 0) {
                    dots.add(index, dot);
                    return;
                } else {
                    index--;
                }
            } else { // if dot > dots[index]
                if(index == len - 1 || dot.compareTo(dots.get(index + 1)) < 0) {
                    dots.add(index + 1, dot);
                    return;
                } else {
                    index++;
                }
            }
        }
    }

    // splits a dot
    // index is not used for now but may be used later
    private void splitDot(Dot dot, int index) {
        if(dot.getDiameter() / 2 > MIN_DIAMETER) {
            // REMOVE THE OLD DOT
            dots.remove(index);
            // make sure index is a valid index in dots
            index = Math.max(0, Math.min(dots.size() - 1, index));
            // SPLIT THE DOT INTO FOUR DOTS
            // Top left dot
            int rad = (int) Math.round(((float) dot.getDiameter()) / 2.0);
            int c = getAvgColor(dot.getX(), dot.getY(), dot.getX() + rad, dot.getY() + rad);
            //dots.add(new Dot(dot.getX(), dot.getY(), rad, c));
            insertDotSorted(new Dot(dot.getX(), dot.getY(), rad, c), index);
            // Top right dot
            c = getAvgColor(dot.getX() + rad, dot.getY(), dot.getX() + dot.getDiameter(), dot.getY() + rad);
            //dots.add(new Dot(dot.getX() + rad, dot.getY(), rad, c));
            insertDotSorted(new Dot(dot.getX() + rad, dot.getY(), rad, c), index);
            // Bottom left dot
            c = getAvgColor(dot.getX(), dot.getY() + rad, dot.getX() + rad, dot.getY() + dot.getDiameter());
            //dots.add(new Dot(dot.getX(), dot.getY() + rad, rad, c));
            insertDotSorted(new Dot(dot.getX(), dot.getY() + rad, rad, c), index);
            // Bottom right dot
            c = getAvgColor(dot.getX() + rad, dot.getY() + rad, dot.getX() + dot.getDiameter(), dot.getY() + dot.getDiameter());
            //dots.add(new Dot(dot.getX() + rad, dot.getY() + rad, rad, c));
            insertDotSorted(new Dot(dot.getX() + rad, dot.getY() + rad, rad, c), index);
        }
    }

    // splits the dot selected by the user
    private void splitSelected(float touchX, float touchY, boolean moving) {

        int len = dots.size();

        //use binary search to find selected dot
//        int checkIndex = len/2;
//        while(!dots.get(checkIndex).inside(touchX, touchY)) {
//            if(dots.get(checkIndex).getY())
//        }




        for(int i = 0; i < len; ++i) {
            Dot dot = dots.get(i);
            // if the user pressed in that dot's bounding square
            if(dot.inside(touchX, touchY)) {
//                if(moving) {
//                    // if the user did not "move into" this Dot (i.e their finger was already
//                    // on the Dot) then do not split it.
//                    if(dot.inside(lastTouchX, lastTouchY)) {
//                        return;
//                    }
//                }
                splitDot(dot, i);
                return;
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
        translateX = 0;
        translateY = 0;
        scaleFactor = 1;
        gameActivity.resetZoomSlider();
        invalidate();

    }


    public void scale(int scaleFactor) {
        this.scaleFactor = (MAX_ZOOM - MIN_ZOOM)*(((float)scaleFactor)/100) + MIN_ZOOM;

        //translate the image so that it zooms towards the center


        invalidate();
    }





//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        @Override
//        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
//
//            scaleFactor *= scaleGestureDetector.getScaleFactor();
//            //don't to let the image get too large or small
//            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
//
//            invalidate();
//
//            return true;
//        }
//    }
}
