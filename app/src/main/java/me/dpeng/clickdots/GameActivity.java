package me.dpeng.clickdots;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
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
import android.widget.Toast;

/**
 * The Activity for the Game. The interface is created here rather than in the XML file. The
 * interface includes back button, score count and give up button across the top, the GameView
 * (i.e the container for the actual game) in the middle, and the bar to guess the hidden image
 * across the bottom.
 */
public class GameActivity extends AppCompatActivity implements ConfirmResignDialogFragment.DialogListener{

    // space at the sides of the screen (we need some space because if there is none then it is
    // difficult for the user to click the dots at the edge of the screen.
    final public static int SIDE_MARGIN = 30;
    // since the corners of the guess bar are rounded, the text would go extend outside of the
    // bar if this padding were zero. So we need to set some padding.
    private final static int GUESS_BAR_PADDING_LEFT = 70;
    private GameView gameView;
    private ConstraintLayout layout; // the layout for the entire activity
    private ConstraintLayout guessBarLayout; // the layout for the bottom bar of the activity
    private EditText et_guess;
    private Button btn_guess;
    private TextView tv_numClicks; // TextView to keep track of the # of clicks the user has made
    // Clicking back button and the reveal image button both ask the user to confirm that they
    // wish to give up. However they perform different actions when the user clicks "CONFIRM": the
    // back button goes to the menu Activity, while the reveal image button reveals the source image.
    // Thus we need to keep track of which button they clicked using this boolean.
    private boolean clickedBack;
    private boolean gameIsOver = false; // if the game is over (this can be if the user won OR lost

    /**
     * Sets up the layout and instantiates the GameView.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // make sure the keyboard does not open when the activity starts
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        super.onCreate(savedInstanceState);
        // set the layout
        setContentView(R.layout.activity_game);
        // get the layout
        layout = findViewById(R.id.game_layout);

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
//                Thread backThread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(GameActivity.this, MenuActivity.class);
//                        startActivity(intent);
//                    }
//                });
//                backThread.start();

                // make sure we go back to the main menu once the user clicks "confirm"
                clickedBack = true;
                if(gameView != null && gameView.sourceRevealed) {
                    finish();
                    return;
                }
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                ConfirmResignDialogFragment dialog = new ConfirmResignDialogFragment();
                dialog.show(ft, "dialog");

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

                // if the user has already finished the game then just toggle the source image
                // (i.e we do not need to ask to confirm to give up)
                if(gameIsOver) {
                    gameView.toggleSourceImage();
                    return;
                }

                // make sure we reveal the source image when the user clicks "confirm"
                clickedBack = false;

                // create dialog to confirm that the user wants to give up
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                ConfirmResignDialogFragment dialog = new ConfirmResignDialogFragment();
                dialog.show(ft, "dialog");
            }
        });
        layout.addView(btn_revealImage);

        // create the guess bar at the bottom of the screen
        // Create Layout for the guessing bar item
        // It consists of the EditText on the left and the "GUESS" button on the right.

        guessBarLayout = new ConstraintLayout(this);
        guessBarLayout.setId(View.generateViewId());
        guessBarLayout.setPadding(GUESS_BAR_PADDING_LEFT, 0, 0, 0);
        ConstraintLayout.LayoutParams rlParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlParams.leftMargin = SIDE_MARGIN;
        rlParams.rightMargin = SIDE_MARGIN;
        guessBarLayout.setLayoutParams(rlParams);
        guessBarLayout.setBackgroundResource(R.drawable.edit_text_style);

        // create guess EditText
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
                    String guess = et_guess.getText().toString();
                    if(guess.isEmpty()) {
                        Toast toast = Toast.makeText(GameActivity.this,
                                R.string.empty_guess_warn_toast, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        // hide the keyboard
                        hideSoftKeyboard(GameActivity.this);
                        gameView.guess(et_guess.getText().toString());
                    }
                    handled = true;
                }
                return handled;
            }
        });
        et_guess.setHint("enter your guess");
        et_guess.setBackgroundColor(0); // set background transparent in order to remove underline
        guessBarLayout.addView(et_guess);


        // create guess button
        btn_guess = new Button(this);
        btn_guess.setId(View.generateViewId());
        btn_guess.setText(R.string.BTN_GUESS);
        btn_guess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameIsOver) {
                    GameActivity.this.nextLevel();
                    return;
                }
                // hide the keyboard
                String guess = et_guess.getText().toString();
                if(guess.isEmpty()) {
                    Toast toast = Toast.makeText(GameActivity.this,
                            R.string.empty_guess_warn_toast, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    hideSoftKeyboard(GameActivity.this);
                    gameView.guess(et_guess.getText().toString());
                }
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

    // convert the guess bar into the next button
    // opposite of showGuessBar()
    public void showNextButton() {
        TransitionManager.beginDelayedTransition(layout);
        et_guess.setWidth(0);
        et_guess.setEnabled(false);
        btn_guess.setText(R.string.next);
        ViewGroup.LayoutParams layoutParams = guessBarLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        guessBarLayout.setPadding(0, 0, 0, 0);
        gameIsOver = true;
    }

    // convert the next button into the guess bar
    // opposite of showNextButton()
    public void showGuessBar() {
        TransitionManager.beginDelayedTransition(layout);
        ConstraintLayout.LayoutParams et_guessParams = (ConstraintLayout.LayoutParams)et_guess.getLayoutParams();
        et_guessParams.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        et_guess.setEnabled(true);
        et_guess.setText("");
        btn_guess.setText(R.string.BTN_GUESS);
        ViewGroup.LayoutParams layoutParams = guessBarLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        guessBarLayout.setPadding(GUESS_BAR_PADDING_LEFT, 0, 0, 0);
        gameIsOver = false;
    }

    // sets the number of clicks. When the user clicks a dot then this is called and it
    // updates the counter at the top of the screen.
    public void setNumclicks(int numClicks) {
        tv_numClicks.setText("" + numClicks);
    }

    // what to do if the user clicks "CONFIRM" on the dialog "Are you sure you want to give up?"
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if(clickedBack) finish();
        else {
            gameView.toggleSourceImage();
            showNextButton();
        }
    }

    // what to do if the user clicks "CANCEL" on the dialog "Are you sure you want to give up?"
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private void nextLevel() {
        // set the next button back to the guess bar
        showGuessBar();
        gameView.onImageURLLoaded();
    }




}
