package com.tomoon.extensions.bluessh;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.view.*;

import java.util.*;
import java.io.*;

import android.util.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MainActivity extends BaseActivity {
    public static final UUID MY_UUID =
            UUID.fromString("c3d3cd23-e209-c3ca-aabc-acad8848dead");
    PrintStream stdin = null;

    static BluetoothAdapter mBluetoothAdapter=null;

    public static final int REQUEST_ENABLE_BT = 0xcf09011d;

    @Override
    public void onPrepareUi() {
        FPS = 1;
        setContentView(R.layout.main);
        setTitle("设备列表");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) { //蓝牙未开启，则开启蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            initList();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_ENABLE_BT) {
            if (!mBluetoothAdapter.isEnabled()) { //蓝牙未开启，则开启蓝牙
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                initList();
            }
        }
    }

    public void initList(){
        Set<BluetoothDevice> devices =  mBluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> deviceList=new ArrayList<>();
        for (BluetoothDevice dev :devices) {
            deviceList.add(dev);
        }
        ((ListView)findViewById(R.id.listBluetooth)).setAdapter(new BlueDevAdapter(deviceList));
        ((ListView)findViewById(R.id.listBluetooth)).setOnItemClickListener(itemClickListener);
    }

    AdapterView.OnItemClickListener itemClickListener =new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice dev = ((BluetoothDevice)parent.getAdapter().getItem(position));
            Intent i =new Intent(MainActivity.this,ShellActivity.class);
            i.putExtra("bdmac",dev.getAddress());
            startActivity(i);
        }
    };

    class BlueDevAdapter extends ArrayAdapter<BluetoothDevice> {

        public BlueDevAdapter( @NonNull List<BluetoothDevice> objects) {
            super(MainActivity.this,R.layout.adapter_bt, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.adapter_bt,null,false);
            BluetoothDevice obj = getItem(position);
            ((TextView)v.findViewById(R.id.txtName)).setText(obj.getName());
            ((TextView)v.findViewById(R.id.txtMac)).setText(obj.getAddress());
            return v;
        }
    }



}
