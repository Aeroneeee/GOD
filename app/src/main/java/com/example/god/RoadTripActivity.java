package com.example.god;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class RoadTripActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private GestureLibrary gestureLibrary = null;

    private double totalScore = 0;

    private TextView scoreText;
    public TextView timerText;
    private ImageView trafficLight;
    public LinearLayout backgroundLayout;

    private String[] sign = {"left", "right", "uturn", "overtake"};

    final Handler handler = new Handler();
    final Random random = new Random();
    private int r = random.nextInt(3);

    public int timeRemaining;

    public boolean isPaused = false;

    private long millisInFuture = 10000; //10 seconds
    public short countDownInterval = 1000; //1 second

    Dialog pauseDialog, gameOverDialog;

    GestureOverlayView gestureOverlayView;

    AnimationDrawable anim;

    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_view);
        fullscreen();

        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        trafficLight = findViewById(R.id.trafficLight);
        trafficLight.setImageResource(R.drawable.stop);
        backgroundLayout = findViewById(R.id.backgroundLayout);

//        changeAnim(R.drawable.straight_animation);
        animToDisplay(r, 0, true);
        init(getApplicationContext());

        //Ready countdown
        new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                switch ((int)(millisUntilFinished / 1000)) {
                    case 5:case 4:case 3:case 2:
                        trafficLight.setImageResource(R.drawable.stop); break;
                    case 1:
                        trafficLight.setImageResource(R.drawable.caution); break;
                    default:
                        trafficLight.setImageResource(R.drawable.go); break;
                }

            }

            @Override
            public void onFinish() {
                if (!isPaused) {
                    trafficLight.setVisibility(View.GONE);
                    anim.start();
                } else anim.stop();

                gestureOverlayView.setVisibility(View.VISIBLE);

                //Initialize a new CountDownTimer instance

                timer.start();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        anim.stop();
                        animToDisplay(r, 1, false);
                        anim.start();
                    }
                }, 1353);

            }
        }.start();

        //Game Over PopUp
        gameOverDialog = new Dialog(this, R.style.PauseDialog);
        gameOverDialog.setContentView(R.layout.gameover_view);
        Objects.requireNonNull(gameOverDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        gameOverDialog.setCanceledOnTouchOutside(false);
        gameOverDialog.setCancelable(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(gameOverDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        gameOverDialog.getWindow().setAttributes(lp);

        //Pause PopUp
        pauseDialog = new Dialog(this, R.style.PauseDialog);
        pauseDialog.setContentView(R.layout.pause_view);
        Objects.requireNonNull(pauseDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pauseDialog.setCanceledOnTouchOutside(false);
        pauseDialog.setCancelable(false);
        lp.copyFrom(pauseDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        pauseDialog.getWindow().setAttributes(lp);

        ImageButton pauseButton = findViewById(R.id.pauseButton);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPaused = true;

                anim.stop();

                pauseDialog.show();

            }
        });

        final ImageButton resumeButton = pauseDialog.findViewById(R.id.resumeButton);
        final ImageButton restartButton = pauseDialog.findViewById(R.id.restartButton);
        final ImageButton quitButton = pauseDialog.findViewById(R.id.quitButton);

        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                bounce(resumeButton);
                anim.start();
                pauseDialog.dismiss();
                fullscreen();
                isPaused = false;
                timer.start();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(restartButton);
                pauseDialog.dismiss();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(quitButton);
//                Intent intent = new Intent(GestureActivity.this, PlayActivity.class);
//                startActivity(intent);
                System.exit(0);

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();
    }

    private void doBindService() {
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
    private void doUnbindService() {
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

    private void init(Context context) {

        gestureOverlayView = findViewById(R.id.gesture_overlay_view);
        if(gestureOverlayView == null) throw new AssertionError();

        gestureOverlayView.setVisibility(View.INVISIBLE);

        gestureOverlayView.addOnGesturePerformedListener(this);

        gestureOverlayView.setGestureStrokeWidth(30);

        if(gestureLibrary == null) {

            gestureLibrary = GestureLibraries.fromRawResource(context, R.raw.gesture);

            if(!gestureLibrary.load()) {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setMessage("Custom gesture file load failed.");
                alertDialog.show();

                finish();
            }
        }

    }

    public void fullscreen() {
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

    /* When GestureOverlayView widget capture a user gesture it will run the code in this method.
           The first parameter is the GestureOverlayView object, the second parameter store user gesture information.*/
    @Override
    public void onGesturePerformed(final GestureOverlayView gestureOverlayView, Gesture gesture) {
        fullscreen();
        // Recognize the gesture and return prediction list.
        ArrayList<Prediction> predictionList = gestureLibrary.recognize(gesture);

        int size = predictionList.size();

        if(size > 0) {
            // Get the first prediction.
            Prediction firstPrediction = predictionList.get(0);

            String action = firstPrediction.name;

            double currentScore = firstPrediction.score;

            /* Higher score higher gesture match. */
            if(firstPrediction.score > 5) {

                if (this.sign[r].equals(action)) {

                    gestureOverlayView.setVisibility(View.INVISIBLE);

//                     The variable that will guard the frame number
                    int timeRemainingNumber = 0;

//                     Get the frame of the animation
                    Drawable currentFrame, checkFrame;
                    currentFrame = anim.getCurrent();

//                     Checks the position of the frame
                    for (int i = 0; i < anim.getNumberOfFrames(); i++) {

                        checkFrame = anim.getFrame(i);

                        if (checkFrame == currentFrame) {
                            timeRemainingNumber = (i*33);
                            break;
                        }

                    }
                    Log.i("TAG", "timeremaining: " + timeRemainingNumber);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            anim.stop();
                            animToDisplay(r, 2, true);
                            anim.start();

                            r = random.nextInt(3);

                            gestureOverlayView.setVisibility(View.VISIBLE);

                            timer.cancel();
                            timer.start();
                        }
                    }, timeRemainingNumber);

                    this.totalScore = this.totalScore + currentScore;

                    StringBuilder scoreStr = new StringBuilder();
                    scoreStr.append(Math.round(this.totalScore));
                    scoreText.setText(scoreStr);

                }

            }

        }
    }

    @Override
    public void onBackPressed() {
        isPaused = true;

        anim.stop();

        pauseDialog.show();
    }

    public void bounce(final ImageButton btn){
        btn.animate().scaleX(0.7f).scaleY(0.7f).setDuration(100).withEndAction(new Runnable() {
            @Override
            public void run() {
                btn.animate().scaleX(1f).scaleY(1f);
                fullscreen();
            }
        });
    }

    public CountDownTimer timer = new CountDownTimer(millisInFuture,countDownInterval){
        public void onTick(long millisUntilFinished){
            //do something in every tick
            if(isPaused) {
                //If the user request to paused the
                //CountDownTimer we will cancel the current instance
                cancel();
            } else {
                //Display the remaining seconds to app interface
                //1 second = 1000 milliseconds
                //Toast.makeText(getApplicationContext(), "TIMER!!!!! " + (millisUntilFinished / 1000), Toast.LENGTH_LONG).show();

                StringBuilder timerStr = new StringBuilder();
                timerStr.append(Math.round(millisUntilFinished / 1000));
                timerText.setText(timerStr);

                //Put count down timer remaining time in a variable
                timeRemaining = (int) millisUntilFinished;
            }
        }
        public void onFinish(){

            gameOver();

        }
    };

    public void gameOver(){
        gameOverDialog.show();
        anim.stop();

        final ImageButton retryGameOverButton = gameOverDialog.findViewById(R.id.retryGameOverButton);
        final ImageButton quitGameOverButton = gameOverDialog.findViewById(R.id.quitGameOverButton);
        final TextView scoreText = gameOverDialog.findViewById(R.id.scoreText);

        StringBuilder scoreStr = new StringBuilder();
        scoreStr.append(Math.round(this.totalScore));
        scoreText.setText(scoreStr);

        retryGameOverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(retryGameOverButton);
                gameOverDialog.dismiss();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        quitGameOverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(quitGameOverButton);
                System.exit(0);

//                Intent intent = new Intent(GestureActivity.this, PlayActivity.class);
//                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    public void animToDisplay(int rand, int ordinal, boolean oneShot){
        int[][] animArray = {{R.drawable.left1_animation, R.drawable.left2_animation, R.drawable.left3_animation}
                            ,{R.drawable.right1_animation, R.drawable.right2_animation, R.drawable.right3_animation}
                            ,{R.drawable.uturn1_animation, R.drawable.uturn2_animation, R.drawable.uturn3_animation}
                            ,{R.drawable.overtake1_animation, R.drawable.overtake2_animation, R.drawable.overtake3_animation}
                            ,{R.drawable.straight_animation}};
        backgroundLayout.setBackgroundResource(animArray[rand][ordinal]);
        anim = (AnimationDrawable)backgroundLayout.getBackground();
        anim.setOneShot(oneShot);
    }

}

