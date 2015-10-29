package com.example.simpleui;

import android.os.AsyncTask;
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


        GeoCodingTask task = new GeoCodingTask();
        task.execute(address);

    }

    private class GeoCodingTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = Utils.getGEOUrl(params[0]);
            String json = new String(Utils.urlToBytes(url));
            String latLng = Utils.getLatLngFromJSON(json);

            return latLng;
        }

        @Override
        protected void onPostExecute(String result) {
            addressTextView.setText(result);
        }
    }
}
