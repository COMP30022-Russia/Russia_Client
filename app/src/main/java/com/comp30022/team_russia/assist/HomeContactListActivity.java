package com.comp30022.team_russia.assist;

import static com.comp30022.team_russia.assist.base.BaseViewModel.combineLatest;

import android.Manifest;
import android.app.Dialog;
import android.arch.lifecycle.MutableLiveData;
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

import com.comp30022.team_russia.assist.base.BannerToggleable;
import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.features.emergency.services.EmergencyAlertService;
import com.comp30022.team_russia.assist.features.jitsi.services.JitsiMeetHolder;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.media.services.MediaManager;
import com.comp30022.team_russia.assist.features.nav.services.NavigationService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileImageManager;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.FirebaseTokenData;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;
import com.comp30022.team_russia.assist.features.push.sys.SocketService;

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
    implements HasSupportFragmentInjector, TitleChangable, BannerToggleable {

    private static final int ERROR_DIALOG_REQUEST = 1001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 1002;

    private static final int AUDIO_LOCATION_AND_CAMERA_PERMISSION_REQUEST_CODE = 9292;

    private static final String AUDIO_RECORD_PERMISSION =
        Manifest.permission.RECORD_AUDIO;

    private static final String MODIFY_AUDIO_PERMISSION =
        Manifest.permission.MODIFY_AUDIO_SETTINGS;

    private static final String FINE_LOCATION =
        Manifest.permission.ACCESS_FINE_LOCATION;

    private static final String COARSE_LOCATION =
        Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final String CAMERA_PERMISSION =
        Manifest.permission.CAMERA;


    private MutableLiveData<Boolean> inNavScreen = new MutableLiveData<>();

    private int navSessionId;


    private static final String TAG = "HomeContactListActivity";
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    AuthService authService;

    @Inject
    NavigationService navigationService;

    @Inject
    PubSubHub pubSubHub;

    @Inject
    EmergencyAlertService emergencyAlertService;

    @Inject
    JitsiMeetHolder jitsiMeetHolder;

    @Inject
    MediaManager mediaManager;

    @Inject
    ProfileImageManager profileImageManager;

    @Inject
    SocketService socketService;

    private Toolbar toolbar;
    private Button emergencyBtn;
    private Button ongoingNavButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_activity_messaging);


        ongoingNavButton = findViewById(R.id.ongoingNavButton);
        emergencyBtn = findViewById(R.id.emergencyButton);

        emergencyBtn.setOnClickListener(v ->
            emergencyAlertService.sendEmergency().thenAccept(result -> {
                if (result.isSuccessful()) {
                    Log.e(TAG, "sendEmergency: successfully sent emergency alert");
                } else {
                    Log.e(TAG, "sendEmergency: failed to send emergency alert");
                }
            }));


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
                    .addOnSuccessListener(this::updateFirebaseTokenAndConnectSocket);
            } else if (value != null && !value) {
                // Not logged in, invoke LoginActivity and quit current activity
                jitsiMeetHolder.destroy();
                navController.navigate(R.id.action_global_loginActivity);
                this.finish();
            }
        });

        // Subscribe to logout and disconnect socket
        pubSubHub.subscribe(PubSubTopics.LOGGED_OUT,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    socketService.disconnect();
                }
            });

        combineLatest(navigationService.getCurrentNavSessionLiveData(), inNavScreen,
            (navSession, isOnScreen) -> {
                if (navSession != null && isOnScreen != null && ! isOnScreen) {
                    return navSession;
                } else {
                    return null;
                }

            }).observe(this, result -> {
                if (result != null && result.getActive() != null) {
                    if (result.getActive() && result.getId() > 0) {

                        ongoingNavButton.setVisibility(View.VISIBLE);
                        navSessionId = result.getId();
                    }

                } else {
                    ongoingNavButton.setVisibility(View.GONE);
                }
            });



        ongoingNavButton.setOnClickListener(v -> {
            // start nav session
            Bundle bundle = new Bundle();
            bundle.putInt("sessionId", navSessionId);
            Boolean isAp = authService.getCurrentUser().getUserType() != User.UserType.AP;
            bundle.putBoolean("apInitiated", isAp);

            navController.navigate(R.id.action_show_nav_screen_from_banner, bundle);
        });



        // Check permissions
        checkPermissions();

        // profile image download upload
        mediaManager.registerMediaType(MediaManager.TYPE_PROFILE, profileImageManager);
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

    private void updateFirebaseTokenAndConnectSocket(InstanceIdResult instanceIdResult) {
        pubSubHub.publish(PubSubTopics.FIREBASE_TOKEN,
            new FirebaseTokenData(
                instanceIdResult.getId(),
                instanceIdResult.getToken())
        );
        socketService.connect(instanceIdResult.getToken());
    }


    // ******************************** EXPLICIT PERMISSIONS ********************************


    /**
     * Check permissions.
     */
    private void checkPermissions() {
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(this,
                Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                AUDIO_RECORD_PERMISSION)
                ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    MODIFY_AUDIO_PERMISSION)
                ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    FINE_LOCATION)
                ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    COARSE_LOCATION)
                ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    CAMERA_PERMISSION)
                ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)
                ) {

                Log.d(TAG, "show request rationale");
                showRequestRationale();

            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: called.");

        switch (requestCode) {
        case AUDIO_LOCATION_AND_CAMERA_PERMISSION_REQUEST_CODE: {
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

    private void requestPermission() {
        String[] permissions = {
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE
        };
        ActivityCompat.requestPermissions(this, permissions,
            AUDIO_LOCATION_AND_CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void showRequestRationale() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Audio and location access");
        alertDialog.setMessage("Please allow audio and location access");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
            (dialog, which) -> {
                dialog.dismiss();
                requestPermission();
            });
        alertDialog.show();
    }

    private void showDeniedDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Denied Audio or Location Access");
        alertDialog.setMessage("We want to know how you sound like and where you live. "
                               + "As well as how you look. Please? Its important");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Review Permission Access",
            (dialog, which) -> {
                dialog.dismiss();
                requestPermission();
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Ignore",
            (dialog, which) -> {
                dialog.dismiss();
                Toast.makeText(this, "GGWP", Toast.LENGTH_LONG).show();
            });
        alertDialog.show();
    }

    @Override
    public void enterNavScreen() {
        inNavScreen.postValue(true);
    }

    @Override
    public void leaveNavScreen() {
        inNavScreen.postValue(false);
    }
}
