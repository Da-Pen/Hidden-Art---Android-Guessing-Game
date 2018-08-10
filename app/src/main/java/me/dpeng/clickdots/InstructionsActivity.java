package me.dpeng.clickdots;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class InstructionsActivity extends AppCompatActivity{

    private ViewPager viewPager;
    private SlideAdapter slideAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(Utilities.isDarkTheme) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_dots);
        tabLayout.setupWithViewPager(viewPager, true);
        slideAdapter = new SlideAdapter(this);
        viewPager.setAdapter(slideAdapter);
    }

    public void backToMenu(View view) {
        finish();
    }

}
