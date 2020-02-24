package com.example.god;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class GestureActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private GestureOverlayView gestureOverlayView;

    private GestureLibrary gestureLibrary = null;

    private String action;

    private double currentScore = 0, totalScore = 0;

    private TextView scoreText;
    public TextView timerText;
    private ImageButton pauseButton;
    private ImageView trafficLight;

    private String[] sign = {"left", "right", "u-turn", "overtake"};

    final Random random = new Random();

    private int r = random.nextInt(3);
    public int timeRemaining;

    public boolean isPaused = false;

    public CountDownTimer timer;

    private long millisInFuture = 10000; //10 seconds
    public short countDownInterval = 1000; //1 second

    Dialog pauseDialog, gameOverDialog;

    GestureOverlayView gesture_overlay_view;

    AnimationDrawable anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_view);
        fullscreen();

        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        trafficLight = findViewById(R.id.trafficLight);
        trafficLight.setImageResource(R.drawable.stop);

        gesture_overlay_view = findViewById(R.id.gesture_overlay_view);
        if(gesture_overlay_view == null) throw new AssertionError();
        gesture_overlay_view.setBackgroundResource(R.drawable.straight_animation);

        anim = (AnimationDrawable)gesture_overlay_view.getBackground();

        //Game over POP UP
        gameOverDialog = new Dialog(this, R.style.PauseDialog);
        gameOverDialog.setContentView(R.layout.gameover_view);
        Objects.requireNonNull(gameOverDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        gameOverDialog.setCanceledOnTouchOutside(false);
        gameOverDialog.setCancelable(false);

        pauseDialog = new Dialog(this, R.style.PauseDialog);
        pauseDialog.setContentView(R.layout.pause_view);
        Objects.requireNonNull(pauseDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pauseDialog.setCanceledOnTouchOutside(false);
        pauseDialog.setCancelable(false);

        //Ready countdown
        new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                if ((millisUntilFinished / 1000) >= 2){
                    trafficLight.setImageResource(R.drawable.stop);
                } else if ((millisUntilFinished / 1000) == 1) {
                    trafficLight.setImageResource(R.drawable.caution);
                } else {
                    trafficLight.setImageResource(R.drawable.go);
                }

            }

            @Override
            public void onFinish() {
                if (!isPaused) {
                    trafficLight.setVisibility(View.INVISIBLE);
                    anim.start();
                } else anim.stop();

                init(getApplicationContext());

                //Initialize a new CountDownTimer instance
                timer = new CountDownTimer(millisInFuture,countDownInterval){
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
                }.start();
            }
        }.start();

        pauseButton = findViewById(R.id.pauseButton);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(GestureActivity.this, PausePopup.class));
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
                timer = new CountDownTimer(timeRemaining,countDownInterval){
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
                        //Toast.makeText(getApplicationContext(), "Game over !!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
//                        signText.setText("GAME OVER");
//                        anim.stop();
                        gameOver();
                    }
                }.start();
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
    }

    private void init(Context context) {

        gestureOverlayView = findViewById(R.id.gesture_overlay_view);

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

        if(gestureOverlayView == null) {
            gestureOverlayView = findViewById(R.id.gesture_overlay_view);
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
    public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
        fullscreen();
        // Recognize the gesture and return prediction list.
        ArrayList<Prediction> predictionList = gestureLibrary.recognize(gesture);

        int size = predictionList.size();

        if(size > 0) {
            StringBuilder messageBuffer = new StringBuilder();

            // Get the first prediction.
            Prediction firstPrediction = predictionList.get(0);

            this.action = firstPrediction.name;

            this.currentScore = firstPrediction.score;

            /* Higher score higher gesture match. */
            if(firstPrediction.score > 5) {

                if (this.sign[r].equals(this.action)) {

                    // The variable that will guard the frame number
                    int timeRemainingNumber = 0;

                    // Get the frame of the animation
//                    Drawable currentFrame, checkFrame;
//                    currentFrame = anim.getCurrent();

                    // Checks the position of the frame
//                    for (int i = 0; i < anim.getNumberOfFrames(); i++) {
//
//                        checkFrame = anim.getFrame(i);
//
//                        if (checkFrame == currentFrame) {
//                            timeRemainingNumber = (i*33);
//                            break;
//                        }
//
//                    }

                    if( this.sign[r].equals(this.sign[0]) ) {

//                        gesture_overlay_view.setBackgroundResource(R.drawable.left2_animation);

                    } else if ( this.sign[r].equals(this.sign[1]) ) {

//                        gesture_overlay_view.setBackgroundResource(R.drawable.right_animation);

                    } else if ( this.sign[r].equals(this.sign[2]) ){

                        gesture_overlay_view.setBackgroundResource(R.drawable.straight_animation);

                    }

                    anim = (AnimationDrawable)gesture_overlay_view.getBackground();
                    anim.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            gesture_overlay_view.setBackgroundResource(R.drawable.straight_animation);
                            anim = (AnimationDrawable)gesture_overlay_view.getBackground();
                            anim.start();
                        }
                    }, 2100); //42 frames * 50 ms

                    timer.cancel();

                    timer = new CountDownTimer(millisInFuture,countDownInterval){
                        public void onTick(long millisUntilFinished){

                            if(isPaused) {
                                //If the user request to pause
                                //CountDownTimer we will cancel the current instance
                                cancel();
                            } else {
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
                    }.start();

                    r = random.nextInt(3);

                    this.totalScore = this.totalScore + this.currentScore;

                    StringBuilder scoreStr = new StringBuilder();
                    scoreStr.append(Math.round(this.totalScore));
                    scoreText.setText(scoreStr);

                }

            }

        }
    }

    @Override
    public void onBackPressed() {
//        startActivity(new Intent(GestureActivity.this, PausePopup.class));
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
}
