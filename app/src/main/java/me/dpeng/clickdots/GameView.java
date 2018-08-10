package me.dpeng.clickdots;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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
 * The View for the game
 */
public class GameView extends View {

    // the "parent" activity
    private GameActivity gameActivity;
    

    ///=== SCREEN & BITMAP VARIABLES ===///
    private Paint paint = new Paint();
    // a list of all the image options. We keep the entire list because so that when the user
    // clicks "next" after completing a game then we do not have to connect to Dropbox again
    private ArrayList<String> imageOptions;
    private int choiceIndex; // stores the index of imageOptions that we are using for the current game
    // an online txt file containing information for where to find the images
    private final String IMAGE_SOURCES_TXT = "https://dl.dropboxusercontent.com/s/h4ayiu9tlaz6xyh/imageSourceURLs.txt?dl=0";
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
    private int viewDiameter;
    // since the source image is too large to calculate the average color of, we instead load
    // a compressed version of it (1/RESOLUTION_RATIO of the size) and use that to do calculations
    public final int RESOLUTION_RATIO = 10;
    // URL of the source image
    private String imageURL;
    // boolean used to check if it is the first time this view has been measured.
    // it is used to scale height of the view to be the same as the width (since we only
    // need to scale it once)
    private boolean firstMeasure = true;

    private Bitmap gameBmp;
    private Canvas gameCanvas;

    ///=== END SCREEN & BITMAP VARIABLES ===///


    ///=== GAME LOGIC VARIABLES ===///

