package com.example.liangmutian.updateapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    public static boolean isDownApk;
    private UpdateService.MyBinder myBinder;
    private TextView textView;
    private  MyServiceConnection myServiceConnection=new MyServiceConnection();

    public String url = "http://openbox.mobilem.360.cn/index/d/sid/2407790";
    //public String url="http://openbox.mobilem.360.cn/index/d/sid/2954492";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=(TextView)findViewById(R.id.textView);
    }


    public void update(View v) {

        isDownApk = true;
        UpdateService.into(this, getString(R.string.app_name), url);



        Intent intent = new Intent(this, UpdateService.class);
        bindService(intent,myServiceConnection , Context.BIND_AUTO_CREATE);





    }
    public void getprogress(View v){

        textView.setText("进度:"+myBinder.getDownload());
        if (myBinder.getDownload()==100)
            unbindService(myServiceConnection);


    }
    class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            Log.d("info", "Service Connection Success");

            myBinder = (UpdateService.MyBinder) service;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
// TODO Auto-generated method stub
            Log.d("info", "Service Connection Filed");
//连接失败执行
        }

    }


}
