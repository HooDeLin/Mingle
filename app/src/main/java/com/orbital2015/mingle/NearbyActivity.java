package com.orbital2015.mingle;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class NearbyActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Button chatButton;
    private Button settingsButton;
    private LocationRequest locationRequest = null;
    private GoogleApiClient locationClient = null;
    private Location currentLocation = null;
    private String currentUserId;
    private ArrayList<UserListItem> userListItems;
    private ListView usersListView;
    private SharedPreferences yourSettings;
    private int currentRadius;
    private int currentLimit;
    private TextView noNearbyTextView;
    private Button mingleButton;
    private Button refreshListButton;
    private TextView postedTextView;
    private TextView nearbyInstruction;
    private Dialog refreshDialog;
    private String previousPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        yourSettings = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        currentRadius = yourSettings.getInt("radius", 0);
        currentLimit = yourSettings.getInt("limit", 0);
        initializeUIElements();

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        if(servicesConnected()){
            buildGoogleApiClient();
        }

        currentUserId = ParseUser.getCurrentUser().getObjectId();

        mingleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mingleButton.getText().toString().equals("Undo")){
                    changeLayoutToNoListView();
                    updatePostToDatabase();
                } else {
                    showInputDialog();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nearby, menu);
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

    @Override
    public void onConnected(Bundle bundle) {
        if(!previousPost.equals("")){
            changeToListViewLayout(previousPost);
            recordLocationAndSavePost(getLocation(), previousPost);
            setListView(getLocation());
            refreshDialog.dismiss();
        }
    }

    private void openProfile(ArrayList<UserListItem> userListItems, int position){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", userListItems.get(position).getMemberName());
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> user, ParseException e) {
                try {
                    if (e == null) {
                        Intent intent = new Intent(getApplicationContext(), ViewProfileActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("NEARBY_ID", user.get(0).getObjectId());
                        extras.putString("NEARBY_PROFILE_CREDENTIALS_ID", user.get(0).getParseObject("profileCredentials").fetchIfNeeded().getObjectId());
                        intent.putExtras(extras);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Error finding that user",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception ex){
                    Log.e("profile error", ex.toString());
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(),
                "Error getting location",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(locationClient != null){
            locationClient.connect();
        }
        previousPost = yourSettings.getString("post", "");

        if(!previousPost.equals("")){
            refreshDialog = ProgressDialog.show(NearbyActivity.this, "", "Refreshing list...", true);
        }
    }

    @Override
    protected  void onStop(){
        locationClient.disconnect();
        super.onStop();
    }

    private boolean servicesConnected(){
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient(){
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private Location getLocation(){
        if(servicesConnected()){
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    private void recordLocationAndSavePost(Location userLocation, String post){
        try {
            ParseObject parseObject = ParseUser.getCurrentUser().getParseObject("userLocation").fetchIfNeeded();
            parseObject.put("userLocation", new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude()));
            parseObject.put("post", post);
            parseObject.put("isOnline", true);
            parseObject.saveInBackground();
        } catch(Exception e){}
    }

    private void setListView(Location currentLocation){
        userListItems = new ArrayList<UserListItem>();

        ParseQuery<ParseObject> nearbyQuery = ParseQuery.getQuery("UserLocation");
        nearbyQuery.whereNotEqualTo("userId", ParseUser.getCurrentUser().getObjectId().toString());
        nearbyQuery.whereEqualTo("isOnline", true);
        ParseGeoPoint currentGeoPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        nearbyQuery.whereWithinKilometers("userLocation", currentGeoPoint, currentRadius);
        nearbyQuery.setLimit(currentLimit);
        try{
            List<ParseObject> nearbyList = nearbyQuery.find();
            Log.e("number of results", Integer.toString(nearbyList.size()));
            for(int i = 0; i < nearbyList.size(); i ++) {
                UserListItem currentUserListItem;
                ParseObject currentParseObject = nearbyList.get(i);
                String currentPost = currentParseObject.getString("post");
                String currentName;
                Bitmap bitPicture;
                ParseObject currentProfile = currentParseObject.fetchIfNeeded().getParseObject("profileCredentials");
                currentName = currentProfile.fetchIfNeeded().getString("username");

                ParseFile profilePicFile = currentProfile.fetchIfNeeded().getParseFile("profilePicture");
                if (profilePicFile == null) {
                    bitPicture = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                } else {
                    byte[] bytes = profilePicFile.getData();
                    bitPicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
                currentUserListItem = new UserListItem(currentName, bitPicture, currentPost, "");
                userListItems.add(currentUserListItem);
            }

            UserListItemAdapter userListItemAdapter = new UserListItemAdapter(getApplicationContext(), userListItems);

            usersListView = (ListView) findViewById(R.id.usersListView);
            usersListView.setAdapter(userListItemAdapter);

            usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openProfile(userListItems, position);
                }
            });

            if(nearbyList.size() == 0){
                usersListView.setVisibility(View.GONE);
                noNearbyTextView.setVisibility(View.VISIBLE);
            } else {
                usersListView.setVisibility(View.VISIBLE);
            }

        } catch(Exception e){
        }
    }

    private void showInputDialog(){
        LayoutInflater layoutInflater = LayoutInflater.from(NearbyActivity.this);
        View promptView = layoutInflater.inflate(R.layout.post_item, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NearbyActivity.this);
        alertDialogBuilder.setView(promptView);
        final TextView characterCountTextView = (TextView) promptView.findViewById(R.id.characterCounterTextView);

        final EditText editText = (EditText) promptView.findViewById(R.id.postItemEditText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String characterCountString = String.format("%d/%d", editText.getText().length(), 30);
                characterCountTextView.setText(characterCountString);
            }
        });
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        currentLocation = getLocation();
                        String inputText = editText.getText().toString();
                        if(inputText.length() == 0){
                            Toast.makeText(getApplicationContext(),
                                    "Please enter a post", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            addPostToLocal(inputText);
                            recordLocationAndSavePost(currentLocation, inputText);
                            setListView(currentLocation);
                            changeToListViewLayout(inputText);
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void initializeUIElements(){
        noNearbyTextView = (TextView) findViewById(R.id.noNearbyTextView);
        chatButton = (Button) findViewById(R.id.chatButton);
        mingleButton = (Button) findViewById(R.id.mingleButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        refreshListButton = (Button) findViewById(R.id.refreshListButton);
        postedTextView = (TextView) findViewById(R.id.postedTextView);
        nearbyInstruction = (TextView) findViewById(R.id.nearbyInstructionTextView);
    }

    private void changeToListViewLayout(final String inputText){
        nearbyInstruction.setVisibility(View.GONE);
        mingleButton.setText("Undo");
        refreshListButton.setVisibility(View.VISIBLE);
        postedTextView.setVisibility(View.VISIBLE);
        postedTextView.setText("You posted: " + inputText);
        refreshListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDialog = ProgressDialog.show(NearbyActivity.this, "", "Refreshing list...", true);
                Location currentLocation = getLocation();
                recordLocationAndSavePost(currentLocation, inputText);
                setListView(currentLocation);
                refreshDialog.dismiss();
            }
        });
    }

    private void changeLayoutToNoListView(){
        nearbyInstruction.setVisibility(View.VISIBLE);
        noNearbyTextView.setVisibility(View.GONE);
        usersListView.setVisibility(View.GONE);
        mingleButton.setText("Mingle");
        refreshListButton.setVisibility(View.GONE);
        postedTextView.setVisibility(View.GONE);
    }

    private void updatePostToDatabase(){
        try {
            ParseObject parseObject = ParseUser.getCurrentUser().getParseObject("userLocation").fetchIfNeeded();
            parseObject.put("post", "");
            parseObject.put("isOnline", false);
            parseObject.saveInBackground();

            SharedPreferences.Editor editor = yourSettings.edit();
            editor.remove("post");
            editor.commit();
        } catch(Exception e){}
    }

    private void addPostToLocal(String inputText){
        SharedPreferences.Editor editor = yourSettings.edit();
        editor.putString("post", inputText);
        editor.commit();
    }
}
