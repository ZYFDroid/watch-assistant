package com.tomoon.extensions.bluessh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ShellActivity extends Activity {

    public static int maxconsoles=18000;
    ScrollView mainScroll;
    ProgressDialog progDialog;
    Handler hWnd = new Handler();



    Switch chkAutoScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);
        mainScroll = findViewById(R.id.mainScroll);
        chkAutoScroll=findViewById(R.id.chkAutoScroll);
        String bdAddr=getIntent().getStringExtra("bdmac");
        final BluetoothDevice dev = MainActivity.mBluetoothAdapter.getRemoteDevice(bdAddr);
        progDialog=new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
        progDialog.setTitle("正在连接到设备");
        progDialog.setCancelable(false);
        progDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = dev.createInsecureRfcommSocketToServiceRecord(MainActivity.MY_UUID);
                    socket.connect();
                    stdin=socket.getInputStream();
                    stdout=socket.getOutputStream();
                    sysout=new PrintStream(stdout);
                    //sysout.println("echo SSH over Bluetooth");
                    initReadWrite();
                } catch (final IOException e) {
                    e.printStackTrace();
                    hWnd.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ShellActivity.this, e.toString()+"\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
                hWnd.post(new Runnable() {
                    @Override
                    public void run() {
                        progDialog.dismiss();
                    }
                });
            }
        }).start();

        ((EditText)findViewById(R.id.txtCommand)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().contains("\n") || s.toString().contains("\r")){
                    send(null);
                }
            }
        });
    }

    StringBuffer stringBuffer=new StringBuffer();


    Runnable writeRunnable = new Runnable() {
        @Override
        public void run() {
            InputStreamReader isr=new InputStreamReader(stdin);
            BufferedReader br=new BufferedReader(isr);
            char[] buffer=new char[8192];
            int len=0;
            String line="";
            try {
                while ((len=br.read(buffer)) > 0) {
                    synchronized (stringBuffer) {
                        stringBuffer.append(buffer, 0, len);
                    }
                    hWnd.post(updateRunnable);
                    Thread.sleep(1);
                }
//                while ((line=br.readLine()) != null) {
//                    stringBuffer.append(line+"\n");
//                    hWnd.post(updateRunnable);
//                    Thread.sleep(32);
//                }

            }catch (Exception ex){
                ex.printStackTrace();
                try {
                    socket.close();
                }catch (Exception em){em.printStackTrace();}
                hWnd.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();;
                    }
                });
            }
        }
    };



    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Editable ext = ((EditText)findViewById(R.id.txtCommandResult)).getText();
            synchronized (stringBuffer) {
                ext.append(stringBuffer.toString());
                stringBuffer.delete(0, stringBuffer.length());
            }
            if(ext.length()>maxconsoles){
                ext.delete(0,ext.length()-maxconsoles);
            }
            hWnd.postDelayed(scrollRUnnable,250);
        }
    };

    Runnable scrollRUnnable=new Runnable() {
        @Override
        public void run() {
            if(chkAutoScroll.isChecked()){
                try{
                    ScrollView scroll=mainScroll;
                    View inner=scroll.getChildAt(0);

                    int offset = inner.getMeasuredHeight() - scroll.getHeight();
                    if (offset < 0) {
                        offset = 0;
                    }

                    scroll.scrollTo(0, offset);
                }catch (Exception ex){ex.printStackTrace();}
            }
        }
    };

    Thread writeThread =null;
    public void initReadWrite(){
        writeThread=new Thread(writeRunnable);
        writeThread.start();
    }

    InputStream stdin=null;
    OutputStream stdout=null;
    PrintStream sysout=null;
    BluetoothSocket socket=null;


    public void send(View view) {

        Editable ext = ((EditText)findViewById(R.id.txtCommandResult)).getText();


        String input=((TextView)findViewById(R.id.txtCommand)).getText().toString().replace("\r","").replace("\n","");
        ext.append("【指令:> ");
        ext.append(input);
        ext.append("】\r\n");
        hWnd.postDelayed(scrollRUnnable,250);
        //stringBuffer.delete(0,stringBuffer.length());

        //sysout.println("echo \"`pwd` > "+(input.replace("\"","\\\"").replace("$","\\$"))+"\"");
        sysout.println(input);
        //sysout.println("echo \"`pwd` > \"");
        ((EditText)findViewById(R.id.txtCommand)).getText().clear();
    }

    @Override
    protected void onDestroy() {
        try {
            stdin.close();
        }catch (Exception em){em.printStackTrace();}
        try {
            stdout.close();
        }catch (Exception em){em.printStackTrace();}
        try {
            socket.close();
        }catch (Exception em){em.printStackTrace();}
        super.onDestroy();
    }

    public void clear(View view) {
        ((EditText)findViewById(R.id.txtCommandResult)).getText().clear();
    }
}
