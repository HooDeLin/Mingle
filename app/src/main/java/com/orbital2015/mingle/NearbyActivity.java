package com.orbital2015.mingle;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
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
    private ArrayAdapter<String> namesArrayAdapter;
    private SharedPreferences yourSettings;
    private int currentRadius;
    private int currentLimit;
    private Bitmap bitPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        yourSettings = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        currentRadius = yourSettings.getInt("radius", 0);
        currentLimit = yourSettings.getInt("limit", 0);
        chatButton = (Button) findViewById(R.id.chatButton);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        settingsButton = (Button) findViewById(R.id.settingsButton);

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
        currentLocation = getLocation();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserLocation");
        query.whereEqualTo("userId", currentUserId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null) {
                    parseObject.put("userLocation", new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    parseObject.saveInBackground();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Some error just occur",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        userListItems = new ArrayList<UserListItem>();
        /*
        ParseQuery<ParseObject> nearbyQuery = ParseQuery.getQuery("UserLocation");
        nearbyQuery.whereNotEqualTo("userId", currentUserId);
        ParseGeoPoint currentGeoPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        nearbyQuery.whereWithinKilometers("userLocation", currentGeoPoint, currentRadius);
        nearbyQuery.setLimit(currentLimit);
        nearbyQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < list.size(); i++) {
                        UserListItem currentUserListItem;
                        ParseObject currentParseObject = list.get(i);
                        String currentParseObjectUserId = currentParseObject.getString("userId");

                        ParseQuery<ParseObject> profilePicQuery = ParseQuery.getQuery("ProfileCredentials");
                        profilePicQuery.whereEqualTo("userId", currentParseObjectUserId);
                        profilePicQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    ParseFile profilePicture = parseObject.getParseFile("profilePicture");
                                    if (profilePicture == null) {
                                        bitPicture = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                                    } else {
                                        profilePicture.getDataInBackground(new GetDataCallback() {
                                            @Override
                                            public void done(byte[] bytes, ParseException e) {
                                                bitPicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                Toast.makeText(getApplicationContext(),
                                                        "Found picture",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Some error just occur",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        currentUserListItem = new UserListItem(currentParseObject.getString("userName"), bitPicture);
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
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });*/
    }

    private void openProfile(ArrayList<UserListItem> userListItems, int position){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", userListItems.get(position).getMemberName());
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> user, ParseException e) {
                if (e == null) {
                    Intent intent = new Intent(getApplicationContext(), ViewProfileActivity.class);
                    intent.putExtra("NEARBY_ID", user.get(0).getObjectId());
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error finding that user",
                            Toast.LENGTH_SHORT).show();
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
}
