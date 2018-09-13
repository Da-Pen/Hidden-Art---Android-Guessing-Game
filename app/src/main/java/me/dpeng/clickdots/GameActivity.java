package me.dpeng.clickdots;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * The Activity for the Game. The interface is created here rather than in the XML file. The
 * interface includes back button, score count and give up button across the top, the GameView
 * (i.e the container for the actual game) in the middle, and the bar to guess the hidden image
 * across the bottom.
 */
public class GameActivity extends AppCompatActivity implements ConfirmResignDialogFragment.DialogListener, NavigationView.OnNavigationItemSelectedListener{

    final private static int REQUEST_SCREENSHOT = 0;

    public Resources res;

    public GameViewModel model;

    // space at the sides of the screen (we need some space because if there is none then it is
    // difficult for the user to click the dots at the edge of the screen.
    //public static int SIDE_MARGIN;
    private GameView gameView;
    private ConstraintLayout layout; // the layout for the entire activity

    private ConstraintLayout guessBarLayout; // the layout for the bottom bar of the activity
    private EditText et_guess;
    private Button btn_clear_guess;
    private boolean isXHidden = true;
    private Button btn_guess;


    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor prefEditor;

    private TextView tv_score; // TextView to keep track of the # of clicks the user has made
    public boolean isGameOver = false; // if the game is over (this can be if the user won OR lost)

    private NavigationView nav;
    private Menu menu;
    private MenuItem toggleSquareModeItem;
    private View headerView;
    private TextView tv_levelNumber;

    private boolean shouldSaveProgress = true;

    public Toast mToast;

    @Override
    protected void onStop() {
        if(shouldSaveProgress) saveProgressInSharedPref();
        super.onStop();
    }

