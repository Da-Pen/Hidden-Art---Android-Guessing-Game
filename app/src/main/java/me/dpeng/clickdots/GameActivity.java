package me.dpeng.clickdots;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // get the intent from the last activity
        Intent intent = getIntent();
        int imageLocation = intent.getIntExtra(MenuActivity.IMAGE, R.drawable.lion);
        ImageView imageView = findViewById(R.id.mainImage);
        imageView.setImageResource(R.drawable.lion);
    }
}
