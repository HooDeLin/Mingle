package com.orbital2015.mingle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private byte[] image;


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

                    ParseFile profilePicture = queryRow.getParseFile("profilePicture");
                    if(profilePicture != null){
                        profilePicture.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {
                                Bitmap bitPicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitPicture.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                                profilePictureImageView.setImageBitmap(bitPicture);
                            }
                        });
                    }

                    String currentGender = queryRow.getString("Gender");
                    if (currentGender != null) {
                        if (currentGender.equals("Male")) {
                            maleRadio.toggle();
                        } else if (currentGender.equals("Female")) {
                            femaleRadio.toggle();
                        } else if (currentGender.equals("Others")) {
                            otherGenderRadio.toggle();
                        }
                    }

                } else {
                    //something is wrong
                }
            }
        });

        saveUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ProfileCredentials");
                query.whereEqualTo("userId", currentUserId);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            parseObject.put("userName", nameEditText.getText().toString());
                            parseObject.put("Nationality", nationalityEditText.getText().toString());
                            parseObject.put("Description", description.getText().toString());
                            parseObject.put("dateOfBirth", dateOfBirthEditText.getText().toString());

                            if(image != null){
                                ParseFile imageFile = new ParseFile("profilePicture.png", image);
                                parseObject.put("profilePicture", imageFile);
                            }

                            if (maleRadio.isChecked()) {
                                parseObject.put("Gender", "Male");
                            } else if (femaleRadio.isChecked()) {
                                parseObject.put("Gender", "Female");
                            } else if (otherGenderRadio.isChecked()) {
                                parseObject.put("Gender", "Others");
                            }

                            parseObject.saveInBackground();

                            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Some error just occur",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        setProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    image = bytes.toByteArray();
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profilePictureImageView.setImageBitmap(thumbnail);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
                profilePictureImageView.setImageBitmap(bm);
                ByteArrayOutputStream galleryBytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, galleryBytes);
                image = galleryBytes.toByteArray();
            }
        }
    }

    @Override
    protected void onDestroy(){
        stopService(new Intent(getApplicationContext(), MessageService.class));
        super.onDestroy();
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
