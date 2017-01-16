package demo.location.bhavya.in.locationupdatedemoapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by bhavyav on 16/01/17.
 */

public abstract class LocationActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<LocationSettingsResult> {


    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final int REQUEST_ENABLE_GPS_SETTINGS = 0x2;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60*1000;//10 * 60 * 1000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 30*1000;//2 * 60 * 1000;


    private static final long DISTANCE_IN_METERS = 3;

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    private Boolean mRequestingLocationUpdates = true;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i("Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISTANCE_IN_METERS);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        mRequestingLocationUpdates = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("Connected to GoogleApiClient");
        boolean isLocationPermissionEnabled = true;
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext() , "Please turn on location permission" , Toast.LENGTH_LONG).show();
                isLocationPermissionEnabled = false;
            }else {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        }
        if(isLocationPermissionEnabled) {
            if (mCurrentLocation == null) {
                Log.d("location null");
                checkLocationSettings();
            } else {
                updateLocationChanged(mCurrentLocation);
            }

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.setAlwaysShow(true);
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i("All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i("PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(LocationActivity.this, "ask user to reinstall the app", Toast.LENGTH_SHORT).show();
                break;
            case LocationSettingsStatusCodes.CANCELED:
                Log.i("Location settings are CANCELED, and cannot be fixed here. Dialog " +
                        "not created.");
                alertUserToEnableLocation();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("User chose not to make required location settings changes.");
                        alertUserToEnableLocation();
                        break;
                }
                break;
            case REQUEST_ENABLE_GPS_SETTINGS:
                checkLocationSettings();
                break;
        }
    }

    AlertDialog permissionsDialog;

    public void alertUserToEnableLocation() {
        if (permissionsDialog == null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Need Geo Location Permission");
            dialog.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    showLocationSettings();
                }
            });
            dialog.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            permissionsDialog = dialog.create();
        }
        if (!permissionsDialog.isShowing()) {
            permissionsDialog.show();
        }

    }

    public void showLocationSettings() {
        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(myIntent, REQUEST_ENABLE_GPS_SETTINGS);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateLocationChanged(location);
    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        boolean isLocationPermissionEnabled = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext() , "Please turn on location permission" , Toast.LENGTH_LONG).show();
            isLocationPermissionEnabled = false;
        }
        if(isLocationPermissionEnabled) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public abstract void updateLocationChanged(Location location);
}
