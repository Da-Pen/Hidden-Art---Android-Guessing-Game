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

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

public class SlideAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;

    // list of images
    private int[] imageIds = {
            R.drawable.instructions_1,
            R.drawable.instructions_2,
            R.drawable.instructions_3,
            R.drawable.instructions_4,
            R.drawable.instructions_5
    };

    private String[] titles = {
            "Click a Dot",
            "It will split into four smaller dots.",
            "Soon an image will be revealed.",
            "Guess what the hidden image is. You get 3 tries!",
            "You score is the number of clicks you have made. The smaller the score, the better!"
    };

    private String[] descriptions = { "", "", "", "", "" };

    private int bgColor = 0;



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
        TextView title = view.findViewById(R.id.tv_slide_title);
        TextView description = view.findViewById(R.id.tv_slide_description);
        layout.setBackgroundColor(bgColor);
        Glide.with(context).load(imageIds[position]).into(image);
        title.setText(titles[position]);
        description.setText(descriptions[position]);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
