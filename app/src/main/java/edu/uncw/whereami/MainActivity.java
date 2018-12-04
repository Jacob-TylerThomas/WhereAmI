package edu.uncw.whereami;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;

import io.objectbox.Box;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=001;
    private FusedLocationProviderClient mFusedLocationClient;
    TextView latText;
    TextView lonText;
    TextView accuracyText;;
    private Box<LocationRecording> locationBox;
    private LocationRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latText = findViewById(R.id.latitude);
        lonText = findViewById(R.id.longitude);
        accuracyText = findViewById(R.id.acc);
        locationBox = ((App) getApplication()).getBoxStore().boxFor(LocationRecording.class);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationRecordAdapter(locationBox);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        latText.setText(String.format("%.7f", location.getLatitude()));
                        lonText.setText(String.format("%.7f", location.getLongitude()));
                        accuracyText.setText(String.format("%.2f", location.getAccuracy()));

                        locationBox.put(new LocationRecording(new Date(location.getTime()), location.getLatitude(), location.getLongitude(), location.getAccuracy()));
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };

        if (savedInstanceState != null) {
            latText.setText(savedInstanceState.getCharSequence(LATITUDE_KEY));
            lonText.setText(savedInstanceState.getCharSequence(LONGITUDE_KEY));
            accuracyText.setText(savedInstanceState.getCharSequence(ACCURACY_KEY));
        }

    }

    public void recordClick(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }

        else {
            //Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
    private static int REQUEST_CHECK_SETTINGS = 123;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private static String LATITUDE_KEY = "latitude";
    private static String LONGITUDE_KEY = "longitude";
    private static String ACCURACY_KEY = "accuracy";

    /**
     * This method is called to kick off the chain of events that requests continuous location updates.
     */
    protected void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            // Regardless of whether the user fixed the issue or not, try again.
            // This could be really annoying for the user...
            // In reality, you should check the resultCode for success. If not successful, you
            // should degrade the functionality.
            createLocationRequest();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        createLocationRequest();
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "I need permission to access location in order to record locations.", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence(LATITUDE_KEY, latText.getText());
        outState.putCharSequence(LONGITUDE_KEY, lonText.getText());
        outState.putCharSequence(ACCURACY_KEY, accuracyText.getText());
        super.onSaveInstanceState(outState);
    }
}
