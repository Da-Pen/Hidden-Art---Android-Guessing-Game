package me.dpeng.clickdots;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class CreditsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utilities.isDarkTheme ? R.style.DarkTheme : R.style.LightTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        // load statistics

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String s = "" +
                sharedPreferences.getInt(Utilities.KEY_GAMES_PLAYED, 0) + "\n" +
                sharedPreferences.getInt(Utilities.KEY_GAMES_WON, 0) + "\n";

        float avgScore = sharedPreferences.getFloat(Utilities.KEY_AVERAGE_SCORE, -1);
        if(avgScore == -1) {
            s += "-";
        } else {
            s += String.format(java.util.Locale.US,"%.1f", avgScore);
        }
        s += "\n";

        int bestScore = sharedPreferences.getInt(Utilities.KEY_BEST_SCORE, -1);
        if(bestScore == -1) {
            s += "-";
        } else {
            s += bestScore;
        }
        TextView tv = findViewById(R.id.tv_stats_right);
        tv.setText(s);

    }

    public void backToMenu(View view) {
        finish();
    }
}
