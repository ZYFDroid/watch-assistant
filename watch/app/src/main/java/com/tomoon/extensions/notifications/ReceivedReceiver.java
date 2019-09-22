package com.tomoon.extensions.notifications;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class ReceivedReceiver extends BroadcastReceiver {
    public static final String ACTION="com.tomoon.extensions.notifications.RECEIVED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i =new Intent(context,ReceivedActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtras(intent);
        wakeUpAndUnlock(context);
        context.startActivity(i);
    }

    public static void wakeUpAndUnlock(Context context){
        //屏锁管理器
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK,"sodirhgosih:sobvhuse");
        //点亮屏幕
        wl.acquire(5000);
    }
}
