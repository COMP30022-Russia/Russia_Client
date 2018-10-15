package com.comp30022.team_russia.assist.features.nav.ui;

import android.Manifest;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentNavigationMapBinding;
import com.comp30022.team_russia.assist.features.nav.adapter.GuideCardAdapter;
import com.comp30022.team_russia.assist.features.nav.adapter.PlaceAutocompleteAdapter;
import com.comp30022.team_russia.assist.features.nav.models.Directions;
import com.comp30022.team_russia.assist.features.nav.models.GuideCard;
import com.comp30022.team_russia.assist.features.nav.models.Leg;
import com.comp30022.team_russia.assist.features.nav.models.PlaceInfo;
import com.comp30022.team_russia.assist.features.nav.models.PlaceSuggestionItem;
import com.comp30022.team_russia.assist.features.nav.models.Route;
import com.comp30022.team_russia.assist.features.nav.models.TransportMode;
import com.comp30022.team_russia.assist.features.nav.vm.NavigationViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.internal.PolylineEncoding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;


/**
 * Navigation Fragment.
 */
public class NavigationFragment extends LocationEnabledFragment implements
    Injectable,
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {


    /* vars */
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private static final String TAG = "NavigationFragment";

    private NavigationViewModel viewModel;

    private FragmentNavigationMapBinding binding;




    /***************** Voice Call ************************/

    /**
     * animation for call button.
     */
    private static Animation scaleAnimation = new ScaleAnimation(
        1f, 1.5f,
        1f, 1.5f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    );

    static {
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setDuration(300);
        scaleAnimation.setInterpolator(new BounceInterpolator());
    }


    /***************** Location Service ************************/

    /**
     * method1 timed calls to get location of ap.
     */
    //todo: use a location service instead (refer to method2)
    private Handler handler = new Handler();
    private static final int DELAY = 100 * 1000; // 2 seconds
    private Runnable runnable;

    /**
     * method2 interval between getting location updates.
     */
    private static final long UPDATE_INTERVAL = 4000; // 4 seconds

    private static final long FASTEST_INTERVAL = 2000; // 2 seconds


    /**
     * default zoom level when moving camera on map.
     */
    private static final float DEFAULT_ZOOM = 18f;


    private GoogleMap googleMap;

    private PlaceAutocompleteAdapter placeAutocompleteAdapter;

    private Marker marker;

    private GeoApiContext geoApiContext;


    /***************** Proximity Check. ************************/

    private static final int ARRIVE_DISTANCE = 10; // 10 meters

    private static final int OFFTRACK_DISTANCE = 20; //20 meters


    /******************** UI. ************************/

    /**
     * title of the navigation bar which would be replaced by destination name.
     */
    public final MutableLiveData<String> title = new MutableLiveData<>();

    /**
     * if the current user has control or not.
     */
    private Boolean userHaveControl;

    /**
     * if the user confirm to switch modes or not.
     * to prevent infinite dialogue popups when a request is declined.
     */
    private Boolean disableModeDialog = false;

    /**
     * Walk mode of transport mode tab.
     */
    private static final int TRANSPORT_MODE_WALK = 0;

    /**
     * Public transport mode of transport mode tab.
     */
    private static final int TRANSPORT_MODE_PT = 1;

    /**
     * to know if current destination is favourite or not.
     */
    private Boolean isFav = true;

    /**
     * to check if we have shown the ap's location before or not.
     * todo: used to move the camera only once in the beginning after getting ap location
     */
    private Boolean shownApLocation = false;

    /**
     * the last polyline saved from when a destination was set.
     * used to keep track of which polylines to remove and which to display on the map.
     */
    private Polyline previousPolyline = null;

    /**
     * the last destination marker saved from when a destination was set.
     * used to keep track of which destination markers to remove and which to display on the map.
     */
    private Marker previousDestinationMarker = null;

    /**
     * used on the Carer's device only
     * the last location of Ap marker saved from when a destination was set.
     * used to keep track of which location of Ap markers to remove and which to display on the map.
     */
    private Marker previousApMarker = null;


    /* widgets */
    private MapView mapView;

    private AutoCompleteTextView searchText;

    private RelativeLayout searchBox;

    private ImageView clearSearchButton;

    private ImageView endNavSessionButton;

    private MenuItem favSelectedItem;

    private MenuItem favUnselectedItem;

    private Button getControlButton;

    private ImageView zoomOutButton;

    private ImageView zoomInButton;

    /**
     * the button to focus position of map to display current location of Ap.
     */
    private ImageView recenterButton;

    /**
     * a "banner" that shows who currently has control.
     */
    private TextView controlStatusTextView;

    /**
     * the tab layout for transport mode.
     */
    private TabLayout transportModeTabLayout;

    /**
     * the current guide card that corresponds to the guide card shown on the map.
     * this is the current guide card that is relevant to the current 'step' in the route.
     * non-null only when a route has been received from server.
     */
    private GuideCard currentGuideCard;

    /**
     * layout manager for the guide card recycler view.
     */
    private RecyclerView.LayoutManager guideCardRecyclerViewLayoutManager;

    /**
     * the recycler view for the guide cards.
     */
    private RecyclerView guideCardsRecyclerView;

    /**
     * the indicator to show which guide card the user is currently viewing.
     */
    private TextView currentGuideCardIndicator;

    /**
     * the previous guide card shown.
     * used to keep track of which guide card to swipe to next,
     * when Ap reaches the guide card end position.
     */
    private int previousGuideCard;





    /*
     * -------------------------------- INITIALISING GOOGLE MAP STUFF ----------------------------
     */

    /**
     * Google API required method, provides the map when ready.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady: map is ready");
        this.googleMap = googleMap;

        if (locationPermissionsGranted) {
            getLastDeviceLocation();

            // explicit permission check required by setMyLocationEnabled function
            if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                   != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if (viewModel.currentUserIsAp) {
                this.googleMap.setMyLocationEnabled(true);
            }
            this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);

            initUiListener();
        }
    }

    /**
     * Initialise the map.
     */
    private void initMap() {
        Log.i(TAG, "initMap: initializing map");
        mapView.getMapAsync(this::onMapReady);

        /* Google Directions */
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                .apiKey(getString(R.string.api_key))
                .build();
        }

    }


    /*
     * -------------------------------- INITIALISING VIEW STUFF ----------------------------
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(NavigationViewModel.class);

        // get values from bundle
        viewModel.assocId.setValue(getArguments().getInt("assocId"));
        viewModel.apInitiated = (getArguments().getBoolean("apInitiated"));

        Log.i(TAG, "onCreateView assocId " + viewModel.assocId.getValue());
        Log.i(TAG, "onCreateView apInitiated " + viewModel.apInitiated);

        viewModel.getNavigationSession();

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_navigation_map, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.voiceCallVm.shouldAnimate.observe(this, value -> {
            if (value != null && value) {
                this.getActivity().runOnUiThread(() ->
                    binding.startNavCall.startAnimation(scaleAnimation));

            } else {
                this.getActivity().runOnUiThread(() -> binding.startNavCall.clearAnimation());
            }
        });

        viewModel.voiceCallVm.showConfirmAcceptDialog.observe(this,
            (x) -> showAcceptVoiceCallDialog());
        viewModel.voiceCallVm.showConfirmEndDialog.observe(this,
            (x) -> showEndVoiceCallDialog());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        zoomInButton = view.findViewById(R.id.zoom_in_button);

        zoomOutButton = view.findViewById(R.id.zoom_out_button);

        getControlButton = view.findViewById(R.id.get_control_button);

        endNavSessionButton = view.findViewById(R.id.end_nav_session);

        searchBox = view.findViewById(R.id.search_box);

        searchText = view.findViewById(R.id.input_search);

        clearSearchButton = view.findViewById(R.id.clear_search_button);

        recenterButton = view.findViewById(R.id.ic_gps);
        controlStatusTextView = view.findViewById(R.id.control_status);

        transportModeTabLayout = view.findViewById(R.id.map_tab_layout);

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        guideCardsRecyclerView = view.findViewById(R.id.guide_card_recyclerview);

        currentGuideCardIndicator = view.findViewById(R.id.total_guide_cards_text);

        wireUpListenEvents();

        /* check location permission allowed by user */
        getLocationPermission();

        initiateMapUiControl();

    }

    private void wireUpListenEvents() {
        Log.i(TAG, "wireUpListenEvents entered");
        viewModel.navSessionStarted.observe(this, this::updateServerApLocation);
        viewModel.currentMode.observe(this, this::updateTransportModeIndicator);
        viewModel.currentApLocation.observe(this, this::refreshApLocation);
        viewModel.currentDirections.observe(this, this::updateDestinationDetails);
        viewModel.currentRoutes.observe(this, this::addPolylinesToMap);
        viewModel.currentGuideCards.observe(this, this::displayGuideCards);
        viewModel.carerHasControl.observe(this, this::refreshMapUiControl);
        viewModel.navSessionEnded.observe(this, this::endNavSession);

        viewModel.apIsOffTrack.observe(this, this::tellCarerApIsOffTrack);
    }

    private void initiateMapUiControl() {
        // i am ap and have control or i am carer and have control
        if ((viewModel.currentUserIsAp && viewModel.apInitiated)
            || (!viewModel.currentUserIsAp && !viewModel.apInitiated)) {
            activateUi();
            userHaveControl = true;
        } else {
            deactivateUi();
            userHaveControl = false;
        }
    }


    /**
     * Used to check location updates every 5 seconds.
     */
    @Override
    public void onResume() {
        // start handler when fragment visible
        handler.postDelayed(runnable = () -> {
            if (locationPermissionsGranted) {
                getLastDeviceLocation();
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

    /*
     * -------------------------------- INITIALISING EVENT LISTENER ----------------------------
     */

    /**
     * Handle ending the nac session.
     * @param navigationEnded received from server if navigation ended
     */
    private void endNavSession(Boolean navigationEnded) {
        if (navigationEnded) {
            getActivity().onBackPressed();
        }
    }

    /**
     * Handle the event when google map marker is clicked.
     * @param allMarker The marker that was clicked.
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker allMarker) {
        if (allMarker.equals(this.marker)) {
            if (!allMarker.isVisible()) {
                marker.showInfoWindow();
            } else {
                marker.hideInfoWindow();
            }
        }
        return true;
    }

    /**
     * Initialise the UI and adapters involved in the map screen.
     */
    private void initUiListener() {
        Log.i(TAG, "init: initializing");

        zoomOutButton.setOnClickListener(v ->
            googleMap.animateCamera(CameraUpdateFactory.zoomOut()));

        zoomInButton.setOnClickListener(v ->
            googleMap.animateCamera(CameraUpdateFactory.zoomIn()));

        searchText.setOnItemClickListener((adapterView, view, i, l) -> {
            hideSoftKeyboard();

            final PlaceSuggestionItem item = placeAutocompleteAdapter.getItem(i);
            viewModel.onSuggestionClicked(item);
        });

        searchText.setThreshold(1); // specify the minimum no. of char before list is shown

        searchText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                viewModel.userStartedTyping = true;
            }
        });

        clearSearchButton.setOnClickListener(v -> {
            searchText.requestFocus();
            if (searchText.getText() != null) {
                searchText.getText().clear();
            }
        });

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(), this.viewModel);

        searchText.setAdapter(placeAutocompleteAdapter);

        recenterButton.setOnClickListener(view -> {
            Log.i(TAG, "onClick: clicked gps icon");
            recenterCamera(viewModel.currentApLocation.getValue(), DEFAULT_ZOOM);
        });

        transportModeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!disableModeDialog) {
                    if (tab.getPosition() == TRANSPORT_MODE_WALK) {
                        // walk_mode is selected
                        Log.i(TAG, "setDestination walk mode selected");
                        showModeChangeConfirmDialog("walk", TRANSPORT_MODE_PT,
                            TransportMode.WALK);

                    } else if (tab.getPosition() == TRANSPORT_MODE_PT) {
                        // public_mode is selected
                        Log.i(TAG, "setDestination public mode selected");
                        showModeChangeConfirmDialog("public transport", TRANSPORT_MODE_WALK,
                            TransportMode.PUBLIC_TRANSPORT);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        hideSoftKeyboard();
    }

    private void showModeChangeConfirmDialog(String title, int otherTab, TransportMode mode) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Confirm change currentMode to " + title);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
            (dialog, which) -> {
                dialog.dismiss();
                viewModel.currentMode.setValue(mode);
                Log.i(TAG, "setDestination transport mode changed to: "
                           + viewModel.currentMode.getValue());
                viewModel.setDestination();
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
            (dialog, which) -> {
                dialog.dismiss();
                disableModeDialog = true;
                transportModeTabLayout.getTabAt(otherTab).select();
                disableModeDialog = false;
                Log.i(TAG, "setDestination mode not changed");
            });
        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
    }


    /*
     * ----------------------- Call ---------------------------------------- */

    private void showAcceptVoiceCallDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Accept voice call?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept",
            (dialog, which) -> {
                dialog.dismiss();
                viewModel.voiceCallVm.onAccept();
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Decline",
            (dialog, which) -> {
                dialog.dismiss();
                viewModel.voiceCallVm.onDecline();
            });
        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private void showEndVoiceCallDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("End voice call?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
            (dialog, which) -> {
                dialog.dismiss();
                viewModel.voiceCallVm.onEnd();
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
            (dialog, which) -> {
                dialog.dismiss();
            });
        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
    }


    /*
     * -------------------------------- FAVOURITE BUTTON ----------------------------
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_navigation, menu);

        favSelectedItem = menu.findItem(R.id.favorite_destination_selected);
        favUnselectedItem = menu.findItem(R.id.favorite_destination_unselected);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.favorite_destination_selected: // destination was UNfavorited
            isFav = true;
            getActivity().invalidateOptionsMenu(); // this calls onPrepareOptionsMenu
            viewModel.toggleFavoriteStatus();
            return true;

        case R.id.favorite_destination_unselected: // destination was favorited
            isFav = false;
            getActivity().invalidateOptionsMenu();
            viewModel.toggleFavoriteStatus();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (viewModel.navSessionStarted.getValue()) {
            if (isFav) {
                favSelectedItem.setVisible(false);
                favUnselectedItem.setVisible(true);
            } else {
                favSelectedItem.setVisible(true);
                favUnselectedItem.setVisible(false);
            }
        } else {
            favSelectedItem.setVisible(false);
            favUnselectedItem.setVisible(false);
        }
    }



    /*
     * -------------------------------- GET INITIAL LOCATION STUFF ----------------------------
     */

    /**
     * Retrieve last location of AP and displays it on the map.
     * Called when navigation screen is first shown and when recenter button is pressed.
     */
    private void getLastDeviceLocation() {
        if (viewModel.currentUserIsAp) { // user type is ap
            Log.i(TAG, "getLastDeviceLocation finding location for AP");
            if (locationPermissionsGranted) {
                getApLocationHelper();
            } else {
                Log.i(TAG, "getLastDeviceLocation: no location permissions");
            }

        } else { // user type is carer
            Log.i(TAG, "getLastDeviceLocation finding location for Carer");
            //ask server for last location of ap
            LatLng apLocation = viewModel.getApLocation();
            if (apLocation != null) {

                Log.i(TAG, "getLastDeviceLocation ap location is: " + apLocation.toString());
                if (!shownApLocation && ! viewModel.routeIsSet.getValue()) {
                    moveCamera(apLocation, DEFAULT_ZOOM);
                    shownApLocation = true;
                }

            } else {
                Log.i(TAG, "getLastDeviceLocation ap location is null");
            }
        }
    }

    /**
     * Get the location of AP from local device memory and prompt Location Service
     * if no history.
     */
    private void getApLocationHelper() {
        FusedLocationProviderClient fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(getContext());

        try {

            if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Location currentLocation = task.getResult();
                    if (currentLocation != null) {
                        Log.i(TAG, "getApLocationHelper: currentLocation is: "
                                   + currentLocation.getLatitude() + " "
                                   + currentLocation.getLongitude());

                        LatLng newApLocation = new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude());

                        if (!shownApLocation && ! viewModel.routeIsSet.getValue()) {
                            moveCamera(newApLocation, DEFAULT_ZOOM);
                            shownApLocation = true;
                        }

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

                    //move camera to current location of ap
                    if (!shownApLocation && ! viewModel.routeIsSet.getValue()) {
                        moveCamera(newApLocation, DEFAULT_ZOOM);
                        shownApLocation = true;
                    }

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


    /*
     * ------------------------------------ UPDATE VIEW -------------------------------------
     */


    /**
     * Firebase notification received that ap is offtrack.
     * Alert carer that they are off track.
     * @param apIsOffTrack if ap is off track from route by 20m
     */
    private void tellCarerApIsOffTrack(Boolean apIsOffTrack) {
        if (apIsOffTrack && !viewModel.currentUserIsAp) {

            // only alert carer if dialog is not showing anymore
            if (! viewModel.apOffTrackDialogStillShown.getValue()) {

                showOffTrackDialogForCarer();
                viewModel.apOffTrackDialogStillShown.setValue(true);
            }
        }
    }


    /**
     * Update location of ap on server.
     * @param navSessionStarted boolean for nav session started
     */
    private void updateServerApLocation(Boolean navSessionStarted) {
        if (navSessionStarted && viewModel.currentUserIsAp) {
            viewModel.updateApLocation(viewModel.currentApLocation.getValue());
        }
    }


    /**
     * Gets called when server updates both devices that ap location has changed.
     * @param newApLocation new location of ap
     */
    private void refreshApLocation(LatLng newApLocation) {

        // User is ap
        if (viewModel.currentUserIsAp) {



            // check if ap went off track
            if (previousPolyline != null) {

                //
                if (! PolyUtil.isLocationOnPath(newApLocation, previousPolyline.getPoints(),
                    true, OFFTRACK_DISTANCE)) {

                    // only alert ap if dialog is not showing anymore
                    if (! viewModel.apOffTrackDialogStillShown.getValue()) {

                        // show Ap that they are off track by showing banner
                        viewModel.apOffTrackDialogStillShown.setValue(true);

                        // update server
                        viewModel.apWentOffTrack();
                    }

                    return;

                } else {
                    // ap is no longer off track
                    viewModel.apOffTrackDialogStillShown.setValue(false);
                }
            }



            if (currentGuideCard != null) {



                // check if its time to automatically swipe guide cards
                Log.e(TAG, "guideCard checking guide card distance");

                ArrayList<LatLng> currentGuideCardEnd = new ArrayList<>();
                currentGuideCardEnd.add(new LatLng(
                    currentGuideCard.getEndLocation().getLocationLat(),
                    currentGuideCard.getEndLocation().getLocationLon()));

                Log.e(TAG, "guideCard end location: " + currentGuideCardEnd.get(0).toString());


                // ap reached next guide card location
                if (PolyUtil.isLocationOnEdge(newApLocation, currentGuideCardEnd,
                    true, ARRIVE_DISTANCE)) {

                    // swipe guide card
                    int nextSlide = previousGuideCard + 1;
                    int guideCardSize = viewModel.currentGuideCards.getValue().size();

                    // only swipe guide card if its not the last guide card
                    if (nextSlide <= guideCardSize) {

                        RecyclerView.SmoothScroller smoothScroller =
                            new LinearSmoothScroller(getContext()) {
                                @Override
                                protected int getVerticalSnapPreference() {
                                    return LinearSmoothScroller.SNAP_TO_END;
                                }
                            };

                        smoothScroller.setTargetPosition(nextSlide);
                        guideCardRecyclerViewLayoutManager.startSmoothScroll(smoothScroller);

                    }
                }






                // check if ap reach destination
                Log.e(TAG, "guideCard checking destination arrival");

                List<GuideCard> allGuideCards = viewModel.currentGuideCards.getValue();
                int lastGuideCardSize = allGuideCards.size() - 1;
                GuideCard lastGuideCard = allGuideCards.get(lastGuideCardSize);


                ArrayList<LatLng> lastGuideCardEnd = new ArrayList<>();
                lastGuideCardEnd.add(new LatLng(
                    lastGuideCard.getEndLocation().getLocationLat(),
                    lastGuideCard.getEndLocation().getLocationLon()));

                Log.e(TAG, "lastguideCard end location: "
                           + lastGuideCard.getEndLocation().toString());

                // ap has reached destination
                if (PolyUtil.isLocationOnEdge(newApLocation, lastGuideCardEnd,
                    true, ARRIVE_DISTANCE)) {

                    // alert Ap that they have reached destination
                    showArrivedDestinationDialog();



                }
            }


            return;







        } else { // User is carer


            Log.i(TAG, "refreshApLocation: updating marker position of ap");
            // clear old ap marker
            if (previousApMarker != null) {
                previousApMarker.remove();
            }

            // update marker for carer
            if (!viewModel.currentUserIsAp) {
                previousApMarker =
                    googleMap.addMarker(new MarkerOptions()
                        .position(newApLocation)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));
            }
        }
    }


    private void showOffTrackDialogForCarer() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Carer Offtrack Alert");
        alertDialog.setMessage("Carer is offtrack from route by 20m, do you want to assist them?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes, call Carer",
            (dialog, which) -> {
                dialog.dismiss();
                viewModel.apIsOffTrack.setValue(false);
                viewModel.apOffTrackDialogStillShown.setValue(false);
                // todo start call with carer if there is no existing call
                // todo need create a boolean to keep track if this offtrack will be resolved
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No, its ok",
            (dialog, which) -> {
                dialog.dismiss();
                viewModel.apIsOffTrack.setValue(false);
                viewModel.apOffTrackDialogStillShown.setValue(false);
            });
        alertDialog.show();
    }

    private void showArrivedDestinationDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Arrived destination safely?");
        alertDialog.setMessage("Are you safe? Have you arrived your destination?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes, end navigation",
            (dialog, which) -> {
                dialog.dismiss();
                viewModel.endNavSession(false);
                viewModel.navSessionEnded.postValue(true);
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No, I need help",
            (dialog, which) -> {
                dialog.dismiss();
                showCallCarerDialog();
            });
        alertDialog.show();
    }

    private void showCallCarerDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Do you want to call Carer?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
            (dialog, which) -> {
                dialog.dismiss();
                //todo call carer
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No, I am alright",
            (dialog, which) -> {
                dialog.dismiss();
                viewModel.endNavSession(false);
                viewModel.navSessionEnded.postValue(true);
            });
        alertDialog.show();
    }

    /**
     * Gets called when receiving control switch from server.
     * Gets called on recipients devices.
     * @param carerGivenControl did carer gain control
     */
    private void refreshMapUiControl(Boolean carerGivenControl) {
        // i am ap and have control or i am carer and have control
        if ((viewModel.currentUserIsAp && !carerGivenControl)
            || (!viewModel.currentUserIsAp && carerGivenControl)) {
            activateUi();
            userHaveControl = true;
        } else {
            deactivateUi();
            userHaveControl = false;
        }
    }

    /**
     * Enable UI control for the device.
     */
    private void activateUi() {
        searchBox.setVisibility(View.VISIBLE);
        transportModeTabLayout.setVisibility(View.VISIBLE);
        endNavSessionButton.setVisibility(View.VISIBLE);
        getControlButton.setVisibility(View.GONE);
        controlStatusTextView.setVisibility(View.GONE);
    }

    /**
     * Disable UI control for the device.
     */
    private void deactivateUi() {
        searchBox.setVisibility(View.GONE);
        transportModeTabLayout.setVisibility(View.GONE);
        endNavSessionButton.setVisibility(View.GONE);
        getControlButton.setVisibility(View.VISIBLE);
        controlStatusTextView.setVisibility(View.VISIBLE);
    }

    /*
     * -------------------------------- ON DESTINATION SET STUFF ----------------------------
     */

    /**
     * Recentre the map view around to the location provided.
     * @param latLng The target position to be centred on.
     * @param zoom The requested zoom level.
     */
    private void moveCamera(LatLng latLng, float zoom) {


        Log.i(TAG, "moveCamera: moving the camera to: lat: "
                   + latLng.latitude + ", lng: " + latLng.longitude);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        // only do this if user is carer and doesnt have control
        if (!userHaveControl && !viewModel.currentUserIsAp) {

            // clear old ap marker
            if (previousApMarker != null) {
                previousApMarker.remove();
            }

            // show position of ap for carer for the first time

            previousApMarker =
                googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE)));


        } else if (userHaveControl && viewModel.currentUserIsAp) {
            // this block runs if user is ap and has control

        } else {
            // this block runs if user is ap and doesnt have control
            // or id user is carer and has control
            if (previousDestinationMarker != null) {
                previousDestinationMarker.remove();
            }
            previousDestinationMarker =
                googleMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }


    /**
     * Recentre the map view to show current ap location.
     * @param latLng Current location of ap.
     * @param zoom The requested zoom level.
     */
    private void recenterCamera(LatLng latLng, float zoom) {
        if (latLng == null) {
            return;
        }

        Log.i(TAG, "recenterCamera: moving the camera to: lat: "
                   + latLng.latitude + ", lng: " + latLng.longitude);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        // Carer called put blue marker
        if (! viewModel.currentUserIsAp) {

            // clear old ap marker
            if (previousApMarker != null) {
                previousApMarker.remove();
            }


            previousApMarker =
                googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE)));


        }

        hideSoftKeyboard();
    }


    /*
     * ---------------------- SHOW ROUTE ON VIEW STUFF --------------------------
     */

    /**
     * Update and get destination details for current destination from Directions Object.
     * Specifically current destination PlaceInfo and Route.
     * @param directions Directions Object received from server
     */
    private void updateDestinationDetails(final Directions directions) {
        int lastWayPoint = directions.getDirectionsGeocodedWaypoints().size() - 1;
        viewModel.getPlaceFromPlaceId(directions.getDirectionsGeocodedWaypoints()
            .get(lastWayPoint).getPlaceId());
    }


    private void updateTransportModeIndicator(TransportMode currentTransportMode) {
        if (currentTransportMode == TransportMode.WALK) {
            disableModeDialog = true;
            transportModeTabLayout.getTabAt(TRANSPORT_MODE_WALK).select();
            disableModeDialog = false;

        } else {
            disableModeDialog = true;
            transportModeTabLayout.getTabAt(TRANSPORT_MODE_PT).select();
            disableModeDialog = false;
        }
    }


    /**
     * Add polyline to map using currentRoutes.
     * @param routes current route
     */
    private void addPolylinesToMap(final List<Route> routes) {
        new Handler(Looper.getMainLooper()).post(() -> {

            if (googleMap == null) {
                // just wait until next update
                return;
            }

            if (routes.size() < 1) {
                Toast.makeText(getContext(), "No routes found", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.generateGuideCards();

            PlaceInfo placeInfo = viewModel.currentDestination.getValue();

            googleMap.clear();

            // Change toolbar title depending on the selected destination
            ((TitleChangable) Objects.requireNonNull(getActivity()))
                .updateTitle(placeInfo.getName());

            if (userHaveControl || viewModel.currentUserIsAp) {
                // this calls onPrepareOptionsMenu to show favourite icon
                getActivity().invalidateOptionsMenu();
            }

            if (previousPolyline != null) {
                previousPolyline.remove();
            }
            if (previousDestinationMarker != null) {
                previousDestinationMarker.remove();
            }

            Route route = routes.get(0);
            Log.i(TAG, "addPolylinesToMap route: " + route.toString());

            /* process the route and extract the legs to display the polyline */
            List<com.google.maps.model.LatLng> decodedPath =
                    PolylineEncoding.decode(route.getRoutePolylineOverview()
                        .getOverViewPolyLinePoints());

            List<LatLng> newDecodedPath = new ArrayList<>();

            // This loops through all the LatLng coordinates of ONE polyline.
            for (com.google.maps.model.LatLng latLng : decodedPath) {
                newDecodedPath.add(new LatLng(
                    latLng.lat,
                    latLng.lng
                ));
            }



            try {
                String snippet = "Address: " + placeInfo.getAddress() + "\n"
                                 + "Phone Number: " + placeInfo.getPhoneNumber() + "\n"
                                 + "Website: " + placeInfo.getWebsiteUri() + "\n"
                                 + "Price Rating: " + placeInfo.getRating() + "\n";

                LatLng latLng = new LatLng(newDecodedPath.get(newDecodedPath.size() - 1).latitude,
                    newDecodedPath.get(newDecodedPath.size() - 1).longitude);

                marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(placeInfo.getName())
                    .snippet(snippet));

                previousDestinationMarker = marker;


            } catch (NullPointerException e) {
                Log.i(TAG, "snipet: NullPointerException: " + e.getMessage());
            }


            Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));


            // once a new route is shown, we will remove the offtrack dialog
            viewModel.apOffTrackDialogStillShown.setValue(false);

            previousPolyline = polyline;

            polyline.setColor(ContextCompat.getColor(getActivity(), R.color.colorBlue));

            showDuration(polyline, route);
            showFullRouteZoomOut(polyline.getPoints());

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void displayGuideCards(List<GuideCard> guideCards) {
        Toast.makeText(getContext(), "loading guide cards", Toast.LENGTH_LONG);
        // make a scroll view horizontal to display guide cards
        // display the guide cards in increasing order
        // have an observer of apLocation to swipe the view for
        // displaying the relevant guide card (tricky)

        GuideCardAdapter guideCardAdapter = new GuideCardAdapter(guideCards);
        guideCardsRecyclerView.setAdapter(guideCardAdapter);
        guideCardRecyclerViewLayoutManager = new LinearLayoutManager(getContext(),
            LinearLayoutManager.HORIZONTAL, false);
        guideCardsRecyclerView.setLayoutManager(guideCardRecyclerViewLayoutManager);

        // this helps to snap the view when swiping
        SnapHelper snapHelper = new PagerSnapHelper();
        guideCardsRecyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(guideCardsRecyclerView);


        // update the position of which guide card the carer is looking at

        previousGuideCard = 0;
        int firstSlide = previousGuideCard + 1;
        currentGuideCardIndicator.setText("( " + firstSlide + " / "
                                          + guideCards.size() + " )");

        guideCardsRecyclerView.setOnScrollChangeListener(
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int currentSnapPosition = guideCardRecyclerViewLayoutManager
                    .getPosition(snapHelper.findSnapView(guideCardRecyclerViewLayoutManager));

                if (previousGuideCard != currentSnapPosition) {
                    previousGuideCard = currentSnapPosition;
                    int currentSlide = previousGuideCard + 1;
                    currentGuideCardIndicator.setText("( " + currentSlide + " / "
                                                      + guideCards.size() + " )");
                }
            });

        currentGuideCardIndicator.setVisibility(View.VISIBLE);


        currentGuideCard = guideCards.get(0);
        Log.e(TAG, "guideCard" + guideCards.get(0).getEndLocation());
    }

    /**
     * Show current route duration as a pop up view.
     * @param polyline the whole polyline for current route
     * @param route the current route
     */
    private void showDuration(Polyline polyline, Route route) {

        // the legs of route is empty, cant show anything
        if (route.getRouteLegs().size() < 1) {
            return;
        }

        int routeLegSize = route.getRouteLegs().size() - 1; // usually 1
        Leg leg = route.getRouteLegs().get(routeLegSize);
        String legDuration = leg.getLegDuration().getText();

        String travelMode = viewModel.currentMode.getValue().toString().toLowerCase();

        LatLng midPoint = getPolylineCentroid(polyline);
        Marker polyMarker = googleMap.addMarker(new MarkerOptions()
            .position(midPoint)
            .title("Duration: " + "(" + travelMode + ")")
            .snippet(legDuration)
            .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.transparent))
            .anchor((float) 0.5, (float) 0.5));

        polyMarker.showInfoWindow();

    }

    /**
     * Get the midpoint of the route.
     * Used in showDuration.
     * @param p the polyline for current route
     * @return latLng mid point of polyline
     */
    private LatLng getPolylineCentroid(Polyline p) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < p.getPoints().size(); i++) {
            builder.include(p.getPoints().get(i));
        }

        LatLngBounds bounds = builder.build();
        return bounds.getCenter();
    }

    /**
     * Generate bitmapDescriptor from vector object.
     * Used to make popup view to display duration on map.
     * @param context context of app
     * @param vectorResId vector resource
     * @return bitmapDescriptor
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(),
            vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Zoom out or in to show the full route on map screen.
     * @param latLngRouteList list of all latLng of current route
     */
    private void showFullRouteZoomOut(List<LatLng> latLngRouteList) {
        if (googleMap == null || latLngRouteList == null || latLngRouteList.isEmpty()) {
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : latLngRouteList) {
            boundsBuilder.include(latLngPoint);
        }

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
            600,
            null
        );
    }


    /*
     * -------------------------------- OTHER HELPERS ----------------------------
     */

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
    }



    /*
     * ---------------------- LOCATION ENABLED FRAGMENT STUFF --------------------------
     */

    @Override
    protected void onLocationPermissionGranted() {
        initMap();
    }

    @Override
    protected void onLocationPermissionDenied() {
        getActivity().onBackPressed();
    }
}
