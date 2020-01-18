package com.example.god;
import android.app.AlertDialog;
import android.content.Context;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class GestureActivity extends AppCompatActivity {

    private GestureOverlayView gestureOverlayView;

    private GestureLibrary gestureLibrary = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_view);

        setTitle("dev2qa.com - Android Custom Gesture Example.");

        Context context = getApplicationContext();

        init(context);

        GesturePerformListener gesturePerformListener = new GesturePerformListener(gestureLibrary);

        gestureOverlayView = findViewById(R.id.gesture_overlay_view);

        gestureOverlayView.addOnGesturePerformedListener(gesturePerformListener);
    }

    /* Initialise class or instance variables. */
    private void init(Context context)
    {
        if(gestureLibrary == null)
        {
            // Load custom gestures from gesture.txt file.
            gestureLibrary = GestureLibraries.fromRawResource(context, R.raw.gesture);

            if(!gestureLibrary.load())
            {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setMessage("Custom gesture file load failed.");
                alertDialog.show();

                finish();
            }
        }

        if(gestureOverlayView == null)
        {
            gestureOverlayView = (GestureOverlayView)findViewById(R.id.gesture_overlay_view);
        }
    }
}
