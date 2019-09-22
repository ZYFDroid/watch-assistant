package com.tomoon.extensions.notificationpusher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class BlockKeywordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_keyword);
        ((TextView)findViewById(R.id.txtKeyword)).setText(SpUtils.getConnSp(this).getString("kw",""));
    }


    @Override
    public void onBackPressed() {
        SpUtils.getConnSp(this).edit().putString("kw",((TextView)findViewById(R.id.txtKeyword)).getText().toString()).commit();
        NotificationService.loadConfiguration(this);
        super.onBackPressed();
    }
}
