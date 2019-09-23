package com.tomoon.extensioin.bluesshd;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Runtime;
import java.util.UUID;

public class MainActivity extends Activity {
    public BluetoothAdapter bluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 0xcf09011d;
    public static final UUID MY_UUID =
            UUID.fromString("c3d3cd23-e209-c3ca-aabc-acad8848dead");

    private static final UUID FTP_UPLOAD_UUID =
            UUID.fromString("c3d3cd23-e209-c3ca-aabc-30addc000021");

    private static final UUID FTP_CTLUPLOAD_UUID =
            UUID.fromString("c3d3cd23-e209-c3ca-aabc-30addc000022");

    private static final UUID FTP_DOWN_UUID =
            UUID.fromString("c3d3cd23-e209-c3ca-aabc-30addc000023");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启，则开启蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            initBlueSocket();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_ENABLE_BT) {
            initBlueSocket();
        }

    }

    public void makeDiscoverable() {


    }

    public void initBlueSocket() {
        if(null==mainThread) {
            mainThread = new BlueSshThread();
            mainThread.start();
        }
    }

    BlueSshThread mainThread;

    Handler hWnd = new Handler();

    class BlueSshThread extends Thread {

        @Override
        public void run() {
            StreamThread stdout =null;
            StreamThread stdin=null;
            BluetoothServerSocket server=null;
            BluetoothSocket socket=null;

            try {
                twMessage("准备连接中");
                server = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BLUESSHD",MY_UUID);
                if (null != server) {
                    while (true) {

                        socket = server.accept();
                        twMessage("手机开始连接");
                        ProcessBuilder builder = new ProcessBuilder("/system/bin/sh","-");
                        builder.redirectErrorStream(true);
                        Process ps = builder.start();
                        stdout = new StreamThread(ps.getInputStream(), socket.getOutputStream());
                        stdin = new StreamThread(socket.getInputStream(), ps.getOutputStream());
                        stdout.start();
                        stdin.start();
                        boolean running=true;
                        while (running){
                            running=stdin.IsRunning() && stdout.IsRunning();
                            Log.e("BLUESSHD","Running...,"+stdout.send+" send and "+stdin.send+" recv");
                        }
                        ps.destroy();
                        socket.close();
                        twMessage("手机断开连接");
                    }
                }

            } catch (Exception e) {
                try {
                    stdin.interrupt();
                }catch (Exception ex){ex.printStackTrace();}
                try {
                    stdout.interrupt();
                }catch (Exception ex){ex.printStackTrace();}
                try {
                    server.close();
                }catch (Exception ex){ex.printStackTrace();}
                try {
                    socket.close();
                }catch (Exception ex){ex.printStackTrace();}
                e.printStackTrace();
            }
            twMessage("连接已关闭");
        }
    }

    class StreamThread extends Thread {

        long send=0;

        InputStream is;
        OutputStream os;


        public StreamThread(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            isRunning = true;
        }

        public boolean isRunning = false;

        @Override
        public void run() {
                try {
                    byte[] buffer = new byte[2048];
                    while (true) {
                        int len = is.read(buffer);
                        os.write(buffer, 0, len);
                        os.flush();
                        send+=len;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                isRunning = false;
        }


        public boolean IsRunning() throws InterruptedException {
            Thread.sleep(500);
            return isRunning;
        }
    }

    public void twMessage(final String str) {
        hWnd.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        try {
            mainThread.interrupt();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        super.onBackPressed();
    }
}
