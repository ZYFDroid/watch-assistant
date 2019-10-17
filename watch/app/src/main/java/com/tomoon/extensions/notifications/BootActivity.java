package com.tomoon.extensions.notifications;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class BootActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(! new CodecUtils(getApplicationContext(),CodecUtils.getCert()).load()){
            throw new AndroidRuntimeException(new FileNotFoundException("Specified file not given"));
        }

        if(null==ReceiverService.mInstance){
            startService(new Intent(this,ReceiverService.class));
        }
        Toast.makeText(this, "服务正在运行", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.settings);
        ((CheckBox)findViewById(R.id.chkVibrate)).setChecked(getSharedPreferences("main",MODE_PRIVATE).getBoolean("vib",true));
        ((CheckBox)findViewById(R.id.chkVibrate)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("main",MODE_PRIVATE).edit().putBoolean("vib",isChecked).commit();
            }
        });
    }

    public void openBt(View view) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent,0);
    }

    public void openDiscovery(View view) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,100);
        startActivityForResult(discoverableIntent,0);
    }
}
