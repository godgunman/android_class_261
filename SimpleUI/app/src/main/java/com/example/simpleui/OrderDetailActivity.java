package com.example.simpleui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView addressTextView;
    private WebView webView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        webView = (WebView) findViewById(R.id.webView);

        addressTextView = (TextView) findViewById(R.id.address);
        imageView = (ImageView) findViewById(R.id.imageView);

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
        protected void onPostExecute(String latLng) {
            addressTextView.setText(latLng);

            String staticMapUrl = Utils.getStaticMapUrl(latLng, "17", "300x600");
            webView.loadUrl(staticMapUrl);

            StaticMapTask task = new StaticMapTask();
            task.execute(latLng);
        }
    }

    private class StaticMapTask extends AsyncTask<String, Integer, byte[]> {

        @Override
        protected byte[] doInBackground(String... params) {
            String staticMapUrl = Utils.getStaticMapUrl(params[0], "17", "300x600");
            return Utils.urlToBytes(staticMapUrl);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bm);
        }
    }
}
