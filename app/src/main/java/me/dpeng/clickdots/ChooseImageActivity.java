package me.dpeng.clickdots;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Created by Daniel Peng on 7/23/2018.
 *
 */

public class ChooseImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        ScrollView scrollView = findViewById(R.id.scrollView);
        LinearLayout linearLayout = findViewById(R.id.scrollLayout);

        for(int imageLocation: MenuActivity.IMAGES) {
            ImageView i = new ImageView(this);
            i.setImageResource(imageLocation);
            linearLayout.addView(i);
        }


    }
}
