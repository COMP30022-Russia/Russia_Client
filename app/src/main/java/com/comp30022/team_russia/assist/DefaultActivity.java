package com.comp30022.team_russia.assist;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.view.View;

import com.comp30022.team_russia.assist.features.home_contacts.ContactListAdapter;

import java.util.ArrayList;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class DefaultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);

        /* setup toolbar */
        Toolbar toolbar = findViewById(R.id.customAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.default_fragment);

        NavController navController = host.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
