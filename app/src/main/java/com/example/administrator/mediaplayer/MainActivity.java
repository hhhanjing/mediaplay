package com.example.administrator.mediaplayer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*  1、如何创建一个listview列表，并将获取到的数据添加到列表中
*   2、如何读取数据
**/
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String UPDATE_ACTION = "org.crazyit.action.UPDATE_ACTION";
    public static final String CAL_ACTION = "org.crazyit.action.CTL_ACTION";
    SimpleAdapter simpleAdapter;
    List<Map<String,Object>> contactListItem = new ArrayList<>();
    ArrayList<String> pathList = new ArrayList<>();
    ActivityReceiver activityReceiver;

    int status = 0;//表示音乐当前的状态
    int current = 0;//当前播放的歌曲在链表中的位置

    LinearLayout ly;
    ImageView imageView;
    TextView titleView;
    TextView artistView;
    ImageButton left;
    ImageButton start;
    ImageButton right;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();//初始化view组件

        simpleAdapter = new SimpleAdapter(this,contactListItem, R.layout.simple_item,new String[]{"title","artist"},new int[]{R.id.title, R.id.artist});
        final ListView contactsView = (ListView)findViewById(R.id.contacts_view);
        contactsView.setAdapter(simpleAdapter);

        //ContextCompat.checkSelfPermission用于检查某项权限
        //如果应用具有此权限，方法将返回 PackageManager.PERMISSION_GRANTED，并且应用可以继续操作。
        // 如果应用不具有此权限，方法将返回 PERMISSION_DENIED，且应用必须明确向用户要求权限。
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},1);//请求权限
        }else{
            readContact();
        }

        //动态注册广播接收器
        activityReceiver = new ActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION);
        registerReceiver(activityReceiver,filter);

        //开启一个service服务
        Intent intent = new Intent(MainActivity.this,MyService.class);
        Log.d(TAG, "onCreate: "+pathList.size());
        intent.putStringArrayListExtra("path",pathList);
        startService(intent);


        //监听“列表项”的点击事件，并发送广播
        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music = new Music();
                music.setArtist(contactListItem.get(position).get("artist").toString());
                music.setTitle(contactListItem.get(position).get("title").toString());
                music.setUrl(contactListItem.get(position).get("url").toString());
                music.setDuration((long)contactListItem.get(position).get("duration"));/////////注意，需要的是Long类型的，得到的是Object类型。注意转换
                titleView.setText(music.getTitle());

                artistView.setText(music.getArtist());
                Intent intent = new Intent("org.crazyit.action.CTL_ACTION");
                intent.putExtra("control",1);//播放
                intent.putExtra("click",true);
                intent.putExtra("current",position);
                intent.putExtra("status",0);
                sendBroadcast(intent);
            }
        });

        //监听最下面整个布局的点击事件，并发广播
        ly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                intent.putExtra("status",status);
                intent.putExtra("current",current);
                startActivity(intent);
            }
        });


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
                intent.putExtra("control",2);//control为2，代表电击的是“上一首”按钮
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
    /** * * * * * * * * * * *  onCreate()方法到此结束  * * * * * * * * * * * * * * * * **/

    //监听从service传回来的广播
    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            int update = intent.getIntExtra("update",-1);//当前的播放状态
             current = intent.getIntExtra("current",-1);//当前正在播放的歌曲的序号
            if(current>=0){
                titleView.setText(contactListItem.get(current).get("title").toString());
                artistView.setText(contactListItem.get(current).get("artist").toString());
            }
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

                    pathList.add(url);
                    contactListItem.add(item);

                }
                simpleAdapter.notifyDataSetChanged();//实现动态刷新列表的功能
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    }


    //请求权限回调结果
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){//将用户同意或者拒绝的结果回调
        switch(requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    readContact();
                }
                else
                    Toast.makeText(this,"you denied the permission",Toast.LENGTH_SHORT).show();
                break;
            default:
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(activityReceiver);
    }

    public void initView(){

        imageView = (ImageView)findViewById(R.id.image);
        titleView = (TextView)findViewById(R.id.titleView);
        artistView = (TextView)findViewById(R.id.artistView);
        left = (ImageButton)findViewById(R.id.left);
        start = (ImageButton)findViewById(R.id.start);
        right = (ImageButton)findViewById(R.id.right);
        ly = (LinearLayout) findViewById(R.id.lt);
    }

}
