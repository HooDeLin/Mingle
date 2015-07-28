package com.orbital2015.mingle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LoginActivity extends ActionBarActivity {

    private EditText loginUsernameEditText;
    private EditText loginPasswordEditText;
    private Button loginButton;
    private String m_username;
    private String m_password;
    private Intent intent;
    private Intent serviceIntent;
    private TextView signUpLink;
    private Dialog progressDialog;
    private Button facebookLoginButton;
    private TextView loginUsernameTextView;
    private TextView loginPasswordTextView;
    private TextView loggedInAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

        class RegisterGcmTask extends AsyncTask<Void, Void, String> {
            String msg = "";

            @Override
            protected String doInBackground(Void... voids){
                try {
                    msg = gcm.register("your-profile-id");
                } catch (IOException ex) {
                    msg = "Error: " + ex.getMessage();
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                intent = new Intent(getApplicationContext(), NearbyActivity.class);
                serviceIntent = new Intent(getApplicationContext(), MessageService.class);

                serviceIntent.putExtra("regId", msg);

                startActivity(intent);
                startService(serviceIntent);
            }
        }

        setContentView(R.layout.activity_login);

        initializeUIElement();

        ParseUser currentUser = ParseUser.getCurrentUser();

        if(currentUser != null){
            (new RegisterGcmTask()).execute();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_username = loginUsernameEditText.getText().toString();
                m_password = loginPasswordEditText.getText().toString();
                ParseUser.logInInBackground(m_username, m_password, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (parseUser != null) {
                            createPreferences();
                            (new RegisterGcmTask()).execute();
                            Toast.makeText(getApplicationContext(),
                                    "Logging in success!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "There was an error logging in.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        ParseUser currentUser = ParseUser.getCurrentUser();
        final Context context = getApplicationContext();
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        initializeUIElement();
        if(!gps_enabled && !network_enabled) {
            loginUsernameTextView.setVisibility(View.GONE);
            loginPasswordTextView.setVisibility(View.GONE);
            loginPasswordEditText.setVisibility(View.GONE);
            loginUsernameEditText.setVisibility(View.GONE);
            signUpLink.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            facebookLoginButton.setVisibility(View.GONE);
            loggedInAs.setText("Please enable location service and internet connection before proceed");
        } else if (currentUser != null) {
            loginUsernameTextView.setVisibility(View.GONE);
            loginPasswordTextView.setVisibility(View.GONE);
            loginPasswordEditText.setVisibility(View.GONE);
            loginUsernameEditText.setVisibility(View.GONE);
            signUpLink.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);

            String loggedInAsString = "Logged in as " + currentUser.getUsername() + ". Sign out? ";
            loggedInAs.setText(loggedInAsString);
            loggedInAs.setVisibility(View.VISIBLE);
            loggedInAs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser.logOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            });

            TextView backToMain = (TextView) findViewById(R.id.backToMainPage);
            backToMain.setVisibility(View.VISIBLE);
            backToMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), NearbyActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            loginUsernameTextView.setVisibility(View.VISIBLE);
            loginPasswordTextView.setVisibility(View.VISIBLE);
            loginPasswordEditText.setVisibility(View.VISIBLE);
            loginUsernameEditText.setVisibility(View.VISIBLE);
            signUpLink.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            facebookLoginButton.setVisibility(View.VISIBLE);
            loggedInAs.setVisibility(View.GONE);
        }
    }

    public void onFacebookLoginClick(View v) {
        progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...", true);

        List<String> permissions = Arrays.asList("public_profile", "email");

        try {
            ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException err) {
                    progressDialog.dismiss();
                    if (user == null) {
                    } else if (user.isNew()) {
                        newSignUpSettings();
                        Intent intent = new Intent(getApplicationContext(), NearbyActivity.class);
                        startActivity(intent);
                        startService(serviceIntent);
                    } else {
                        updateDatabase();
                        createPreferences();
                        Intent intent = new Intent(getApplicationContext(), NearbyActivity.class);
                        startActivity(intent);
                        startService(serviceIntent);
                    }
                }
            });
        } catch(Exception e){
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),
                   "Something went wrong, please try again later",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void initializeUIElement(){
        loginUsernameEditText = (EditText) findViewById(R.id.loginUsernameEditText);
        loginPasswordEditText = (EditText) findViewById(R.id.loginPasswordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);
        intent = new Intent(getApplicationContext(), NearbyActivity.class);
        serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        signUpLink = (TextView) findViewById(R.id.signUpLink);
        facebookLoginButton = (Button) findViewById(R.id.facebookLoginButton);
        loginUsernameTextView = (TextView) findViewById(R.id.loginUsernameText);
        loginPasswordTextView = (TextView) findViewById(R.id.loginPasswordText);
        loggedInAs = (TextView) findViewById(R.id.loggedInTextView);
    }

    private void createPreferences(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("radius", 1);
        editor.putInt("limit", 20);
        editor.commit();
    }

    private void initializeDatabase(){
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                String currentUserId = ParseUser.getCurrentUser().getObjectId().toString();
                ParseUser currentUser = ParseUser.getCurrentUser();
                ParseObject userLocation = new ParseObject("UserLocation");
                ParseObject userProfileCredentials = new ParseObject("ProfileCredentials");
                userLocation.put("userId", currentUserId);
                userProfileCredentials.put("userId", currentUserId);
                List<String> emptyList = new ArrayList<String>();
                userProfileCredentials.put("ChatHistory", emptyList);
                List<Integer> emptyNewMessageList = new ArrayList<Integer>();
                userProfileCredentials.put("newMessage", emptyNewMessageList);

                if(jsonObject != null){
                    try {
                        userLocation.put("userName", jsonObject.getString("name"));
                        userProfileCredentials.put("userName", jsonObject.getString("name"));
                        currentUser.setUsername(jsonObject.getString("name"));
                        currentUser.save();
                        userProfileCredentials.save();
                        userLocation.save();
                    } catch (Exception e) {
                        Log.e("Saving info", "failed");
                    }
                }
            }
        });
        request.executeAsync();
    }

    private void newSignUpSettings(){
        createPreferences();
        initializeDatabase();
    }

    private void updateDatabase(){
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                try {
                    String currentUserId = ParseUser.getCurrentUser().getObjectId().toString();

                    ParseUser currentUser = ParseUser.getCurrentUser();
                    ParseQuery<ParseObject> profileCredentialsQuery = ParseQuery.getQuery("ProfileCredentials");
                    ParseQuery<ParseObject> userLocationQuery = ParseQuery.getQuery("UserLocation");
                    profileCredentialsQuery.whereEqualTo("userId", currentUserId);
                    userLocationQuery.whereEqualTo("userId", currentUserId);
                    ParseObject userProfileCredentials = profileCredentialsQuery.getFirst();
                    ParseObject userLocation = userLocationQuery.getFirst();

                    if(jsonObject != null) {
                        if(!ParseUser.getCurrentUser().getUsername().equals(jsonObject.getString("name"))){
                            userLocation.put("userName", jsonObject.getString("name"));
                            userProfileCredentials.put("userName", jsonObject.getString("name"));
                            currentUser.setUsername(jsonObject.getString("name"));

                            userProfileCredentials.save();
                            userLocation.save();
                            currentUser.save();
                            Log.e("Update", "Update");
                        }
                    }
                } catch(Exception e){
                    Log.e("Update info", "failed");
                }
            }
        });
        request.executeAsync();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
