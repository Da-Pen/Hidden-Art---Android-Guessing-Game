package me.dpeng.clickdots;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Daniel Peng on 7/22/2018.
 *
 */

public class GameView extends SurfaceView {

    private Bitmap srcBmp;
    private Bitmap bmp;
    private SurfaceHolder holder;
    private Paint paint = new Paint();
    private Path path = new Path();
    private int screenWidth;
    private int screenHeight;
    GameThread gameThread;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        // get screen dimensions
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        bmp = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        gameThread = new GameThread(this);

        //add SurfaceHolder
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("WrongCall")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                gameThread.setRunning(true);
                gameThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                gameThread.setRunning(false);
                while (retry) {
                    try {
                        gameThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                path.moveTo(touchX, touchY);
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
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        canvas.drawCircle(screenWidth/2, screenHeight/2, screenWidth/2, paint);
        //canvas.drawBitmap(srcBmp, 0, 0, paint);
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
}
