package com.cwsu.client;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.cwsu.client.Setting.gatewayURL;
import static com.cwsu.client.Setting.serverURL;
import static com.cwsu.client.Setting.spu;
import static util.Calculate.decrypt;
import static util.Calculate.xor;

public class GetDataActivity extends AppCompatActivity{

    private sessionManager sessionHelper= null;
    String dtype = "";
    String rorh = "";
    String URL = "";
    String Ku = "";
    Bundle bundle;
    RequestQueue queue;
    TextView txtGetDataHead,txtSensor,txtYaxis;
    LineChart graphSensor;
    String sensor="",yaxis="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getdata);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_getdata_layout);
        initModule();
        initLayout();
        bundle = GetDataActivity.this.getIntent().getExtras();
        dtype = bundle.getString("datatype");
        rorh = bundle.getString("rorh");

        txtSensor.setText(setDataType(dtype));
        txtYaxis.setText(setDataType2(dtype));
        if(rorh.equals("realtime")){
            txtGetDataHead.setText("即時資料");
            URL = gatewayURL + "/getdata";
        }else{
            txtGetDataHead.setText("歷史資料");
            URL = serverURL + "/gethistorydata";
        }
        getData();
    }


    private void initModule(){

        queue = Volley.newRequestQueue(GetDataActivity.this);
        sessionHelper = new sessionManager(getApplicationContext());
    }


    private void initLayout(){
        txtGetDataHead = (TextView)findViewById(R.id.getdatahead);
        txtSensor = (TextView)findViewById(R.id.sensor);
        txtYaxis = (TextView)findViewById(R.id.Yaxis);
        graphSensor = (LineChart) findViewById(R.id.graph_sensor);
        graphSensor.setDescription("");
        configCharAxis(graphSensor);
    }


    private void getData(){

        final String signinIdText = sessionHelper.getUserName();
        Ku = sessionHelper.getKu();
        String token = sessionHelper.getToken();

        String getdata = xor(signinIdText + "_" + token, spu);
        JSONObject request = new JSONObject();
        try {
            request.put("getdata",getdata);
            request.put("datatype",dtype);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, URL, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObj) {
                        try {
                            String response = decrypt(responseObj.getString("data"), spu);
                            generateGraph(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Can not post request to server1", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Can not post request to server2", Toast.LENGTH_LONG).show();
                    }
                }
        );
        queue.add(strReq);
    }


    private void generateGraph(String response) throws JSONException {

        JSONObject obj = new JSONObject(response);

        List<String> dateSensor = new ArrayList<>();
        List<Entry> dataSensor = new ArrayList<>();
        JSONArray sensorArr = obj.getJSONArray(dtype);
        for(int i=0; i<sensorArr.length(); i++){
            dateSensor.add(sensorArr.getJSONObject(i).getString("TimeStamp"));
            dataSensor.add(new Entry(Float.parseFloat(sensorArr.getJSONObject(i).getString("Value")),i));
        }

        LineDataSet dataSetSensor = new LineDataSet(dataSensor, setDataType(dtype));
        LineData ldSensor = new LineData(dateSensor,dataSetSensor);
        graphSensor.setData(ldSensor);
        graphSensor.setData(ldSensor);
        graphSensor.setVisibility(View.VISIBLE);
    }

    private String setDataType(String type){
        switch(type){
            case "1":
                return "血氧感測裝置-血氧";
            case "2":
                return "心跳感測裝置-心跳";
            case "3":
                return "溫度感測裝置";
            case "4":
                return "濕度感測裝置";
            default:
                return null;
        }
    }
    private String setDataType2(String type){
        switch(type){
            case "1":
                return "百分比";
            case "2":
                return "次數/分鐘";
            case "3":
                return "攝氏";
            case "4":
                return "百分比";
            default:
                return null;
        }
    }


    private void configCharAxis(LineChart chartLine){
        XAxis xAxis = chartLine.getXAxis();
        xAxis.setTextSize(6);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis rYAxis = chartLine.getAxisRight();
        rYAxis.setEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id==0){
            Intent intent = new Intent();
            intent.setClass(GetDataActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}