    // The maximum number of clicks before the dot cannot be clicked anymore. I.e there is a
    // maximum of 4^MAXCLICKS dots on the screen.
    private final static int MAX_CLICKS = 6;
    // the smallest diameter for a dot.
    // SMALLEST_DIAMETER = Math.max(3, (int)Math.floor(viewDiameter / 2^MAX_CLICKS)));
    // It is calculated when the game is initialised.
    private int SMALLEST_DIAMETER;
    // whether or not the user has sourceRevealed the source image
    public boolean sourceRevealed = false;
    private int lastTouchX;
    private int lastTouchY;
    private int numClicks = 0; // tracks the number of clicks the user has made (i.e their score)
    private boolean isLoading = true;
    private String[] validGuesses; // a list of all guesses that are considered correct
    ArrayList<Dot> dots; // list of all the dots
    final private static int START_GUESSES = 3; // the total # of guesses that the user gets
    private int guessesLeft;
    // used to track how many clicks in a row (and not drags) the user has done. If they have
    // clicked many times, show a Toast with a hint telling them that they can drag their finger
    private int clicksInARow;
    private static final int CLICKS_IN_A_ROW_BEFORE_HINT = 15; // # of clicks before showing drag hint
    private boolean connectionFailed;
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
        init(context);
    }


    // initialises the variables needed for the game to start
    private void init(Context context) {
        connectionFailed = false;
        gameActivity = (GameActivity) context;

        // get the SharedPreferences to check if squareMode is on
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(gameActivity);
        Utilities.isSquareMode = sharedPreferences.getBoolean(Utilities.KEY_IS_SQUARE_MODE, false);

        // create a thread to get the URL for the image that we want to use
        Thread getImgURLThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // get a list of the image urls from Dropbox
                try {
                    URL url = new URL(IMAGE_SOURCES_TXT);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(20000); // time out in 20 sec
                    try {
                        BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String currLine;
                        imageOptions = new ArrayList<>(); // each item is a string containing
                        // the URL plus a list of valid guesses
                        while (true) {
                            currLine = bf.readLine();
                            if (currLine != null) imageOptions.add(currLine);
                            else break;
                        }

                        bf.close();

                        gameActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onImageURLLoaded();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        // if the connection failed then let the user know
                        gameActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectionFailed = true;
                                invalidate();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // if the connection failed then let the user know
                    connectionFailed = true;
                    gameActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectionFailed = true;
                            invalidate();
                        }
                    });
                }
            }
        });

        getImgURLThread.start();

        isLoading = true;

        // get screen dimensions
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        viewDiameter = dm.widthPixels - GameActivity.SIDE_MARGIN*2;

        // calculate the smallest diameter for a Dot before it is unsplittable
        SMALLEST_DIAMETER = Math.max(3, (int)Math.floor(viewDiameter / Math.pow(2, MAX_CLICKS)));


        gameBmp = Bitmap.createBitmap(viewDiameter, viewDiameter, Bitmap.Config.ARGB_8888);
        gameCanvas = new Canvas(gameBmp);


    }

    // gets the valid guesses, loads the image into the source Bitmap, and starts the game
    public void onImageURLLoaded() {
        isLoading = true;
        sourceRevealed = false;
        invalidate();
        // choose a random image from the list of images
        choiceIndex = Utilities.randRange(1, imageOptions.size()) - 1;
        imageInfo = imageOptions.get(choiceIndex);
        // GET THE IMAGE
        // imageInfo is of the format:
        // url,valid Guess 1,valid Guess 2, ...
        // we need to split it to store the real url and the valid guesses.
        String[] splitURLandGuesses = imageInfo.split(",");
        imageURL = splitURLandGuesses[0];

        validGuesses = Arrays.copyOfRange(splitURLandGuesses, 1, splitURLandGuesses.length);


        Glide.with(GameView.this).asBitmap().load(imageURL).apply(new
                RequestOptions().override(viewDiameter, viewDiameter)).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                // once the image is ready then start the game.
                srcBmp = Bitmap.createScaledBitmap(resource, viewDiameter /RESOLUTION_RATIO,
                        viewDiameter /RESOLUTION_RATIO, false);
                screenSizeBmp = resource;
                isLoading = false;
                onImageLoaded();
            }

        });


        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        GameActivity.hideSoftKeyboard(gameActivity);

        if(connectionFailed) {
            init(gameActivity);
            invalidate();
        }

        if(isLoading) {
            gameActivity.mToast.setText(R.string.str_still_loading_image);
            gameActivity.mToast.show();
            return false;
        }

        // whether or not the view has to be redrawn. For example, if the user clicks on a dot
        // that is already at the minimum size then there is no need to redraw the canvas.
        boolean shouldRedraw;

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                clicksInARow++;
                if(clicksInARow > CLICKS_IN_A_ROW_BEFORE_HINT) {
                    gameActivity.mToast.setText(R.string.str_toast_drag_hint);
                    gameActivity.mToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    gameActivity.mToast.show();
                    // don't show the hint again this game
                    clicksInARow = -1000;
                }
                final float x = event.getX();
                final float y = event.getY();
                // split the dot at the location (x, y)
                shouldRedraw = splitSelected(x, y, false);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                clicksInARow = 0;
                final float x = event.getX();
                final float y = event.getY();
                // split the dot at the location (x, y)
                shouldRedraw = splitSelected(x, y, true);
                break;
            }
            default: {
                return false;
            }
        }

        if(shouldRedraw) invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(connectionFailed) {
            paint.setColor(Utilities.isDarkTheme ? Color.WHITE : Color.BLACK);
            paint.setTextSize(60);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Could not connect to Dropbox.", viewDiameter/2, viewDiameter/2, paint);
            paint.setTextSize(40);
            canvas.drawText("Click to try again", viewDiameter/2, viewDiameter/2 + 100, paint);
        } else if(isLoading) {
            // if loading then draw the loading bitmap
            // canvas.drawBitmap(loadingBmp, 0, 0, null);
            // if loading then draw three dots to show loading
            drawLoadingDots(canvas);

        } else if(sourceRevealed) {
            // if the user won or gave up then show the source image
            canvas.drawBitmap(screenSizeBmp, 0, 0, paint);

        } else {
            // otherwise draw all the dots
            canvas.drawBitmap(gameBmp, 0, 0, null);

        }

    }

    private void drawLoadingDots(Canvas canvas) {
        int radius = 30; // radius of loading dots
        int color = Color.GRAY;
        int y = viewDiameter /2 - radius;
        int x1 = viewDiameter /4 - radius;
        int x2 = viewDiameter /2 - radius;
        int x3 = (viewDiameter *3)/4 - radius;

        Dot d1 = new Dot(x1, y, radius*2, color);
        Dot d2 = new Dot(x2, y, radius*2, color);
        Dot d3 = new Dot(x3, y, radius*2, color);
        d1.draw(canvas, paint, false);
        d2.draw(canvas, paint, false);
        d3.draw(canvas, paint, false);


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

        // if the area is large, then we do not have to actually look at every single pixel.
        // instead we look at every divFactor(th) pixel. For example, if divFactor=10 then we
        // will look at every tenth pixel. This helps to speed up this function.
        // look at approx 5*5 pixels for large circles to determine average color
        int divFactor = Math.round(((float)(endX - startX)) / ((float)RESOLUTION_RATIO)/((float)5.0));
        if(divFactor == 0) divFactor = 1; // avoid infinite loop
        int totalPixels = 0;
        startX /= RESOLUTION_RATIO;
        startY /= RESOLUTION_RATIO;
        endX /= RESOLUTION_RATIO;
        endY /= RESOLUTION_RATIO;
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        for(int x = startX; x < endX; x += divFactor) {
            for(int y = startY; y < endY; y += divFactor) {
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
     * Splits a Dot.
     * @param dot the dot to split
     * @param index the index of the dot in the dots list (this is not really used now but
     *              may be used later)
     * @return whether or not the split was successful (if the Dot is already the minimum size
     * then it is unsuccessful)
     */
    private boolean splitDot(Dot dot, int index) {
        // only split it if there has been less than MAX_CLICKS

        if(dot.getDiameter() / 2 > SMALLEST_DIAMETER) {
            if(!gameActivity.gameIsOver) {
                // if the game is already over, the user can continue to click without
                // increasing their score
                numClicks++;
                gameActivity.setNumclicks(numClicks);
            }
            // REMOVE THE OLD DOT
            dots.remove(index);
            int size = dots.size();
            // SPLIT THE DOT INTO FOUR DOTS
            // Top left dot
            int rad = (int) Math.round(((float) dot.getDiameter()) / 2.0);
            int c = getAvgColor(dot.getX(), dot.getY(), dot.getX() + rad, dot.getY() + rad);
            dots.add(new Dot(dot.getX(), dot.getY(), rad, c));
            dots.get(size).draw(gameCanvas, paint, true);
            size++;
            // Top right dot
            c = getAvgColor(dot.getX() + rad, dot.getY(), dot.getX() + dot.getDiameter(), dot.getY() + rad);
            dots.add(new Dot(dot.getX() + rad, dot.getY(), rad, c));
            dots.get(size).draw(gameCanvas, paint, true);
            size++;
            // Bottom left dot
            c = getAvgColor(dot.getX(), dot.getY() + rad, dot.getX() + rad, dot.getY() + dot.getDiameter());
            dots.add(new Dot(dot.getX(), dot.getY() + rad, rad, c));
            dots.get(size).draw(gameCanvas, paint, true);
            // Bottom right dot
            size++;
            c = getAvgColor(dot.getX() + rad, dot.getY() + rad, dot.getX() + dot.getDiameter(), dot.getY() + dot.getDiameter());
            dots.add(new Dot(dot.getX() + rad, dot.getY() + rad, rad, c));
            dots.get(size).draw(gameCanvas, paint, true);
            return true;

        }
        return false;
    }

    /**
     * splits the dot selected by the user
     * @param touchX the x coordinate of the touch
     * @param touchY the y coordinate of the touch
     * @param moving whether or not the user's finger is moving. Used to make sure not too many
     *               dots are clicked when the user drags their finger across the screen.
     * @return whether or not any dots were actually split
     */
    private boolean splitSelected(float touchX, float touchY, boolean moving) {
        int len = dots.size();

        // long lastTime = System.currentTimeMillis();
        for(int i = 0; i < len; ++i) {
            Dot dot = dots.get(i);
            // if the user pressed in that dot's bounding square
            if(dot.isTouchInside(touchX, touchY)) {
                if(moving) {
                    // if the user did not "move into" this Dot (i.e their finger was already
                    // on the Dot) then do not split it.
                    if(dot.isTouchInside(lastTouchX, lastTouchY)) {
                        return false;
                    }
                }

                // System.out.println("finding the clicked dot took " + (System.currentTimeMillis() - lastTime) + "ms.");
                boolean splitSuccessful = splitDot(dot, i);
                lastTouchX = (int)touchX;
                lastTouchY = (int)touchY;
                return splitSuccessful;
            }
        }
        return false;
    }


    public void toggleSourceImage() {
        sourceRevealed = !sourceRevealed;
        invalidate();
    }

    // resets the game
    private void onImageLoaded() {
        numClicks = 0;
        clicksInARow = 0;
        gameActivity.setNumclicks(0);
        guessesLeft = START_GUESSES;
        dots = new ArrayList<>();
        int avgColor = getAvgColor(0, 0, viewDiameter, viewDiameter);
        // add first dot
        dots.add(new Dot(0, 0, viewDiameter, avgColor));
        dots.get(0).draw(gameCanvas, paint, true);
        invalidate();
    }



    // run when the user makes a guess, returns true if the user won
    public boolean guess(String guess) {
        // make sure the image has already loaded
        if(isLoading) {
            // show toast to notify the user that the image has not loaded yet
            gameActivity.mToast.setText(R.string.str_please_wait_load);
            gameActivity.mToast.show();
            return false;
        }

        // check if the user guessed correctly
        guess = guess.toLowerCase().trim();


        for(String validGuess: validGuesses) {
            if(guess.equals(validGuess)) {
                win();
                return true;
            }
        }

        guessesLeft--;

        // if the user has no more guesses then they lose
        if(guessesLeft <= 0) {
            gameOver();
            return false;
        }

        // if the user guessed wrong and still has some lives left then let them know how many
        String toastText = getResources().getString(R.string.str_toast_incorrect)
                + " " + guessesLeft + " ";
        if(guessesLeft == 1) toastText += getContext().getString(R.string.str_toast_guess_left);
        else toastText += getContext().getString(R.string.str_toast_guesses_left);
        gameActivity.mToast.setText(toastText);
        gameActivity.mToast.show();
        return false;
    }

    private void gameOver() {
        // show Toast to tell the user what the correct word was
        String toastText = getResources().getString(R.string.str_toast_a_correct_ans_was) + " ";
        toastText += validGuesses[0];
        gameActivity.mToast.setText(toastText);
        gameActivity.mToast.show();
        gameActivity.showNextButton();
    }

    private void win() {
        // show toast to let them know they won
        gameActivity.mToast.setText(R.string.str_toast_correct);
        gameActivity.mToast.show();
        sourceRevealed = true;
        gameActivity.showNextButton();
        invalidate();
    }

}
