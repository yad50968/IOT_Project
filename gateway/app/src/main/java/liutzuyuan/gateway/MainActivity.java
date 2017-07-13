package liutzuyuan.gateway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, Gateway.class);
        startService(intent);
        /*
        Gateway gatewayserver = Gateway.getInstance(this);
        gatewayserver.start();
        */
    }
}
