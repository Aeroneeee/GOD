package com.example.god;
import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class RoadTripActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private GestureLibrary gestureLibrary = null;

    private double totalScore = 0;

    private TextView scoreText, timerText, levelText, readyLabel;
    private ImageView trafficLight;
    public LinearLayout backgroundLayout;

    final Handler handler = new Handler();
    final Random random = new Random();

    private String[] sign = {"left", "right", "uturn", "overtake"};

    public int[] randRangePerLvl = {0, 2, 2, 2, 3, 3, 3, 4, 4, 4};

    private int r = 0, stage = 0, level = 0;

    public int timeRemaining, turnAnim = 0, startAnim = 0;

    public byte[] numOfDrawPerLvl = {0, 5 ,7 ,10, 10, 10, 12, 15, 20, 30};

    public boolean isPaused = false;
    public boolean canEnd = false;

    Dialog pauseDialog, gameOverDialog;

    GestureOverlayView gestureOverlayView;

    AnimationDrawable anim;

    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.road_trip_gesture_view);
        fullscreen();

        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        levelText = findViewById(R.id.levelText);
        trafficLight = findViewById(R.id.trafficLight);
        trafficLight.setImageResource(R.drawable.stop);
        backgroundLayout = findViewById(R.id.backgroundLayout);
        readyLabel = findViewById(R.id.readyLabel);
        init(getApplicationContext());

        //Ready countdown
        new CountDownTimer(5000, 1000) {
            String[] ready = {"Ready", "Set", "Go!"};
            @Override
            public void onTick(long millisUntilFinished) {

                switch ((int)(millisUntilFinished / 1000)) {
                    case 5:case 4:case 3:case 2:
                        trafficLight.setImageResource(R.drawable.stop);
                        readyLabel.setText(ready[0]);
                        break;
                    case 1:
                        trafficLight.setImageResource(R.drawable.caution);
                        readyLabel.setText(ready[1]);
                        break;
                    default:
                        trafficLight.setImageResource(R.drawable.go);
                        readyLabel.setText(ready[2]);
                        break;
                }

            }

            @Override
            public void onFinish() {
                if (!isPaused) {
                    level++;
                    stage++;
                    r = random.nextInt(randRangePerLvl[level]);
                    trafficLight.setVisibility(View.GONE);
                    readyLabel.setVisibility(View.GONE);
                    animToDisplay(r, 0, true);
                } else anim.stop();

                gestureOverlayView.setVisibility(View.VISIBLE);
                counter(false);

                //Initialize a new CountDownTimer instance
//                timer.start();
                counter(true);
                showLevel.start();

                if (r == 4) startAnim = 907;
                else startAnim = 1303;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    animToDisplay(r, 1, false);
                    }
                }, startAnim);

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
                counter(true);
//                timer.start();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(restartButton);
                pauseDialog.dismiss();
                anim.stop();
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
//
//    private void doBindService() {
//        bindService(new Intent(this,MusicService.class),
//                Scon, Context.BIND_AUTO_CREATE);
//        mIsBound = true;
//    }
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
            if((firstPrediction.score > 5) && (this.sign[r].equals(action))) {

                counter(false);
                canEnd = false;

                if (r == 4) {
                    turnAnim = 1485;
                    startAnim = 400;
                } else {
                    turnAnim = 2640;
                    startAnim = 1303;
                }

                Log.i("TAG", "run: turning");
                animToDisplay(r, 2, true);

                if (stage == numOfDrawPerLvl[level]) {
                    stage = 0;
                    level++;

                    showLevel.start();
                }

                stage++;
                r = random.nextInt(randRangePerLvl[level]);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    Log.i("TAG", "run: new gesture");
                    animToDisplay(r, 0, true);

//                        timer.start();
                    counter(true);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("TAG", "run: looping");

                            animToDisplay(r, 1, false);
                        }
                    }, startAnim);

                    }
                }, turnAnim);

                this.totalScore = this.totalScore + currentScore;

                StringBuilder scoreStr = new StringBuilder();
                scoreStr.append(Math.round(this.totalScore));
                scoreText.setText(scoreStr);
            }
        }
    }

    CountDownTimer timer10 = new CountDownTimer(10000, 1000) {
        public void onTick(long millisUntilFinished) {

            if (isPaused) {
                Log.i("TAG", "PAUSED!!!!");
                cancel();

            } else {
                StringBuilder timerStr = new StringBuilder();
                timerStr.append(Math.round((int)(millisUntilFinished / 1000)));
                timerText.setText(timerStr);

                timeRemaining = (int) millisUntilFinished;
            }
        }

        public void onFinish() {

            gameOver();

        }
    };
    CountDownTimer timer8 = new CountDownTimer(8000, 1000) {
        public void onTick(long millisUntilFinished) {
            canEnd = true;
            if (isPaused) {

                cancel();

            } else {
                StringBuilder timerStr = new StringBuilder();
                timerStr.append(Math.round((int)(millisUntilFinished / 1000)));
                timerText.setText(timerStr);

                timeRemaining = (int) millisUntilFinished;
            }
        }

        public void onFinish() {
            Log.i("TAG", "Charrrr");
            if (canEnd) gameOver();

        }
    };
    CountDownTimer timer6 = new CountDownTimer(6000, 1000) {
        public void onTick(long millisUntilFinished) {

            if (isPaused) {

                cancel();

            } else {
                StringBuilder timerStr = new StringBuilder();
                timerStr.append(Math.round((int)(millisUntilFinished / 1000)));
                timerText.setText(timerStr);

                timeRemaining = (int) millisUntilFinished;
            }
        }

        public void onFinish() {

            gameOver();

        }
    };
    CountDownTimer timer4 = new CountDownTimer(4000, 1000) {
        public void onTick(long millisUntilFinished) {

            if (isPaused) cancel();
            else {
                StringBuilder timerStr = new StringBuilder();
                timerStr.append(Math.round((int)(millisUntilFinished / 1000)));
                timerText.setText(timerStr);

                timeRemaining = (int) millisUntilFinished;
            }
        }

        public void onFinish() {

            gameOver();

        }
    };

    public void counter (boolean run) {
        timer10.cancel();
        timer8.cancel();
        timer6.cancel();
        timer4.cancel();

        if (run) {
            Log.i("TAG", "counter: STAAAAART");

            switch (level) {
                case 1: case 2: case 3:
                    timer10.start();
                    break;
                case 4: case 5: case 6:
                    timer8.start();
                    break;
                case 7: case 8: case 9:
                    timer6.start();
                    break;
                default: timer4.start();
            }
        }

    }

    public CountDownTimer showLevel = new CountDownTimer(3000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            StringBuilder lvlStr = new StringBuilder();
            lvlStr.append("Level ").append(Math.round(level));
            levelText.setText(lvlStr);
            levelText.setVisibility(View.VISIBLE);
        }
        @Override
        public void onFinish() {
            levelText.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onBackPressed() {
        isPaused = true;

        if (anim.isRunning()) anim.stop();

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

    public void gameOver(){
        anim.stop();
        gameOverDialog.show();

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
                            ,{R.drawable.overtake1_animation, R.drawable.overtake2_animation, R.drawable.overtake3_animation}};

        backgroundLayout.setBackgroundResource(animArray[rand][ordinal]);
        anim = (AnimationDrawable)backgroundLayout.getBackground();

        anim.setOneShot(oneShot);
        anim.start();
    }

}

