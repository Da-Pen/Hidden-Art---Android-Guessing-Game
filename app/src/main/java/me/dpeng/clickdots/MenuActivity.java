package me.dpeng.clickdots;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;


public class MenuActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "me.dpeng.clickdots.MESSAGE";
    // string used as extra name corresponding to game image location used in intent to start game
    public static final String IMAGE = "me.dpeng.clickdots.IMAGE";
    public static final String IMAGE_404 = "https://pbs.twimg.com/profile_images/610486974990913536/5MdbcHvF.png";
    private Toast mToast;
    private SharedPreferences preferences;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // determine and apply the app's theme color
        // find preferences file
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Utilities.isDarkTheme = preferences.getBoolean(Utilities.KEY_IS_DARK_THEME, false);
        Utilities.isSquareMode = preferences.getBoolean(Utilities.KEY_IS_SQUARE_MODE, false);

        if(Utilities.isDarkTheme) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }

        super.onCreate(savedInstanceState);

        // check if there is an internet connection
        if(!Utilities.isNetworkAvailable(this)) {
            // if there is no internet then load the no internet layout
            setContentView(R.layout.activity_no_internet);
        } else {
            // if there is an internet connection then load the normal menu layout
            setContentView(R.layout.activity_menu);
            recalculateButtonResources();
        }
    }


    /**
     * Starts the game
     */
    public void startGame(View view) {
        Intent intent = new Intent (this, GameActivity.class);
        startActivity(intent);
    }

    public void instructions(View view) {
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }



    /**
     * Re-checks if there is an internet connection.
     * @param view
     */
    public void recheckInternet(View view) {
        if(Utilities.isNetworkAvailable(this)) {
            // if there is now an internet connection then transition into the normal menu layout
            ViewGroup noInternetSceneRoot = findViewById(R.id.no_internet_layout);

            Scene menuScene = Scene.getSceneForLayout(noInternetSceneRoot,
                    R.layout.activity_menu, this);
            TransitionManager.go(menuScene, new Fade());
            recalculateButtonResources();

        } else {
            // if there is still no internet, notify the user via Toast
            mToast.setText(R.string.str_no_internet);
            mToast.show();
        }
    }

    public void toggleSquareMode(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Utilities.isSquareMode = !sharedPreferences.getBoolean(Utilities.KEY_IS_SQUARE_MODE, false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Utilities.KEY_IS_SQUARE_MODE, Utilities.isSquareMode);
        editor.apply();

        // let the user know that vector_square mode is on or off via Toast

        mToast.setText(Utilities.isSquareMode ? getResources().getString(R.string.str_square_mode_on):getResources().getString(R.string.str_square_mode_off));
        mToast.show();

        // alter the button image
        recalculateButtonResources();

    }

    public void toggleDarkTheme(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Utilities.isDarkTheme = !sharedPreferences.getBoolean(Utilities.KEY_IS_DARK_THEME, false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Utilities.KEY_IS_DARK_THEME, Utilities.isDarkTheme);
        editor.apply();

        // restart this activity so that the theme will update
//        Intent intent = new Intent(this,MenuActivity.class);
        recreate();
//        startActivity(intent);
    }

    /**
     * Recalculates the image resources for the 3 buttons along the bottom of the screen.
     * For example, if the theme is dark then the images will be white, and if squareMode is
     * on then the middle image will be a circle after this function is run.
     */
    public void recalculateButtonResources() {

        // recalculate btn_toggleSquareMode image resource
        ImageButton toggleSquareModeButton = findViewById(R.id.btn_toggleSquareMode);
        if(Utilities.isSquareMode) {
            toggleSquareModeButton.setImageResource(R.drawable.vector_square);
        } else {
            toggleSquareModeButton.setImageResource(R.drawable.vector_circle);
        }

    }

    public void goToCredits(View view) {
        finish();
        Intent intent = new Intent(this, CreditsActivity.class);
        startActivity(intent);
    }

}
