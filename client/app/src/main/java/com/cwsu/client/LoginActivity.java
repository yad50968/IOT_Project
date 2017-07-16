package com.cwsu.client;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;
import static com.cwsu.client.Setting.Xs;
import static com.cwsu.client.Setting.gatewayURL;
import static com.cwsu.client.Setting.op;
import static com.cwsu.client.Setting.spu;
import static util.Calculate.decrypt;
import static util.Calculate.encrypt;
import static util.Calculate.generateAKRES;
import static util.Calculate.generateMAC_2;
import static util.Calculate.generateRandom256bit01;
import static util.Calculate.sha3;
import static util.Calculate.xor;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int REQUEST_READ_CONTACTS = 0;

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private Button registerButton;
    private RequestQueue queue;
    private sessionManager sessionHelper;
    private String r1 = "";
    private String token = "";
    private String isAdmin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        initModule();
        initLayout();
    }

    private void initModule(){
        queue = Volley.newRequestQueue(LoginActivity.this);
        sessionHelper = new sessionManager(getApplicationContext());
    }

    private void initLayout(){
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;

                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        registerButton = (Button) findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
/*
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
*/
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void loginBy4G (String id,String pw){
        final String idText = id;
        final String pwText = pw;

        String authUrl = gatewayURL + "/auth_1";
        String XP = xor(pwText, sha3(idText + "_" + Xs));

        final String Ku = sha3(idText + "_" + Xs + "_" + pwText);
        final String PV = xor(sha3(Ku + "_" + pwText), Ku);

        try {
            r1 = generateRandom256bit01();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        JSONObject request = new JSONObject();
        try {
            request.put("X1", encrypt(XP + "_" + idText + "_" + r1, spu));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, authUrl, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObj) {
                        try {
                            String authUrl2 = gatewayURL + "/auth_2";
                            String CA1 = responseObj.getString("CA1");
                            String RAND = responseObj.getString("RAND");
                            String SQN_xor_AK = responseObj.getString("SQN_xor_AK");
                            String AMF = responseObj.getString("AMF");
                            String MAC_A1 = responseObj.getString("MAC_A");
                            Map<?, ?> AK_RES = generateAKRES(Ku, RAND, op);

                            String AK = Arrays.toString((byte[])AK_RES.get("AK"));
                            String RES = Arrays.toString((byte[])AK_RES.get("RES"));
                            String SQN = xor(SQN_xor_AK, AK);
                            String MAC_A2 = Arrays.toString(generateMAC_2(Ku, RAND, SQN, AMF, op));
                            if(MAC_A1.equals(MAC_A2)){
                                String CA2 = xor(sha3(idText + "_" + pwText), r1);
                                if(CA1.equals(CA2)){
                                    String L = xor(
                                            xor(
                                                    sha3(
                                                            xor(
                                                                    sha3(PV + "_" + Ku),
                                                                    RES)
                                                    ),
                                                    sha3(PV + "_" + Ku)
                                            ),
                                            RES);
                                    JSONObject request2 = new JSONObject();
                                    try {
                                        request2.put("L", L);
                                        request2.put("ID",idText);
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                    JsonObjectRequest strReq2 = new JsonObjectRequest(Request.Method.POST, authUrl2, request2,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject responseObj) {
                                                    try {
                                                        String tmp_token = responseObj.getString("token");

                                                        String tmp_isAdmin = responseObj.getString("admin");
                                                        if(tmp_token.equals("false")) {
                                                            Toast.makeText(getApplicationContext(), "authenticated fail", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            token = decrypt(tmp_token, spu);
                                                            isAdmin = decrypt(tmp_isAdmin,spu);
                                                            sessionHelper.setKu(Ku);
                                                            sessionHelper.setUserName(idText);
                                                            sessionHelper.setToken(token);
                                                            sessionHelper.setAdmin(isAdmin);
                                                            Intent intent = new Intent();
                                                            if(isAdmin.equals("1")){
                                                                intent.setClass(LoginActivity.this, MenuAdminActivity.class);
                                                            }else if(isAdmin.equals("0")){
                                                                intent.setClass(LoginActivity.this, MenuNormalActivity.class);
                                                            }
                                                            startActivity(intent);
                                                        }
                                                    } catch (JSONException e) {
                                                        Toast.makeText(getApplicationContext(), "Can not post request to server", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(getApplicationContext(), "Can not post request to server", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                    );
                                    queue.add(strReq2);
                                }else {
                                    Toast.makeText(getApplicationContext(), "authenticated fail", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "authenticated fail", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "authenticated fail", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Can not post request to server", Toast.LENGTH_LONG).show();
                    }
                }
        );
        queue.add(strReq);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            loginBy4G(mEmail,mPassword);
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}


