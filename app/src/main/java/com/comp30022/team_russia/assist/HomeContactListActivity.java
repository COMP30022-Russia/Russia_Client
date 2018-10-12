package com.comp30022.team_russia.assist;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.jitsi.services.JitsiMeetHolder;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.FirebaseTokenData;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

import javax.inject.Inject;

/**
 * The primary (home) Activity.
 */
public class HomeContactListActivity extends AppCompatActivity
    implements HasSupportFragmentInjector, TitleChangable {

    private static final int ERROR_DIALOG_REQUEST = 1001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 1002;

    private static final int AUDIO_PERMISSION_REQUEST_CODE = 9292;

    private static final String AUDIO_RECORD_PERMISSION = Manifest.permission.RECORD_AUDIO;

    private static final String MODIFY_AUDIO_PERMISSION =
        Manifest.permission.MODIFY_AUDIO_SETTINGS;


    private static final String TAG = "HomeContactListActivity";
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    AuthService authService;

    @Inject
    PubSubHub pubSubHub;

    @Inject
    JitsiMeetHolder jitsiMeetHolder;

    private Toolbar toolbar;
    private Button emergencyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_activity_messaging);

        emergencyBtn = findViewById(R.id.emergencyButton);

        /* setup toolbar */
        toolbar = findViewById(R.id.customAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.default_fragment);

        NavController navController = host.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);

        // Whenever the user is logged out, or not logged in, show the LoginActivity.
        authService.isLoggedIn().observe(this, value -> {
            if (value != null && value) {
                // Show/hide button depending on user type
                if (authService.getCurrentUser().getUserType() == User.UserType.Carer) {
                    emergencyBtn.setVisibility(View.GONE);
                }

                // once logged in, update Firebase Token.
                FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnSuccessListener(this::updateFirebaseToken);

            } else if (value != null && !value) {
                // Not logged in, invoke LoginActivity and quit current activity
                jitsiMeetHolder.destroy();
                navController.navigate(R.id.action_global_loginActivity);
                this.finish();
            }
        });


        // Check voice permissions
        checkVoicePermissions();
    }

    @Override
    protected void onUserLeaveHint() {
        jitsiMeetHolder.onUserLeaveHint();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        jitsiMeetHolder.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        jitsiMeetHolder.onActivityStop(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public void updateTitle(String title) {
        toolbar.setTitle(title);
    }


    @Override
    protected void onStart() {
        super.onStart();

        checkGooglePlayServices();
        checkMapsEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        jitsiMeetHolder.onActivityResume(this);
        checkGooglePlayServices();
        checkMapsEnabled();
    }

    /**
     * Check if device has Google Play Services.
     */
    private void checkGooglePlayServices() {
        Log.d(TAG, "checkGooglePlayServices: checking google services version");

        int available = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "checkGooglePlayServices: Google Play Services is working");

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "checkGooglePlayServices: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this,
                available, ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if device has GPS enabled.
     */
    private void checkMapsEnabled() {
        final LocationManager manager =
            (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enable your gps.")
            .setCancelable(false)
            .setPositiveButton("Yes", (dialog, id) -> {
                Intent enableGpsIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
            });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void updateFirebaseToken(InstanceIdResult instanceIdResult) {
        pubSubHub.publish(PubSubTopics.FIREBASE_TOKEN,
            new FirebaseTokenData(
                instanceIdResult.getId(),
                instanceIdResult.getToken())
        );
    }


    /**
     * Check voice permissions.
     */
    private void checkVoicePermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED)
            || (ContextCompat.checkSelfPermission(this,
            Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED)) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                AUDIO_RECORD_PERMISSION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                MODIFY_AUDIO_PERMISSION)) {

                Log.d(TAG, "show request rationale");
                showRequestRationale();

            } else {
                requestVoicePermission();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: called.");

        switch (requestCode) {
        case AUDIO_PERMISSION_REQUEST_CODE: {
            if (grantResults.length > 0) {
                // check that all permissions are granted
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                        showDeniedDialog();
                        Log.d(TAG, "onRequestPermissionsResult: permission failed");
                        return;
                    }
                }
                Log.d(TAG, "onRequestPermissionsResult: permission granted");

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

    private void requestVoicePermission() {
        String[] permissions = {
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO
        };
        ActivityCompat.requestPermissions(this, permissions, AUDIO_PERMISSION_REQUEST_CODE);
    }

    private void showRequestRationale() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Audio access");
        alertDialog.setMessage("Please allow audio access");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
            (dialog, which) -> {
                dialog.dismiss();
                requestVoicePermission();
            });
        alertDialog.show();
    }

    private void showDeniedDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Denied Audio Access");
        alertDialog.setMessage("We want to know how you sound like. Please?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Review Audio Access",
            (dialog, which) -> {
                dialog.dismiss();
                requestVoicePermission();
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Ignore",
            (dialog, which) -> {
                dialog.dismiss();
                Toast.makeText(this, "GGWP", Toast.LENGTH_LONG).show();
            });
        alertDialog.show();
    }
}
