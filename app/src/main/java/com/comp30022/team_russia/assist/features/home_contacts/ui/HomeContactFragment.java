package com.comp30022.team_russia.assist.features.home_contacts.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentHomeBinding;
import com.comp30022.team_russia.assist.features.home_contacts.vm.HomeContactViewModel;
import com.comp30022.team_russia.assist.features.jitsi.services.JitsiMeetHolder;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

/**
 * Home Screen (Contact List).
 */
public class HomeContactFragment extends BaseFragment implements Injectable {

    private static final String TAG = "HomeContactFragment";
    //todo: use a location service instead
    private Handler handler = new Handler();
    private static final int DELAY = 10 * 1000; // 10 seconds
    private Runnable runnable;

    private static final long UPDATE_INTERVAL = 4000; // 4 seconds

    private static final long FASTEST_INTERVAL = 2000; // 2 seconds


    private HomeContactViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    ProfileDetailsService profileDetailsService;

    private FragmentHomeBinding binding;
    private ContactListAdapter adapter;

    @Inject
    JitsiMeetHolder jitsiMeetHolder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        /* view model binding */
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(HomeContactViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_home, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        adapter = new ContactListAdapter(viewModel, this, profileDetailsService);
        configureRecyclerView();
        setupNavigationHandler(viewModel);
        subscribeToListChange();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    private void configureRecyclerView() {
        RecyclerView recyclerView = binding.contactListRecyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void subscribeToListChange() {
        viewModel.contactList.observe(this, newContactList -> {
            if (newContactList != null) {
                adapter.setContactItemList(newContactList);
            }
            binding.executePendingBindings();
        });
    }

    @Override
    public void onResume() {
        // start handler when fragment visible
        handler.postDelayed(runnable = () -> {

            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity()
                .getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

                // send
                getApLocation();
            }
            handler.postDelayed(runnable, DELAY);
        }, DELAY);
        super.onResume();
    }

    @Override
    public void onPause() {
        // stop handler when fragment not visible
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void getApLocation() {
        FusedLocationProviderClient fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(getContext());

        try {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Location currentLocation = task.getResult();
                    if (currentLocation != null) {
                        Log.i(TAG, "getApLocationHelper: currentLocation is: "
                                   + currentLocation.getLatitude() + " "
                                   + currentLocation.getLongitude());

                        LatLng newApLocation = new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude());

                        //update ap location using newApLocation to server
                        viewModel.updateApLocation(newApLocation);

                    } else {
                        Log.i(TAG, "getApLocationHelper: currentLocation is NULL!");
                        requestLocation(fusedLocationProviderClient);
                    }
                } else {
                    Log.i(TAG, "getApLocationHelper: not successful");
                }
            });

        } catch (SecurityException e) {
            Log.i(TAG, "getApLocation: SecurityException: " + e.getMessage());
        }
    }


    /**
     * No location has been retrieved before, so get new location position.
     * Called from Ap device.
     * @param fusedLocationProviderClient instance of FusedLocationProviderClient
     */
    private void requestLocation(FusedLocationProviderClient fusedLocationProviderClient) {
        // request location
        LocationRequest locationRequest = LocationRequest.create()
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Location location = locationResult.getLastLocation();

                if (location != null) {

                    LatLng newApLocation =
                        new LatLng(location.getLatitude(), location.getLongitude());

                    Log.i(TAG, "requestLocation: newApLocation is: "
                               + newApLocation.latitude + " "
                               + newApLocation.longitude);

                    //update ap location using newApLocation to server
                    viewModel.updateApLocation(newApLocation);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(getContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.myLooper());
    }
}