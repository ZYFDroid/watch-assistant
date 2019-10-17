package com.tomoon.extensions.notificationpusher;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.app.NotificationManagerCompat;

import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    public static UUID BtPort = UUID.fromString("c3d3cd23-e209-c3ca-aabd-30addc000000");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(! new SpUtils(getApplicationContext(),SpUtils.getCert()).load()){
            throw new AndroidRuntimeException(new FileNotFoundException("Specified file not given"));
        }

        setContentView(R.layout.activity_main);
    }


    public void startConnect(View view) {
        if(!isNotificationListenerEnabled(this)){
            openNotificationListenSettings();
            return;
        }
        if(null==NotificationService.mInstance){
            startService(new Intent(this,NotificationService.class));
        }
        else{
            NotificationService.mInstance.isUserStop=true;
            NotificationService.mInstance.stopSelf();
            startService(new Intent(this,NotificationService.class));
        }

    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }

    public void selectDevice(View view) {

        startActivity(new Intent(this,SelectDeviceActivity.class));

    }



    public boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (packageNames.contains(context.getPackageName())) {
            return true;
        }
        return false;
    }

    public void openNotificationListenSettings() {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNotificationBlock(View view) {
        startActivity(new Intent(this,BlockActivity.class));
    }
}
