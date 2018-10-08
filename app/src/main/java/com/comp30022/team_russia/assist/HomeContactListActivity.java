package com.comp30022.team_russia.assist;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private static final String TAG = "HomeContactListActivity";
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    AuthService authService;

    @Inject
    PubSubHub pubSubHub;

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

        // Whenever the user is logged out, or not logged in, show the
        // LoginActivity.
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
                navController.navigate(R.id.action_global_loginActivity);
                this.finish();
            }
        });

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
}
