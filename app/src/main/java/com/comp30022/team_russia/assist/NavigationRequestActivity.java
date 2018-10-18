package com.comp30022.team_russia.assist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.comp30022.team_russia.assist.features.nav.models.NavMapScreenStartArgs;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.NewNavStartPushNotification;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

import javax.inject.Inject;

/**
 * NavigationRequestActivity.
 */
public class NavigationRequestActivity extends AppCompatActivity
    implements HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    PubSubHub pubSubHub;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_nav_session_request);

        String text = "Do you want to start a nav session with "
                      + getIntent().getExtras().getString("senderName") + " ?";
        ((TextView) findViewById(R.id.navSessionTitle)).setText(text);


        findViewById(R.id.declineButton).setOnClickListener(r -> this.finish());

        findViewById(R.id.acceptButton).setOnClickListener(r -> {

            pubSubHub.publish(PubSubTopics.NAV_ACCEPTED, new NavMapScreenStartArgs(
                getIntent().getExtras().getString("senderName"),
                getIntent().getExtras().getInt("sessionId"),
                getIntent().getExtras().getInt("assocId"),
                getIntent().getExtras().getBoolean("apInitiated")
            ));

            this.finish();
        });

    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }
}