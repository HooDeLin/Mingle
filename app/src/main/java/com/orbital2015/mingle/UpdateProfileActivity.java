package com.orbital2015.mingle;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class UpdateProfileActivity extends ActionBarActivity {

    private EditText nameEditText;
    private EditText nationalityEditText;
    private Button saveUpdateProfileButton;
    private Button setProfilePictureButton;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadio;
    private RadioButton femaleRadio;
    private RadioButton otherGenderRadio;
    private EditText description;
    private EditText dateOfBirthEditText;
    private ImageView profilePictureImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        nationalityEditText = (EditText) findViewById(R.id.nationalityEditText);
        genderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);
        description = (EditText) findViewById(R.id.descriptionEditText);
        saveUpdateProfileButton = (Button) findViewById(R.id.saveUpdateProfileButton);
        setProfilePictureButton = (Button) findViewById(R.id.setProfilePictureButton);
        dateOfBirthEditText = (EditText) findViewById(R.id.dateOfBirthEditText);
        profilePictureImageView = (ImageView) findViewById(R.id.profilePictureImageView);
        maleRadio = (RadioButton) findViewById(R.id.maleRadioButton);
        femaleRadio = (RadioButton) findViewById(R.id.femaleRadioButton);
        otherGenderRadio = (RadioButton) findViewById(R.id.otherGenderRadioButton);

        final String currentUserId = ParseUser.getCurrentUser().getObjectId();

        ParseQuery<ParseObject> profileCredentialsQuery = ParseQuery.getQuery("ProfileCredentials");
        profileCredentialsQuery.whereEqualTo("userId", currentUserId);
        profileCredentialsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    ParseObject queryRow = list.get(0);
                    nameEditText.setText(queryRow.getString("userName"));
                    nationalityEditText.setText(queryRow.getString("Nationality"));
                    description.setText(queryRow.getString("Description"));
                    dateOfBirthEditText.setText(queryRow.getString("dateOfBirth"));

                    String currentGender = queryRow.getString("Gender");
                    if (currentGender.equals("Male")) {
                        maleRadio.toggle();
                    } else if (currentGender.equals("Female")) {
                        femaleRadio.toggle();
                    } else if (currentGender.equals("Others")) {
                        otherGenderRadio.toggle();
                    }
                } else {
                    //something is wrong
                }
            }
        });

        saveUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery queryRow = ParseQuery.getQuery("ProfileCredentials");
                queryRow.getInBackground(currentUserId, new GetCallback() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            parseObject.put("userName", nameEditText.getText().toString());
                            parseObject.put("Nationality", nationalityEditText.getText().toString());
                            parseObject.put("Description", description.getText().toString());
                            parseObject.put("dateOfBirth", dateOfBirthEditText.getText().toString());

                            if(maleRadio.isChecked()){
                                parseObject.put("Gender", "Male");
                            } else if(femaleRadio.isChecked()){
                                parseObject.put("Gender", "Female");
                            } else if(otherGenderRadio.isChecked()){
                                parseObject.put("Gender", "Others");
                            }

                            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void done(Object o, Throwable throwable) {

                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_profile, menu);
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
