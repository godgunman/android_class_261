package com.example.simpleui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private CheckBox hideCheckBox;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private ListView historyListView;
    private Spinner storeInfoSpinner;
    private ImageView photoImageView;

    private static final int REQUEST_DRINK_MENU = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;

    private String drinkMenuResult;

    private ProgressDialog progressDialog;

    private List<ParseObject> queryResult;

    private boolean hasPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sp.edit();

        inputText = (EditText) findViewById(R.id.inputText);
        inputText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                String text = inputText.getText().toString();
                editor.putString("inputText", text);
                editor.commit();

                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (i == KeyEvent.KEYCODE_ENTER) {
                        submit(view);
                        return true;
                    }
                }
                return false;
            }
        });

        hideCheckBox = (CheckBox) findViewById(R.id.hideCheckBox);
        hideCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                editor.putBoolean("hideCheckBox", isChecked);
                editor.commit();
            }
        });

        historyListView = (ListView) findViewById(R.id.historyListView);
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                goToOrderDetail(position);
            }
        });

        storeInfoSpinner = (Spinner) findViewById(R.id.storeInfoSpinner);

        inputText.setText(sp.getString("inputText", ""));
        hideCheckBox.setChecked(sp.getBoolean("hideCheckBox", false));

        photoImageView = (ImageView) findViewById(R.id.photo);
        progressDialog = new ProgressDialog(this);

        setHistory();
        setStoreInfo();
        Intent i = getIntent();
        String username = i.getStringExtra("username");
        TextView nameTextView = (TextView) findViewById(R.id.textView);
        nameTextView.setText(username);
    }

    private void setStoreInfo() {
        ParseQuery<ParseObject> query = new ParseQuery<>("StoreInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                String[] data = new String[objects.size()];

                for (int i = 0; i < objects.size(); i++) {
                    ParseObject object = objects.get(i);
                    data[i] = object.getString("name") + "," + object.getString("address");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, data);
                storeInfoSpinner.setAdapter(adapter);
            }
        });

    }

    private void setHistory() {
        ParseQuery<ParseObject> query = new ParseQuery<>("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    queryResult = objects;
                    orderObjectToListView(objects);
                    historyListView.setVisibility(View.VISIBLE);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            }
        });
    }

    private void orderObjectToListView(List<ParseObject> rawData) {
        List<Map<String, String>> data = new ArrayList<>();

        for (int i = 0; i < rawData.size(); i++) {
            ParseObject object = rawData.get(i);
            String note = object.getString("note");
            String store_info = object.getString("store_info");
            JSONArray menu = object.getJSONArray("menu");

            Map<String, String> item = new HashMap<>();
            item.put("note", note);
            item.put("store_info", store_info);
            item.put("drink_number", getDrinkNumber(menu));

            data.add(item);
        }

        String[] from = new String[]{"note", "store_info", "drink_number"};
        int[] to = new int[]{R.id.note, R.id.store_info, R.id.drink_number};

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.listview_item, from, to);
        historyListView.setAdapter(adapter);
    }

    private String getDrinkNumber(JSONArray menu) {
        return "13";
    }

    public void submit(View view) {

        progressDialog.setTitle("Loading...");
        progressDialog.show();

        String text = inputText.getText().toString();
        if (hideCheckBox.isChecked()) {
            text = "*********";
        }
        try {
            inputText.setText("");

            ParseObject orderObject = new ParseObject("Order");
            orderObject.put("note", text);
            orderObject.put("store_info", storeInfoSpinner.getSelectedItem());
            if (drinkMenuResult != null) {
                orderObject.put("menu", new JSONArray(drinkMenuResult));
            }
            if (hasPhoto == true) {
                Uri uri = Utils.getPhotoUri();
                ParseFile parseFile = new ParseFile("photo.png", Utils.uriToBytes(this, uri));
                orderObject.put("photo", parseFile);
            }
            orderObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    progressDialog.dismiss();
                    setHistory();
                    Toast.makeText(MainActivity.this, "done", Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void goToDrinkMenu(View view) {
        String storeInfoString = (String) storeInfoSpinner.getSelectedItem();
        Intent intent = new Intent();
        intent.setClass(this, DrinkMenuActivity.class);
        intent.putExtra("store_info", storeInfoString);
        startActivityForResult(intent, REQUEST_DRINK_MENU);
    }

    private void goToCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getPhotoUri());
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void goToOrderDetail(int position) {
        ParseObject object = queryResult.get(position);

        Intent intent = new Intent();
        intent.setClass(this, OrderDetailActivity.class);
        intent.putExtra("store_info", object.getString("store_info"));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DRINK_MENU) {
            if (resultCode == RESULT_OK) {
                drinkMenuResult = data.getStringExtra("result");
                Log.d("debug", drinkMenuResult);
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                Uri uri = Utils.getPhotoUri();
                photoImageView.setImageURI(uri);
                hasPhoto = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_take_photo) {
            goToCamera();
        }

        return super.onOptionsItemSelected(item);
    }

}
