package com.example.god;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.varunest.sparkbutton.SparkButton;

public class PlayActivity extends AppCompatActivity {

    private ImageButton settingsBtn;
    private ViewPager slidePager;
    private LinearLayout dotsLayout;

    private TextView[] dots;

    private SliderAdapter sliderAdapter;

    private ImageButton nextButton;
    private ImageButton previousButton;

    private int currentPage;

    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        fullscreen();

        settingsBtn = findViewById(R.id.settingsBtnId);
        slidePager = findViewById(R.id.slidePagerId);
        dotsLayout = findViewById(R.id.dotsLayoutId);

        nextButton = findViewById(R.id.nextButtonId);
        previousButton = findViewById(R.id.previousButtonId);

        sliderAdapter = new SliderAdapter(this);
        slidePager.setAdapter(sliderAdapter);

        addDotsIndicator(0);
        slidePager.addOnPageChangeListener(viewListener);

        myDialog = new Dialog(this);

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(settingsBtn);
                ShowPopup(v);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidePager.setCurrentItem(currentPage + 1);
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidePager.setCurrentItem(currentPage - 1);
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

    public void addDotsIndicator(int position) {

        dots = new TextView[2];
        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {

            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(40);
            dots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));

            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0){
            dots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }

    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            addDotsIndicator(position);
            currentPage = position;

            if (position == 0) {
                nextButton.setEnabled(true);
                previousButton.setEnabled(false);
                nextButton.setVisibility(View.VISIBLE);
                previousButton.setVisibility(View.INVISIBLE);
            } else if (position == dots.length - 1) {
                nextButton.setEnabled(false);
                previousButton.setEnabled(true);
                nextButton.setVisibility(View.INVISIBLE);
                previousButton.setVisibility(View.VISIBLE);
            } else {
                nextButton.setEnabled(true);
                previousButton.setEnabled(true);
                nextButton.setVisibility(View.VISIBLE);
                previousButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

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

    public void ShowPopup(View v) {
        Button okButton;
        myDialog.setContentView(R.layout.settings_popup);
        okButton = (Button) myDialog.findViewById(R.id.okButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        myDialog.show();
    }
}
