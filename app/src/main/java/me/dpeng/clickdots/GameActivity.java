package me.dpeng.clickdots;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // get the intent from the last activity
        Intent intent = getIntent();
        int imageLocation = intent.getIntExtra(MenuActivity.IMAGE, R.drawable.lion);

        Drawable drawable = getResources().getDrawable(imageLocation);
        Bitmap sourceBitmap = ((BitmapDrawable)drawable).getBitmap();

        ConstraintLayout layout = findViewById(R.id.game_layout);
        GameView gameView = new GameView(this, layout, sourceBitmap);
        gameView.setId(View.generateViewId());
        layout.addView(gameView);
        ConstraintSet c = new ConstraintSet();
        c.clone(layout);
        c.connect(gameView.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
        c.connect(gameView.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM);
        c.applyTo(layout);

        //gameView.setSrcBmp(scaledBitmap);
//        ViewGroup.LayoutParams gameLayoutParams = new ViewGroup.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT);
//
//        layout.addView(gameView, gameLayoutParams);

        // set the height of the game content to be the same as the width
//        View gameView = findViewById(R.id.gameView);
//        android.view.ViewGroup.LayoutParams mParams = gameView.getLayoutParams();
//        mParams.height = Resources.getSystem().getDisplayMetrics().widthPixels;
//        gameView.setLayoutParams(mParams);



        //ImageView imageView = findViewById(R.id.mainImage);
        //imageView.setImageResource(imageLocation);
    }


}
