package cis.gvsu.edu.geocalculator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.parceler.Parcels;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class LookupActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    int PLACE_AUTOCOMPLETE_REQUEST_CODE1 = 1 , PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 2;
    private static final String TAG = "LookupActivity";

    @BindView(R.id.Location1) EditText Location1;
    @BindView(R.id.Location2)  EditText Location2;
    @BindView(R.id.lookup_date) TextView Lookupdateview;

    private DateTime look_date;
    private DatePickerDialog dpDialog;
    private LocationLookup loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookup);
        ButterKnife.bind(this);

        loc = new LocationLookup();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DateTime today = DateTime.now();
        dpDialog = DatePickerDialog.newInstance(this,
                today.getYear(), today.getMonthOfYear() - 1, today.getDayOfMonth());


        Lookupdateview.setText(formatted(today));

        look_date = today;

    }

    //@OnClick({R.id.Location1, R.id.Location2})
    @OnClick(R.id.Location1)

    public void location1Pressed() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE1);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.Location2)

    public void location2Pressed() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE2);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    @OnClick(R.id.lookup_date)
    public void datePressed() {
        dpDialog.show(getFragmentManager(), "daterangedialog");
    }

    @OnClick(R.id.fab)
    public void FABPressed() {
        Intent result = new Intent();



        // add more code to initialize the rest of the fields
        Parcelable parcel = Parcels.wrap(loc);
        result.putExtra("Locations", parcel);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE1) {
            if (resultCode == RESULT_OK) {
                Place pl = PlaceAutocomplete.getPlace(this, data);
                Location1.setText(pl.getName());
                loc.origLat = pl.getLatLng().latitude;
                loc.origLng = pl.getLatLng().longitude;

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status stat = PlaceAutocomplete.getStatus(this, data);
                Log.d(TAG, "onActivityResult: ");
            }

        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == RESULT_OK) {
                Place pl = PlaceAutocomplete.getPlace(this, data);
                Location2.setText(pl.getName());
                loc.endLat = pl.getLatLng().latitude;
                loc.endLng = pl.getLatLng().longitude;

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status stat = PlaceAutocomplete.getStatus(this, data);
                Log.d(TAG, "onActivityResult: ");
            }
        } else if (requestCode == RESULT_CANCELED){
            System.out.println("Cancelled by the user");
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }


    private String formatted(DateTime d) {
        return d.monthOfYear().getAsShortText(Locale.getDefault()) + " " +
                d.getDayOfMonth() + ", " + d.getYear();
    }



    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        look_date = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);

        Lookupdateview.setText(formatted(look_date));

    }


}