    /**
     * Sets up the layout and instantiates the GameView.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        res = getResources();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = sharedPreferences.edit();

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

        model = ViewModelProviders.of(this).get(GameViewModel.class);


        // set the layout
        setContentView(R.layout.activity_game);
        // get the layout
        layout = findViewById(R.id.game_layout);

        // initialize the listener for the drawer
        nav = findViewById(R.id.nav_view);
        nav.setNavigationItemSelectedListener(this);

        // set the "square mode" button as the right one (square or circle)
        menu = nav.getMenu();
        toggleSquareModeItem = menu.findItem(R.id.nav_toggle_square_mode);

        headerView = nav.getHeaderView(0);
        tv_levelNumber = headerView.findViewById(R.id.tv_levelNumber);
        // set the name as "Level x", adding 1 because the number stored in sharedPref is the index
        tv_levelNumber.setText(String.format(res.getString(R.string.str_nav_level),
                sharedPreferences.getInt(Utilities.KEY_LEVEL_NUMBER, 0) + 1));

        toggleSquareModeItem.setIcon(
                Utilities.isSquareMode ? R.drawable.vector_square : R.drawable.vector_circle
        );

        gameView = findViewById(R.id.gameView);
        ConstraintLayout.LayoutParams gameViewLayout = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, 0);
        gameViewLayout.leftMargin = gameView.sideMargin;
        gameViewLayout.rightMargin = gameView.sideMargin;
        gameView.setLayoutParams(gameViewLayout);

        // initialize the model's variables if they have not already been initiated
//        if(model.bitmap == null) {
//            model.bitmap = Bitmap.createBitmap(gameView.viewDiameter, gameView.viewDiameter, Bitmap.Config.ARGB_8888);
//        }
//        if(model.dots == null) {
//            model.dots = new ArrayList<>();
//        }

        gameView.init(this);

        tv_score = findViewById(R.id.tv_score);
        int score = sharedPreferences.getInt(Utilities.KEY_SCORE, 0);
        tv_score.setText("" + score);
        gameView.score = score;

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
                        mToast.setText(R.string.str_toast_empty_guess_warn);
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
        String oldText = sharedPreferences.getString(Utilities.KEY_GUESSES_BAR_TEXT, "");
        et_guess.setText(oldText);
        et_guess.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if((s.length() == 0 && !isXHidden) || isGameOver) {
                    hideX();
                } else if (s.length() != 0 && isXHidden) {
                    showX();
                }
            }
        });

        btn_clear_guess = findViewById(R.id.btn_clear_guess);
        btn_guess = findViewById(R.id.btn_guess);


        // if the game was over last time then show the next button
        if(sharedPreferences.getBoolean(Utilities.KEY_IS_GAME_OVER, false)) {
            showNextButton();
        }

        if(oldText.isEmpty()) {
            hideX();
        }
    }

    // convert the guess bar into the next button
    // opposite of showGuessBar()
    public void showNextButton() {
        // change the GUESS button to the NEXT button
        //TransitionManager.beginDelayedTransition(layout);
        et_guess.setEnabled(false);
        btn_clear_guess.setEnabled(false);
        btn_clear_guess.setText("");
        btn_guess.setText(R.string.str_next);
        // ViewGroup.LayoutParams layoutParams = guessBarLayout.getLayoutParams();
        // layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        isGameOver = true;
    }

    // convert the next button into the guess bar
    // opposite of showNextButton()
    public void showGuessBar() {
        //TransitionManager.beginDelayedTransition(layout);
        ConstraintLayout.LayoutParams et_guessParams = (ConstraintLayout.LayoutParams)et_guess.getLayoutParams();
        et_guessParams.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        btn_clear_guess.setText(R.string.str_btn_clear_guess);
        et_guess.setEnabled(true);
        et_guess.setText("");
        ConstraintLayout.LayoutParams btn_clear_guessParams = (ConstraintLayout.LayoutParams)btn_clear_guess.getLayoutParams();
        btn_clear_guessParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        btn_clear_guess.setEnabled(true);
        btn_guess.setText(R.string.str_btn_guess);
//        ViewGroup.LayoutParams layoutParams = guessBarLayout.getLayoutParams();
//        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    // sets the number of clicks. When the user clicks a dot then this is called and it
    // updates the counter at the top of the screen.
    public void setScore(int score) {
        tv_score.setText("" + score);
    }

    // what to do if the user clicks "CONFIRM" on the dialog "Are you sure you want to give up?"
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // TODO resign
        gameView.gameOver();
    }

    // what to do if the user clicks "CANCEL" on the dialog "Are you sure you want to give up?"
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private void nextLevel() {

        // set the level number in SharedPreferences
        // get the last level number
        int lastLevel = sharedPreferences.getInt(Utilities.KEY_LEVEL_NUMBER, 0);
        prefEditor.putInt(Utilities.KEY_LEVEL_NUMBER, lastLevel + 1);
        // clear the level progress
        resetSharedPreferences(false);


        model.sourceRevealed = false;
        // change the Level in the navigation bar to the next level
        // we need to add 2 to lastLevel: 1 because indices start at 0
        // and 1 because it is the LAST level
        tv_levelNumber.setText(String.format(res.getString(R.string.str_nav_level),
                lastLevel + 2));
        // change score back to 0
        prefEditor.putInt(Utilities.KEY_SCORE, 0);
        prefEditor.apply();


        setScore(0);

        // change the model values back to their defaults
        model.bitmap = null;
        model.dots = null;

        // set the next button back to the guess bar
        showGuessBar();
        // change the gameView to the next level
        gameView.onImageURLLoaded();
        isGameOver = false;
        hideX();
    }


    /**
     * Opens the drawer.
     * @param view the wrapper for the drawer (i.e the NavigationView object)
     */
    public void openDrawer(View view) {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.openDrawer(findViewById(R.id.nav_view));
    }

    // called when the user selects an item in the menu at the right side of the screen
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.nav_toggle_square_mode: {
                // change the value stored in SharedPreferences
                Utilities.isSquareMode = !sharedPreferences.getBoolean(Utilities.KEY_IS_SQUARE_MODE, false);
                prefEditor.putBoolean(Utilities.KEY_IS_SQUARE_MODE, Utilities.isSquareMode);
                prefEditor.apply();

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

                // change the theme
                Utilities.isDarkTheme = !sharedPreferences.getBoolean(Utilities.KEY_IS_DARK_THEME, false);
                prefEditor.putBoolean(Utilities.KEY_IS_DARK_THEME, Utilities.isDarkTheme);
                // if the game is running then save the score
                if(gameView != null && !gameView.isLoading)  {
                        prefEditor.putInt(Utilities.KEY_SCORE, gameView.score);
                } else {
                    prefEditor.putInt(Utilities.KEY_SCORE, 0);
                }
                prefEditor.putBoolean(Utilities.KEY_IS_GAME_OVER, isGameOver);
                prefEditor.apply();

                // since the progress is saved in the ViewModel we do not need to save it
                // in SharedPreference. so set shouldSaveProgress to false so onStop doesn't save
                // in SharedPreferences.
                shouldSaveProgress = false;

                // recreate the activity
                recreate();
                break;
            }

