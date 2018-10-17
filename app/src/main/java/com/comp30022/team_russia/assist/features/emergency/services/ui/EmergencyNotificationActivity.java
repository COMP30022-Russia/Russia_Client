package com.comp30022.team_russia.assist.features.emergency.services.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.comp30022.team_russia.assist.R;

/**
 * Emergency Notification.
 */
public class EmergencyNotificationActivity extends AppCompatActivity {

    private String theNumber = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            theNumber = getIntent().getExtras().getString("mobileNumber", "");
        }

        setContentView(R.layout.activity_emergency_notification);

        ((Button) findViewById(R.id.btnEmerCall)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCall(theNumber);
            }
        });

        ((Button) findViewById(R.id.btnEmerIgnore)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmergencyNotificationActivity.this.finish();
            }
        });
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
}
