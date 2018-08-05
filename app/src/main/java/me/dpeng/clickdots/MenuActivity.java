package me.dpeng.clickdots;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.AutoTransition;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MenuActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "me.dpeng.clickdots.MESSAGE";
    // string used as extra name corresponding to game image location used in intent to start game
    public static final String IMAGE = "me.dpeng.clickdots.IMAGE";
    public static final int[] IMAGES = {R.drawable.lion, R.drawable.panda};
    public static final String IMAGE_404 = "https://pbs.twimg.com/profile_images/610486974990913536/5MdbcHvF.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check if there is an internet connection
        if(!Utilities.isNetworkAvailable(this)) {
            // if there is no internet then load the no internet layout
            setContentView(R.layout.activity_no_internet);
        } else {
            // if there is an internet connection then load the normal menu layout
            setContentView(R.layout.activity_menu);
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
                    R.string.no_internet, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
