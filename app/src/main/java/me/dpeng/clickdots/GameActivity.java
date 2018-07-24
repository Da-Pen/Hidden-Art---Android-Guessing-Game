package me.dpeng.clickdots;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameActivity extends AppCompatActivity {

    final private static int SIDE_MARGIN = 16;

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
        final GameView gameView = new GameView(this, layout, sourceBitmap);
        gameView.setId(View.generateViewId());
        layout.addView(gameView);

        // "back" button
        final Button btn_back = new Button(this);
        btn_back.setText("BACK (change to image)");
        btn_back.setId(View.generateViewId());
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread backThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(GameActivity.this, MenuActivity.class);
                        startActivity(intent);
                    }
                });
                backThread.start();

            }
        });
        layout.addView(btn_back);
        
        // "click all" button
        final Button btn_clickAll = new Button(this);
        btn_clickAll.setText(R.string.btn_clickAll);
        btn_clickAll.setId(View.generateViewId());
        btn_clickAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.splitAll();
            }
        });
        layout.addView(btn_clickAll);
        
        
        // "reset" button
        final Button btn_reset = new Button(this);
        btn_reset.setText(R.string.btn_reset);
        btn_reset.setId(View.generateViewId());
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.resetGame();
            }
        });
        layout.addView(btn_reset);
        
        // "reveal image" button
        final Button btn_revealImage = new Button(this);
        btn_revealImage.setText(R.string.btn_revealImage);
        btn_revealImage.setId(View.generateViewId());
        btn_revealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gameView.revealOrHideSourceImage();
                if(btn_revealImage.getText().equals(getResources().getString(R.string.btn_revealImage))) {
                    btn_revealImage.setText(R.string.btn_hideImage);
                } else {
                    btn_revealImage.setText(R.string.btn_revealImage);
                }

            }
        });
        layout.addView(btn_revealImage);

        ConstraintSet c = new ConstraintSet();

        c.clone(layout);
        //center game view on screen
        c.connect(gameView.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
        c.connect(gameView.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM);
        c.connect(gameView.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT);
        c.connect(gameView.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT);

        //constrain back button to top-left
        c.connect(btn_back.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
        c.connect(btn_back.getId(), ConstraintSet.BOTTOM, gameView.getId(), ConstraintSet.TOP);
        c.connect(btn_back.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, SIDE_MARGIN);

        //constrain reveal button to top-right
        c.connect(btn_revealImage.getId(), ConstraintSet.BOTTOM, btn_back.getId(), ConstraintSet.BOTTOM);
        c.connect(btn_revealImage.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, SIDE_MARGIN);

        //constrain back button to bottom-left
        c.connect(btn_clickAll.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM);
        c.connect(btn_clickAll.getId(), ConstraintSet.TOP, gameView.getId(), ConstraintSet.BOTTOM);
        c.connect(btn_clickAll.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, SIDE_MARGIN);

        //constrain back button to bottom-right
        c.connect(btn_reset.getId(), ConstraintSet.BOTTOM, btn_clickAll.getId(), ConstraintSet.BOTTOM);
        c.connect(btn_reset.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, SIDE_MARGIN);

        c.applyTo(layout);
        
    }


}
