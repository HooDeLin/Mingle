package com.orbital2015.mingle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class LoginActivity extends ActionBarActivity {

    private EditText loginUsernameEditText;
    private EditText loginPasswordEditText;
    private Button loginButton;
    private String m_username;
    private String m_password;
    private Intent intent;
    private Intent serviceIntent;
    private TextView signUpLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsernameEditText = (EditText) findViewById(R.id.loginUsernameEditText);
        loginPasswordEditText = (EditText) findViewById(R.id.loginPasswordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);
        intent = new Intent(getApplicationContext(), NearbyActivity.class);
        serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        signUpLink = (TextView) findViewById(R.id.signUpLink);

        ParseUser currentUser = ParseUser.getCurrentUser();

        if(currentUser != null){
            startService(serviceIntent);
            startActivity(intent);
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
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putInt("radius", 1);
                            editor.putInt("limit", 20);
                            editor.commit();
                            startService(serviceIntent);
                            startActivity(intent);
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
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null){
            TextView loginUsernameTextView = (TextView) findViewById(R.id.loginUsernameText);
            TextView loginPasswordTextView = (TextView) findViewById(R.id.loginPasswordText);

            loginUsernameTextView.setVisibility(View.GONE);
            loginPasswordTextView.setVisibility(View.GONE);

            loginUsernameEditText = (EditText) findViewById(R.id.loginUsernameEditText);
            loginPasswordEditText = (EditText) findViewById(R.id.loginPasswordEditText);

            loginPasswordEditText.setVisibility(View.GONE);
            loginUsernameEditText.setVisibility(View.GONE);

            loginButton = (Button) findViewById(R.id.loginButton);

            loginButton.setVisibility(View.GONE);

            TextView loggedInAs = (TextView) findViewById(R.id.loggedInTextView);
            loggedInAs.setVisibility(View.VISIBLE);
            loggedInAs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser.logOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
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
