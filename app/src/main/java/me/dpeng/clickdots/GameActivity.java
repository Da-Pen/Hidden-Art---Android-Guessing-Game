package me.dpeng.clickdots;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    final public static int SIDE_MARGIN = 30;
    private GameView gameView;

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





        gameView = new GameView(this, layout, sourceBitmap);
        gameView.setId(View.generateViewId());
        layout.addView(gameView);
        ConstraintLayout.LayoutParams gameViewLayout = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        gameViewLayout.leftMargin = SIDE_MARGIN;
        gameViewLayout.rightMargin = SIDE_MARGIN;
        gameView.setLayoutParams(gameViewLayout);

        // "back" button
        final ImageButton btn_back = new ImageButton(this);
        btn_back.setImageResource(R.drawable.icon_back);
        btn_back.setBackgroundColor(0); //set background to be transparent
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
        final ImageButton btn_clickAll = new ImageButton(this);
        btn_clickAll.setImageResource(R.drawable.icon_click);
        btn_clickAll.setBackgroundColor(0); //set background to be transparent
        btn_clickAll.setId(View.generateViewId());
        btn_clickAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(GameActivity.this, "Clicking all ...", Toast.LENGTH_SHORT);
                toast.show();
                gameView.splitAll();
                toast.cancel();

            }
        });
        layout.addView(btn_clickAll);
        
        
        // "reset" button
        final ImageButton btn_reset = new ImageButton(this);
        btn_reset.setImageResource(R.drawable.icon_reset);
        btn_reset.setBackgroundColor(0); //set background to be transparent
        btn_reset.setId(View.generateViewId());
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.resetGame();
            }
        });
        layout.addView(btn_reset);
        
        // "reveal image" button
        final ImageButton btn_revealImage = new ImageButton(this);
        btn_revealImage.setImageResource(R.drawable.icon_show_image);
        btn_revealImage.setBackgroundColor(0); //set background to be transparent
        btn_revealImage.setId(View.generateViewId());
        btn_revealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gameView.revealOrHideSourceImage();

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

        // the four buttons are along the top of the layout in the following order:
        // back --- click all --- reset --- reveal image

        // constrain back button to top-left
        c.connect(btn_back.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
        c.connect(btn_back.getId(), ConstraintSet.BOTTOM, gameView.getId(), ConstraintSet.TOP);
        c.connect(btn_back.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT);


        // constrain click all button to right of back button
        c.connect(btn_clickAll.getId(), ConstraintSet.BOTTOM, btn_back.getId(), ConstraintSet.BOTTOM);
        //c.connect(btn_clickAll.getId(), ConstraintSet.LEFT, btn_back.getId(), ConstraintSet.RIGHT);
    
        // constrain reset button to right of clickAll button
        c.connect(btn_reset.getId(), ConstraintSet.BOTTOM, btn_clickAll.getId(), ConstraintSet.BOTTOM);
        //c.connect(btn_reset.getId(), ConstraintSet.LEFT, btn_clickAll.getId(), ConstraintSet.RIGHT);
    
        // constrain click all button to right of reset button
        // and constrain its right side to the right side of the layout
        c.connect(btn_revealImage.getId(), ConstraintSet.BOTTOM, btn_reset.getId(), ConstraintSet.BOTTOM);
        //c.connect(btn_revealImage.getId(), ConstraintSet.LEFT, btn_reset.getId(), ConstraintSet.RIGHT);
        c.connect(btn_revealImage.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT);


        // create chain for buttons
        c.createHorizontalChain(layout.getId(), ConstraintSet.LEFT, layout.getId(),
                ConstraintSet.RIGHT, new int[]{btn_back.getId(), btn_clickAll.getId(),
                        btn_reset.getId(), btn_revealImage.getId()}, null, ConstraintSet.CHAIN_SPREAD);




        c.applyTo(layout);
        
    }


}
