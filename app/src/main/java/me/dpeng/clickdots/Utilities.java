package me.dpeng.clickdots;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;

import java.util.Random;


public abstract class Utilities {

    public static boolean isDarkTheme;
    public static boolean isSquareMode;

    // the keys used to retrieve the preferences from SharedPreferences
    public static final String KEY_IS_DARK_THEME = "isDarkTheme";
    public static final String KEY_IS_SQUARE_MODE = "isSquareTheme";

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


}
