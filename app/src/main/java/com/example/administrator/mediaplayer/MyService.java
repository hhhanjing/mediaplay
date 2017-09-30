package com.example.administrator.mediaplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class MyService extends Service {
    private static final String TAG = "MyService";


    private MyApplication myApplication;

    ArrayList<String> listMusic;
    ServiceReceiver serviceReceiver;

   // int currentPosition;
    int current = 0;
    int status = 0;//代表当前没有播放

    @Override
    public void onCreate(){
        super.onCreate();
        serviceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CAL_ACTION);
        registerReceiver(serviceReceiver,filter);
        myApplication = (MyApplication)getApplication();

            myApplication.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {//监听播放下一首
                public void onCompletion(MediaPlayer mp) {
                    if(status!=0){
                        playNextMusic();
                        myApplication.getMediaPlayer().start();
                    }
                }
            });

    }


    public MyService(){
        super();

    }

    public void playLastMusic(){
        if(current == 0){
            current = listMusic.size() -1;
        }else{
            current--;
        }
        Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
        sendIntent.putExtra("current",current);
        sendIntent.putExtra("update",status);
        sendBroadcast(sendIntent);
        prepareAndPlay(listMusic.get(current));


    }

    public void playNextMusic(){
        current++;
        if(current>=listMusic.size()){
            current = 0;
        }
        Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
        sendIntent.putExtra("current",current);
        sendIntent.putExtra("update",status);
        sendBroadcast(sendIntent);
        prepareAndPlay(listMusic.get(current));

    }

    public void prepareAndPlay(String path){//加载、准备、播放

        try {
            ////mediaPlayer.reset();
            myApplication.getMediaPlayer().reset();
            /////mediaPlayer.setDataSource(path);
            myApplication.getMediaPlayer().setDataSource(path);
           //// mediaPlayer.prepare();
            myApplication.getMediaPlayer().prepare();

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public class ServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(final Context context,Intent intent){

            int control = intent.getIntExtra("control",-1);
            boolean click = intent.getBooleanExtra("click",false);
            if(click == true){
                current = intent.getIntExtra("current",-1);
                status = intent.getIntExtra("status",0);
            }
            switch (control){
//                case 0://拖动进度条
//                    int dest = intent.getIntExtra("dest",0);
//                    int sMax = intent.getIntExtra("sMax",0);
//                    mediaPlayer.seekTo(mediaPlayer.getDuration()*dest/sMax);
//                    currentPosition =  mediaPlayer.getCurrentPosition();

                case 1://播放的指令
                    if(status==0){
                        prepareAndPlay(listMusic.get(current));
                        myApplication.getMediaPlayer().start();
                        Log.d(TAG, "onReceive: -------"+listMusic.get(current));
                        status = 1;
                    }else if (status == 1){
                        ////mediaPlayer.pause();
                        myApplication.getMediaPlayer().pause();
                        status = 2;
                    }else if(status == 2){
                        ////mediaPlayer.start();
                        myApplication.getMediaPlayer().start();
                        status = 1;
                    }
                    break;
                case 2://上一首
                    playLastMusic();
                    if(status==1){
                        myApplication.getMediaPlayer().start();
                    }
                    break;
                case 3://下一首
                    playNextMusic();
                    if(status==1){
                        myApplication.getMediaPlayer().start();
                    }
                    break;

            }
            Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
           // sendIntent.putExtra("control",control);
            sendIntent.putExtra("update",status);
            sendIntent.putExtra("current",current);
          //  sendIntent.putExtra("currentPosition",currentPosition);//当前进度条位置
          //  sendIntent.putExtra("duration",mediaPlayer.getDuration());
            sendBroadcast(sendIntent);
        }
    }


    @Override
    public int onStartCommand(Intent intent,int flags,int startId){

        listMusic = intent.getStringArrayListExtra("path");
        prepareAndPlay(listMusic.get(current));



        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }



}
