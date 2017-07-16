package com.cwsu.client;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by cwsu on 2017/7/16.
 */

public class MenuNormalActivity extends AppCompatActivity {

    Button bAuthorized_device, bBlutooth, bManage_account;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menunormal);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_menunormal_layout);

        initLayout();
        initListener();

    }

    private void initLayout(){
        bAuthorized_device  = (Button) findViewById(R.id.bAuthorized_device);
        bBlutooth           = (Button) findViewById(R.id.bBlutooth);
        bManage_account     = (Button) findViewById(R.id.bManage_account);
    }
    private void initListener(){

        intent = new Intent();

        bAuthorized_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(MenuNormalActivity.this, SensorActivity.class);
                startActivity(intent);
            }
        });


    }
}
