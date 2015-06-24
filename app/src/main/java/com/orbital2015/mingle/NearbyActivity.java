package com.orbital2015.mingle;

import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


public class NearbyActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;
    private Location currentLocation = null;
    private String currentUserId;
    private ArrayList<String> names;
    private ListView usersListView;
    private ArrayAdapter<String> namesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        locationRequest = LocationRequest.create();
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        currentUserId = ParseUser.getCurrentUser().getObjectId();
        names = new ArrayList<String>();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.whereNear("location", new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if(e == null){
                    for(int i = 0; i < list.size(); i ++){
                        names.add(list.get(i).getUsername().toString());
                    }

                    usersListView = (ListView) findViewById(R.id.usersListView);
                    namesArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.user_list_item, names);
                    usersListView.setAdapter(namesArrayAdapter);

                    usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //open profile
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
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
        currentLocation = getLocation();
        ParseUser.getCurrentUser().put("location", new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
        ParseUser.getCurrentUser().saveInBackground();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        //update location and list
    }

    @Override
    protected void onStart(){
        super.onStart();

        locationClient.connect();
    }

    @Override
    protected  void onStop(){
        locationClient.disconnect();
        super.onStop();
    }

    private boolean servicesConnected(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private Location getLocation(){
        if(servicesConnected()){
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }
}
