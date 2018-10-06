package com.comp30022.team_russia.assist.features.nav.vm;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.BindingAdapter;
import android.location.Location;
import android.view.View;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.nav.models.PlaceInfo;
import com.comp30022.team_russia.assist.features.nav.models.PlaceSuggestionItem;
import com.comp30022.team_russia.assist.features.nav.models.Route;
import com.comp30022.team_russia.assist.features.nav.models.TransportMode;
import com.comp30022.team_russia.assist.features.nav.service.NavigationService;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.NewNavControlPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewPositionPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewRoutePushNotification;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

/**
 * Navigation View Model.
 */
public class NavigationViewModel extends BaseViewModel {

    public boolean apInitiated;

    public boolean userStartedTyping;

    public boolean apLocationSyncedWithServer;

    public boolean getControlButtonVisible;

    public final MutableLiveData<String> currentSearchText = new MutableLiveData<>();

    public final MutableLiveData<PlaceInfo> currentDestination = new MutableLiveData<>();

    public final MutableLiveData<TransportMode> currentMode = new MutableLiveData<>();

    public final MutableLiveData<Boolean> carerHasControl = new MutableLiveData<>();

    public final MutableLiveData<LatLng> currentApLocation = new MutableLiveData<>();

    public final MutableLiveData<List<Route>> currentRoutes = new MutableLiveData<>();

    public final MutableLiveData<Boolean> routeIsSet = new MutableLiveData<>();

    public final MutableLiveData<Integer> assocId = new MutableLiveData<>();

    public final MutableLiveData<Boolean> navSessionStarted = new MutableLiveData<>();

    public final MutableLiveData<Boolean> navSessionEnded = new MutableLiveData<>();


    /* local variables */
    private final MutableLiveData<Integer> currentSessionId = new MutableLiveData<>();

    private final MutableLiveData<String> currentPlaceId = new MutableLiveData<>();

    private final MutableLiveData<String> currentAddress = new MutableLiveData<>();

    private final GoogleApiClient googleApiClient;

    private final Application russiaApp;

    private final NavigationService navigationService;

    private final PubSubHub pubSubHub;

    private final LoggerInterface logger;

    private final ToastService toastService;

    private final Gson gson = new Gson();

    private Disposable notificationSubscription;


