package com.tomoon.extensions.notificationpusher;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceActivity extends Activity {
    static BluetoothAdapter mBluetoothAdapter=null;
    public static final int REQUEST_ENABLE_BT = 0xcf09011d;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

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
            SpUtils.getConnSp(SelectDeviceActivity.this).edit().putString("mac",dev.getAddress()).commit();
            finish();
        }
    };

    class BlueDevAdapter extends ArrayAdapter<BluetoothDevice> {

        public BlueDevAdapter(  List<BluetoothDevice> objects) {
            super(SelectDeviceActivity.this,R.layout.adapter_bt, objects);
        }


        @Override
        public View getView(int position, View convertView,  ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.adapter_bt,null,false);
            BluetoothDevice obj = getItem(position);
            ((TextView)v.findViewById(R.id.txtName)).setText(obj.getName());
            ((TextView)v.findViewById(R.id.txtMac)).setText(obj.getAddress());
            return v;
        }
    }
}
