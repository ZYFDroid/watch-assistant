package com.tomoon.extensions.notificationpusher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;

public class NotificationService extends NotificationListenerService {

    public static NotificationService mInstance = null;

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

    }

    public static ArrayList<String> blockedAppList = new ArrayList<>();
    public static ArrayList<String> blockedKeywordList = new ArrayList<>();


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Log.e("DEBGMIUI","BeforeOngoing");

        if (!sbn.isOngoing()) {

            Log.e("DEBGMIUI","BeforeFilter");

            if(blockedAppList.contains(sbn.getPackageName())){return;}

            Bundle extras = sbn.getNotification().extras;
            String title = extras.getString(Notification.EXTRA_TITLE, "");
            String content = extras.getString(Notification.EXTRA_TEXT, "");

            String summary = title+"^&(&%&"+content;
            for (String kw :
                    blockedKeywordList) {
                if(summary.contains(kw)){
                    return;
                }
            }
            Log.e("DEBGMIUI","Sending");
            String app = getAppName(this, sbn.getPackageName());



            //Log.e("HOOKEDNOTIFICATION",title+":"+content);

            sendAsync(app, title, content);

        }

    }


    public static String getAppName(Context context, String packageName) {

        PackageManager pm = context.getPackageManager();
        String Name;
        try {
            Name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Name = packageName;
        }
        return Name;
    }


    public void sendAsync(final String app, final String title, final String content) {
        if (title.trim().isEmpty() && content.trim().isEmpty()) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                synchronized (syncObj) {
                    send(app, title, content);
                }
            }
        }.start();
    }

    Object syncObj = new Object();

    int failCount = 20;

    public void send(String app, String title, String content) {

        if (null == printStream) {
            return;
        }
        try {
            JSONObject jobj = new JSONObject();
            jobj.put("app", app);
            jobj.put("title", title);
            jobj.put("content", content);
            String msg = Base64.encodeToString(jobj.toString().getBytes("UTF-8"), Base64.URL_SAFE);

            try {
                printStream.print(msg);
                printStream.print(";");
                //Log.e("==SENDEDNOTIFICATION",title+":"+content);
            } catch (Exception ex) {
                ex.printStackTrace();
                toastMessage("无法连接到手表");
                printStream = null;
                failCount++;
                if (failCount > 20) {
                    failCount = 0;
                    toastMessage("真的无法连接到手表，服务关闭");
                    stopSelf();
                    return;
                }
                new ConnectThread().start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new ConnectThread().start();
        loadConfiguration(this);
        mInstance = this;
        isUserStop=false;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ;
        mInstance = null;
        super.onDestroy();
    }

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    PrintStream printStream;


    class ConnectThread extends Thread {

        @Override
        public void run() {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                String macaddr = SpUtils.getConnSp(NotificationService.this).getString("mac", "");
                BluetoothDevice device = null;
                try {
                    device = bluetoothAdapter.getRemoteDevice(macaddr);
                    if (null == device) {
                        toastMessage("未绑定手表");
                        stopSelf();
                        return;
                    }
                } catch (Exception ex) {
                    toastMessage("未绑定手表");
                    stopSelf();
                    return;
                }

                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(MainActivity.BtPort);
                    socket.connect();
                    printStream = new PrintStream(socket.getOutputStream());
                    new StatusThread(socket).start();
                } catch (IOException e) {
                    toastMessage("无法连接到手表");
                    stopSelf();
                    return;
                }
                toastMessage("服务已启动");
            } else {
                toastMessage("蓝牙没有打开");
                stopSelf();
            }
        }
    }

    class StatusThread extends  Thread{
        BluetoothSocket socket;

        public StatusThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    socket.getInputStream().read(buffer);
                    Thread.sleep(5000);
                }
            }catch (Exception ex){
                ex.printStackTrace();
                hWnd.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!isUserStop) {
                            postNotification();
                        }
                        printStream=null;
                    }
                });
            }
        }
    }

    Handler hWnd = new Handler();

    public void toastMessage(final String mesg) {
        hWnd.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NotificationService.this, "手表连接应用：\r\n" + mesg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isUserStop=false;


    public static void loadConfiguration(Context $this){
        blockedAppList.clear();
        blockedKeywordList.clear();
        SharedPreferences sp = SpUtils.getConnSp($this);
        String[] kws = sp.getString("kw","").split("\\n");
        for (int i = 0; i < kws.length; i++) {
            if(kws[i].trim().isEmpty()){continue;}
            blockedKeywordList.add(kws[i]);
        }

        Set<String> apps =sp.getStringSet("bapps",null);
        if(null!=apps){
            blockedAppList.addAll(apps);
        }
    }


    public void postNotification(){

        Log.e("TMSEND","Sending notifications for disconnect");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "disconn"; //根据业务执行
            String channelName ="断线通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            notificationManager.createNotificationChannel(new NotificationChannel( channelId, channelName, importance));
        }

        NotificationCompat.Builder builder =null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(getApplicationContext(), "disconn");
        }else{
            builder = new NotificationCompat.Builder(getApplicationContext());
        }
        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("手表断开连接")
                .setContentText("您将无法在手表上查看最新的通知，轻触打开连接界面").setWhen(System.currentTimeMillis());

        //设置点击通知之后的响应，启动SettingActivity类
        Intent resultIntent = new Intent(this,MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        try {
            Log.e("TMSEND","TRYSEND");
            notificationManager.notify(1,notification);
        }catch (NullPointerException ex){ex.printStackTrace();}
    }
}
