package com.tomoon.extensions.notifications;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.DateTimeKeyListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceivedActivity extends Activity {

    NotificationAdapter mAdapter = null;
    Vibrator vibrator = null;

    VibrationEffect efft;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received);
        mAdapter=new NotificationAdapter();
        vibrator=(Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
        ((ListView)findViewById(R.id.listMessage)).setAdapter(mAdapter);
        mAdapter.addEntry(getIntent().getStringExtra("app"),getIntent().getStringExtra("title"),getIntent().getStringExtra("content"));
        IntentFilter filter =new IntentFilter(ReceivedReceiver.ACTION);
        filter.setPriority(5);
        registerReceiver(mInActivityReceiver,filter);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    BroadcastReceiver mInActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.addEntry(intent.getStringExtra("app"),intent.getStringExtra("title"),intent.getStringExtra("content"));
            abortBroadcast();
            ReceivedReceiver.wakeUpAndUnlock(ReceivedActivity.this);
            setTopApp();
        }
    };

    public void setTopApp() {
        Intent i =new Intent(this,getClass());
        i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(i);
    }


    @Override
    protected void onPause() {
        super.onPause();
//        try {
//            unregisterReceiver(mInActivityReceiver);
//        }catch (Exception ex){}
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mInActivityReceiver);
        }catch (Exception ex){}
        super.onDestroy();
    }

    class NotificationAdapter extends BaseAdapter {

        ArrayList<NotificationStruct> lists = new ArrayList<>();

        private Date d=new Date();
        private SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
        public void addEntry(String app,String title,String content){
            d.setTime(System.currentTimeMillis());
            lists.add(0,new NotificationStruct(title,content,app+" "+sdf.format(d)));
            //vibrator.vibrate(250);
            Log.e("TMNOT","REVEIVED");
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public NotificationStruct getItem(int position) {
            return lists.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NotificationStruct obj=getItem(position);
            View v=getLayoutInflater().inflate(R.layout.adapter_message,null,false);
            ((TextView)v.findViewById(R.id.txtTitle)).setText(obj.title);
            ((TextView)v.findViewById(R.id.txtContent)).setText(obj.content);
            ((TextView)v.findViewById(R.id.txtTime)).setText(obj.time);
            return v;
        }
    }
}

class NotificationStruct{
    public String title,content,time;
    public NotificationStruct(String title, String content,String time) {
        this.title = title;
        this.content = content;
        this.time=time;
    }
}
