package com.example.petfinder.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "GeofencingEvent error: " + geofencingEvent.getErrorCode());
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for(Geofence geofence: geofenceList){
            Log.d(TAG, "onReceive: "+ geofence.getRequestId());
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Handle geofence transition
        switch (geofenceTransition){
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "Pet is inside the geofence", Toast.LENGTH_SHORT).show();
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "Pet exits the geofence", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
