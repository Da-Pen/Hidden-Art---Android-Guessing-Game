package me.dpeng.clickdots;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // determine and apply the app's theme color
        // find preferences file
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkTheme = preferences.getBoolean(Utilities.KEY_IS_DARK_THEME, false);
        // save the boolean in an application-wide variable so it can easily be accessed in other activities
        Utilities.isDarkTheme = isDarkTheme;
        if(isDarkTheme) {
            //setTheme(R.style.DarkTheme);
            setTheme(R.style.Dark);
        } else {
            //setTheme(R.style.LightTheme);
            setTheme(R.style.Light);
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

        } else {
            // if there is still no internet, notify the user via Toast
            Toast toast = Toast.makeText(this,
                    R.string.str_no_internet, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void toggleSquareMode(View view) {
        System.out.println("toggling square mode");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Utilities.isSquareMode = !sharedPreferences.getBoolean(Utilities.KEY_IS_SQUARE_MODE, false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Utilities.KEY_IS_SQUARE_MODE, Utilities.isSquareMode);
        editor.apply();

        // let the user know that square mode is on or off via Toast
        Toast toast = Toast.makeText(this,
                Utilities.isSquareMode ? getResources().getString(R.string.str_square_mode_on):
                        getResources().getString(R.string.str_square_mode_off), Toast.LENGTH_SHORT);
        toast.show();

        // alter the button image
        recalculateButtonResources();

    }

    public void toggleDarkTheme(View view) {
        System.out.println("toggling dark theme");
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
        // recalculate btn_toggleDarkTheme image resource
        ImageButton toggleDarkThemeButton = findViewById(R.id.btn_toggleDarkTheme);
        if(Utilities.isDarkTheme) {
            toggleDarkThemeButton.setImageResource(R.drawable.color_switch_white_border);
        } else {
            toggleDarkThemeButton.setImageResource(R.drawable.color_switch_black_border);
        }

        // recalculate btn_toggleSquareMode image resource
        ImageButton toggleSquareModeButton = findViewById(R.id.btn_toggleSquareMode);
        if(Utilities.isSquareMode) {
            if(Utilities.isDarkTheme) {
                toggleSquareModeButton.setImageResource(R.drawable.square_white);
            } else {
                toggleSquareModeButton.setImageResource(R.drawable.square_black);
            }
        } else {
            if(Utilities.isDarkTheme) {
                toggleSquareModeButton.setImageResource(R.drawable.circle_white);
            } else {
                toggleSquareModeButton.setImageResource(R.drawable.circle_black);
            }
        }

        // recalculate btn_credits image resource
        ImageButton creditsButton = findViewById(R.id.btn_credits);
        if(Utilities.isDarkTheme) {
            creditsButton.setImageResource(R.drawable.info_white);
        } else {
            creditsButton.setImageResource(R.drawable.info_black);
        }

    }

    public void goToCredits(View view) {
        Intent intent = new Intent(this, CreditsActivity.class);
        startActivity(intent);
    }
}
