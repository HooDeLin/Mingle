package com.orbital2015.mingle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;


public class ViewProfileActivity extends ActionBarActivity {

    private String nearbyId;
    private TextView targetNameTextView;
    private TextView targetDateOfBirthTextView;
    private TextView targetNationalityTextView;
    private TextView targetDescriptionTextView;
    private TextView targetGenderTextView;
    private ImageView viewProfilePictureImageView;
    private Button viewProfileChatButton;
    private String currentProfileId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        nearbyId = extras.getString("NEARBY_ID");
        currentProfileId = extras.getString("NEARBY_PROFILE_CREDENTIALS_ID");
        Log.e("currentProfileId", currentProfileId);
        Log.e("nearbyId", nearbyId);

        targetDateOfBirthTextView = (TextView) findViewById(R.id.targetDateOfBirthTextView);
        targetDescriptionTextView = (TextView) findViewById(R.id.targetDescriptionTextView);
        targetGenderTextView = (TextView) findViewById(R.id.targetGenderTextView);
        targetNameTextView = (TextView) findViewById(R.id.targetNameTextView);
        targetNationalityTextView = (TextView) findViewById(R.id.targetNationalityTextView);
        viewProfilePictureImageView = (ImageView) findViewById(R.id.viewProfilePictureImageView);
        viewProfileChatButton = (Button) findViewById(R.id.viewProfileChatButton);

        ParseQuery<ParseUser> nearbyUserQuery = ParseUser.getQuery();
        nearbyUserQuery.whereEqualTo("objectId", nearbyId);
        try {
            ParseUser nearbyUser = nearbyUserQuery.getFirst();
            ParseObject nearbyUserProfile = nearbyUser.fetchIfNeeded().getParseObject("profileCredentials");
            String username = nearbyUserProfile.fetchIfNeeded().getString("username");
            String nationality = nearbyUserProfile.fetchIfNeeded().getString("nationality");
            String gender = nearbyUserProfile.fetchIfNeeded().getString("gender");
            String dateOfBirth = nearbyUserProfile.fetchIfNeeded().getString("dateOfBirth");
            String description = nearbyUserProfile.fetchIfNeeded().getString("description");
            if (username != null) {
                targetNameTextView.setText(username);
            }

            if (nationality != null) {
                targetNationalityTextView.setText(nationality);
            }

            if (gender != null) {
                targetGenderTextView.setText(gender);
            }

            if (dateOfBirth != null) {
                targetDateOfBirthTextView.setText(dateOfBirth);
            }

            if (description != null) {
                targetDescriptionTextView.setText(description);
            }

            ParseFile profilePicture = nearbyUserProfile.fetchIfNeeded().getParseFile("profilePicture");

            if(profilePicture != null){
                profilePicture.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        Bitmap bitPicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitPicture.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        viewProfilePictureImageView.setImageBitmap(bitPicture);
                    }
                });
            }
        } catch(Exception e){
            Toast.makeText(getApplicationContext(),
                    "There was an error finding user.",
                    Toast.LENGTH_LONG).show();
        }

        viewProfileChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
                intent.putExtra("RECIPIENT_ID", nearbyId);
                intent.putExtra("NEARBY_PROFILE_CREDENTIALS_ID", currentProfileId);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy(){
        stopService(new Intent(getApplicationContext(), MessageService.class));
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_profile, menu);
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
