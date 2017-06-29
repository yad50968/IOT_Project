package com.cwsu.client;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;


public class SensorActivity extends AppCompatActivity{

    TableLayout tableLayout;
    TableRow tableHd, tableTp, tableHb, tableBo;
    Button btnHdR, btnHdH, btnHdP;
    Button btnTpR, btnTpH, btnTpP;
    Button btnHbR, btnHbH, btnHbP;
    Button btnBoR, btnBoH, btnBoP;
    Intent intent;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_getdata_layout);
        initLayout();
        initListener();
    }


    private void initLayout(){

        tableLayout = (TableLayout) findViewById(R.id.table_layout);
        tableHd = (TableRow) findViewById(R.id.hd_table);
        btnHdR = (Button)findViewById(R.id.hd_R_Btn);
        btnHdH = (Button)findViewById(R.id.hd_H_Btn);
        btnHdP = (Button)findViewById(R.id.hd_P_Btn);

        tableTp = (TableRow) findViewById(R.id.tp_table);
        btnTpR = (Button)findViewById(R.id.tp_R_Btn);
        btnTpH = (Button)findViewById(R.id.tp_H_Btn);
        btnTpP = (Button)findViewById(R.id.tp_P_Btn);

        tableHb = (TableRow) findViewById(R.id.hb_table);
        btnHbR = (Button)findViewById(R.id.hb_R_Btn);
        btnHbH = (Button)findViewById(R.id.hb_H_Btn);
        btnHbP = (Button)findViewById(R.id.hb_P_Btn);

        tableBo = (TableRow) findViewById(R.id.bo_table);
        btnBoR = (Button)findViewById(R.id.bo_R_Btn);
        btnBoH = (Button)findViewById(R.id.bo_H_Btn);
        btnBoP = (Button)findViewById(R.id.bo_P_Btn);
    }


    private void initListener(){

        intent = new Intent();
        intent.setClass(SensorActivity.this, GetDataActivity.class);
        bundle = new Bundle();
        btnHdR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("datatype", "4");
                bundle.putString("rorh","realtime");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        btnHdH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("datatype", "4");
                bundle.putString("rorh","history");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnTpR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("datatype", "3");
                bundle.putString("rorh","realtime");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        btnTpH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("datatype", "3");
                bundle.putString("rorh","history");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnHbR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("datatype", "2");
                bundle.putString("rorh","realtime");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        btnHbH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("datatype", "2");
                bundle.putString("rorh","history");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnBoR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("datatype", "1");
                bundle.putString("rorh","realtime");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        btnBoH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("datatype", "1");
                bundle.putString("rorh","history");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
