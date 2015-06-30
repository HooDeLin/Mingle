package com.orbital2015.mingle;

import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;


public class EditSearchActivity extends ActionBarActivity {

    private RadioGroup searchDistanceRadioGroup;
    private RadioGroup numberOfResultRadioGroup;
    private ArrayList<Integer> availableSearchDistance;
    private ArrayList<Integer> availableLimit;
    private Button editSearchSettingsConfirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_search);

        final SharedPreferences yourSettings = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        availableSearchDistance = new ArrayList<Integer>();
        availableSearchDistance.add(1);
        availableSearchDistance.add(2);
        availableSearchDistance.add(5);
        availableSearchDistance.add(10);

        availableLimit = new ArrayList<Integer>();
        availableLimit.add(10);
        availableLimit.add(20);
        availableLimit.add(50);

        int currentRadius = yourSettings.getInt("radius", 0);
        int currentLimit = yourSettings.getInt("limit", 0);

        searchDistanceRadioGroup = (RadioGroup) findViewById(R.id.search_distance_radio_group);

        for(int i = 0; i < availableSearchDistance.size(); i ++){
            int thisRadius = availableSearchDistance.get(i);

            RadioButton button = new RadioButton(this);
            button.setId(i);
            button.setText(Integer.toString(thisRadius));

            searchDistanceRadioGroup.addView(button, i);

            if (currentRadius == thisRadius) {
                searchDistanceRadioGroup.check(i);
            }
        }

        numberOfResultRadioGroup = (RadioGroup) findViewById(R.id.number_of_result_radio_group);

        for(int i = 0; i < availableLimit.size(); i ++){
            int thisLimit = availableLimit.get(i);

            RadioButton button = new RadioButton(this);
            button.setId(i);
            button.setText(Integer.toString(thisLimit));

            numberOfResultRadioGroup.addView(button, i);

            if (currentLimit == thisLimit) {
                numberOfResultRadioGroup.check(i);
            }
        }

        editSearchSettingsConfirmButton = (Button) findViewById(R.id.editSearchConfirmButton);

        editSearchSettingsConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = yourSettings.edit();

                int searchSelectId = searchDistanceRadioGroup.getCheckedRadioButtonId();
                editor.putInt("radius", availableSearchDistance.get(searchSelectId));

                int limitSelectId = numberOfResultRadioGroup.getCheckedRadioButtonId();
                editor.putInt("radius", availableLimit.get(limitSelectId));

                editor.commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_search, menu);
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
