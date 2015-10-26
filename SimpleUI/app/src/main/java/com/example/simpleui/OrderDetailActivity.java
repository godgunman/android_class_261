package com.example.simpleui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class OrderDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        String storeInfo = getIntent().getStringExtra("store_info");
        String address = storeInfo.split(",")[1];
        Log.d("debug", "address=" + address);
    }
}
