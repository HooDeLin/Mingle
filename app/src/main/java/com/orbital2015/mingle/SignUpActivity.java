package com.orbital2015.mingle;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;


public class SignUpActivity extends ActionBarActivity {

    private EditText signUpUserNameEditText;
    private EditText signUpPasswordEditText;
    private EditText signUpConfirmPasswordEditText;
    private Button signUpButton;
    private TextView warningTextView;
    private TextView signUpLoggedInAsTextView;
    private TextView signUpLogOutTextView;
    private TextView signUpUserNameTextView;
    private TextView signUpPasswordTextView;
    private TextView signUpConfirmPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeUIElements();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = signUpUserNameEditText.getText().toString();
                String password = signUpPasswordEditText.getText().toString();
                String confirmPassword = signUpConfirmPasswordEditText.getText().toString();

                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("username", username);
                int numberOfResult = 0;
                try{
                    numberOfResult = userQuery.count();
                } catch (Exception e){}

                if(numberOfResult != 0) {
                    warningTextView.setText("Username has been taken. Please try again");
                    warningTextView.setVisibility(View.VISIBLE);
                } else if(!password.equals(confirmPassword)){
                    warningTextView.setText("Password incorrect, please reenter again.");
                    warningTextView.setVisibility(View.VISIBLE);
                } else {
                    warningTextView.setVisibility(View.GONE);
                    ParseUser user = new ParseUser();
                    user.setUsername(username);
                    user.setPassword(password);

                    user.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                newSignUpSettings();

                                Intent intent = new Intent(getApplicationContext(), NearbyActivity.class);
                                Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
                                startActivity(intent);
                                startService(serviceIntent);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "There was an error signing in.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null){
            setLoggedInLayout();
        }
    }

    private void initializeUIElements(){
        signUpUserNameEditText = (EditText) findViewById(R.id.signUpUserNameEditText);
        signUpPasswordEditText = (EditText) findViewById(R.id.signUpPasswordEditText);
        signUpConfirmPasswordEditText = (EditText) findViewById(R.id.signUpConfirmPasswordEditText);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        warningTextView = (TextView) findViewById(R.id.warningTextView);
        signUpLoggedInAsTextView = (TextView) findViewById(R.id.signUpLoggedInAsTextView);
        signUpLogOutTextView = (TextView) findViewById(R.id.signUpLogOutTextView);
        signUpUserNameTextView = (TextView) findViewById(R.id.signUpUsernameTextView);
        signUpPasswordTextView = (TextView) findViewById(R.id.signUpPasswordTextView);
        signUpConfirmPasswordTextView = (TextView) findViewById(R.id.signUpConfirmPasswordTextView);
    }

    private void setLoggedInLayout(){
        signUpUserNameEditText.setVisibility(View.GONE);
        signUpPasswordEditText.setVisibility(View.GONE);
        signUpConfirmPasswordEditText.setVisibility(View.GONE);
        signUpButton.setVisibility(View.GONE);
        warningTextView.setVisibility(View.GONE);
        signUpLoggedInAsTextView.setVisibility(View.VISIBLE);
        signUpLogOutTextView.setVisibility(View.VISIBLE);
        signUpUserNameTextView.setVisibility(View.GONE);
        signUpPasswordTextView.setVisibility(View.GONE);
        signUpConfirmPasswordEditText.setVisibility(View.GONE);

        signUpLoggedInAsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NearbyActivity.class);
                Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
                startActivity(intent);
                startService(serviceIntent);
            }
        });

        signUpLogOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createPreferences(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("radius", 1);
        editor.putInt("limit", 20);
        editor.commit();
    }

    private void initializeDatabase(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId().toString();
        ParseObject userLocation = new ParseObject("UserLocation");
        userLocation.put("userId", currentUserId);
        userLocation.put("userName", ParseUser.getCurrentUser().getUsername());
        userLocation.saveInBackground();

        ParseObject userProfileCredentials = new ParseObject("ProfileCredentials");
        userProfileCredentials.put("userName", ParseUser.getCurrentUser().getUsername());
        userProfileCredentials.put("userId", currentUserId);
        List<String> emptyList = new ArrayList<String>();
        userProfileCredentials.put("ChatHistory", emptyList);
        List<Integer> emptyNewMessageList = new ArrayList<Integer>();
        userProfileCredentials.put("newMessage", emptyNewMessageList);
        userProfileCredentials.saveInBackground();
    }

    private void newSignUpSettings(){
        createPreferences();
        initializeDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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
