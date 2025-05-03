package com.example.gpssender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ReceiverInfo receiverInfo;
    private EditText phoneNumber;
    private Button findButton;
    private TextView locationText;
    private GoogleMap mMap;
    private SmsReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = findViewById(R.id.phoneNumber_id);
        findButton = findViewById(R.id.find_phone_button);
        locationText = findViewById(R.id.location_text);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        findButton.setOnClickListener(v -> {
            String number = phoneNumber.getText().toString();
            receiverInfo = new ReceiverInfo(number, 0.0, 0.0);
            requestLocation();
            updateLocationText(receiverInfo.getLatitude(), receiverInfo.getLongitude());
        });

        registerSMSReceiver();
    }

    private void requestLocation() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(receiverInfo.getPhoneNumber(), null, "Hi!, good morning. :)", null, null);
    }

    private void updateLocationText(double latitude, double longitude) {
        String locationMessage = "Location - Latitude: " + latitude + "  Longitude: " + longitude;
        locationText.setText(locationMessage);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    // Register the SMS receiver
    private void registerSMSReceiver() {
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        smsReceiver = new SmsReceiver();
        registerReceiver(smsReceiver, new IntentFilter());
    }

    public class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdus.length];

                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                for (SmsMessage message : messages) {
                    String messageBody = message.getMessageBody();
                    String senderNumber = message.getOriginatingAddress();

                    String[] location = messageBody.split(",");
                    if (location.length == 2) {
                        double latitude = Double.parseDouble(location[0]);
                        double longitude = Double.parseDouble(location[1]);

                        receiverInfo.setLatitude(latitude);
                        receiverInfo.setLongitude(longitude);

                        runOnUiThread(() -> {
                           if (receiverInfo == null) {
                               receiverInfo = new ReceiverInfo(senderNumber, latitude, longitude);
                           }
                           else {
                               receiverInfo.setPhoneNumber(senderNumber);
                               receiverInfo.setLatitude(latitude);
                               receiverInfo.setLongitude(longitude);
                           }
                        });

                        /* TODO - Fix update location
                            {murag sa receiverinfo ni, dili maka pass sa field.}
                         */
                        updateLocationText(latitude, longitude);

                        LatLng receiverLocation = new LatLng(latitude, longitude);
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(receiverLocation).title("Receiver's Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(receiverLocation, 15));
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }
}
