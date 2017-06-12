package cis.gvsu.edu.geocalculator;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.location.Location;
import java.text.DecimalFormat;
import java.math.RoundingMode;
import android.view.inputmethod.InputMethodManager;
import com.google.firebase.database.ChildEventListener;
import org.joda.time.DateTime;
import org.parceler.Parcels;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import butterknife.OnClick;
import com.google.firebase.database.DataSnapshot;
import java.util.List;
import java.util.ArrayList;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import com.google.firebase.database.DatabaseError;

public class MainActivity extends AppCompatActivity  {

    public static int SETTINGS_RESULT = 1;
    public static int HISTORY_RESULT = 2;
    private final static int locations_lookup = 123;


    private String bearingUnits = "degrees";
    private String distanceUnits = "kilometers";
    DatabaseReference topRef;
    public static List<LocationLookup> allHistory;

    private EditText p1Lat = null;
    private EditText p1Lng = null;
    private EditText p2Lat = null;
    private EditText p2Lng = null;
    private TextView distance = null;
    private TextView bearing = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        allHistory = new ArrayList<LocationLookup>();

        Button clearButton = (Button)this.findViewById(R.id.clear);
        Button calcButton = (Button)this.findViewById(R.id.calc);
        Button  search =  (Button) this.findViewById(R.id.search);
//        Button settingsButton = (Button) this.findViewById(R.id.settings);

        p1Lat = (EditText) this.findViewById(R.id.p1Lat);
        p1Lng = (EditText) this.findViewById(R.id.p1Lng);
        p2Lat = (EditText) this.findViewById(R.id.p2Lat);
        p2Lng = (EditText) this.findViewById(R.id.p2Lng);
        distance = (TextView) this.findViewById(R.id.distance);
        bearing = (TextView) this.findViewById(R.id.bearing);

/*
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                p1Lat.getText().clear();
                p1Lng.getText().clear();
                p2Lat.getText().clear();
                p2Lng.getText().clear();
            }
        });


 */

        search.setOnClickListener(v -> {
            Intent look = new Intent(MainActivity.this, LookupActivity.class);
            startActivityForResult(look, 111);

        });




        clearButton.setOnClickListener(v -> {
            hideKeyboard();
            p1Lat.getText().clear();
            p1Lng.getText().clear();
            p2Lat.getText().clear();
            p2Lng.getText().clear();
            distance.setText("Distance:");
            bearing.setText("Bearing:");
        });

//        settingsButton.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, MySettingsActivity.class);
//            startActivityForResult(intent, SETTINGS_RESULT );
//        });

        calcButton.setOnClickListener(v -> {
            updateScreen();
        });
    }

    @Override
    public void onResume(){

        super.onResume();

        allHistory.clear();

        topRef = FirebaseDatabase.getInstance().getReference("history");

        topRef.addChildEventListener (chEvListener);

    }

    @Override

    public void onPause(){

        super.onPause();

        topRef.removeEventListener(chEvListener);

    }

    @OnClick(R.id.fab)
    public void addNewActivity() {
        Intent newActivity = new Intent(MainActivity.this, LookupActivity.class);
        startActivityForResult(newActivity, locations_lookup);
    }
    private void updateScreen()
    {
        try {
            Double lat1 = Double.parseDouble(p1Lat.getText().toString());
            Double lng1 = Double.parseDouble(p1Lng.getText().toString());
            Double lat2 = Double.parseDouble(p2Lat.getText().toString());
            Double lng2 = Double.parseDouble(p2Lng.getText().toString());



            Location p1 = new Location("");//provider name is unecessary
            p1.setLatitude(lat1);//your coords of course
            p1.setLongitude(lng1);

            Location p2 = new Location("");
            p2.setLatitude(lat2);
            p2.setLongitude(lng2);

            double b = p1.bearingTo(p2);
            double d = p1.distanceTo(p2) / 1000.0d;

            if (this.distanceUnits.equals("Miles")) {
                d *= 0.621371;
            }

            if (this.bearingUnits.equals("Mils")) {
                b *= 17.777777778;
            }

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);


            String dStr = "Distance: " + df.format(d) + " " + this.distanceUnits;
            distance.setText(dStr);

            String bStr = "Bearing: " + df.format(b) + " " + this.bearingUnits;
            bearing.setText(bStr);

            // remember the calculation.
            //HistoryContent.HistoryItem item = new HistoryContent.HistoryItem(lat1.toString(),
              //     lng1.toString(), lat2.toString(), lng2.toString(), DateTime.now());
           //HistoryContent.addItem(item);



            hideKeyboard();
        } catch (Exception e) {
            return;
        }

    }



    private void hideKeyboard()
    {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            //this.getSystemService(Context.INPUT_METHOD_SERVICE);
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SETTINGS_RESULT) {
            this.bearingUnits = data.getStringExtra("bearingUnits");
            this.distanceUnits = data.getStringExtra("distanceUnits");
            updateScreen();
        } else if (resultCode == HISTORY_RESULT) {
            String[] vals = data.getStringArrayExtra("item");
            this.p1Lat.setText(vals[0]);
            this.p1Lng.setText(vals[1]);
            this.p2Lat.setText(vals[2]);
            this.p2Lng.setText(vals[3]);
            this.updateScreen();  // code that updates the calcs.
        } else if (requestCode == 111) {
            if (data != null && data.hasExtra("Locations")) {
               Parcelable par = data.getParcelableExtra("Locations");
               LocationLookup l = Parcels.unwrap(par);
                this.updateScreen();
               topRef.push().setValue(l);
                LocationLookup entry = new LocationLookup();

                //entry.setOrigLat(lat1);

                //entry.setOrigLng(lng1);

                //entry.setEndLat(lat2);

                //entry.setEndLng(lng2);


                DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

                entry.setTimestamp(fmt.print(DateTime.now()));

                topRef.push().setValue(entry);
               // Snackbar.make(toolbar, "New Activity Added", Snackbar.LENGTH_SHORT).show();


            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, MySettingsActivity.class);
            startActivityForResult(intent, SETTINGS_RESULT );
            return true;
        } else if(item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivityForResult(intent, HISTORY_RESULT );
            return true;
        }
        return false;
    }
    private ChildEventListener chEvListener = new ChildEventListener() {

        @Override

        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            LocationLookup entry = (LocationLookup)

                    dataSnapshot.getValue(LocationLookup.class);

            entry._key = dataSnapshot.getKey();

            allHistory.add(entry);

        }

        @Override

        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override

        public void onChildRemoved(DataSnapshot dataSnapshot) {

            LocationLookup entry = (LocationLookup)

                    dataSnapshot.getValue(LocationLookup.class);

            List<LocationLookup> newHistory = new ArrayList<LocationLookup>();

            for (LocationLookup t : allHistory) {

                if (!t._key.equals(dataSnapshot.getKey())) {

                    newHistory.add(t);

                }

            }

            allHistory = newHistory;

        }
        @Override

        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override

        public void onCancelled(DatabaseError databaseError) {

        }

    };



}
