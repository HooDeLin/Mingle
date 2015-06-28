package com.orbital2015.mingle;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private Button signUpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsernameEditText = (EditText) findViewById(R.id.loginUsernameEditText);
        loginPasswordEditText = (EditText) findViewById(R.id.loginPasswordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);
        intent = new Intent(getApplicationContext(), NearbyActivity.class);
        serviceIntent = new Intent(getApplicationContext(), MessageService.class);

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
                        if(parseUser != null){
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

        signUpButton = (Button) findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_username = loginUsernameEditText.getText().toString();
                m_password = loginPasswordEditText.getText().toString();
                ParseUser user = new ParseUser();
                user.setUsername(m_username);
                user.setPassword(m_password);

                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getApplicationContext(),
                                    "Signing up success!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "There was an error signing in.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
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
