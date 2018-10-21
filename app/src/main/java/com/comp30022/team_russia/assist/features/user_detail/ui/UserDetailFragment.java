package com.comp30022.team_russia.assist.features.user_detail.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentUserDetailBinding;
import com.comp30022.team_russia.assist.features.nav.ui.LocationEnabledFragment;
import com.comp30022.team_russia.assist.features.user_detail.vm.UserDetailViewModel;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

import javax.inject.Inject;

/**
 * User Detail Fragment.
 */
public class UserDetailFragment extends LocationEnabledFragment
    implements OnMapReadyCallback, Injectable {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private UserDetailViewModel viewModel;

    // Map constants
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final float DEFAULT_ZOOM = 18f;

    // Map view
    private GoogleMap googleMap;
    private MapView mapView;
    private Marker previousMarker;

    private Handler handler = new Handler();
    private static final int DELAY = 30 * 1000; // 30 seconds
    private Runnable runnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(UserDetailViewModel.class);

        FragmentUserDetailBinding binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_user_detail, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Parse input arguments
        Bundle bundle = Objects.requireNonNull(getArguments());
        viewModel.associationId.setValue(bundle.getInt("assocId"));
        viewModel.currentUserIsAp = bundle.getBoolean("apInitiated");
        viewModel.otherUserId.setValue(bundle.getInt("otherUserId"));

        // Load contact details
        viewModel.getOtherUserDetails();

        // Initialise map and watch for location if user is AP
        if (!viewModel.currentUserIsAp) {
            viewModel.currentApLocation.observe(this, this::showApLocationOnMap);
            mapView = view.findViewById(R.id.mapView);
            initGoogleMap(savedInstanceState);
        }
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    private void showApLocationOnMap(LatLng apLocation) {
        googleMap.animateCamera(CameraUpdateFactory
            .newLatLngZoom(apLocation, DEFAULT_ZOOM));

        if (previousMarker != null) {
            previousMarker.remove();
        }
        previousMarker = googleMap.addMarker(new MarkerOptions()
            .position(apLocation)
            .icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_AZURE)));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!viewModel.currentUserIsAp) {
            Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
            if (mapViewBundle == null) {
                mapViewBundle = new Bundle();
                outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
            }

            mapView.onSaveInstanceState(mapViewBundle);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!viewModel.currentUserIsAp) {
            this.googleMap = googleMap;

            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            viewModel.getApLocation();
        }
    }

    @Override
    public void onResume() {
        if (!viewModel.currentUserIsAp) {
            // Start handler when fragment visible
            handler.postDelayed(runnable = () -> {
                viewModel.getApLocation();

                handler.postDelayed(runnable, DELAY);
            }, DELAY);
        }
        super.onResume();
        if (!viewModel.currentUserIsAp) {
            mapView.onResume();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!viewModel.currentUserIsAp) {
            mapView.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!viewModel.currentUserIsAp) {
            mapView.onStop();
        }

    }

    @Override
    public void onPause() {
        if (!viewModel.currentUserIsAp) {
            mapView.onPause();

            // stop handler when fragment not visible
            handler.removeCallbacks(runnable);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (!viewModel.currentUserIsAp) {
            mapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (!viewModel.currentUserIsAp) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onLocationPermissionGranted() {
    }

    @Override
    protected void onLocationPermissionDenied() {
        mapView.setVisibility(View.GONE);
    }
}
