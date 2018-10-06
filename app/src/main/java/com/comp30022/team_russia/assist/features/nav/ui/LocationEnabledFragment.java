package com.comp30022.team_russia.assist.features.nav.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.comp30022.team_russia.assist.base.BaseFragment;

/**
 * Represents a fragment that requires location permissions.
 */
public abstract class LocationEnabledFragment extends BaseFragment {

    /* variables */
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 51234;

    private static String TAG = "LocationEnabledFragment";

    protected Boolean locationPermissionsGranted = false;

    /* methods */
    protected abstract void onLocationPermissionGranted();

    protected abstract void onLocationPermissionDenied();

    /**
     * Check location permission given by user.
     */
    protected void getLocationPermission() {
        Log.d("", "getLocationPermission: getting location permissions");

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity()
            .getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            /* all permissions are granted, initialise the map */
            locationPermissionsGranted = true;
            onLocationPermissionGranted();

        } else {
            // one of the location permission is not yet granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                COARSE_LOCATION)) {
                Log.d(TAG, "show request rationale");
                showRequestRationale();

            } else {
                requestLocationPermission();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: called.");
        locationPermissionsGranted = false;

        switch (requestCode) {
        case LOCATION_PERMISSION_REQUEST_CODE: {
            if (grantResults.length > 0) {
                // check that all permissions are granted
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                        showDeniedDialog();
                        locationPermissionsGranted = false;
                        Log.d(TAG, "onRequestPermissionsResult: permission failed");
                        return;
                    }
                }
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
                locationPermissionsGranted = true;

                //initialize our map
                onLocationPermissionGranted();
            } else {

                // permission denied
                showDeniedDialog();
            }
            break;
        }
        default:
            break;
        }
    }

    private void showRequestRationale() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Location access");
        alertDialog.setMessage("If you want help, please allow location access la");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
            (dialog, which) -> {
                dialog.dismiss();
                requestLocationPermission();
            });
        alertDialog.show();
    }

    private void showDeniedDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Denied Location Access");
        alertDialog.setMessage("We want to know where you live! Tell us!");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Review Location Access",
            (dialog, which) -> {
                dialog.dismiss();
                requestLocationPermission();
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Ignore",
            (dialog, which) -> {
                dialog.dismiss();
                onLocationPermissionDenied();
            });
        alertDialog.show();
    }

    private void requestLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

}
