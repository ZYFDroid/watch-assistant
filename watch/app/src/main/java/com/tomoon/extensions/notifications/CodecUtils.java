package com.tomoon.extensions.notifications;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class CodecUtils {

    public static String encodeAsPacket(JSONObject jsonObject){
        try {
            return Base64.encodeToString(jsonObject.toString().getBytes("UTF-8"), Base64.URL_SAFE)+";";
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject decodeFromPacket(String packet) throws JSONException {
        if(packet.contains(";")){packet=packet.replace(";","");}
        String raw = null;
        try {
            raw = new String(Base64.decode(packet, Base64.URL_SAFE), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return new JSONObject(raw);
    }

}
