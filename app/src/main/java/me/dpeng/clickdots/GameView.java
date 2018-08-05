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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Daniel Peng on 7/22/2018.
 *
 */

public class GameView extends View {


    private GameActivity gameActivity;





    private Paint paint = new Paint();


    ///=== SCREEN & BITMAP VARIABLES ===///
    // includes the URL of the image and valid guesses in the format:
    // imageURL,validGuess1,validGuess2, ...
    private String imageInfo;
    // a bitmap of the source image that has dimensions equal to the screen width (i.e width x width)
    private Bitmap screenSizeBmp;
    // the source image except scaled down to 1/RESOLUTION_RATIOth the size of screenSizeBmp. This
    // is used to calculate average colors.
    private Bitmap srcBmp;
    // private Bitmap loadingBmp;
    // height of this view, in px
    private int gameHeight;
    // since the source image is too large to calculate the average color of, we instead load
    // a compressed version of it (1/RESOLUTION_RATIO of the size) and use that to do calculations
    public final static int RESOLUTION_RATIO = 10;
    // URL of the source image
    private String imageURL;
    // true if the image has finished loading
    private boolean loaded = false;
    // boolean used to check if it is the first time this view has been measured.
    // it is used to scale height of the view to be the same as the width (since we only
    // need to scale it once)
    private boolean firstMeasure = true;
    ///=== END SCREEN & BITMAP VARIABLES ===///


    ///=== GAME LOGIC VARIABLES ===///

    // minimum diameter for a dot (in pixels). Once the dots are about to become smaller than this,
    // clicking on the dots does nothing.
    public final static int MIN_DIAMETER = 10;
    // in square mode, squares are displayed instead of circles
    private boolean squareMode = false;
    // whether or not the user has sourceRevealed the source image
    private boolean sourceRevealed = false;
    private int lastTouchX;
    private int lastTouchY;
    private int numClicks = 0;
    private boolean isLoading;
    private String[] validGuesses;
    ArrayList<Dot> dots; // list of all the dots
    final private static int START_GUESSES = 3; // the total # of guesses that the user gets
    private int guessesLeft;

    ///=== END GAME LOGIC VARIABLES ===///



    ///=== CONSTRUCTORS ===///

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public GameView(Context context, ConstraintLayout constraintLayout) {
        this(context);

        init(constraintLayout, context);
    }


    // initialises the variables needed for the game to start
    private void init(ConstraintLayout constraintLayout, Context context) {

        gameActivity = (GameActivity) context;

        // create a thread to get the URL for the image that we want to use
        Thread getImgURLThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // get a list of the image urls from Firebase
                try {
                    URL url = new URL("https://firebasestorage.googleapis.com/v0/b/clickdots-1a597.appspot.com/o/imageSourceURLs.txt?alt=media&token=e766a225-99cf-4186-a679-ff97f7601bfe");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(30000); // time out in 30 sec
                    try {
                        BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String currLine;
                        ArrayList<String> items = new ArrayList<>(); // each item is a string containing
                        // the URL plus a list of valid guesses
                        while (true) {
                            currLine = bf.readLine();
                            if (currLine != null) items.add(currLine);
                            else break;
                        }

                        int choiceIndex = Utilities.randRange(1, items.size()) - 1;

                        // choiceIndex = 7; // use for testing (i.e use the car image)


                        imageInfo = items.get(choiceIndex);
                        bf.close();

                        gameActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onImageLoaded();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        getImgURLThread.start();



        isLoading = true;

        // get screen dimensions
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        gameHeight = dm.widthPixels - GameActivity.SIDE_MARGIN*2;


    }

    private void onImageLoaded() {
        // GET THE IMAGE
        // imageInfo is of the format:
        // url,valid Guess 1,valid Guess 2, ...
        // we need to split it to store the real url and the valid guesses.
        String[] splitURLandGuesses = imageInfo.split(",");
        imageURL = splitURLandGuesses[0];


        validGuesses = Arrays.copyOfRange(splitURLandGuesses, 1, splitURLandGuesses.length);


        Glide.with(GameView.this).asBitmap().load(imageURL).apply(new
                RequestOptions().override(gameHeight, gameHeight)).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                // once the image is ready then start the game.
                srcBmp = Bitmap.createScaledBitmap(resource, gameHeight/RESOLUTION_RATIO,
                        gameHeight/RESOLUTION_RATIO, false);
                screenSizeBmp = resource;
                isLoading = false;
                resetGame();

            }

        });



