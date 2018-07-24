package me.dpeng.clickdots;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class MenuActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "me.dpeng.clickdots.MESSAGE";
    // string used as extra name corresponding to game image location used in intent to start game
    public static final String IMAGE = "me.dpeng.clickdots.IMAGE";
    public static final int[] IMAGES = {R.drawable.lion, R.drawable.panda};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void randomImage(View view) {
        Thread startGameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int choiceIndex = (int)(Math.random()* IMAGES.length);
                startGame(IMAGES[choiceIndex]);
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

    public void startGame(final int imageId) {
        Intent intent = new Intent (MenuActivity.this, GameActivity.class);
        intent.putExtra(IMAGE, imageId);
        startActivity(intent);
    }

}
