package com.example.gpssender;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private EditText phoneNumberEditText;
    private Button sendSmsButton;
    private MapView map;
    private final int PERMISSION_REQUEST_CODE = 1;
    // Cebu City coordinates
    private static final double CEBU_LATITUDE = 10.311566798090368;
    private static final double CEBU_LONGITUDE = 123.91778041403927;

    // Broadcast receiver for location updates
    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("LOCATION_UPDATE")) {
                double latitude = intent.getDoubleExtra("latitude", 0);
                double longitude = intent.getDoubleExtra("longitude", 0);
                Log.d(TAG, "Received broadcast with location: " + latitude + ", " + longitude);
                if (latitude != 0 && longitude != 0) {
                    updateMapWithLocation(latitude, longitude);
                }
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_main);

        // Initialize views
        phoneNumberEditText = findViewById(R.id.phoneNumber);
        sendSmsButton = findViewById(R.id.findButton);
        map = findViewById(R.id.map);

        // Configure map
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Set initial location to Cebu
        GeoPoint cebuLocation = new GeoPoint(CEBU_LATITUDE, CEBU_LONGITUDE);
        map.getController().setCenter(cebuLocation);
        map.getController().setZoom(15.0);

        // Add a marker at Cebu
        Marker cebuMarker = new Marker(map);
        cebuMarker.setPosition(cebuLocation);
        cebuMarker.setTitle("Cebu City");
        map.getOverlays().add(cebuMarker);

        // Force a redraw
        map.invalidate();

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter("LOCATION_UPDATE");
        registerReceiver(locationUpdateReceiver, filter);
        Log.d(TAG, "Broadcast receiver registered");

        // Request permissions
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);

        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS();
            }
        });
        // Handle location data from intent
        handleLocationIntent(getIntent());
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(locationUpdateReceiver);
        Log.d(TAG, "Broadcast receiver unregistered");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called");
        setIntent(intent); // Important: Update the stored intent
        handleLocationIntent(intent);
    }

    private void handleLocationIntent(Intent intent) {
        if (intent != null) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            Log.d(TAG, "handleLocationIntent called with coordinates: " + latitude + ", " + longitude);
            if (latitude != 0 && longitude != 0) {
                Toast.makeText(this, "Updating map with location: " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                updateMapWithLocation(latitude, longitude);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        map.onResume();
        // Check for location data in the current intent
        handleLocationIntent(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        map.onPause();
    }

    private void sendSMS() {
        String phone = phoneNumberEditText.getText().toString();
        String msg = "Asa naka?";
        Log.d(TAG, "Sending SMS to: " + phone + " with message: " + msg);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "SMS permission denied");
            Toast.makeText(this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current location before sending SMS
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Check if GPS is enabled
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "GPS enabled: " + isGPSEnabled);

            // Check if Network is enabled
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "Network enabled: " + isNetworkEnabled);

            Location location = null;
            if (isGPSEnabled) {
                Log.d(TAG, "Trying to get location from GPS");
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (location == null && isNetworkEnabled) {
                Log.d(TAG, "Trying to get location from Network");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                Log.d(TAG, "Current location when sending SMS: " + lat + ", " + lon);
            } else {
                Log.e(TAG, "No location available when sending SMS");
            }
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone, null, msg, null, null);
        Toast.makeText(this, "SMS Sent", Toast.LENGTH_SHORT).show();
    }

    // Method to update map with received location
    public void updateMapWithLocation(double latitude, double longitude) {
        Log.d(TAG, "updateMapWithLocation called with: " + latitude + ", " + longitude);
        if (map != null) {
            try {
                // Clear existing overlays
                map.getOverlays().clear();

                // Create new location point
                GeoPoint location = new GeoPoint(latitude, longitude);

                // Create and add marker
                Marker marker = new Marker(map);
                marker.setPosition(location);
                marker.setTitle("Received Location");
                map.getOverlays().add(marker);

                // Center map on location
                map.getController().setCenter(location);
                map.getController().setZoom(15.0);

                // Force a redraw
                map.invalidate();

                Log.d(TAG, "Map updated successfully with location: " + latitude + ", " + longitude);
                Toast.makeText(this, "Map updated with location: " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Error updating map: " + e.getMessage());
                Toast.makeText(this, "Error updating map: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "Map is null");
            Toast.makeText(this, "Map not initialized yet", Toast.LENGTH_SHORT).show();
        }
    }
}