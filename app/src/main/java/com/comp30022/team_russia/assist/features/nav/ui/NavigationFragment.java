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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
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

    private Handler handler = new Handler();
    // TODO: temporary fix for updating location of ap to server
    private static final int DELAY = 1 * 1000; // 5 seconds
    private Runnable runnable;

    /* vars */
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private static final String TAG = "NavigationFragment";

    private static final float DEFAULT_ZOOM = 18f;

    private static final long UPDATE_INTERVAL = 4000;

    private static final long FASTEST_INTERVAL = 2000;

    public final MutableLiveData<String> title = new MutableLiveData<>();

    private NavigationViewModel viewModel;

    private FragmentNavigationMapBinding binding;

    private GoogleMap googleMap;

    private PlaceAutocompleteAdapter placeAutocompleteAdapter;

    private Marker marker;

    private GeoApiContext geoApiContext;

    private Boolean userHaveControl;

    private Boolean switchBack = false;

    private Boolean isFav = true;

    // used to move the camera only once in the beginning after getting ap location
    private Boolean shownApLocation = false;

    private Polyline previousPolyline = null;

    private Marker previousDestinationMarker = null;

    private Marker previousApMarker = null;


    /* widgets */
    private AutoCompleteTextView searchText;

    private RelativeLayout searchBox;

    private ImageView recenterButton;

    private ImageView clearSearchButton;

    private ImageView endNavSessionButton;

    private TextView controlStatusTextView;

    private TabLayout mapTabLayout;

    private MenuItem favSelectedItem;

    private MenuItem favUnselectedItem;

    private Button getControlButton;

    private MapView mapView;

    private RecyclerView guideCardsRecyclerView;

    private ImageView zoomOutButton;

    private ImageView zoomInButton;



    /*
     * -------------------------------- INITIALISING GOOGLE MAP STUFF ----------------------------
     */

    /**
     * Google API required method, provides the map when ready.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "onMapReady: map is ready");
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
        Log.e(TAG, "initMap: initializing map");
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

        Log.e(TAG, "onCreateView assocId " + viewModel.assocId.getValue());
        Log.e(TAG, "onCreateView apInitiated " + viewModel.apInitiated);

        viewModel.getNavigationSession();

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_navigation_map, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

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

        mapTabLayout = view.findViewById(R.id.map_tab_layout);

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        guideCardsRecyclerView = view.findViewById(R.id.guide_card_recyclerview);

        wireUpListenEvents();

        /* check location permission allowed by user */
        getLocationPermission();

        initiateMapUiControl();
    }

    private void wireUpListenEvents() {
        Log.e(TAG, "wireUpListenEvents entered");
        viewModel.navSessionStarted.observe(this, this::updateServerApLocation);
        viewModel.currentApLocation.observe(this, this::refreshApLocation);
        viewModel.currentDirections.observe(this, this::updateDestinationDetails);
        viewModel.currentRoutes.observe(this, this::addPolylinesToMap);
        viewModel.currentGuideCards.observe(this, this::displayGuideCards);
        viewModel.carerHasControl.observe(this, this::refreshMapUiControl);
        viewModel.navSessionEnded.observe(this, this::endNavSession);
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
        Log.e(TAG, "init: initializing");

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
            Log.e(TAG, "onClick: clicked gps icon");
            getLastDeviceLocation();
            moveCamera(viewModel.currentApLocation.getValue(), DEFAULT_ZOOM);
        });

        mapTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!switchBack) {
                    if (tab.getPosition() == 0) {
                        // walk_mode is selected
                        Log.e(TAG, "setDestination walk mode selected");
                        showModeChangeConfirmDialog("walk", 1, TransportMode.WALK);
                    } else if (tab.getPosition() == 1) {
                        // public_mode is selected
                        Log.e(TAG, "setDestination public mode selected");
                        showModeChangeConfirmDialog("public transport", 0,
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
                viewModel.currentMode.postValue(mode);
                viewModel.setDestination();
            });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
            (dialog, which) -> {
                dialog.dismiss();
                switchBack = true;
                mapTabLayout.getTabAt(otherTab).select();
                switchBack = false;
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
            Log.e(TAG, "getLastDeviceLocation finding location for AP");
            if (locationPermissionsGranted) {
                getApLocationHelper();
            } else {
                Log.e(TAG, "getLastDeviceLocation: no location permissions");
            }

        } else { // user type is carer
            Log.e(TAG, "getLastDeviceLocation finding location for Carer");
            //ask server for last location of ap
            LatLng apLocation = viewModel.getApLocation();
            if (apLocation != null) {

                Log.e(TAG, "getLastDeviceLocation ap location is: " + apLocation.toString());
                if (!shownApLocation && ! viewModel.routeIsSet.getValue()) {
                    moveCamera(apLocation, DEFAULT_ZOOM);
                    shownApLocation = true;
                }

            } else {
                Log.e(TAG, "getLastDeviceLocation ap location is null");
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
                        Log.e(TAG, "getApLocationHelper: currentLocation is: "
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
                        Log.e(TAG, "getApLocationHelper: currentLocation is NULL!");
                        requestLocation(fusedLocationProviderClient);
                    }
                } else {
                    Log.e(TAG, "getApLocationHelper: not successful");
                }
            });

        } catch (SecurityException e) {
            Log.e(TAG, "getApLocation: SecurityException: " + e.getMessage());
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

                    Log.e(TAG, "requestLocation: newApLocation is: "
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
     * Update location of ap on server.
     * Called by Ap Device.
     * @param navSessionStarted boolean for nav session started
     */
    private void updateServerApLocation(Boolean navSessionStarted) {
        if (navSessionStarted && viewModel.currentUserIsAp) {
            viewModel.updateApLocation(viewModel.currentApLocation.getValue());
        }
    }

    /**
     * Gets called when ap location changes are received from server.
     * Called by Carer Device.
     * @param newApLocation new location of ap
     */
    private void refreshApLocation(LatLng newApLocation) {

        // check if ap deviated from path
        if (viewModel.currentUserIsAp) {
            return;
            /*
            if (!PolyUtil.isLocationOnPath(newApLocation, previousPolyline.getPoints(),
                true, 10.0)) {
                // todo notify that user is off track
            }

            if (true) {
                // todo check for distance to current guidecard's latlng
                // get currrent guide card's latLng and compare to newApLocation
            }

            List<LatLng> latLngs = new ArrayList<>();
            latLngs.add(previousPolyline.getPoints().get(previousPolyline.getPoints().size()));
            if (PolyUtil.isLocationOnEdge(newApLocation, latLngs, true, 1)) {
                // todo ap reached destination within 1m

            }

            return;
            */
        }


        Log.e(TAG, "refreshApLocation: updating marker position of ap");
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
        mapTabLayout.setVisibility(View.VISIBLE);
        endNavSessionButton.setVisibility(View.VISIBLE);
        getControlButton.setVisibility(View.GONE);
        controlStatusTextView.setVisibility(View.GONE);
    }

    /**
     * Disable UI control for the device.
     */
    private void deactivateUi() {
        searchBox.setVisibility(View.GONE);
        mapTabLayout.setVisibility(View.GONE);
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
        Log.e(TAG, "moveCamera: moving the camera to: lat: "
                   + latLng.latitude + ", lng: " + latLng.longitude);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!userHaveControl && !viewModel.currentUserIsAp) {
            googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_AZURE)));

        } else if (userHaveControl && viewModel.currentUserIsAp) {
            // do nothing

        } else {
            if (previousDestinationMarker != null) {
                previousDestinationMarker.remove();
            }
            previousDestinationMarker =
                googleMap.addMarker(new MarkerOptions().position(latLng));
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


    /**
     * Add polyline to map using currentRoutes.
     * @param routes current route
     */
    private void addPolylinesToMap(final List<Route> routes) {
        new Handler(Looper.getMainLooper()).post(() -> {

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
            Log.e(TAG, "addPolylinesToMap route: " + route.toString());

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
                Log.e(TAG, "snipet: NullPointerException: " + e.getMessage());
            }


            Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));

            previousPolyline = polyline;

            polyline.setColor(ContextCompat.getColor(getActivity(), R.color.colorBlue));

            showDuration(polyline, route);
            showFullRouteZoomOut(polyline.getPoints());

        });
    }

    private void displayGuideCards(List<GuideCard> guideCards) {
        Toast.makeText(getContext(), "loading guide cards", Toast.LENGTH_LONG);
        // make a scroll view horizontal to display guide cards
        // display the guide cards in increasing order
        // have an observer of apLocation to swipe the view for
        // displaying the relevant guide card (tricky)

        GuideCardAdapter guideCardAdapter = new GuideCardAdapter(guideCards);
        guideCardsRecyclerView.setAdapter(guideCardAdapter);
        guideCardsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
            LinearLayoutManager.HORIZONTAL, false));
        SnapHelper snapHelper = new PagerSnapHelper();
        guideCardsRecyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(guideCardsRecyclerView);

        // just for logging
        int i = 0;
        for (GuideCard guideCard : guideCards) {
            Log.e(TAG,"displayGuideCards " + i + " " + guideCard.toString());
            i++;
        }
    }

    /**
     * Show current route duration as a pop up view.
     * @param polyline the whole polyline for current route
     * @param route the current route
     */
    private void showDuration(Polyline polyline, Route route) {

        // todo might be an issue if its not 1
        int routeLegSize = route.getRouteLegs().size() - 1; // usually 1
        Leg leg = route.getRouteLegs().get(routeLegSize);
        String legDuration = leg.getLegDuration().getText();

        String travelMode = leg.getLegSteps().get(0).getStepTravelMode().toLowerCase();

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
