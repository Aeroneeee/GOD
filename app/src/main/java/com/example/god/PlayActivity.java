package com.example.god;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.varunest.sparkbutton.SparkButton;

public class PlayActivity extends AppCompatActivity {

    private ImageButton settingsBtn;
    private ViewPager slidePager;
    private LinearLayout dotsLayout;

    private SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        fullscreen();

        settingsBtn = findViewById(R.id.settingsBtnId);
        slidePager = findViewById(R.id.slidePagerId);
        this.dotsLayout = findViewById(R.id.dotsLayoutId);

        sliderAdapter = new SliderAdapter(this);
        slidePager.setAdapter(sliderAdapter);

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(settingsBtn);
            }
        });

    }

    private void bounce(final ImageButton btn){
        btn.animate().scaleX(0.7f).scaleY(0.7f).setDuration(100).withEndAction(new Runnable() {
            @Override
            public void run() {
                btn.animate().scaleX(1f).scaleY(1f);
                fullscreen();
            }
        });
    }

    

    private void fullscreen() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }
}
