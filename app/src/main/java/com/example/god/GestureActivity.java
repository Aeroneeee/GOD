package com.example.god;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class GestureActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private GestureOverlayView gestureOverlayView;

    private GestureLibrary gestureLibrary = null;

    private String action;

    private double currentScore = 0, totalScore = 0;

    private TextView scoreText, timerText, signText;
    private ImageButton pauseButton;

    private String[] sign = {"left", "right", "winding"};

    final Random random = new Random();

    private int r = random.nextInt(3);
    private int timeRemaining;

    private boolean isPaused = false;

    private CountDownTimer timer;

    private long millisInFuture = 10000; //10 seconds
    private long countDownInterval = 1000; //1 second

    Dialog pauseDialog;

    GestureOverlayView gesture_overlay_view;

    AnimationDrawable anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_view);
        fullscreen();

        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        signText = findViewById(R.id.signText);

        gesture_overlay_view = findViewById(R.id.gesture_overlay_view);
        if(gesture_overlay_view == null) throw new AssertionError();
        gesture_overlay_view.setBackgroundResource(R.drawable.straight_animation);

        anim = (AnimationDrawable)gesture_overlay_view.getBackground();

        //Ready countdown
        new CountDownTimer(3000, 1000) {
            Toast mToast = null;

            @Override
            public void onTick(long millisUntilFinished) {
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(getApplicationContext(), "" + ((millisUntilFinished / 1000)+ 1), Toast.LENGTH_SHORT);
                mToast.show();
            }

            @Override
            public void onFinish() {
                anim.start();
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(getApplicationContext(), "Go!", Toast.LENGTH_SHORT);
                mToast.show();

                init(getApplicationContext());

                //Toast.makeText(getApplicationContext(), "Draw the " + sign[r] + " sign", Toast.LENGTH_LONG).show();

                signText.setText(sign[r]);

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
                        //Toast.makeText(getApplicationContext(), "Game over !!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
                        signText.setText("GAME OVER");
                        anim.stop();
                    }
                }.start();
            }
        }.start();

        pauseDialog = new Dialog(this);

        pauseButton = findViewById(R.id.pauseButton);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                isPaused = true;

                anim.stop();

                pauseDialog.setContentView(R.layout.pause_view);
                Objects.requireNonNull(pauseDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                pauseDialog.setCanceledOnTouchOutside(false);
                pauseDialog.show();

                Button resumeButton = pauseDialog.findViewById(R.id.resumeButton);
                Button restartButton = pauseDialog.findViewById(R.id.restartButton);

                resumeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
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
                                signText.setText("GAME OVER");
                                anim.stop();
                            }
                        }.start();
                    }
                });

                restartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pauseDialog.dismiss();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

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
            gestureOverlayView = (GestureOverlayView)findViewById(R.id.gesture_overlay_view);
        }
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

                    timer.cancel();

                    timer = new CountDownTimer(millisInFuture,countDownInterval){
                        public void onTick(long millisUntilFinished){
                            //do something in every tick
                            if(isPaused)
                            {
                                //If the user request to paused the
                                //CountDownTimer we will cancel the current instance
                                cancel();
                            }
                            else {
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

                            signText.setText("GAME OVER");
                            anim.stop();
                        }
                    }.start();

                    r = random.nextInt(3);

                    this.totalScore = this.totalScore + this.currentScore;

                    StringBuilder scoreStr = new StringBuilder();
                    scoreStr.append(Math.round(this.totalScore));
                    scoreText.setText(scoreStr);

                    //Toast.makeText(getApplicationContext(), "Draw the " + this.sign[r] + " sign. Your Total score: " + scoreStr, Toast.LENGTH_LONG).show();

                    signText.setText(sign[r]);

                }
                messageBuffer.append("Your gesture match ")
                        .append(this.action)
                        .append(" with the score of ")
                        .append(this.currentScore)
                        .append(" Draw the ")
                        .append(this.sign[r]);

            } else {

                messageBuffer.append("Your gesture do not match any predefined gestures.");

            }

            // Display a snackbar with related messages.
            Snackbar snackbar = Snackbar.make(gestureOverlayView, messageBuffer.toString(), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}
