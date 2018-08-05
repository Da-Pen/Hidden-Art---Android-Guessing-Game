package me.dpeng.clickdots;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    final public static int SIDE_MARGIN = 30;
    private View gameView;
    private EditText et_guess;
    private TextView tv_numClicks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // make sure the keyboard does not open when the activity starts
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        super.onCreate(savedInstanceState);

        // set the layout
        setContentView(R.layout.activity_game);
        // get the layout
        final ConstraintLayout layout = findViewById(R.id.game_layout);


        ///---CREATE LAYOUT ITEMS---///

        // create the main view for the game
        gameView = new GameView(this, layout);
        gameView.setId(View.generateViewId());
        layout.addView(gameView);
        ConstraintLayout.LayoutParams gameViewLayout = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
        
        // text to show how number of clicks so far
        tv_numClicks = new TextView(this);
        tv_numClicks.setId(View.generateViewId());
        tv_numClicks.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
        tv_numClicks.setText("0");
        layout.addView(tv_numClicks);

        // "reveal image" button
        final ImageButton btn_revealImage = new ImageButton(this);
        btn_revealImage.setImageResource(R.drawable.icon_show_image);
        btn_revealImage.setBackgroundColor(0); //set background to be transparent
        btn_revealImage.setId(View.generateViewId());
        btn_revealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if it is a gameView object (i.e it is not in the loading stage)
                if(gameView instanceof GameView)
                    ((GameView)gameView).revealOrHideSourceImage();
            }
        });
        layout.addView(btn_revealImage);

        ConstraintLayout guessBarLayout = new ConstraintLayout(this);
        guessBarLayout.setId(View.generateViewId());
        guessBarLayout.setPadding(70, 0, 0, 0);
        ConstraintLayout.LayoutParams rlParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlParams.leftMargin = SIDE_MARGIN;
        rlParams.rightMargin = SIDE_MARGIN;
        guessBarLayout.setLayoutParams(rlParams);
        guessBarLayout.setBackgroundResource(R.drawable.edit_text_style);

        // guess EditText
        et_guess = new EditText(this);
        et_guess.setId(View.generateViewId());
        // the text box should expand to match constrains, so we create a new LayoutParams
        ConstraintLayout.LayoutParams et_guessParams = new ConstraintLayout.LayoutParams(
                ConstraintSet.MATCH_CONSTRAINT, ConstraintSet.WRAP_CONTENT);
        et_guess.setLayoutParams(et_guessParams);
        et_guess.setImeOptions(EditorInfo.IME_ACTION_DONE);
        et_guess.setSingleLine();
        et_guess.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide the keyboard
                    hideSoftKeyboard(GameActivity.this);
                    // if it is a gameView object (i.e it is not in the loading stage)
                    // then make a guess
                    if(gameView instanceof GameView)
                        ((GameView)gameView).guess(et_guess.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });
        et_guess.setHint("enter your guess");
        et_guess.setBackgroundColor(0); // set background transparent in order to remove underline
        //et_guess.setPadding(30, 0, 30, 0);
        guessBarLayout.addView(et_guess);




        // guess button
        final Button btn_guess = new Button(this);
        btn_guess.setId(View.generateViewId());
        btn_guess.setText(R.string.BTN_GUESS);
        btn_guess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide the keyboard
                hideSoftKeyboard(GameActivity.this);
                // if it is a gameView object (i.e it is not in the loading stage)
                if(gameView instanceof GameView)
                    ((GameView)gameView).guess(et_guess.getText().toString());
            }
        });
        btn_guess.setBackground(null);

        guessBarLayout.addView(btn_guess);

        layout.addView(guessBarLayout);

        ///---CREATE CONSTRAINTS---///
        ConstraintSet c = new ConstraintSet();
        c.clone(layout);


        //center game view on screen
        c.connect(gameView.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
        c.connect(gameView.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM);
        c.connect(gameView.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT);
        c.connect(gameView.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT);

        // the top row layout is as follows:
        // back button --- # of clicks --- reveal image button

        // constrain back button to top-left
        c.connect(btn_back.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
        c.connect(btn_back.getId(), ConstraintSet.BOTTOM, gameView.getId(), ConstraintSet.TOP);
        c.connect(btn_back.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT);


        // constrain numguesses textview to the right of back button
        c.connect(tv_numClicks.getId(), ConstraintSet.LEFT, btn_back.getId(), ConstraintSet.RIGHT);
        c.connect(tv_numClicks.getId(), ConstraintSet.BOTTOM, btn_back.getId(), ConstraintSet.BOTTOM);
    
        // constrain click all button to right of reset button
        // and constrain its right side to the right side of the layout
        c.connect(btn_revealImage.getId(), ConstraintSet.TOP, btn_back.getId(), ConstraintSet.TOP);
        //c.connect(btn_revealImage.getId(), ConstraintSet.LEFT, btn_reset.getId(), ConstraintSet.RIGHT);
        c.connect(btn_revealImage.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT);


        // create chain for top row
        c.createHorizontalChain(layout.getId(), ConstraintSet.LEFT, layout.getId(),
                ConstraintSet.RIGHT, new int[]{btn_back.getId(), tv_numClicks.getId(),
                        btn_revealImage.getId()}, null, ConstraintSet.CHAIN_SPREAD);


        c.connect(guessBarLayout.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT);
        c.connect(guessBarLayout.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT);
        c.connect(guessBarLayout.getId(), ConstraintSet.TOP, gameView.getId(), ConstraintSet.BOTTOM);
        c.connect(guessBarLayout.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM);

        ConstraintSet c2 = new ConstraintSet();
        c2.clone(guessBarLayout);
//        c2.connect(et_guess.getId(), ConstraintSet.LEFT, guessBarLayout.getId(), ConstraintSet.LEFT);
//        c2.connect(btn_guess.getId(), ConstraintSet.LEFT, et_guess.getId(), ConstraintSet.RIGHT);
        c2.connect(et_guess.getId(), ConstraintSet.TOP, guessBarLayout.getId(), ConstraintSet.TOP);
        c2.connect(et_guess.getId(), ConstraintSet.BOTTOM, guessBarLayout.getId(), ConstraintSet.BOTTOM);
        c2.connect(btn_guess.getId(), ConstraintSet.TOP, et_guess.getId(), ConstraintSet.TOP);
        c2.createHorizontalChain(guessBarLayout.getId(), ConstraintSet.LEFT, guessBarLayout.getId(),
                ConstraintSet.RIGHT, new int[]{et_guess.getId(), btn_guess.getId()},
              null, ConstraintSet.CHAIN_SPREAD);
        c2.applyTo(guessBarLayout);

        // apply the ConstraintSet to the layout
        c.applyTo(layout);
        
    }

    // hides the keyboard
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    // sets the number of clicks. When the user clicks a dot then this is called and it
    // updates the counter at the top of the screen.
    public void setNumclicks(int numClicks) {
        tv_numClicks.setText("" + numClicks);
    }
}