    @Inject
    public NavigationViewModel(Application appContext,
                               NavigationService navigationService,
                               PubSubHub notificationHub,
                               ToastService toastService,
                               LoggerFactory loggerFactory) {

        this.russiaApp = appContext;
        this.navigationService = navigationService;
        this.pubSubHub = notificationHub;
        this.logger = loggerFactory.create(this.getClass().getSimpleName());
        this.toastService = toastService;

        // establish a google api client and connect it
        googleApiClient = new GoogleApiClient
            .Builder(russiaApp.getApplicationContext())
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .addOnConnectionFailedListener(connectionResult ->
                logger.error("Google api client Connection FAILED"))
            .build();

        googleApiClient.connect();


        // initial values
        userStartedTyping = false;
        navSessionStarted.setValue(false);
        routeIsSet.setValue(false);
        apLocationSyncedWithServer = false;

        currentSearchText.setValue("");
        currentMode.setValue(TransportMode.WALK);


        /* ----------- Listen to Firebase Notification ------------  */

        // Listener for new route
        this.pubSubHub.configureTopic(PubSubTopics.NEW_ROUTE, null,
            new PayloadToObjectConverter<NewRoutePushNotification>() {
                @Override
                public NewRoutePushNotification fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, NewRoutePushNotification.class);
                }

                @Override
                public String toString(NewRoutePushNotification payload) {
                    return null;
                }
            });

        this.notificationSubscription = pubSubHub.subscribe(PubSubTopics.NEW_ROUTE,
            new SubscriberCallback<NewRoutePushNotification>() {
                @Override
                public void onReceived(NewRoutePushNotification payload) {
                    if (payload.getSessionId() == currentSessionId.getValue()) {
                        getRoutes();
                        logger.error("firebase notification received for new route");
                    }
                }
            });


        // Listener for new AP location
        this.pubSubHub.configureTopic(PubSubTopics.NEW_AP_LOCATION,
            NewPositionPushNotification.class,
            new PayloadToObjectConverter<NewPositionPushNotification>() {
                @Override
                public NewPositionPushNotification fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, NewPositionPushNotification.class);
                }

                @Override
                public String toString(NewPositionPushNotification payload) {
                    return null;
                }
            });

        this.notificationSubscription = pubSubHub.subscribe(PubSubTopics.NEW_AP_LOCATION,
            new SubscriberCallback<NewPositionPushNotification>() {
                @Override
                public void onReceived(NewPositionPushNotification payload) {
                    if (payload.getSessionId() == currentSessionId.getValue()) {
                        LatLng latLng = new LatLng(payload.getLat(), payload.getLon());
                        currentApLocation.postValue(latLng);
                        logger.error("firebase notification received for new ap location");
                    }
                }
            });

        // Listener for change of navigation control
        this.pubSubHub.configureTopic(PubSubTopics.NAV_CONTROL_SWTICH,
            NewNavControlPushNotification.class,
            new PayloadToObjectConverter<NewNavControlPushNotification>() {
                @Override
                public NewNavControlPushNotification fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, NewNavControlPushNotification.class);
                }

                @Override
                public String toString(NewNavControlPushNotification payload) {
                    return null;
                }
            });

        this.notificationSubscription = pubSubHub.subscribe(PubSubTopics.NAV_CONTROL_SWTICH,
            new SubscriberCallback<NewNavControlPushNotification>() {
                @Override
                public void onReceived(NewNavControlPushNotification payload) {
                    if (payload.getSessionId() == currentSessionId.getValue()) {
                        carerHasControl.postValue(payload.getCarerHasControl());
                        logger.error("firebase notification received for switching control");
                    }
                }
            });

    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    /**
     * Get a navigation session associated to the current association id.
     */
    public void getNavigationSession() {

        /* Check for current nav session */
        logger.error("startNavigationSession: trying to get existing session...");
        navigationService.getCurrentNavigationSession().thenAccept(existingIdResult -> {
            if (existingIdResult.isSuccessful() && existingIdResult.unwrap().getId() > 0) {

                logger.error("startNavigationSession: successfully get existing nav session");
                this.currentSessionId.postValue(existingIdResult.unwrap().getId());
                navSessionStarted.postValue(true);
                carerHasControl.postValue(existingIdResult.unwrap().getCarerHasControl());

                if (! existingIdResult.unwrap().getRoute().getDirectionsRoutes().isEmpty()) {
                    currentRoutes.postValue(existingIdResult.unwrap().getRoute()
                        .getDirectionsRoutes());
                    routeIsSet.setValue(true);
                }

                if (convertStringToTransportMode(existingIdResult.unwrap().getTransportMode())
                    != null) {
                    currentMode.postValue(convertStringToTransportMode(
                        existingIdResult.unwrap().getTransportMode()));
                }

                logger.error("startNavigationSession existing session id: "
                           + currentSessionId.getValue());

            } else {
                logger.error("startNavigationSession: failed to get existing nav session");

                /* Start new nav session */
                startNewNavSession();
            }
        });
    }

    private void startNewNavSession() {
        navigationService.createNewNavigationSession(assocId.getValue()).thenAccept(newIdResult -> {
            logger.error("startNavigationSession: trying to start new session...");
            if (newIdResult.isSuccessful() && newIdResult.unwrap().getId() > 0) {
                logger.error("startNavigationSession: successfully started new nav session");

                this.currentSessionId.postValue(newIdResult.unwrap().getId());
                navSessionStarted.postValue(true);
                carerHasControl.postValue(newIdResult.unwrap().getCarerHasControl());

                if (! newIdResult.unwrap().getRoute().getDirectionsRoutes().isEmpty()) {
                    currentRoutes.postValue(newIdResult.unwrap().getRoute().getDirectionsRoutes());
                    routeIsSet.setValue(true);
                }


                if (convertStringToTransportMode(newIdResult.unwrap().getTransportMode())
                    != null) {
                    currentMode.postValue(convertStringToTransportMode(
                        newIdResult.unwrap().getTransportMode()));
                }

                logger.error("startNavigationSession new session id: "
                             + currentSessionId.getValue());


            } else {
                logger.error("startNavigationSession: failed to get new nav session");
            }
        });
    }


    private TransportMode convertStringToTransportMode(String transportMode) {
        if (transportMode.equals("Walking")) {
            return TransportMode.WALK;
        }
        if (transportMode.equals("PT")) {
            return TransportMode.PUBLIC_TRANSPORT;
        }
        return null;
    }

    /**
     * Set destination of current nav session on server
     * Note: spelling of currentMode is important (for server)
     * Eg. "Walking", "PT"
     */
    public void setDestination() {

        if (!navSessionStarted.getValue()) {
            logger.error("setDestination navSessionStarted" + navSessionStarted.getValue());
            getNavigationSession();
            setDestination();
            return;
        } else if (currentSessionId.getValue() == null) {
            logger.error("setDestination currentSessionId is null");
            return;
        } else if (currentPlaceId.getValue() == null) {
            logger.error("setDestination currentPlaceId is null");
            return;
        } else if (currentAddress.getValue() == null) {
            logger.error("setDestination currentAddress is null");
            return;
        }

        // Only set destination if navigation session has started
        if (navSessionStarted.getValue()) {

            // Tell server that destination is using walking mode
            if (this.currentMode.getValue() == TransportMode.WALK) {
                logger.error("setDestination setting destination on server walk mode "
                             + currentMode.getValue().toString());

                navigationService.setDestination(this.currentSessionId.getValue(),
                    this.currentPlaceId.getValue(), this.currentAddress.getValue(), "Walking")
                    .thenAccept(result -> {
                        if (result.isSuccessful()) {
                            // do nothing, route is shown on screen
                        } else {
                            toastService.toastShort("Could not set destination, try again");
                        }
                    });
            } else if (this.currentMode.getValue() == TransportMode.PUBLIC_TRANSPORT) {
                // Tell server that destination is using public transport mode
                logger.error("setDestination setting destination on server public mode "
                             + currentMode.getValue().toString());

                navigationService.setDestination(this.currentSessionId.getValue(),
                    this.currentPlaceId.getValue(), this.currentAddress.getValue(), "PT")
                    .thenAccept(result -> {
                        if (result.isSuccessful()) {
                            // do nothing, route is shown on screen
                        } else {
                            toastService.toastShort("Could not set destination, try again");
                        }
                    });
            }

        } else {
            logger.error("setDestination: ERROR setting destination "
                         + "navSessionStarted " + navSessionStarted.getValue()
                         + " currentSessionId: " + currentSessionId.getValue());
        }
    }


    /**
     * Get the route to current destination after firebase notifies us.
     */
    private void getRoutes() {
        navigationService.getRoute(this.currentSessionId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                if (! result.unwrap().isEmpty()) {
                    logger.error("getRoutes successful");
                    this.currentRoutes.postValue(result.unwrap());
                    routeIsSet.postValue(true);
                }
                logger.error("getRoutes currentRoute is: " + currentRoutes.getValue());
            }
        });
    }


    /**
     * End nav session.
     */
    private void endNavSession() {
        if (currentSessionId.getValue() != null) {
            navigationService.endNavigationSession(this.currentSessionId.getValue())
                .thenAccept(result -> {
                    if (result.isSuccessful()) {
                        toastService.toastShort("Successfully ended nav session "
                                                + currentSessionId.getValue());
                    } else {
                        toastService.toastShort("Error ending nav session "
                                                + currentSessionId.getValue());
                    }
                });
            logger.error("endNavSession (startNavigationSession) exited nav session id: "
                         + currentSessionId.getValue());
        } else {
            logger.error("endNavSession: session id is null");
        }
    }


    /**
     * Update location of AP to server.
     */
    public void updateApLocation(LatLng latLng) {
        logger.error("updateApLocation entered");
        // Todo: Temp patch for crash
        if (currentSessionId.getValue() == null) {
            return;
        }

        if (latLng == null) {
            logger.error("updateApLocation error latLng is null");
            return;
        }

        // server already has history of ap location
        if (apLocationSyncedWithServer) {
            Location newLocation = new Location("");
            newLocation.setLatitude(latLng.latitude);
            newLocation.setLongitude(latLng.longitude);
            Location currentLocation = new Location("");
            currentLocation.setLatitude(currentApLocation.getValue().latitude);
            currentLocation.setLongitude(currentApLocation.getValue().longitude);

            /* Only update ap location to server if distance is more than 1m difference */
            if (newLocation.distanceTo(currentLocation) < 1) {
                logger.error("updateApLocation: ERROR ap location is only "
                             + "1m different from previous");
                return;
            } else {
                updateApLocationToServer(latLng);
            }
        } else {
            // server has no history of ap location
            getNavigationSession();
            updateApLocationToServer(latLng);
            apLocationSyncedWithServer = true;
        }
    }

    private void updateApLocationToServer(LatLng latLng) {
        logger.error("updateApLocation updating ap location to server...");
        currentApLocation.postValue(latLng);
        navigationService.updateCurrentLocation(currentSessionId.getValue(), latLng)
            .thenAccept(result -> {
                if (result.isSuccessful()) {
                    toastService.toastLong("Updated server of new ap location");
                } else {
                    toastService.toastShort("Error updating location of ap");
                }
            });
    }


    /**
     * Get location of AP from server for updating Carer's screen.
     */
    public LatLng getApLocation() {
        // Todo: Temp patch for crash
        if (currentSessionId.getValue() == null) {
            return null;
        }
        navigationService.getCurrentLocation(currentSessionId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.error("getApLocation successfully retrieved AP location");
                this.currentApLocation.postValue(new LatLng(result.unwrap().latitude,
                    result.unwrap().longitude));
                logger.error("getApLocation currentApLocation: " + currentApLocation.getValue());
            } else {
                logger.error("getApLocation failed to retrieved AP location");
            }
        });

        return currentApLocation.getValue();
    }


    /**
     * Toggle the status of favourite.
     */
    public void toggleFavoriteStatus() {
        //todo (iter3): set/unset favorite state of destination
    }



    /**
     * Handles sending destination to server when a destination is selected on the list.
     * @param item The {@PlaceSuggestionItem} being clicked.
     */
    public void onSuggestionClicked(PlaceSuggestionItem item) {
        final String placeId = item.getGoogleMapsPlaceId();

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
            .getPlaceById(googleApiClient, placeId);

        placeResult.setResultCallback(places -> {
            if (!places.getStatus().isSuccess()) {
                logger.error("onResult: Place query did not complete successfully: "
                          + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {
                logger.error("onSuggestionClicked: Entered try block");

                PlaceInfo placeInfo = PlaceInfo.fromGoogleApiPlace(place);
                NavigationViewModel.this.currentDestination.setValue(placeInfo);

                this.currentPlaceId.setValue(placeId);
                this.currentAddress.setValue(place.getAddress().toString());

                //send to server the destination selected
                this.setDestination();

                logger.error("onResult: selected destination: " + placeInfo.toString());

            } catch (NullPointerException e) {
                logger.error("onResult: NullPointerException: " + e.getMessage());
            }

            places.release();
        });
    }


    /**
     * Handle switch control button clicked.
     */
    public void onGainControlButtonClicked() {
        navigationService.switchControl(this.currentSessionId.getValue());
    }


    /**
     * Switch control of user.
     */
    private void switchNavControl() {
        if (this.getControlButtonVisible) {
            this.carerHasControl.postValue(true);
            this.getControlButtonVisible = false;
        }
    }


    /**
     * Handle start call button clicked.
     */
    public void onStartCallButtonClicked() {

    }


    /**
     * Handle show back camera button clicked.
     */
    public void onShowRearCameraButtonClicked() {

    }


    /**
     * Handle confirm route button clicked.
     */
    public void onConfirmRouteButtonClicked() {
        // show guide cards
    }


    /**
     * Gets called when view model is destroyed.
     */
    @Override
    protected void onCleared() {
        // always clean up the subscriptions in LiveModels, to prevent leaking.
        if (this.notificationSubscription != null) {
            this.notificationSubscription.dispose();
        }
        googleApiClient.disconnect();
    }

    /**
     * Handle close navigation session button clicked.
     */
    public void onCloseNavSessionButtonClicked() {
        navSessionEnded.postValue(true);
        endNavSession();
    }

    /**
     * Convoluted way to handle visibility of button used because its the only way.
     * - Used in fragment_navigation_map.xml
     *
     * @param view The {@View} to change visibility for.
     * @param visible To show or hide.
     */
    @BindingAdapter({"visibility"})
    public static void setVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}