package com.comp30022.team_russia.assist.features.emergency.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.features.emergency.services.EmergencyAlertService;
import com.comp30022.team_russia.assist.features.emergency.sys.AudioPlayer;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

import javax.inject.Inject;

/**
 * Emergency Notification.
 */
public class EmergencyNotificationActivity extends AppCompatActivity
    implements HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    EmergencyAlertService  emergencyAlertService;

    private String theNumber = "";

    private final AudioPlayer audioPlayer = new AudioPlayer();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getIntent().getExtras() != null) {
            theNumber = getIntent().getExtras().getString("mobileNumber", "");
        }

        int eventId = getIntent().getExtras().getInt("eventId");

        setContentView(R.layout.activity_emergency_notification);

        findViewById(R.id.btnEmerCall).setOnClickListener(v -> {
            audioPlayer.stop();
            emergencyAlertService.handleEmergency(eventId);
            startCall(theNumber);
            EmergencyNotificationActivity.this.finish();
        });

        findViewById(R.id.btnEmerIgnore).setOnClickListener(
            v -> {
                audioPlayer.stop();
                emergencyAlertService.handleEmergency(eventId);
                EmergencyNotificationActivity.this.finish();
            });

        audioPlayer.playLooping(this, R.raw.alarm);
    }

    private void startCall(String mobileNumber) {
        //String number = "0426591074"; // set to daniel's number
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + mobileNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }
}
