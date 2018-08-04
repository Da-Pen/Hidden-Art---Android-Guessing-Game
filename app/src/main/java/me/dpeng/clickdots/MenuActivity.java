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
            setContentView(R.layout.activity_no_internet);
        } else {
            setContentView(R.layout.activity_menu);
        }
    }

    public void randomImage(View view) {
        startGame();

    }

    public void chooseImage(View view) {
        Thread chooseImageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, ChooseImageActivity.class);
                startActivity(intent);
            }
        });
        chooseImageThread.start();
    }

    public void startGame() {
        Intent intent = new Intent (MenuActivity.this, GameActivity.class);
        startActivity(intent);
    }

    public void recheckInternet(View view) {
        if(Utilities.isNetworkAvailable(this)) {
            ViewGroup noInternetSceneRoot = (ViewGroup) findViewById(R.id.no_internet_layout);
            Scene menuScene = Scene.getSceneForLayout(noInternetSceneRoot,
                    R.layout.activity_menu, this);
            TransitionManager.go(menuScene, new Fade());

        } else {
            Toast toast = Toast.makeText(this,
                    R.string.no_internet, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
