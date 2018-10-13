package com.comp30022.team_russia.assist.features.nav.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Location Service.
 */
public class LocationService extends Service {

    private Location currentLocation;

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient fusedLocationClient;
    private static final long UPDATE_INTERVAL = 2 * 1000;  /* 2 secs */
    private static final long FASTEST_INTERVAL = 1000; /* 1 sec */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //notification for api 26 and above
        if (Build.VERSION.SDK_INT >= 26) {
            String channelId = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(channelId,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("")
                .setContentText("").build();

            startForeground(1, notification); // TODO WHY IS THIS 1?
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY; // start_not_sticky only runs while getLocation is running
    }

    private Location getLocation() {

        //todo
        /*
        if (//user is signed out) {
            stopSelf();
        }
        */

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest locationRequestHighAccuracy = new LocationRequest();
        locationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        locationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return null;
        }
        Log.d(TAG, "getLocation: getting location information.");
        fusedLocationClient.requestLocationUpdates(locationRequestHighAccuracy,
            new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Log.d(TAG, "onLocationResult: got location result.");

                    Location location = locationResult.getLastLocation();

                    if (location != null) {
                        currentLocation = location;
                    }
                }
            },
            // Looper.myLooper tells this to repeat forever until thread is destroyed
            Looper.myLooper());

        return currentLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
}