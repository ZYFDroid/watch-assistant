package com.tomoon.extensions.notifications;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;

public class ReceiverService extends Service {

    public static UUID BtPort = UUID.fromString("c3d3cd23-e209-c3ca-aabd-30addc000000");
    Vibrator vibrator = null;
    long[] defaultPattern={0,100,200,100};
    public static ReceiverService mInstance =null;

    @Override
    public IBinder onBind(Intent intent) {return null;}


    @Override
    public void onCreate() {
        super.onCreate();
    }

    BluetoothAdapter nBluetoothAdapter;
    Handler hWnd=new Handler();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mInstance=this;
        vibrator=(Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
        nBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            server.start();
        }catch (Exception ex){}
        return super.onStartCommand(intent, flags, startId);
    }

    ServerThread server = new ServerThread();

    class ServerThread extends Thread{
        @Override
        public void run() {
            BluetoothServerSocket server=null;

            while (true) {
                try {
                    Log.e("TMNOT", "准备连接中");
                    server = nBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BLUESSHD", BtPort);
                    if (null != server) {
                        while (true) {
                            BluetoothSocket socket = server.accept();
                            Log.e("TMNOT", "手机开始连接");
                            sendMessage("手表连接","连接状态","手机连接成功");
                            new SocketThread(socket).start();
                        }
                    }

                } catch (Exception e) {
                    try {
                        server.close();
                    }catch (Exception ex){ex.printStackTrace();}
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        return;
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    class SocketThread extends Thread{
        BluetoothSocket mSocket;
        public SocketThread(BluetoothSocket mSocket) {
            this.mSocket = mSocket;
        }
        StringBuffer strbuf = new StringBuffer();
        @Override
        public void run() {
            try {
                InputStreamReader reader = new InputStreamReader(mSocket.getInputStream());
                OutputStream os = mSocket.getOutputStream();
                PrintStream ps = new PrintStream(os);
                char[] buffer = new char[2048];
                String message=null;

                while (true) {

                    int len=reader.read(buffer);
                    //Log.e("TMRECV", new String(buffer));

                    for(int i=0;i<len;i++){
                        if(buffer[i]==';'){
                            message=strbuf.toString();
                            try {
                                dataArrival(CodecUtils.decodeFromPacket(message));
                            }catch (JSONException jex){
                                try {
                                    JSONObject jobj = new JSONObject();
                                    jobj.put("status","500");
                                    jobj.put("ex",jex.toString());
                                    ps.print(CodecUtils.encodeAsPacket(jobj));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            strbuf.delete(0,strbuf.length());
                        }
                        else{
                            strbuf.append(buffer[i]);
                        }
                    }
                }
            } catch (IOException e) {
                try {
                    mSocket.close();
                }catch (Exception ex){}
                e.printStackTrace();
                sendMessage("手表连接","连接状态","手机断开连接");
                vibrator.vibrate(1500);
            }
        }
    }

    void dataArrival(JSONObject root) throws  JSONException{
            String status = root.getString(ConstantUtil.TAG_ACTION);
            if(status.equals(ConstantUtil.ACTION_NOTIFICATION)) {
                JSONObject jobj = root.getJSONObject(ConstantUtil.TAG_DATA);
                String title = jobj.getString("title");
                String cotent = jobj.getString("content");
                String app = "";
                if (jobj.has("app")) {
                    try {
                        app = jobj.getString("app");
                    } catch (JSONException jex) {
                        jex.printStackTrace();
                    }
                }
                hWnd.post(new NewMessagePoster(app, title, cotent));
            }

    }

    class NewMessagePoster implements  Runnable{
        String title,content,app;
        public NewMessagePoster(String app,String title, String content) {
            this.title = title;
            this.app=app;
            this.content = content;
        }
        @Override
        public void run() {
            sendMessage(app,title,content);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.interrupt();

        mInstance=null;
    }

    public void sendMessage(String app,String title,String content){
        if(getSharedPreferences("main",MODE_PRIVATE).getBoolean("vib",true)){
            vibrator.vibrate(defaultPattern,-1);
        }
        Intent i =new Intent(ReceivedReceiver.ACTION);
        i.putExtra("title",title);
        i.putExtra("content",content);
        i.putExtra("app",app);
        sendOrderedBroadcast(i,null);
    }
}
