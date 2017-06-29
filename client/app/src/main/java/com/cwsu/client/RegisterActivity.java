package com.cwsu.client;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cwsu.client.Setting.Xs;
import static com.cwsu.client.Setting.gatewayURL;
import static com.cwsu.client.Setting.spu;
import static util.Calculate.encrypt;
import static util.Calculate.sha3;
import static util.Calculate.xor;


public class RegisterActivity extends AppCompatActivity{


    EditText signupId;
    EditText signupPw;
    EditText signupConfirmPw;
    EditText signupEmail;
    Button btnDoSignup;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_register_layout);
        initModule();
        initLayout();
        initListener();
    }


    private void initModule(){
        queue = Volley.newRequestQueue(RegisterActivity.this);
    }


    private void initLayout() {

        signupId = (EditText) findViewById(R.id.signup_id);
        signupPw = (EditText) findViewById(R.id.signup_pw);
        signupConfirmPw = (EditText) findViewById(R.id.signup_confirm_pw);
        signupEmail = (EditText)findViewById(R.id.email) ;
        btnDoSignup = (Button) findViewById(R.id.btn_do_signup);

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(5); // 5 is the length i am passing
        signupId.setFilters(FilterArray);
        signupId.setLineSpacing(2, 1);
    }


    private void initListener() {
        btnDoSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String url = gatewayURL + "/register";
                JSONObject request = new JSONObject();
                final String signupIdText = signupId.getText().toString();
                String signupPwText = signupPw.getText().toString();
                String signupConfirmPwText = signupConfirmPw.getText().toString();
                String Ku = sha3(signupIdText + "_" + Xs + "_" + signupPwText);
                String PV = xor(sha3(Ku + "_" + signupPwText), Ku);

                if(signupPwText.equals(signupConfirmPwText)){
                    try {
                        request.put("ID", signupIdText);
                        request.put("PV",PV);
                        request.put("Espu_P", encrypt(signupPwText, spu));
                        request.put("Espu_Ku", encrypt(Ku, spu));

                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, url, request,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject responseObj) {
                                    try {
                                        String response = responseObj.getString("response");
                                        if(response.equals("yes")){
                                            Toast.makeText(getApplicationContext(), "register success", Toast.LENGTH_LONG).show();
                                            RegisterActivity.this.finish();
                                        }else{
                                            Toast.makeText(getApplicationContext(), "id already exist", Toast.LENGTH_LONG).show();
                                            signupId.setText("");
                                            signupPw.setText("");
                                            signupConfirmPw.setText("");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Can not post request to server", Toast.LENGTH_LONG).show();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), "Can not post request to server", Toast.LENGTH_LONG).show();
                                    System.out.println("sss");
                                }
                            }
                    );
                    queue.add(strReq);
                }else{
                    Toast.makeText(getApplicationContext(), "password and confirm password are not equal", Toast.LENGTH_LONG).show();
                    signupPw.setText("");
                    signupConfirmPw.setText("");
                }
            }
        });
    }
}
