package com.example.god;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;


import androidx.annotation.Nullable;



public class MusicService extends Service implements MediaPlayer.OnErrorListener {

    private final IBinder mBinder = new ServiceBinder();
    private int length = 0;
    MediaPlayer mainPlayer;


    public MusicService() { }


    public class ServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }

    @Override
    public void onCreate() {
        super.onCreate();

        //play activity
        mainPlayer = MediaPlayer.create(this, R.raw.mainbackgroundmusic);
        mainPlayer.setOnErrorListener(this);


        if (mainPlayer != null) {
            mainPlayer.setLooping(true);
            mainPlayer.setVolume(50, 50);
        }


        mainPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                onError(mainPlayer, what, extra);
                return true;
            }
        });

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mainPlayer.start();
        return START_NOT_STICKY;
    }

    public void pauseMusic() {

            if (mainPlayer.isPlaying()) {
                mainPlayer.pause();
                length = mainPlayer.getCurrentPosition();
            }

    }

    public void resumeMusic() {

            if (!mainPlayer.isPlaying()) {
                mainPlayer.seekTo(length);
                mainPlayer.start();
            }

    }
    public void startMusic(){
        mainPlayer = MediaPlayer.create(this,R.raw.mainbackgroundmusic);
        mainPlayer.setOnErrorListener(this);

        if (mainPlayer != null){
            mainPlayer.setLooping((true));
            mainPlayer.setVolume(50, 50);
            mainPlayer.start();
        }
    }


    public void stopMusic() {

            mainPlayer.stop();
            mainPlayer.release();
            mainPlayer = null;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainPlayer != null) {
            try {
                mainPlayer.stop();
                mainPlayer.release();
            } finally {
                mainPlayer = null;
            }
        }
    }

    public boolean onError (MediaPlayer mp, int what, int extra ){
        Toast.makeText(this, "Music Player failed." , Toast.LENGTH_SHORT).show();
        if (mainPlayer != null){
            try{
                mainPlayer.stop();
                mainPlayer.release();
            } finally {
                mainPlayer = null;
            }
        }
        return false;
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
//        mainPlayer = MediaPlayer.create(this, R.raw.main_bg_music);
//        mainPlayer.setLooping(true);
//        mainPlayer.start();
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mainPlayer.stop();
//    }
}
