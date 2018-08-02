package me.dpeng.clickdots;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
        setContentView(R.layout.activity_menu);
    }

    public void randomImage(View view) {


        Thread startGameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // get a list of the image urls from Google Drive

                try {
                    URL url = new URL("http://dpeng.me/ClickdotsSources.txt");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(30000); // time out in 30 sec
                    BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String currLine;
                    ArrayList<String> items = new ArrayList<>(); // each item is a string containing
                    // the URL plus a list of valid guesses
                    while(true) {
                        currLine = bf.readLine();
                        if(currLine != null) items.add(currLine);
                        else break;
                    }

                    int choiceIndex = (int)(Math.random()*items.size());
                    startGame(items.get(choiceIndex));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        startGameThread.start();

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

    public void startGame(final String imageURL) {
        Intent intent = new Intent (MenuActivity.this, GameActivity.class);
        intent.putExtra(IMAGE, imageURL);
        startActivity(intent);
    }

}
