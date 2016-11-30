
package com.example.liangmutian.updateapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/***
 * 升级服务
 */
public class UpdateService extends Service {

    private static final int down_step_custom = 5;

    private static final int TIMEOUT = 100 * 1000;// 超时
    private static String down_url;
    private static final int DOWN_OK = 1;
    private static final int DOWN_ERROR = 0;

    private String app_name;
    private Notification.Builder builder;
    public static String extra_name = "name";
    public static String extra_url = "url";
    private MyBinder mBinder = new MyBinder();
    int updateCount = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    class MyBinder extends Binder {

        public int getDownload() {
            Log.d("TAG", "getDownload ");
            return updateCount;

        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TAG", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("TAG", "onStartCommand");

        try {
            app_name = intent.getStringExtra(extra_name);
            down_url = intent.getStringExtra(extra_url);
        } catch (Exception e) {
            Message message = new Message();
            message.what = DOWN_ERROR;
            handler.sendMessage(message);
        }
        FileUtil.createFile(app_name);
        if (FileUtil.isCreateFileSucess) {
            createNotification();
            createThread();
        } else {
            MainActivity.isDownApk = false;
            Toast.makeText(this, "请插入SD卡", Toast.LENGTH_SHORT).show();
            stopSelf();

        }
        return super.onStartCommand(intent, flags, startId);
    }


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @SuppressWarnings("deprecation")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_OK:
                    /**安装APK */
                    MainActivity.isDownApk = false;
                    builder.setContentText("下载成功,正在安装");
                    builder.setOngoing(false);
                    builder.setAutoCancel(true);
                    builder.setProgress(100, 100, false);
                    builder.getNotification().flags = Notification.FLAG_AUTO_CANCEL;
                    //notificationManager.cancel(R.layout.notification_item);
                    //notificationManager.notify(R.layout.notification_item, builder.getNotification());
                    installApk();
                    stopSelf();
                    break;
                case DOWN_ERROR:
                    MainActivity.isDownApk = false;
                    builder.setContentText("下载失败");
                    builder.setOngoing(false);
                    builder.setAutoCancel(true);
                    builder.getNotification().flags = Notification.FLAG_AUTO_CANCEL;
                    //notificationManager.notify(R.layout.notification_item, builder.getNotification());
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
    };

    private void installApk() {
        Uri uri = Uri.fromFile(FileUtil.updateFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        UpdateService.this.startActivity(intent);
    }


    public void createThread() {
        new DownLoadThread().start();
    }

    private class DownLoadThread extends Thread {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message message = new Message();
            try {
                long downloadSize = downloadUpdateFile(down_url,
                        FileUtil.updateFile.toString());
                if (downloadSize > 0) {
                    message.what = DOWN_OK;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                message.what = DOWN_ERROR;
                handler.sendMessage(message);
            }
        }
    }


    public void createNotification() {
        builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher).setOngoing(false)
                .setContentText("正在下载")
                .setContentTitle("XXXX下载");
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setTicker("XXXX正在下载");
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setProgress(100, 0, false);
        //notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_MAX);
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        notification.icon = R.mipmap.ic_launcher;
        startForeground(1, notification);

       // notificationManager.notify(R.layout.notification_item, notification);
    }


    public long downloadUpdateFile(String down_url, String file)  throws Exception{

        long down_step = down_step_custom;// 提示step
        long totalSize;// 文件总大小
        long downloadCount = 0;// 已经下载好的大小
        // 已经上传的文件大小

        InputStream inputStream=null;
        OutputStream outputStream=null;

        URL url = new URL(down_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(TIMEOUT);
        httpURLConnection.setReadTimeout(TIMEOUT);
        totalSize = httpURLConnection.getContentLength();

        if (httpURLConnection.getResponseCode() == 404) {
            throw new Exception("download fail!");
        }

        try {
            inputStream = httpURLConnection.getInputStream();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            outputStream = new FileOutputStream(file, false);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(inputStream!=null && outputStream!=null){
            byte buffer[] = new byte[1024];
            int readsize = 0;
            while ((readsize = read(inputStream, buffer)) != -1) {
                try {
                    outputStream.write(buffer, 0, readsize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                downloadCount += readsize;// 时时获取下载到的大小
                if (updateCount == 0 || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
                    updateCount += down_step;
                    builder.setProgress(100, updateCount, false);
                    builder.setContentText("正在下载" + updateCount + "%");
                    startForeground(1, builder.getNotification());
                    //notificationManager.notify(R.layout.notification_item, builder.getNotification());
                }
            }
            httpURLConnection.disconnect();
        }

        boolean exceptin = false;
        try {
            if(inputStream!=null)
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            exceptin = true;
        }

        try {
            if(outputStream!=null)
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            exceptin = true;
        }
        if (exceptin) {
            Message message = new Message();
            message.what = DOWN_ERROR;
            handler.sendMessage(message);
        }

        return downloadCount;
    }

    private int read(InputStream inputStream, byte[] buffer) {
        int readsize = -1;
        try {
            readsize = inputStream.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readsize;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy");
    }


    public static void into(Context context, String name, String url) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.putExtra(extra_name, name);
        intent.putExtra(extra_url, url);
        context.startService(intent);
    }
}
