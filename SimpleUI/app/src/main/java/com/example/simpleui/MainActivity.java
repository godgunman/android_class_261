package com.example.simpleui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private CheckBox hideCheckBox;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private ListView historyListView;
    private Spinner storeInfoSpinner;

    private static final int REQUEST_DRINK_MENU = 1;
    private String drinkMenuResult;

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
        storeInfoSpinner = (Spinner) findViewById(R.id.storeInfoSpinner);

        inputText.setText(sp.getString("inputText", ""));
        hideCheckBox.setChecked(sp.getBoolean("hideCheckBox", false));

        setHistory();
        setStoreInfo();
    }

    private void setStoreInfo() {
        String[] data = getResources().getStringArray(R.array.storeInfo);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item ,data);
        storeInfoSpinner.setAdapter(adapter);
    }

    private void setHistory() {

        String[] data = Utils.readFile(this, "history.txt").split("\n");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        historyListView.setAdapter(adapter);
    }

    public void submit(View view) {
        String text = inputText.getText().toString();
        if (hideCheckBox.isChecked()) {
            text = "*********";
        }
        JSONObject object = new JSONObject();
        try {
            object.put("note", text);
            object.put("store_info", (String)storeInfoSpinner.getSelectedItem());
            object.put("menu", new JSONArray(drinkMenuResult));

            text = object.toString();
            Utils.writeFile(this, "history.txt", text + "\n");
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            inputText.setText("");
            setHistory();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void goToDrinkMenu(View view){
        String storeInfoString = (String) storeInfoSpinner.getSelectedItem();
        Intent intent = new Intent();
        intent.setClass(this, DrinkMenuActivity.class);
        intent.putExtra("store_info", storeInfoString);
        startActivityForResult(intent, REQUEST_DRINK_MENU);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_DRINK_MENU) {
            if(resultCode == RESULT_OK){
                drinkMenuResult = data.getStringExtra("result");
                Log.d("debug", drinkMenuResult);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
