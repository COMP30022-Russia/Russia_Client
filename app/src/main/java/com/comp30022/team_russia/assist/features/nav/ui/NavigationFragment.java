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
import android.widget.TextView;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentNavigationMapBinding;
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

    private Boolean switchBack = false;

    private Boolean isFav = true;

    // used to move the camera only once in the beginning after getting ap location
    private Boolean showApLocation = false;


    /* widgets */
    private AutoCompleteTextView searchText;

    private ImageView recenterButton;

    private TextView controlStatusTextView;

    private TabLayout mapTabLayout;

    private MenuItem favSelectedItem;

    private MenuItem favUnselectedItem;

    private Button getControlButton;

    private MapView mapView;



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
            this.googleMap.setMyLocationEnabled(true);
            this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            this.googleMap.getUiSettings().setZoomControlsEnabled(true);

            init();
        }
    }

    private void init() {
        Log.e(TAG, "init: initializing");

        searchText.setOnItemClickListener((adapterView, view, i, l) -> {
            hideSoftKeyboard();

            final PlaceSuggestionItem item = placeAutocompleteAdapter.getItem(i);
            viewModel.onSuggestionClicked(item);
        });

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(), this.viewModel);

        searchText.setAdapter(placeAutocompleteAdapter);

        recenterButton.setOnClickListener(view -> {
            Log.e(TAG, "onClick: clicked gps icon");
            getLastDeviceLocation();
            moveCamera(viewModel.currentApLocation.getValue(), DEFAULT_ZOOM, null);
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

        viewModel.getNavigationSession();

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_navigation_map, container, false);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getControlButton = view.findViewById(R.id.get_control_button);

        // show get control button if user is Carer
        if (!viewModel.apInitiated) {
            getControlButton.setVisibility(View.VISIBLE);
            viewModel.getControlButtonVisible = true;
        } else {
            viewModel.getControlButtonVisible = false;
        }

        searchText = view.findViewById(R.id.input_search);
        searchText.setThreshold(1); // specify the minimum no. of char before list is shown

        searchText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                viewModel.userStartedTyping = true;
            }
        });

        recenterButton = view.findViewById(R.id.ic_gps);
        controlStatusTextView = view.findViewById(R.id.control_status);

        mapTabLayout = view.findViewById(R.id.map_tab_layout);

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        wireUpListenEvents();

        /* check location permission allowed by user */
        getLocationPermission();
    }

    private void wireUpListenEvents() {
        Log.e(TAG, "wireUpListenEvents entered");
        viewModel.navSessionStarted.observe(this, this::updateServerApLocation);
        viewModel.currentApLocation.observe(this, this::refreshApLocation);
        viewModel.currentDestination.observe(this, this::onDestinationSelected);
        viewModel.currentRoutes.observe(this, this::addPolylinesToMap);
        //viewModel.carerHasControl.observe(this, this::refreshMapControl);
        viewModel.navSessionEnded.observe(this, this::endNavSession);
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
     * Event: on screen load OR on recenter pressed
     */
    private void getLastDeviceLocation() {
        if (viewModel.apInitiated) {
            // user type is ap
            Log.e(TAG, "getLastDeviceLocation finding location for AP");
            if (locationPermissionsGranted) {
                getApLocationHelper();
            } else {
                Log.e(TAG, "getLastDeviceLocation: no location permissions");
            }

        } else {
            // user type is carer
            Log.e(TAG, "getLastDeviceLocation finding location for Carer");
            //ask server for last location of ap
            LatLng apLocation = viewModel.getApLocation();
            if (apLocation != null) {

                Log.e(TAG, "getLastDeviceLocation ap location is: " + apLocation.toString());
                if (!showApLocation && ! viewModel.routeIsSet.getValue()) {
                    moveCamera(apLocation, DEFAULT_ZOOM, null);
                    showApLocation = true;
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

                        if (!showApLocation && ! viewModel.routeIsSet.getValue()) {
                            moveCamera(newApLocation, DEFAULT_ZOOM, null);
                            showApLocation = true;
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
                    if (!showApLocation && ! viewModel.routeIsSet.getValue()) {
                        moveCamera(newApLocation, DEFAULT_ZOOM, null);
                        showApLocation = true;
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


    private void updateServerApLocation(Boolean navSessionStarted) {
        if (navSessionStarted) {
            viewModel.updateApLocation(viewModel.currentApLocation.getValue());
        }

    }

    /*
     * ------------------------------------ UPDATE VIEW -------------------------------------
     */

    private void refreshApLocation(LatLng newApLocation) {
        Log.e(TAG, "refreshApLocation: updating marker position of ap");
        // todo update Marker of ap
    }

    private void refreshMapControl(Boolean carerHasControl) {
        // todo need to FIX, not sure if apInitiated is current user or not
        // todo how to disable control?
        // todo hide controlStatusTextView

        // if current user is ap
        if (viewModel.apInitiated) {
            if (carerHasControl) {
                //todo: disable ap's control && enable carer's control
                // show status
                controlStatusTextView.setVisibility(View.VISIBLE);
            } else {
                // hide status
                controlStatusTextView.setVisibility(View.GONE);
            }
        }

        // if current user is carer
        if (!viewModel.apInitiated) {
            if (carerHasControl) {
                //todo: disable carer's control && enable ap's control
                // show button
                getControlButton.setVisibility(View.GONE);
                // hide status
                controlStatusTextView.setVisibility(View.VISIBLE);
            } else {
                // show button
                getControlButton.setVisibility(View.VISIBLE);
                // hide status
                controlStatusTextView.setVisibility(View.GONE);
            }
        }
    }

    /*
     * -------------------------------- ON DESTINATION SET STUFF ----------------------------
     */


    private void onDestinationSelected(PlaceInfo placeInfo) {
        Log.e(TAG, "onDestinationSelected");
        if (placeInfo == null) {
            Log.e(TAG, "onDestinationSelected place info is NULL");
            // clear pin
        } else {
            Log.e(TAG, "onDestinationSelected place info is not null");
            LatLng latLng = placeInfo.getLatlng();
            moveCamera(latLng, DEFAULT_ZOOM, placeInfo);

            // Change toolbar title depending on the selected destination
            ((TitleChangable) Objects.requireNonNull(getActivity()))
                .updateTitle(placeInfo.getName());
            getActivity().invalidateOptionsMenu(); // this calls onPrepareOptionsMenu

        }
    }

    /**
     * Recentre the map view around to the location provided.
     *
     * @param latLng The target position to be centred on.
     * @param zoom The requested zoom level.
     * @param placeInfo The place info of the target. Used to display info on a Marker.
     */
    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        Log.e(TAG, "moveCamera: moving the camera to: lat: "
                   + latLng.latitude + ", lng: " + latLng.longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        // googleMap.clear(); // dont clear the map

        if (placeInfo != null) {
            try {
                String snippet = "Address: " + placeInfo.getAddress() + "\n"
                                 + "Phone Number: " + placeInfo.getPhoneNumber() + "\n"
                                 + "Website: " + placeInfo.getWebsiteUri() + "\n"
                                 + "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(placeInfo.getName())
                    .snippet(snippet);
                marker = googleMap.addMarker(options);

            } catch (NullPointerException e) {
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage());
            }
        } else {
            googleMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }


    /*
     * ---------------------- SHOW ROUTE ON VIEW STUFF --------------------------
     */

    /**
     * Add polyline to map using currentRoutes.
     */
    private void addPolylinesToMap(final List<Route> routes) {
        new Handler(Looper.getMainLooper()).post(() -> {

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

            Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
            polyline.setColor(ContextCompat.getColor(getActivity(), R.color.colorBlue));

            showDuration(polyline, route);
            zoomRoute(polyline.getPoints());

        });
    }

    /* show current route duration as a pop up view */
    private void showDuration(Polyline polyline, Route route) {

        int lastLeg = route.getRouteLegs().size() - 1;
        String totalDuration = route.getRouteLegs().get(lastLeg).getLegDuration().getText();
        String travelMode = route.getRouteLegs().get(lastLeg).getLegSteps().get(0)
            .getStepTravelMode().toLowerCase();

        LatLng midPoint = getPolylineCentroid(polyline);
        Marker polyMarker = googleMap.addMarker(new MarkerOptions()
            .position(midPoint)
            .title("Duration: " + "(" + travelMode + ")")
            .snippet(totalDuration)
            .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.transparent))
            .anchor((float) 0.5, (float) 0.5));

        polyMarker.showInfoWindow();
    }

    /* get the centre of the route: used in showDuration */
    private LatLng getPolylineCentroid(Polyline p) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < p.getPoints().size(); i++) {
            builder.include(p.getPoints().get(i));
        }

        LatLngBounds bounds = builder.build();

        return bounds.getCenter();
    }

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

    private void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) {
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute) {
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

    private void endNavSession(Boolean navigationEnded) {
        if (navigationEnded) {
            getActivity().onBackPressed();
        }
    }

}
