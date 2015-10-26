package com.example.simpleui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView addressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        addressTextView = (TextView) findViewById(R.id.address);

        String storeInfo = getIntent().getStringExtra("store_info");
        final String address = storeInfo.split(",")[1];
        Log.d("debug", "address=" + address);

        addressTextView.setText(address);

        // will replace to AsyncTask
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Utils.getGEOUrl(address);
                final String result = new String(Utils.urlToBytes(url));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addressTextView.setText(Utils.getLatLngFromJSON(result));
                    }
                });

            }
        }).start();
    }
}
