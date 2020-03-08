package com.example.god;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class RoadMusicService extends Service implements MediaPlayer.OnErrorListener {
    private final IBinder roadBinder = new RoadMusicService.ServiceBinder();
    private int length = 0;
    MediaPlayer carPlayer;

    public RoadMusicService() { }


    public class ServiceBinder extends Binder {
        RoadMusicService getService() {
            return RoadMusicService.this;
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return roadBinder;
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

        //roadtrip activity
        carPlayer = MediaPlayer.create(this, R.raw.cargamemusic);
        carPlayer.setOnErrorListener(this);


        if (carPlayer != null) {
            carPlayer.setLooping(true);
            carPlayer.setVolume(50, 50);
        }


        carPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                onError(carPlayer, what, extra);
                return true;
            }
        });

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        carPlayer.start();
        return START_NOT_STICKY;
    }

    public void pauseMusic() {

        if (carPlayer.isPlaying()) {
            carPlayer.pause();
            length = carPlayer.getCurrentPosition();
        }

    }

    public void resumeMusic() {

        if (!carPlayer.isPlaying()) {
            carPlayer.seekTo(length);
            carPlayer.start();
        }

    }
    public void startMusic(){
        carPlayer = MediaPlayer.create(this,R.raw.cargamemusic);
        carPlayer.setOnErrorListener(this);

        if (carPlayer != null){
            carPlayer.setLooping((true));
            carPlayer.setVolume(50, 50);
            carPlayer.start();
        }
    }


    public void stopMusic() {

        carPlayer.stop();
        carPlayer.release();
        carPlayer = null;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (carPlayer != null) {
            try {
                carPlayer.stop();
                carPlayer.release();
            } finally {
                carPlayer = null;
            }
        }
    }

    public boolean onError (MediaPlayer mp, int what, int extra ){
        Toast.makeText(this, "Music Player failed." , Toast.LENGTH_SHORT).show();
        if (carPlayer != null){
            try{
                carPlayer.stop();
                carPlayer.release();
            } finally {
                carPlayer = null;
            }
        }
        return false;
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
//        carPlayer = MediaPlayer.create(this, R.raw.main_bg_music);
//        carPlayer.setLooping(true);
//        carPlayer.start();
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        carPlayer.stop();
//    }
}
