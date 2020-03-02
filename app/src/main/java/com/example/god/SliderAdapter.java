package com.example.god;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.varunest.sparkbutton.SparkButton;

public class SliderAdapter extends PagerAdapter {

    public Context getContext() {
        return context;
    }

    Context context;
    LayoutInflater layoutInflater;

    public Intent getIntent() {
        return gameIntent;
    }

    public void setIntent(Intent intent) {
        this.gameIntent = intent;
    }

    Intent gameIntent;

//    PlayActivity playActivity = new PlayActivity();

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
        final View view = layoutInflater.inflate(R.layout.slide_layout, container,false);

        ImageView slideImageView = view.findViewById(R.id.nameId);
        final SparkButton slideSparkButton = view.findViewById(R.id.logoId);

        slideImageView.setImageResource(slide_name_image[position]);
        slideSparkButton.setActiveImage(slide_logo_image[position]);
        slideSparkButton.setInactiveImage(slide_logo_image[position]);

        Animation zoom_in_move_up = AnimationUtils.loadAnimation(context, R.anim.zoom_in_move_up);
        Animation zoom_in_move_down = AnimationUtils.loadAnimation(context, R.anim.zoom_in_move_down);

        slideSparkButton.startAnimation(zoom_in_move_up);
        slideImageView.startAnimation(zoom_in_move_down);

        slideSparkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                slideSparkButton.playAnimation();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        Intent intent = new Intent(getContext(), RoadTripActivity.class);
                        context.startActivity(getIntent());
                    }
                }, 700);

            }
        });

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}
