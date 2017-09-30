package com.example.administrator.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Main2Activity extends AppCompatActivity {

    private static final String TAG = "Main2Activity";
    public static final String UPDATE_ACTION = "org.crazyit.action.UPDATE_ACTION";
    public static final String CAL_ACTION = "org.crazyit.action.CTL_ACTION";

    private MyApplication myApplication;
    List<Map<String,Object>> musicListItem = new ArrayList<>();
    Activity2Receiver activity2Receiver;

    int status ;//表示音乐当前的状态
    int current;

    int dest=0;
    int sMax=0;
    int mMax=1;


    TextView titleView;
    ImageView picture;
    SeekBar seekBar;
    ImageButton left;
    ImageButton start;
    ImageButton right;
    TextView currentTimeView;
    TextView sumTimeView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Log.d(TAG, "onCreate: "+this);

        myApplication = (MyApplication)getApplication();

        picture = (ImageView)findViewById(R.id.picture2);
        titleView = (TextView)findViewById(R.id.title2);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        left = (ImageButton)findViewById(R.id.left2);
        start = (ImageButton)findViewById(R.id.start2);
        right = (ImageButton)findViewById(R.id.right2);
        currentTimeView = (TextView)findViewById(R.id.currentTime);
        sumTimeView = (TextView)findViewById(R.id.sumTime);
        currentTimeView.setText("00:00");
        sumTimeView.setText("00:00");
        activity2Receiver = new Activity2Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION);
        registerReceiver(activity2Receiver,filter);

        readContact();
        Intent intent = getIntent();
        status = intent.getIntExtra("status",0);
        current = intent.getIntExtra("current",0);
        titleView.setText(musicListItem.get(current).get("title").toString());
        if (status==0){
            start.setImageResource(R.drawable.start);
        }else if (status==1){
            start.setImageResource(R.drawable.pause);
        }


        //监听“进度条”拖动
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                dest = seekBar.getProgress();
                sMax = seekBar.getMax();
                mMax = myApplication.getMediaPlayer().getDuration();
                myApplication.getMediaPlayer().seekTo(mMax*dest/sMax);

            }
        });

        DelayThread dThread = new DelayThread(100);
        dThread.start();



        //监听“播放/暂停”按钮点击事件，并发广播
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("org.crazyit.action.CTL_ACTION");
                intent.putExtra("control",1);//control为1，代表点击的是“播放/暂停”按钮
                intent.putExtra("click",false);
                sendBroadcast(intent);
            }
        });

        //监听“上一首”按钮点击事件，并发广播
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("org.crazyit.action.CTL_ACTION");
                intent.putExtra("control",2);//control为2，代表点击的是“上一首”按钮
                intent.putExtra("click",false);
                sendBroadcast(intent);
            }
        });

        //监听“下一首”按钮点击事件，并发广播
        right.setOnClickListener(new View.OnClickListener() {//监听“下一首”按钮点击事件，并发广播
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("org.crazyit.action.CTL_ACTION");
                intent.putExtra("control",3);//control为3，代表点击的是“下一首”按钮
                intent.putExtra("click",false);
                sendBroadcast(intent);
            }
        });


    }
/**    ********************** onCreate()到此结束  *******************************************/



//所有注释全部关于拖放进度条的

    Handler mHandle = new Handler(){
        @Override
        public void handleMessage(Message msg){
            int position = myApplication.getMediaPlayer().getCurrentPosition();
            int mMax = myApplication.getMediaPlayer().getDuration();
            int sMax = seekBar.getMax();
            seekBar.setProgress(position*sMax/mMax);
            currentTimeView.setText(formaTime(myApplication.getMediaPlayer().getCurrentPosition()));
            sumTimeView.setText(formaTime(myApplication.getMediaPlayer().getDuration()));

       }
    };

    public class DelayThread extends Thread {
        int milliseconds;

        public DelayThread(int i){
            milliseconds = i;
        }
        public void run() {
            Log.d(TAG, "run: "+this);
            while(true){
                try {
                    sleep(milliseconds);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                mHandle.sendEmptyMessage(0);

            }
        }
    }



    public class Activity2Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent){


            int update = intent.getIntExtra("update",-1);//当前的播放状态
            int current = intent.getIntExtra("current",-1);//当前正在播放的歌曲的序号
            titleView.setText(musicListItem.get(current).get("title").toString());

                switch (update){
                    case 0:
                        start.setImageResource(R.drawable.start);
                        status = 0;
                        break;
                    case 1:
                        start.setImageResource(R.drawable.pause);//播放状态下使用暂停图标
                        status = 1;
                        break;
                    case 2:
                        start.setImageResource(R.drawable.start);//暂停状态下使用播放图标
                        status = 2;
                        break;
                }
        }
    }



    public static String formaTime(long time){
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }


    //获取所有音乐信息并显示到listview中
    private void readContact(){
        Cursor cursor = null;
        try {
            //查询所有的音乐文件
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if(cursor!=null){

                while(cursor.moveToNext()){
                    //获取音乐信息
                    Music music = new Music();
                    music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    music.setUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    music.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                    String title = music.getTitle();
                    String artist = music.getArtist();
                    String url = music.getUrl();
                    long duration = music.getDuration();

                    Map<String,Object> item = new HashMap<>();
                    item.put("title",title);
                    item.put("artist",artist);
                    item.put("url",url);
                    item.put("duration",duration);
                    item.put("object", music);

                    musicListItem.add(item);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(activity2Receiver);
    }

}
