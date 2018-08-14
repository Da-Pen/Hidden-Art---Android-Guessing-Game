package me.dpeng.clickdots;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
public class GameActivity extends AppCompatActivity implements ConfirmResignDialogFragment.DialogListener, NavigationView.OnNavigationItemSelectedListener{

    // space at the sides of the screen (we need some space because if there is none then it is
    // difficult for the user to click the dots at the edge of the screen.
    //public static int SIDE_MARGIN;
    private GameView gameView;
    private ConstraintLayout layout; // the layout for the entire activity

    private ConstraintLayout guessBarLayout; // the layout for the bottom bar of the activity
    private EditText et_guess;
    private Button btn_guess;


    private TextView tv_numClicks; // TextView to keep track of the # of clicks the user has made
    public boolean gameIsOver = false; // if the game is over (this can be if the user won OR lost

    public Toast mToast;

    /**
     * Sets up the layout and instantiates the GameView.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // create global Toast
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        // make sure the keyboard does not open when the activity starts
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // set the theme to be dark if dark mode is on
        if(Utilities.isDarkTheme) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }


        super.onCreate(savedInstanceState);
        // set the layout
        setContentView(R.layout.activity_game);
        // get the layout
        layout = findViewById(R.id.game_layout);

        // initialize the listener for the drawer
        NavigationView v = findViewById(R.id.nav_view);
        v.setNavigationItemSelectedListener(this);

        gameView = findViewById(R.id.gameView);
        ConstraintLayout.LayoutParams gameViewLayout = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, 0);
        gameViewLayout.leftMargin = gameView.sideMargin;
        gameViewLayout.rightMargin = gameView.sideMargin;
        gameView.setLayoutParams(gameViewLayout);
        gameView.init(this);

        tv_numClicks = findViewById(R.id.tv_numClicks);
        guessBarLayout = findViewById(R.id.guess_bar);

        et_guess = findViewById(R.id.editText);
        et_guess.setImeOptions(EditorInfo.IME_ACTION_DONE);
        et_guess.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String guess = et_guess.getText().toString();
                    if(guess.isEmpty()) {
                        mToast.setText(R.string.str_empty_guess_warn_toast);
                        mToast.show();
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
        et_guess.setBackgroundColor(0); // set background transparent in order to remove underline

        btn_guess = findViewById(R.id.btn_guess);

    }

    // hides the keyboard
    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // convert the guess bar into the next button
    // opposite of showGuessBar()
    public void showNextButton() {
        // set the level number in SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // get the last level number
        int lastLevel = sharedPreferences.getInt(Utilities.KEY_LEVEL_NUMBER, -1);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Utilities.KEY_LEVEL_NUMBER, lastLevel + 1);
        // clear the level progress
        editor.putString(Utilities.KEY_LEVEL_PROGRESS, "");
        editor.apply();

        // change the GUESS button to the NEXT button
        TransitionManager.beginDelayedTransition(layout);
        et_guess.setWidth(0);
        et_guess.setEnabled(false);
        btn_guess.setText(R.string.str_next);
        ViewGroup.LayoutParams layoutParams = guessBarLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
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
        btn_guess.setText(R.string.str_btn_guess);
        ViewGroup.LayoutParams layoutParams = guessBarLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
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
        // TODO resign
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


    /**
     * Opens the drawer.
     * @param view the wrapper for the drawer (i.e the NavigationView object)
     */
    public void openDrawer(View view) {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.openDrawer(findViewById(R.id.nav_view));
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.nav_toggle_square_mode: {
                // change the value stored in SharedPreferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                Utilities.isSquareMode = !sharedPreferences.getBoolean(Utilities.KEY_IS_SQUARE_MODE, false);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Utilities.KEY_IS_SQUARE_MODE, Utilities.isSquareMode);
                editor.apply();

                // change the icon
                item.setIcon(Utilities.isSquareMode ?
                        R.drawable.vector_square : R.drawable.vector_circle);

                if(gameView != null) {
                    // change the dots on the gameView
                    // first store the dots in SharedPreferences
//                    editor.putString(Utilities.KEY_LEVEL_PROGRESS, gameView.dotsToString());
//                    editor.apply();
                    // then redraw the gameView with squares or circles
                    gameView.toggleSquareMode();
                }
                break;
            }

            case R.id.nav_toggle_theme: {
                // save the level progress in SharedPreferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // if the game has started and not finished then we save the current progress
                if(!gameView.sourceRevealed && !gameView.isLoading) {
                    editor.putString(Utilities.KEY_LEVEL_PROGRESS, gameView.dotsToString());
                } else {
                    // otherwise clear the string so that the next time we play it does not load the wrong thing
                    editor.putString(Utilities.KEY_LEVEL_PROGRESS, "");
                }

                // change the theme
                Utilities.isDarkTheme = !sharedPreferences.getBoolean(Utilities.KEY_IS_DARK_THEME, false);
                editor.putBoolean(Utilities.KEY_IS_DARK_THEME, Utilities.isDarkTheme);
                editor.apply();

                // recreate the activity
                recreate();
            }
        }
        return true;
    }

    /**
     * goes back to the main menu.
     * @param view
     */
    public void backToMenu(View view) {
        // if the game has not been loaded yet then do nothing
        if(gameView == null) {
            finish();
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // if the game has started and not finished then we save the current progress
        if(!gameView.sourceRevealed && !gameView.isLoading) {
            editor.putString(Utilities.KEY_LEVEL_PROGRESS, gameView.dotsToString());
        } else {
            // otherwise clear the string so that the next time we play it does not load the wrong thing
            editor.putString(Utilities.KEY_LEVEL_PROGRESS, "");
        }

        editor.apply();

        finish();

//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
//        if (prev != null) {
//            ft.remove(prev);
//        }
//        ft.addToBackStack(null);
//        ConfirmResignDialogFragment dialog = new ConfirmResignDialogFragment();
//        dialog.show(ft, "dialog");
    }


    public void recenterGameView() {
        ConstraintSet c = new ConstraintSet();
        c.clone(layout);

        //center game view on screen
        c.connect(gameView.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
        c.connect(gameView.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM);
        c.connect(gameView.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT);
        c.connect(gameView.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT);
        c.applyTo(layout);
    }

    public void guess(View view) {
        if(gameIsOver) { // if the text on the Button was "NEXT"
            nextLevel();
            return;
        }
        // hide the keyboard
        String guess = et_guess.getText().toString();
        if(guess.isEmpty()) {
            mToast.setText(R.string.str_empty_guess_warn_toast);
            mToast.show();
        } else {
            hideSoftKeyboard(GameActivity.this);
            gameView.guess(et_guess.getText().toString());
        }
    }

    // override the method when the user clicks the back button (on the nav bar at the bottom
    // of the screen) to do the same thing as the top back button
    @Override
    public void onBackPressed() {
        backToMenu(null);
    }

}
