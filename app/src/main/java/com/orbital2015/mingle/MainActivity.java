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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private Button nearByButton;
    private Button settingsButton;
    private String currentUserID;
    private ArrayList<UserListItem> userListItems;
    private ListView usersListView;
    private ArrayAdapter<String> namesArrayAdapter;
    private Bitmap bitPicture;
    private TextView noPreviousChatTextView;
    ParseObject parseObject;
    private static ParseQuery<ParseUser> chatHistoryQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noPreviousChatTextView = (TextView) findViewById(R.id.noPreviousChatTextView);
        nearByButton = (Button) findViewById(R.id.nearbyButton);
        usersListView = (ListView) findViewById(R.id.usersListView);

        nearByButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NearbyActivity.class);
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
        try {
            ParseObject currentUserParseObject = ParseUser.getCurrentUser().fetchIfNeeded().getParseObject("profileCredentials");
            List<String> listString = currentUserParseObject.fetchIfNeeded().getList("chatHistory");
            ParseQuery<ParseObject> profileCredentialsQuery;
            List<ParseQuery<ParseObject>> queryList = new ArrayList<ParseQuery<ParseObject>>();
            for(int i = 0; i < listString.size(); i ++){
                ParseQuery<ParseObject> credentialsQuery = ParseQuery.getQuery("ProfileCredentials");
                credentialsQuery.whereEqualTo("objectId", listString.get(i));
                queryList.add(credentialsQuery);
            }
            userListItems = new ArrayList<UserListItem>();
            profileCredentialsQuery = ParseQuery.or(queryList);
            profileCredentialsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    try {
                        for (int i = 0; i < list.size(); i++) {
                            UserListItem currentUserListItem;
                            ParseObject currentProfile = list.get(i);
                            String currentUsername = currentProfile.getString("username");
                            String currentUsernameId = currentProfile.getObjectId();
                            ParseFile profilePicFile = currentProfile.getParseFile("profilePicture");
                            if (profilePicFile == null) {
                                bitPicture = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                            } else {
                                byte[] bytes = profilePicFile.getData();
                                bitPicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            }
                            currentUserListItem = new UserListItem(currentUsername, bitPicture, "", currentUsernameId);
                            userListItems.add(currentUserListItem);
                            UserListItemAdapter userListItemAdapter = new UserListItemAdapter(getApplicationContext(), userListItems);

                            usersListView.setAdapter(userListItemAdapter);

                            usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    openConversation(userListItems, position);
                                }
                            });
                        }
                    } catch (Exception ex) {
                    }
                }
            });
        } catch(Exception e){
            Log.e("error", e.toString());
        }
    }

    public void openConversation(ArrayList<UserListItem> userListItems, int pos) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", userListItems.get(pos).getMemberName());
        final String profileCredentialsId = userListItems.get(pos).getId();
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> user, ParseException e) {
                if (e == null) {
                    Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
                    intent.putExtra("RECIPIENT_ID", user.get(0).getObjectId());
                    intent.putExtra("NEARBY_PROFILE_CREDENTIALS_ID", profileCredentialsId);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onDestroy(){
        stopService(new Intent(this, MessageService.class));
        super.onDestroy();
    }
}
