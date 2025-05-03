package com.example.gpssender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Receiver extends BroadcastReceiver {
    private ReceiverInfo receiverInfo;
    private GoogleMap map;

    public Receiver() {

    }

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

                String[] location = messageBody.split(",");
                if (location.length == 2) {
                    double latitude = Double.parseDouble(location[0]);
                    double longitude = Double.parseDouble(location[1]);

                    receiverInfo.setLatitude(latitude);
                    receiverInfo.setLongitude(longitude);
                }

                LatLng receiverLocation = new LatLng(receiverInfo.getLatitude(), receiverInfo.getLongitude());
                map.clear();
                map.addMarker(new MarkerOptions().position(receiverLocation).title("Receiver's Location."));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(receiverLocation, 15));
            }
        }
    }
}
