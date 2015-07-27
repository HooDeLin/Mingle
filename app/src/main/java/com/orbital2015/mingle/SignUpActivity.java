package com.orbital2015.mingle;

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
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;


public class SignUpActivity extends ActionBarActivity {

    private EditText signUpUserNameEditText;
    private EditText signUpPasswordEditText;
    private EditText signUpConfirmPasswordEditText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpUserNameEditText = (EditText) findViewById(R.id.signUpUserNameEditText);
        signUpPasswordEditText = (EditText) findViewById(R.id.signUpPasswordEditText);
        signUpConfirmPasswordEditText = (EditText) findViewById(R.id.signUpConfirmPasswordEditText);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = signUpUserNameEditText.getText().toString();
                String password = signUpPasswordEditText.getText().toString();
                String confirmPassword = signUpConfirmPasswordEditText.getText().toString();
                if(!password.equals(confirmPassword)){
                    Toast.makeText(getApplicationContext(),
                            "There was an error signing in.",
                            Toast.LENGTH_LONG).show();
                } else {
                    ParseUser user = new ParseUser();
                    user.setUsername(username);
                    user.setPassword(password);

                    user.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putInt("radius", 1);
                                editor.putInt("limit", 20);
                                editor.commit();
                                Toast.makeText(getApplicationContext(),
                                        "Signing up success!",
                                        Toast.LENGTH_LONG).show();

                                String currentUserId = ParseUser.getCurrentUser().getObjectId().toString();
                                ParseObject userLocation = new ParseObject("UserLocation");
                                userLocation.put("userId", currentUserId);
                                userLocation.put("userName", username);
                                userLocation.saveInBackground();

                                ParseObject userProfileCredentials = new ParseObject("ProfileCredentials");
                                userProfileCredentials.put("userName", username);
                                userProfileCredentials.put("userId", currentUserId);
                                List<String> emptyList = new ArrayList<String>();
                                userProfileCredentials.put("ChatHistory", emptyList);
                                List<Integer> emptyNewMessageList = new ArrayList<Integer>();
                                userProfileCredentials.put("NewMessage", emptyNewMessageList);
                                userProfileCredentials.saveInBackground();

                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
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
