package me.dpeng.clickdots;

import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * This class is used to store information about the game so that when the activity is recreated
 * (ex. when the user changes the theme), we do not need to recalculate all of these variables.
 */
public class GameViewModel extends ViewModel {
    public Bitmap bitmap;
    public ArrayList<Dot> dots;
    public boolean sourceRevealed;
}
