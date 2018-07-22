package me.dpeng.clickdots;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // get the intent from the last activity
        Intent intent = getIntent();
        int imageLocation = intent.getIntExtra(MenuActivity.IMAGE, R.drawable.lion);
        Drawable d = getResources().getDrawable(imageLocation);
        Bitmap b = ((BitmapDrawable)d).getBitmap();


        ConstraintLayout c = findViewById(R.id.game_layout);
        GameView g = new GameView(this);
        g.setSrcBmp(b);
        ViewGroup.LayoutParams gameLayoutParams = new ViewGroup.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT);

        c.addView(g, gameLayoutParams);

        // set the height of the game content to be the same as the width
//        View gameView = findViewById(R.id.gameView);
//        android.view.ViewGroup.LayoutParams mParams = gameView.getLayoutParams();
//        mParams.height = Resources.getSystem().getDisplayMetrics().widthPixels;
//        gameView.setLayoutParams(mParams);



        //ImageView imageView = findViewById(R.id.mainImage);
        //imageView.setImageResource(imageLocation);
    }
}
