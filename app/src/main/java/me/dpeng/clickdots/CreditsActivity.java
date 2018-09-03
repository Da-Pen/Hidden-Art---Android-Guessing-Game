package me.dpeng.clickdots;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class CreditsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utilities.isDarkTheme ? R.style.DarkTheme : R.style.LightTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        TextView text = findViewById(R.id.textView2);
        text.setText(R.string.str_credits_text);
    }

    public void backToMenu(View view) {
        finish();
    }
}
