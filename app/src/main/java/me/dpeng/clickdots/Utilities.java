package me.dpeng.clickdots;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;


public abstract class Utilities {

    public static boolean isDarkTheme = false;
    public static boolean isSquareMode = false;

    // the keys used to retrieve the preferences from SharedPreferences
    // settings
    public static final String KEY_IS_DARK_THEME = "id";
    public static final String KEY_IS_SQUARE_MODE = "is";

    // info from the last level
    public static final String KEY_LEVEL_NUMBER = "ln";
    // a string representing the dots in the game
    public static final String KEY_LEVEL_PROGRESS = "lp";
    public static final String KEY_IS_GAME_OVER = "go";
    public static final String KEY_GUESSES_LEFT = "gl";
    public static final String KEY_GUESSES_BAR_TEXT = "gt";
    public static final String KEY_SCORE = "s";

    // statistics
    public static final String KEY_AVERAGE_SCORE = "AS";
    public static final String KEY_GAMES_WON = "GW";
    public static final String KEY_GAMES_PLAYED = "GP";
    public static final String KEY_BEST_SCORE = "BS";


    // whether or not the info about the last game is stored in sharedPref
    // (it could also be stored in a ViewModel)
    public static final String KEY_IS_INFO_IN_SHARED_PREF = "i";




    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null) return false;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Gets a random number between min and max (inclusive)
     * @param min lower bound
     * @param max upper bound
     * @return a random number between min and max inclusive
     */
    public static int randRange(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static int spToPx(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int dpToPx( Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int dpToSp(Context context, int dp) {
        return (int) (dpToPx(context, dp) / context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static int fetchAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);
        int colorId = a.getResourceId(0, 0);

        a.recycle();

        return colorId;
    }

    public static int getAttributeColorId(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return typedValue.resourceId;
    }

    public static int fetchForegroundColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { android.R.attr.colorForeground });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void saveImageToExternalStorage(Context context, Bitmap finalBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images/");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }

}
