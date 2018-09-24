package com.comp30022.team_russia.assist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

import javax.inject.Inject;

/**
 * The primary (home) Activity.
 */
public class HomeContactListActivity extends AppCompatActivity
    implements HasSupportFragmentInjector, TitleChangable {

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    AuthService authService;

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
            if (value) {
                // Show/hide button depending on user type
                if (authService.getCurrentUser().getUserType() == User.UserType.Carer) {
                    emergencyBtn.setVisibility(View.GONE);
                }
            } else {
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
}
