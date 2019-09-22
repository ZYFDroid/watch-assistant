package com.tomoon.extensions.notificationpusher;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SpUtils {
    public static SharedPreferences getConnSp(Context ctx){
        return ctx.getSharedPreferences("conn",MODE_PRIVATE);

    }
}
