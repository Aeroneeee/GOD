package com.example.god;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.varunest.sparkbutton.SparkButton;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    PlayActivity playActivity = new PlayActivity();

    public SliderAdapter(Context context) {
        this.context = context;
    }

    //Arrays
    public  int[] slide_logo_image = {
        R.drawable.menu_car_game_logo,
        R.drawable.menu_monster_attack_logo
    };

    public  int[] slide_name_image = {
        R.drawable.road_trip_typography_logo,
        R.drawable.monster_attack_typography_logo
    };

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container,false);

        ImageView slideImageView = (ImageView) view.findViewById(R.id.nameId);
        SparkButton slideSparkButton = (SparkButton) view.findViewById(R.id.logoId);

        slideImageView.setImageResource(slide_name_image[position]);
        slideSparkButton.setActiveImage(slide_logo_image[position]);
        slideSparkButton.setInactiveImage(slide_logo_image[position]);
//
//        slideSparkButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(SliderAdapter.this, RoadGameActivity.class);
//
//            }
//        });

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}
