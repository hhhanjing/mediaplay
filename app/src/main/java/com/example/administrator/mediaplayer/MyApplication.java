package com.example.administrator.mediaplayer;

import android.app.Application;
import android.media.MediaPlayer;

/**
 * Created by Administrator on 2017/9/26.
 */

public class MyApplication extends Application {
    MediaPlayer mediaPlayer;

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mediaPlayer = new MediaPlayer();

    }

}
