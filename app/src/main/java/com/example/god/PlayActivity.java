package com.example.god;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.varunest.sparkbutton.SparkButton;

import java.util.Objects;

public class PlayActivity extends AppCompatActivity {

    private ImageButton settingsBtn;
    private SparkButton logo;
    private ImageView name;

    private ViewPager slidePager;
    private LinearLayout dotsLayout;

    private TextView[] dots;

    private SliderAdapter sliderAdapter;

    private ImageButton nextButton;
    private ImageButton previousButton;

    private int currentPage;

    Dialog quitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

    //BIND Music Service
        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);

//        startService(new Intent(this, MusicService.class));
        fullscreen();

        settingsBtn = findViewById(R.id.settingsBtnId);
        logo = findViewById(R.id.logoId);
        name = findViewById(R.id.nameId);

        slidePager = findViewById(R.id.slidePagerId);
        dotsLayout = findViewById(R.id.dotsLayoutId);

        nextButton = findViewById(R.id.nextButtonId);
        previousButton = findViewById(R.id.previousButtonId);

        sliderAdapter = new SliderAdapter(this);
        slidePager.setAdapter(sliderAdapter);

        addDotsIndicator(0);
        slidePager.addOnPageChangeListener(viewListener);

        quitDialog = new Dialog(this, R.style.PauseDialog);
        Objects.requireNonNull(quitDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quitDialog.setContentView(R.layout.exit_view);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(quitDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        quitDialog.getWindow().setAttributes(lp);


        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(settingsBtn);
                Intent intent = new Intent(PlayActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_top, R.anim.fade_out);
            }
        });


//        Animation zoom_in_move_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_move_up);
//        Animation zoom_in_move_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_move_down);
//
//        logo.startAnimation(zoom_in_move_up);
//        name.startAnimation(zoom_in_move_down);

//        logo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                logo.playAnimation();
//                Intent intent = new Intent(PlayActivity.this, GestureActivity.class);
//                startActivity(intent);
//                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//            }
//        });



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

    private void doBindService() {
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
    private void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    //Bind/Unbind music service
    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Detect idle screen
        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //UNBIND music service
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);

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



    @Override
    public void onBackPressed() {

        final ImageButton yesButton = quitDialog.findViewById(R.id.yesButton);
        final ImageButton noButton = quitDialog.findViewById(R.id.noButton);

        mServ.pauseMusic();
        quitDialog.show();

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(yesButton);
                mServ.stopMusic();
                finish();
                System.exit(0);
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(noButton);
                mServ.resumeMusic();
                quitDialog.dismiss();
            }
        });
    }
}
