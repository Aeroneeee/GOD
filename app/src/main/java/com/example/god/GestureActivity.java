package com.example.god;
import android.app.AlertDialog;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Random;

public class GestureActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private GestureOverlayView gestureOverlayView;

    private GestureLibrary gestureLibrary = null;

    private String action;

    private double currentScore = 0, totalScore = 0;

    private TextView scoreText;
    private ImageButton pauseButton;

    private String[] sign = {"left", "right", "winding"};

    final Random random = new Random();

    private int r = random.nextInt(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_view);
        fullscreen();

        scoreText = findViewById(R.id.scoreText);
        pauseButton = findViewById(R.id.pauseButton);
        timer.start();
    }

    //Ready countdown
    CountDownTimer timer = new CountDownTimer(3000, 1000) {
        Toast mToast = null;
        @Override
        public void onTick(long millisUntilFinished) {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(getApplicationContext(), ""+(millisUntilFinished/1000) + 1, Toast.LENGTH_SHORT);
            mToast.show();
        }

        @Override
        public void onFinish() {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(getApplicationContext(), "Go!", Toast.LENGTH_SHORT);
            mToast.show();

            Context context = getApplicationContext();
            init(context);

            Toast.makeText(getApplicationContext(), "Draw the " + sign[r] + " sign", Toast.LENGTH_LONG).show();

//            pauseButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
        }
    };

    private void init(Context context) {

        gestureOverlayView = findViewById(R.id.gesture_overlay_view);

        gestureOverlayView.addOnGesturePerformedListener(this);

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

                    r = random.nextInt(3);

                    this.totalScore = this.totalScore + this.currentScore;

                    StringBuilder scoreStr = new StringBuilder();
                    scoreStr.append(Math.round(this.totalScore));
                    scoreText.setText(scoreStr);

                    Toast.makeText(getApplicationContext(), "Draw the " + this.sign[r] + " sign. Your Total score: " + scoreStr, Toast.LENGTH_LONG).show();

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
