package liutzuyuan.gateway;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.apache.commons.codec.DecoderException;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import threegpp.milenage.MilenageResult;

import static android.content.ContentValues.TAG;
import static liutzuyuan.gateway.Calculate.encrypt;
import static liutzuyuan.gateway.Calculate.decrypt;
import static liutzuyuan.gateway.Calculate.generateAuthVector;
import static liutzuyuan.gateway.Calculate.generateRandom256bit01;
import static liutzuyuan.gateway.Calculate.sha3;
import static liutzuyuan.gateway.Calculate.xor;

import static liutzuyuan.gateway.Setting.*;


public class Gateway extends Service {

    @Override
    public void onCreate() {

        super.onCreate();

        Log.i(TAG, "Service onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {




        Log.i(TAG, "Service onStartCommand");

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR

        new Thread(new Runnable() {
            @Override
            public void run() {
                final DBHelper dbh = new DBHelper();

                final RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
                System.out.println("ff");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        final JSONObject ob = dbh.historyDataSendToServer();
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverURL + "/gatewayhistorydata",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        System.out.println("post result:" + response);
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println("post error:" + error.toString());
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<>();
                                map.put("data", encrypt(ob.toString(), gsu));
                                return map;
                            }
                        };
                        mQueue.add(stringRequest);
                    }
                }, 1000, sendDataToServerTime);
            }
        }).start();;

        new Thread(new Runnable() {
            @Override
            public void run() {

                final DBHelper dbh = new DBHelper();
                final RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
                AsyncHttpServer gatewayserver = new AsyncHttpServer();
                gatewayserver.post("/register", new HttpServerRequestCallback() {
                    @Override
                    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                        String ID, PV, Espu_P, Espu_Ku;
                        String Ku, hash_ID_Xs, e_hash_ID_Xs, IDX;
                        String hash_PV_Ku, e_hash_PV_Ku, XPV;
                        String P, UKP;

                        try {
                            JSONObject requestBody_JSON = ((JSONObjectBody) request.getBody()).get();
                            ID = requestBody_JSON.getString("ID");
                            PV = requestBody_JSON.getString("PV");
                            Espu_P = requestBody_JSON.getString("Espu_P");
                            Espu_Ku = requestBody_JSON.getString("Espu_Ku");

                            Ku = decrypt(Espu_Ku, spu);
                            hash_ID_Xs = sha3(ID + "_" + Xs);
                            e_hash_ID_Xs = encrypt(hash_ID_Xs, spu);
                            IDX = xor(e_hash_ID_Xs, Xs);
                            hash_PV_Ku = sha3(PV + "_" + Ku);
                            e_hash_PV_Ku = encrypt(hash_PV_Ku, spu);
                            XPV = xor(e_hash_PV_Ku, Xs);

                            P = decrypt(Espu_P, spu);
                            UKP = encrypt(ID + "_" + Ku + "_" + P, spu);

                            User newUser = new User(ID, IDX, XPV, UKP, Ku);

                            JSONObject res = new JSONObject();
                            if(dbh.addUserData(newUser)) {
                                res.put("response", "yes");
                                response.send(res);
                                postRegisterUserToServer(ID, Ku, serverURL, mQueue);
                            } else {
                                res.put("response", "ID have been registered");
                                response.send(res);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });


                gatewayserver.post("/auth_1", new HttpServerRequestCallback() {

                    @Override
                    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                        JSONObject res = new JSONObject();

                        String X1 = "", XP, ID, r1, Ku, G1, G2;
                        String P, CA1;
                        String RAND, RES, SQN, AK, SQN_xor_AK, AMF, MAC_A;
                        String[] Dspu_X1;
                        Map<?, ?> authVectorResult = new HashMap<>();
                        Map<MilenageResult, byte[]> av;

                        try {
                            JSONObject requestBody_JSON = ((JSONObjectBody) request.getBody()).get();
                            X1 = requestBody_JSON.getString("X1");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Dspu_X1 = decrypt(X1, spu).split("_");
                        XP = Dspu_X1[0];
                        ID = Dspu_X1[1];
                        r1 = Dspu_X1[2];
                        User user = dbh.selectUserData(ID);
                        if(user == null) {
                            try {
                                res.put("response", "No user");
                                response.send(res);
                                return;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Ku = user.getKu();
                        G1 = sha3(ID + "_" + Xs);
                        G2 = decrypt(
                                xor(user.getIDX(), Xs),
                                spu);
                        if (G1.equals(G2)) {
                            P = xor(XP, G2);
                            CA1 = xor(sha3(ID + "_" + P), r1);
                            try {
                                authVectorResult = generateAuthVector(Ku);
                            } catch (NoSuchAlgorithmException | DecoderException e) {
                                e.printStackTrace();
                            }
                            RAND = authVectorResult.get("RAND").toString();
                            AMF = authVectorResult.get("AMF").toString();
                            SQN = authVectorResult.get("SQN").toString();
                            av = (Map<MilenageResult, byte[]>) authVectorResult.get("av");


                            MAC_A = Arrays.toString(av.get(MilenageResult.MAC_A));
                            AK = Arrays.toString(av.get(MilenageResult.AK));
                            SQN_xor_AK = xor(SQN, AK);
                            RES = Arrays.toString(av.get(MilenageResult.RES));

                            dbh.addOrUpdateResData(ID, RES);

                            try {
                                res.put("CA1", CA1);
                                res.put("RAND", RAND);
                                res.put("SQN_xor_AK", SQN_xor_AK);
                                res.put("AMF", AMF);
                                res.put("MAC_A", MAC_A);
                                response.send(res);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                res.put("response", "false");
                                response.send(res);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


                gatewayserver.post("/auth_2", new HttpServerRequestCallback() {

                    @Override
                    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                        String L = "";
                        String ID = "";
                        String RES, XPV;
                        String CB1, CB2, CB3;

                        JSONObject res = new JSONObject();
                        try {
                            JSONObject requestBody_JSON = ((JSONObjectBody) request.getBody()).get();
                            L = requestBody_JSON.getString("L");
                            ID = requestBody_JSON.getString("ID");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        RES = dbh.selectResData(ID);
                        XPV = dbh.selectUserDataXPV(ID);
                        CB1 = xor(L, RES);
                        CB2 = decrypt(
                                xor(XPV, Xs),
                                spu);
                        CB3 = xor(
                                sha3(xor(CB2, RES)),
                                CB2);

                        if (CB1.equals(CB3)) {
                            String token = "";
                            try {
                                token = generateRandom256bit01();
                                dbh.addOrUpdateUserToken(ID, token);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }

                            try {
                                res.put("token", encrypt(token, spu));
                                response.send(res);
                                postTokenToServer(ID, token, serverURL, mQueue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                res.put("token", "false");
                                response.send(res);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


                gatewayserver.post("/getdata", new HttpServerRequestCallback() {

                    @Override
                    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                        String getdata = "";
                        int datatype = 0;
                        String[] Dspu_getdata;
                        String ID , token;
                        String dbToken;

                        JSONObject res = new JSONObject();
                        try {
                            JSONObject requestBody_JSON = ((JSONObjectBody) request.getBody()).get();
                            getdata = requestBody_JSON.getString("getdata");
                            datatype = Integer.parseInt(requestBody_JSON.getString("datatype"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Dspu_getdata = decrypt(getdata, spu).split("_");
                        ID = Dspu_getdata[0];
                        token = Dspu_getdata[1];
                        dbToken = dbh.selectUserToken(ID);

                        if (dbToken.equals(token)) {

                            JSONObject obj = dbh.getRealTimeData(datatype);
                            String en_data = encrypt(obj.toString(), spu);
                            try {
                                res.put("data", en_data);
                                response.send(res);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                res.put("temperature", "auth fail");
                                res.put("humidity", "auth fail");
                                res.put("heartbeat", "auth fail");
                                response.send(res);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


                gatewayserver.post("/sensordata", new HttpServerRequestCallback() {

                    @Override
                    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                        String DeviceId, TimeStamp, name, reportid;
                        int DataType;
                        double Value;

                        try {
                            JSONObject requestBody_JSON = ((JSONObjectBody) request.getBody()).get();
                            DeviceId = requestBody_JSON.getString("DeviceId");
                            TimeStamp = requestBody_JSON.getString("TimeStamp");
                            DataType = requestBody_JSON.getInt("DataType");
                            Value = requestBody_JSON.getDouble("Value");
                            name = requestBody_JSON.getString("name");
                            reportid = requestBody_JSON.getString("reportid");

                            dbh.insertSensorData(DeviceId, TimeStamp, name, reportid, DataType, Value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        response.send("OK");
                    }
                });


                gatewayserver.listen(gatewaylistenport);


            }
        }).start();

        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "Service onDestroy");
        stopSelf();
    }


    private void postRegisterUserToServer(final String username, final String Ku, final String url, final RequestQueue mQueue) {

        String regurl = url + "/register";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, regurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("post result:" + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("post error:" + error.toString());
                    }
                }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("username", encrypt(username, gsu));
                    map.put("Ku", encrypt(Ku, gsu));
                    return map;
                }
        };
        mQueue.add(stringRequest);
    }


    private void postTokenToServer(final String username, final String token, final String url, final RequestQueue mQueue) {
        String tokenUrl = url + "/updateToken";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tokenUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("post result:" + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("post error:" + error.toString());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        map.put("username", encrypt(username, gsu));
                        map.put("token", encrypt(token, gsu));
                        return map;
                    }
                };
        mQueue.add(stringRequest);
    }

}