        //        // load the loading image
        //        loadingBmp = Bitmap.createScaledBitmap(
        //                BitmapFactory.decodeResource(context.getResources(), R.drawable.loading),
        //                gameHeight, gameHeight, false);


        paint.setStyle(Paint.Style.FILL);
        loaded = true;
        // once we have initialised the variables then we can draw the canvas
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(isLoading) {
            return false;
        }

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                final float y = event.getY();
                // split the dot at the location (x, y)
                splitSelected(x, y, false);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX();
                final float y = event.getY();
                // split the dot at the location (x, y)
                splitSelected(x, y, true);
                break;
            }
            default: {
                return false;
            }
        }

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //numClicks++;
        if(isLoading) {
            // if loading then draw the loading bitmap
            // canvas.drawBitmap(loadingBmp, 0, 0, null);
            // if loading then draw three dots to show loading
            drawLoadingDots(canvas);

        } else if(sourceRevealed) {
            // if the user won or gave up then show the source image
            canvas.drawBitmap(screenSizeBmp, 0, 0, paint);

        } else {
            // otherwise draw all the dots
            for (Dot d : dots) {
                d.draw(canvas, paint, squareMode);
            }
        }

    }

    private void drawLoadingDots(Canvas canvas) {
        int radius = 30; // radius of loading dots
        int color = Color.GRAY;
        int y = gameHeight/2 - radius;
        int x1 = gameHeight/4 - radius;
        int x2 = gameHeight/2 - radius;
        int x3 = (gameHeight*3)/4 - radius;

        Dot d1 = new Dot(x1, y, radius*2, color);
        Dot d2 = new Dot(x2, y, radius*2, color);
        Dot d3 = new Dot(x3, y, radius*2, color);
        d1.draw(canvas, paint, squareMode);
        d2.draw(canvas, paint, squareMode);
        d3.draw(canvas, paint, squareMode);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(!firstMeasure) { // we only need to resize the view once
            return;
        }
        firstMeasure = false;
        // make the height of this view the same as its width (i.e make it square)
        this.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams mParams = GameView.this.getLayoutParams();
                mParams.height = GameView.this.getWidth();
                GameView.this.setLayoutParams(mParams);
                // GameView.this.postInvalidate();
            }
        });

    }

    // Gets the average color of the rectangle defined by the parameters (in pixels, inclusive) of the srcBmp
    private int getAvgColor(int startX, int startY, int endX, int endY) {

        int totalPixels = 0;
        startX /= RESOLUTION_RATIO;
        startY /= RESOLUTION_RATIO;
        endX /= RESOLUTION_RATIO;
        endY /= RESOLUTION_RATIO;
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        for(int x = startX; x < endX; ++x) {
            for(int y = startY; y < endY; ++y) {
                totalPixels++;
                int pix = srcBmp.getPixel(x, y);
                sumR += Color.red(pix);
                sumG += Color.green(pix);
                sumB += Color.blue(pix);
            }
        }

        if(totalPixels == 0) {
            return -1;
        }

        return Color.rgb(Math.min(sumR/totalPixels, 255), Math.min(sumG/totalPixels, 255), Math.min(sumB/totalPixels, 255));
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

    // numClicks a dot
    // index is not used for now but may be used later
    private void splitDot(Dot dot, int index) {
        // only split it if the resulting dots' diameters will be larger than MIN_DIAMETER
        if(dot.getDiameter() / 2 > MIN_DIAMETER) {
            numClicks++;
            gameActivity.setNumclicks(numClicks);
            // REMOVE THE OLD DOT
            dots.remove(index);
            // make sure index is a valid index in dots
            index = Math.max(0, Math.min(dots.size() - 1, index));
            // SPLIT THE DOT INTO FOUR DOTS
            // Top left dot
            int rad = (int) Math.round(((float) dot.getDiameter()) / 2.0);
            int c = getAvgColor(dot.getX(), dot.getY(), dot.getX() + rad, dot.getY() + rad);
            //dots.add(new Dot(dot.getX(), dot.getY(), rad, c));
            dots.add(new Dot(dot.getX(), dot.getY(), rad, c));
            // Top right dot
            c = getAvgColor(dot.getX() + rad, dot.getY(), dot.getX() + dot.getDiameter(), dot.getY() + rad);
            //dots.add(new Dot(dot.getX() + rad, dot.getY(), rad, c));
            dots.add(new Dot(dot.getX() + rad, dot.getY(), rad, c));
            // Bottom left dot
            c = getAvgColor(dot.getX(), dot.getY() + rad, dot.getX() + rad, dot.getY() + dot.getDiameter());
            //dots.add(new Dot(dot.getX(), dot.getY() + rad, rad, c));
            dots.add(new Dot(dot.getX(), dot.getY() + rad, rad, c));
            // Bottom right dot
            c = getAvgColor(dot.getX() + rad, dot.getY() + rad, dot.getX() + dot.getDiameter(), dot.getY() + dot.getDiameter());
            //dots.add(new Dot(dot.getX() + rad, dot.getY() + rad, rad, c));
            dots.add(new Dot(dot.getX() + rad, dot.getY() + rad, rad, c));
        }
    }

    // splits the dot selected by the user
    private void splitSelected(float touchX, float touchY, boolean moving) {

        int len = dots.size();

        for(int i = 0; i < len; ++i) {
            Dot dot = dots.get(i);
            // if the user pressed in that dot's bounding square
            if(dot.isTouchInside(touchX, touchY)) {
                if(moving) {
                    // if the user did not "move into" this Dot (i.e their finger was already
                    // on the Dot) then do not split it.
                    if(dot.isTouchInside(lastTouchX, lastTouchY)) {
                        return;
                    }
                }
                splitDot(dot, i);
                lastTouchX = (int)touchX;
                lastTouchY = (int)touchY;
                return;
            }
        }

    }

    // splits all the dots
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
        sourceRevealed = !sourceRevealed;
        invalidate();
    }

    public void resetGame() {
        numClicks = 0;
        guessesLeft = START_GUESSES;
        dots = new ArrayList<>();
        int avgColor = getAvgColor(0, 0, gameHeight, gameHeight);
        // add first dot
        dots.add(new Dot(0, 0, gameHeight, avgColor));
        invalidate();

    }

    // run when the user makes a guess
    public void guess(String guess) {
        // make sure the image has already loaded
        if(!loaded) {
            // show toast to notify the user that the image has not loaded yet
            Toast toast = Toast.makeText(gameActivity,
                    R.string.please_wait_load, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        guess = guess.toLowerCase().trim();
        guessesLeft--;
        if(guessesLeft <= 0) {
            gameOver();
        }
        for(String validGuess: validGuesses) {
            if(guess.equals(validGuess)) {
                win();
                return;
            }
        }
        String toastText = "INCORRECT: " + guessesLeft;
        if(guessesLeft == 1) toastText += " guess left!";
        else toastText += " guesses left!";
        Toast toast = Toast.makeText(gameActivity, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void gameOver() {
    }

    private void win() {
        // show toast to let them know they won
        Toast toast = Toast.makeText(gameActivity, "CORRECT!", Toast.LENGTH_SHORT);
        toast.show();
        sourceRevealed = true;

        invalidate();
    }

}
