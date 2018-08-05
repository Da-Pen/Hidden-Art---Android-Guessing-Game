package me.dpeng.clickdots;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlideAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;

    // list of images
    public int[] imageIds = {
            R.drawable.black_square,
            R.drawable.brown_square
    };

    public String[] titles = {
            "TITLE1",
            "TITLE2"
    };

    public int[] bgColors = {
            Color.RED,
            Color.BLUE
    };

    public SlideAdapter(Context context) {
        this.context = context;
    }


    @Override
    public int getCount() {
        return imageIds.length;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.instructions_slide, container, false);
        LinearLayout layout = view.findViewById(R.id.slide_linear_layout);
        ImageView image = view.findViewById(R.id.slide_image);
        TextView title = view.findViewById(R.id.slide_title);
        layout.setBackgroundColor(bgColors[position]);
        image.setImageResource(imageIds[position]);
        title.setText(titles[position]);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout)object;
    }
}
