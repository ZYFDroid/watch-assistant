package com.tomoon.extensions.notificationpusher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BlockActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);
    }

    public void blockSomeApp(View view) {
        startActivity(new Intent(this,BlockAppActivity.class));
    }

    public void blockSomeKeyword(View view) {
        startActivity(new Intent(this,BlockKeywordActivity.class));
    }
}
