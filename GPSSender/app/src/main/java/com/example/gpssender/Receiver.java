package com.example.gpssender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String message = sms.getMessageBody();
                    Log.d("SmsReceiver", "Received SMS: " + message);

                    // Check if message starts with expected prefix
                    if (message.startsWith("[RECEIVER] Location")) {
                        try {
                            // Example: "[RECEIVER] Location - Latitude: 10.29736, Longitude: 123.89664"
                            String latStr = message.substring(message.indexOf("Latitude:") + 9, message.indexOf(",")).trim();
                            String lonStr = message.substring(message.indexOf("Longitude:") + 10).trim();

                            double latitude = Double.parseDouble(latStr);
                            double longitude = Double.parseDouble(lonStr);

                            // Send broadcast to update map
                            Intent locationIntent = new Intent("LOCATION_UPDATE");
                            locationIntent.putExtra("latitude", latitude);
                            locationIntent.putExtra("longitude", longitude);
                            context.sendBroadcast(locationIntent);

                            Log.d("SmsReceiver", "Broadcast sent with lat: " + latitude + ", lon: " + longitude);
                        } catch (Exception e) {
                            Log.e("SmsReceiver", "Failed to parse location from SMS", e);
                        }
                    }
                }
            }
        }
    }
}


