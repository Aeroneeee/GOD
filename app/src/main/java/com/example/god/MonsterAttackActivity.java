package com.example.god;

import android.app.AlertDialog;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Random;

public class MonsterAttackActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private GestureOverlayView mGestureOverlayView;

    private GestureLibrary mGestureLibrary = null;

    private ImageView monster1, monster2, monster3, monster4, monster5;
    private ImageView ninja;
    private TextView readyLabel;

    private TranslateAnimation monsterTrack1 = new TranslateAnimation(0, 150, 0, -1300);
    private TranslateAnimation monsterTrack2 = new TranslateAnimation(0, 0, 0, -1300);
    private TranslateAnimation monsterTrack3 = new TranslateAnimation(0, -150, 0, -1300);

    private AnimationDrawable monsterAnim1, monsterAnim2, monsterAnim3;
    private AnimationDrawable ninjaAnim;

    final Handler handler = new Handler();

    final Random random = new Random();

    private int r1 = 0, r2 = 0, r3 = 0, level = 0, wave = 0;

    private String[] actionList = {"ver", "hor", "down", "up", "heart", "close", "open"};

    private int[] randRangePerLvl = {0, 2, 2, 2, 4, 4, 4, 6, 6, 6, 7};

    private byte[] numOfWavePerLvl = {0, 5 ,7 ,10, 10, 10, 12, 15, 20, 30};

    private int[] durationPerLvl = {0, 15000, 15000, 10000, 10000, 8000, 8000, 8000, 5000, 5000, 5000};

    private boolean killedMonster1, killedMonster2, killedMonster3;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monster_attack_gesture_view);
        fullscreen();

        init(getApplicationContext());

        readyLabel = findViewById(R.id.readyLabel);

        monster1 = findViewById(R.id.monster1);
        monster2 = findViewById(R.id.monster2);
        monster3 = findViewById(R.id.monster3);

        ninja = findViewById(R.id.ninja);

        new CountDownTimer(5000, 1000) {
            String[] ready = {"Ready", "Set", "Fight!"};
            @Override
            public void onTick(long millisUntilFinished) {

                switch ((int)(millisUntilFinished / 1000)) {
                    case 5:case 4:case 3:case 2:
                        readyLabel.setText(ready[0]);
                        break;
                    case 1:
                        readyLabel.setText(ready[1]);
                        break;
                    default:
                        readyLabel.setText(ready[2]);
                        break;
                }

            }

            @Override
            public void onFinish() {

                readyLabel.setVisibility(View.GONE);
                mGestureOverlayView.setVisibility(View.VISIBLE);

                ninja.setBackgroundResource(R.drawable.ninja_idle);
                ninjaAnim = (AnimationDrawable)ninja.getBackground();
                ninjaAnim.start();

                level = 1;

                killedMonster1 = killedMonster2 = killedMonster3 = false;

                generateWave(randRangePerLvl[level], durationPerLvl[level]);

            }
        }.start();
    }

    private void init(Context context) {
        if(mGestureLibrary == null) {
            // Load custom gestures from gesture.txt file.

            mGestureLibrary = GestureLibraries.fromRawResource(context, R.raw.gesture_monster);

            if(!mGestureLibrary.load()) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setMessage("Custom gesture file load failed.");
                alertDialog.show();

                finish();
            }
        }

        if(mGestureOverlayView == null) {
            mGestureOverlayView = (GestureOverlayView)findViewById(R.id.monster_gesture_overlay_view);
        }

        mGestureOverlayView.setVisibility(View.INVISIBLE);
        mGestureOverlayView.addOnGesturePerformedListener(this);
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        fullscreen();

        ArrayList<Prediction> predictionList = mGestureLibrary.recognize(gesture);

        int size = predictionList.size();

        if(size > 0) {

            // Get the first prediction.
            Prediction firstPrediction = predictionList.get(0);

            String action = firstPrediction.name;

            /* Higher score higher gesture match. */
            if(firstPrediction.score > 5) {

                if (action.equals(actionList[r1])){

                    monster1.clearAnimation();
                    monster1.setVisibility(View.INVISIBLE);
                    killedMonster1 = true;
                }

                if (action.equals(actionList[r2])){

                    monster2.clearAnimation();
                    monster2.setVisibility(View.INVISIBLE);
                    killedMonster2 = true;

                }

                if (action.equals(actionList[r3])){

                    monster3.clearAnimation();
                    monster3.setVisibility(View.INVISIBLE);
                    killedMonster3 = true;
                    nextWave();

                }

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

    private void generateWave(int range, int duration){
        int[] animArray = {R.drawable.monster_a,
                            R.drawable.monster_b,
                            R.drawable.monster_c,
                            R.drawable.monster_d,
                            R.drawable.monster_e,
                            R.drawable.monster_f,
                            R.drawable.monster_g};

        monster1.setVisibility(View.VISIBLE);
        monster2.setVisibility(View.VISIBLE);
        monster3.setVisibility(View.VISIBLE);

        r1 = random.nextInt(range);
        r2 = random.nextInt(range);
        r3 = random.nextInt(range);

        monster1.setImageResource(animArray[r1]);
        monster2.setImageResource(animArray[r2]);
        monster3.setImageResource(animArray[r3]);

        monsterAnim1 = (AnimationDrawable)monster1.getDrawable();
        monsterAnim2 = (AnimationDrawable)monster2.getDrawable();
        monsterAnim3 = (AnimationDrawable)monster3.getDrawable();

        monsterAnim1.start();
        monsterAnim2.start();
        monsterAnim3.start();

        monsterTrack1.setDuration(duration);
        monsterTrack2.setDuration(duration);
        monsterTrack3.setDuration(duration);

        monster1.startAnimation(monsterTrack1);
        monster2.startAnimation(monsterTrack2);
        monster3.startAnimation(monsterTrack3);
    }

    private void nextWave() {

        if (killedMonster1 && killedMonster2 && killedMonster3) {

            Log.i("TAG!", "another wave");
            if (numOfWavePerLvl[level] == wave) {

                wave = 0;
                level++;

            }

            wave++;

            killedMonster1 = killedMonster2 = killedMonster3 = false;

            generateWave(randRangePerLvl[level], durationPerLvl[level]);

        }
    }
}
