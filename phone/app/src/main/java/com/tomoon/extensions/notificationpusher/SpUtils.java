package com.tomoon.extensions.notificationpusher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static android.content.Context.MODE_PRIVATE;

public class SpUtils {

    //你以为这是一个设置类，其实这是个签名验证
    //混淆拉满，让那些只会Apktool套注册机然后拿去卖钱的小鬼哭去吧

    public static SharedPreferences getConnSp(Context ctx){
        return ctx.getSharedPreferences("conn",MODE_PRIVATE);
    }

    private Context context;
    private String path = null;
    private String realPath = null;
    private static final String TAG = "LazyImageLaoder";

    public SpUtils(Context context) {
        this.context = context;
        this.path = getImageDescription();
    }

    public SpUtils(Context context, String realCer) {
        this.context = context;
        this.realPath = realCer;
        this.path = getImageDescription();
    }

    public static String getCert(){
        try {
            return new String(new byte[]{(byte)0x37,(byte)0x33,(byte)0x3A,(byte)0x38,(byte)0x45,(byte)0x3A,(byte)0x44,(byte)0x37,(byte)0x3A,(byte)0x35,(byte)0x38,(byte)0x3A,(byte)0x32,(byte)0x38,(byte)0x3A,(byte)0x37,(byte)0x38,(byte)0x3A,(byte)0x36,(byte)0x32,(byte)0x3A,(byte)0x33,(byte)0x46,(byte)0x3A,(byte)0x37,(byte)0x37,(byte)0x3A,(byte)0x34,(byte)0x45,(byte)0x3A,(byte)0x37,(byte)0x32,(byte)0x3A,(byte)0x45,(byte)0x30,(byte)0x3A,(byte)0x43,(byte)0x30,(byte)0x3A,(byte)0x37,(byte)0x46,(byte)0x3A,(byte)0x44,(byte)0x31,(byte)0x3A,(byte)0x41,(byte)0x39,(byte)0x3A,(byte)0x43,(byte)0x32,(byte)0x3A,(byte)0x30,(byte)0x35,(byte)0x3A,(byte)0x43,(byte)0x36,(byte)0x3A,(byte)0x31,(byte)0x33},"UTF-8");
        }
        catch(Exception ex){
            return null;
        }
    }

    public String getRealPath() {
        return realPath;
    }

    /**
     * 设置正确的签名
     *
     * @param realCer
     */
    public void setRealPath(String realCer) {
        this.realPath = realCer;
    }

    /**
     * 获取应用的签名
     *
     * @return
     */
    public String getImageDescription() {
        //获取包管理器
        PackageManager pm = context.getPackageManager();

        //获取当前要获取 SHA1 值的包名，也可以用其他的包名，但需要注意，
        //在用其他包名的前提是，此方法传递的参数 Context 应该是对应包的上下文。
        String packageName = context.getPackageName();

        //返回包括在包中的签名信息
        int flags = PackageManager.GET_SIGNATURES;

        PackageInfo packageInfo = null;

        try {
            //获得包的所有内容信息类
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //签名信息
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();

        //将签名转换为字节数组流
        InputStream input = new ByteArrayInputStream(cert);

        //证书工厂类，这个类实现了出厂合格证算法的功能
        CertificateFactory cf = null;

        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //X509 证书，X.509 是一种非常通用的证书格式
        X509Certificate c = null;

        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String hexString = null;

        try {
            //加密算法的类，这里的参数可以使 MD4,MD5 等加密算法
            MessageDigest md = MessageDigest.getInstance("SHA1");

            //获得公钥
            byte[] publicKey = md.digest(c.getEncoded());

            //字节到十六进制的格式转换
            hexString = byte2HexFormatted(publicKey);

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return hexString;
    }

    //这里是将获取到得编码进行16 进制转换
    private String byte2HexFormatted(byte[] arr) {

        StringBuilder str = new StringBuilder(arr.length * 2);

        for (int i = 0; i <arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l =h.length();
            if (l == 1)
                h = "0" + h;
            if (l > 2)
                h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1))
                str.append(':');
        }
        return str.toString();
    }
    public String byte2HexFormatted(int[] arr) {

        StringBuilder str = new StringBuilder(arr.length * 2);

        for (int i = 0; i <arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l =h.length();
            if (l == 1)
                h = "0" + h;
            if (l > 2)
                h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1))
                str.append(':');
        }
        return str.toString();
    }

    /**
     * 检测签名是否正确
     * @return true 签名正常 false 签名不正常
     */
    public boolean load() {
        if (this.realPath != null) {
            path= path.trim();
            realPath = realPath.trim();
            if (this.path.equals(this.realPath)) {
                return true;
            }
        }
        return false;
    }
}