            case R.id.nav_screenshot: {
                screenshot();
                break;
            }

            case R.id.nav_resign: {
                resign();
                break;
            }
        }

        // close the drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(findViewById(R.id.nav_view));

        return true;
    }

    /**
     * goes back to the main menu.
     * @param view
     */
    public void backToMenu(View view) {

        finish();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
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

    public void clearGuess(View view) {
        et_guess.setText("");
    }

    // called when the user clicks the "GUESS" Button
    public void guess(View view) {
        if(isGameOver) { // if the text on the Button was "NEXT"
            nextLevel();
            return;
        }
        // hide the keyboard
        String guess = et_guess.getText().toString();
        if(guess.isEmpty()) {
            mToast.setText(R.string.str_toast_empty_guess_warn);
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

    // called when the user resigns (from the right menu)
    private void resign() {
        // if the game is already over then there is no need to resign
        if(isGameOver) {
            mToast.setText(R.string.str_toast_give_up_when_finished);
            mToast.show();
            return;
        }
        // reveal the source image
        // possibly call the function called when they lose
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ConfirmResignDialogFragment dialog = new ConfirmResignDialogFragment();
        dialog.show(ft, "dialog");
    }

    // saves the game bitmap in the user's external storage
    private void screenshot() {

        if(model == null || model.bitmap == null) {
            mToast.setText(R.string.str_toast_screenshot_fail);
            mToast.show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // dynamically request permission to write to external storage
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_SCREENSHOT);
                return;
            }
        }

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/" + getResources().getString(R.string.str_folder_name) + "/");
        myDir.mkdirs();

        String fname = "Image-0.png";
        File file = new File(myDir, fname);

        // make sure we do not accidentally name it something that already exists
        int saveNum = 0;
        while(file.exists()) {
            saveNum++;
            file = new File(myDir, "Image-" + saveNum + ".png");
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            model.bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            mToast.setText(R.string.str_toast_screenshot_success);
            mToast.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    // used for screenshot, when the app requests to access the user's external storage
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // if the permission was granted then take the screenshot
        if(requestCode == REQUEST_SCREENSHOT) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                screenshot();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void resetSharedPreferences(boolean gameOver) {
        prefEditor.putString(Utilities.KEY_LEVEL_PROGRESS, "");
        prefEditor.putInt(Utilities.KEY_SCORE, 0);
        prefEditor.putInt(Utilities.KEY_GUESSES_LEFT, gameView.START_GUESSES);
        prefEditor.putBoolean(Utilities.KEY_IS_GAME_OVER, gameOver);
        isGameOver = gameOver;
        prefEditor.apply();
    }

    private void saveProgressInSharedPref() {
        if(gameView != null) {
            prefEditor.putInt(Utilities.KEY_SCORE, gameView.score);
            prefEditor.putString(Utilities.KEY_GUESSES_BAR_TEXT, et_guess.getText().toString());
            if(!isGameOver) {
                prefEditor.putInt(Utilities.KEY_GUESSES_LEFT, gameView.guessesLeft);
                prefEditor.putString(Utilities.KEY_LEVEL_PROGRESS, gameView.dotsToString());
            }
            prefEditor.putBoolean(Utilities.KEY_IS_GAME_OVER, isGameOver);
            prefEditor.apply();
        }
    }

    // hides the "X" button used to clear text
    private void hideX() {
        btn_clear_guess.setEnabled(false);
        btn_clear_guess.setText("");
        // remove width if X
        ViewGroup.LayoutParams lp = btn_clear_guess.getLayoutParams();
        lp.width = 1;
        btn_clear_guess.setLayoutParams(lp);
        isXHidden = true;
    }

    // shows the "X" button used to clear text
    private void showX() {
        btn_clear_guess.setEnabled(true);
        btn_clear_guess.setText(R.string.str_btn_clear_guess);
        // add width if X
        ViewGroup.LayoutParams lp = btn_clear_guess.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        btn_clear_guess.setLayoutParams(lp);
        isXHidden = false;
    }

    public void setLevelNumber(int x){
        tv_levelNumber.setText(String.format(res.getString(R.string.str_nav_level), x));
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

}
