package com.orbital2015.mingle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;


public class SettingsActivity extends ActionBarActivity {
    private Button nearByButton;
    private Button chatButton;
    private Button updateProfileButton;
    private Button editSearchButton;
    private Button logoutButton;
    private TextView settingsAboutUsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        nearByButton = (Button) findViewById(R.id.nearbyButton);

        nearByButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NearbyActivity.class);
                startActivity(intent);
            }
        });

        chatButton = (Button) findViewById(R.id.settingsButton);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        updateProfileButton = (Button) findViewById(R.id.updateProfileButton);
        editSearchButton = (Button) findViewById(R.id.editSearchButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                startActivity(intent);
            }
        });

        editSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditSearchActivity.class);
                startActivity(intent);
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPreferences();
                stayOffline();
                ParseUser.logOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                stopService(new Intent(getApplicationContext(), MessageService.class));
            }
        });

        settingsAboutUsTextView = (TextView) findViewById(R.id.settingsAboutUsTextView);
        settingsAboutUsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AboutUsActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onDestroy(){
        stopService(new Intent(this, MessageService.class));
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    private void stayOffline(){
        try {
            ParseObject parseObject = ParseUser.getCurrentUser().getParseObject("userLocation").fetchIfNeeded();
            parseObject.put("post", "");
            parseObject.put("isOnline", false);
            parseObject.saveInBackground();
        } catch(Exception e){}
    }

    private void clearPreferences(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
