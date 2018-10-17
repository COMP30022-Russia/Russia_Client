package com.comp30022.team_russia.assist.features.nav.vm;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.jitsi.services.VoiceCoordinator;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.nav.models.Directions;
import com.comp30022.team_russia.assist.features.nav.models.GuideCard;
import com.comp30022.team_russia.assist.features.nav.models.Leg;
import com.comp30022.team_russia.assist.features.nav.models.PlaceInfo;
import com.comp30022.team_russia.assist.features.nav.models.PlaceSuggestionItem;
import com.comp30022.team_russia.assist.features.nav.models.Recents;
import com.comp30022.team_russia.assist.features.nav.models.Route;
import com.comp30022.team_russia.assist.features.nav.models.TransportMode;
import com.comp30022.team_russia.assist.features.nav.services.NavigationService;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.NewGenericPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewNavControlPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewPositionPushNotification;
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Navigation View Model.
 */
public class NavigationViewModel extends BaseViewModel {

    /* Only update ap location to server if distance is more than 0.5m difference */
    private static final double UPDATE_DISTANCE_BUFFER = 0.5;

    public boolean apInitiated;

    public boolean userStartedTyping;

    public boolean apLocationSyncedWithServer;

    public boolean currentUserIsAp;

    private int currentUserId;

    public List<Recents> recentDestinations = new ArrayList<>();

    public final MutableLiveData<String> currentSearchText = new MutableLiveData<>();

    public final MutableLiveData<PlaceInfo> currentDestination = new MutableLiveData<>();

    public final MutableLiveData<TransportMode> currentMode = new MutableLiveData<>();

    public final MutableLiveData<Boolean> carerHasControl = new MutableLiveData<>();

    public final MutableLiveData<LatLng> currentApLocation = new MutableLiveData<>();

    public final MutableLiveData<List<Route>> currentRoutes = new MutableLiveData<>();

    public final MutableLiveData<Directions> currentDirections = new MutableLiveData<>();

    public final MutableLiveData<List<GuideCard>> currentGuideCards = new MutableLiveData<>();

    public final MutableLiveData<Boolean> routeIsSet = new MutableLiveData<>();

    public final MutableLiveData<Boolean> navSessionStarted = new MutableLiveData<>();

    public final MutableLiveData<Boolean> navSessionEnded = new MutableLiveData<>();

    public final MutableLiveData<Boolean> apIsOffTrack = new MutableLiveData<>();

    public final MutableLiveData<Boolean> apOffTrackDialogStillShown = new MutableLiveData<>();

    /**
     * Sub-ViewModel for voice call related UI logic.
     */
    public final NavVoiceCallViewModel voiceCallVm;


    /* local variables */
    public final MutableLiveData<Integer> assocId = new MutableLiveData<>();

    private final MutableLiveData<Integer> currentSessionId = new MutableLiveData<>();

    private final MutableLiveData<String> currentPlaceId = new MutableLiveData<>();

    private final MutableLiveData<String> currentAddress = new MutableLiveData<>();

    private final GoogleApiClient googleApiClient;

    private final Application russiaApp;

    private final AuthService authService;

    private final NavigationService navigationService;

    private final PubSubHub pubSubHub;

    private final LoggerInterface logger;

    private final ToastService toastService;

    private final Gson gson = new Gson();

    private Disposable notificationSubscription;


    /**
     * View Model for NavigationFragment.
     * @param appContext context of app
     * @param authService authentication service
     * @param navigationService navigation service
     * @param notificationHub firebase notification
     * @param toastService toast service
     * @param loggerFactory logging service
     */
    @Inject
    public NavigationViewModel(Application appContext,
                               AuthService authService,
                               NavigationService navigationService,
                               PubSubHub notificationHub,
                               ToastService toastService,
                               VoiceCoordinator voiceCoordinator,
                               LoggerFactory loggerFactory) {

        this.russiaApp = appContext;
        this.authService = authService;
        this.navigationService = navigationService;
        this.pubSubHub = notificationHub;
        this.logger = loggerFactory.getLoggerForClass(this.getClass());
        this.toastService = toastService;

        voiceCallVm = new NavVoiceCallViewModel(pubSubHub,
            loggerFactory,
            voiceCoordinator,
            navigationService,
            toastService);

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
        currentUserId = authService.getCurrentUser().getUserId();
        currentUserIsAp = (authService.getCurrentUser().getUserType() == User.UserType.AP);
        userStartedTyping = false;
        navSessionStarted.setValue(false);
        routeIsSet.setValue(false);
        apLocationSyncedWithServer = false;
        apIsOffTrack.setValue(false);
        apOffTrackDialogStillShown.setValue(false);

        currentSearchText.setValue("");
        currentMode.setValue(TransportMode.WALK); // default mode is walking


        /* ----------- Listen to Firebase Notification ------------  */

        // Listener for end of nav session
        this.pubSubHub.configureTopic(PubSubTopics.NAV_END,
            NewGenericPushNotification.class,
            new PayloadToObjectConverter<NewGenericPushNotification>() {
                @Override
                public NewGenericPushNotification fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, NewGenericPushNotification.class);
                }

                @Override
                public String toString(NewGenericPushNotification payload) {
                    return null;
                }
            });

        this.notificationSubscription = pubSubHub.subscribe(PubSubTopics.NAV_END,
            new SubscriberCallback<NewGenericPushNotification>() {
                @Override
                public void onReceived(NewGenericPushNotification payload) {
                    if (payload.getSessionId() == currentSessionId.getValue()) {
                        // end nav session
                        endNavSession(true);
                        navSessionEnded.postValue(true);
                        logger.info("firebase notification received for nav end");
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
                    LatLng latLng = new LatLng(payload.getLat(), payload.getLon());
                    currentApLocation.postValue(latLng);
                    logger.info("firebase notification received for new ap location");
                }
            });

        // Listener for new route
        this.pubSubHub.configureTopic(PubSubTopics.NEW_ROUTE, null,
            new PayloadToObjectConverter<NewGenericPushNotification>() {
                @Override
                public NewGenericPushNotification fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, NewGenericPushNotification.class);
                }

                @Override
                public String toString(NewGenericPushNotification payload) {
                    return null;
                }
            });

        this.notificationSubscription = pubSubHub.subscribe(PubSubTopics.NEW_ROUTE,
            new SubscriberCallback<NewGenericPushNotification>() {
                @Override
                public void onReceived(NewGenericPushNotification payload) {
                    if (payload.getSessionId() == currentSessionId.getValue()) {
                        getDirections();
                        logger.info("firebase notification received for new route");
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
                        switchNavControl(payload.getCarerHasControl());
                        logger.info("firebase notification received for switching control");
                    }
                }
            });

        // Listener for ap off track
        this.pubSubHub.configureTopic(PubSubTopics.NAV_OFF_TRACK,
            NewGenericPushNotification.class,
            new PayloadToObjectConverter<NewGenericPushNotification>() {
                @Override
                public NewGenericPushNotification fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, NewGenericPushNotification.class);
                }

                @Override
                public String toString(NewGenericPushNotification payload) {
                    return null;
                }
            });

        this.notificationSubscription = pubSubHub.subscribe(PubSubTopics.NAV_OFF_TRACK,
            new SubscriberCallback<NewGenericPushNotification>() {
                @Override
                public void onReceived(NewGenericPushNotification payload) {
                    if (payload.getSessionId() == currentSessionId.getValue()) {
                        apIsOffTrack.postValue(true);
                        logger.info("firebase notification received for ap offtrack");
                    }
                }
            });

    }

    /**
     * Get the googleApiClient.
     * Used by PlaceAutocompleteAdapter.
     * @return
     */
    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }


    /**
     * Get a navigation session associated to the current association id.
     */
    public void getNavigationSession() {

        /* Check for current nav session */
        logger.info("startNavigationSession: trying to get existing session...");
        navigationService.getCurrentNavigationSession().thenAccept(existingIdResult -> {
            if (existingIdResult.isSuccessful() && existingIdResult.unwrap().getId() > 0) {

                logger.info("startNavigationSession: successfully get existing nav session");
                this.currentSessionId.postValue(existingIdResult.unwrap().getId());
                navSessionStarted.postValue(true);
                carerHasControl.postValue(existingIdResult.unwrap().getCarerHasControl());

                if (! existingIdResult.unwrap().getRoute().getDirectionsRoutes().isEmpty()) {
                    currentDirections.postValue(existingIdResult.unwrap().getRoute());
                }

                if (convertStringToTransportMode(existingIdResult.unwrap().getTransportMode())
                    != null) {
                    currentMode.postValue(convertStringToTransportMode(
                        existingIdResult.unwrap().getTransportMode()));
                }

                logger.info("startNavigationSession existing session id: "
                           + currentSessionId.getValue());

            } else {
                logger.info("startNavigationSession: failed to get existing nav session");

                /* Start new nav session */
                startNewNavSession();
            }
        });

        // get recents
        navigationService.getDestinations(currentUserId, 5).thenAccept(result -> {
            if (result.isSuccessful()) {

                List<Recents> recents = result.unwrap().getRecents();
                for (Recents recent : recents) {
                    recentDestinations.add(recent);
                }
            }
        });
    }

    /**
     * So start new nav session. No existing session for association id.
     */
    private void startNewNavSession() {
        navigationService.createNewNavigationSession(assocId.getValue()).thenAccept(newIdResult -> {
            logger.info("startNavigationSession: trying to start new session...");
            if (newIdResult.isSuccessful() && newIdResult.unwrap().getId() > 0) {
                logger.info("startNavigationSession: successfully started new nav session");

                this.currentSessionId.postValue(newIdResult.unwrap().getId());
                navSessionStarted.postValue(true);
                carerHasControl.postValue(newIdResult.unwrap().getCarerHasControl());

                if (! newIdResult.unwrap().getRoute().getDirectionsRoutes().isEmpty()) {
                    currentDirections.postValue(newIdResult.unwrap().getRoute());
                }

                if (convertStringToTransportMode(newIdResult.unwrap().getTransportMode())
                    != null) {
                    currentMode.postValue(convertStringToTransportMode(
                        newIdResult.unwrap().getTransportMode()));
                }

                logger.info("startNavigationSession new session id: "
                             + currentSessionId.getValue());


            } else {
                logger.info("startNavigationSession: failed to get new nav session");
            }
        });
    }


    /**
     * Set destination of current nav session on server
     * Note: spelling of currentMode is important (for server)
     * Eg. "Walking", "PT"
     */
    public void setDestination() {

        if (!navSessionStarted.getValue()) {
            logger.info("setDestination navSessionStarted" + navSessionStarted.getValue());
            getNavigationSession();
            setDestination();
            return;
        } else if (currentSessionId.getValue() == null) {
            logger.info("setDestination currentSessionId is null");
            return;
        } else if (currentPlaceId.getValue() == null) {
            logger.info("setDestination currentPlaceId is null");
            return;
        } else if (currentAddress.getValue() == null) {
            logger.info("setDestination currentAddress is null");
            return;
        }

        // Only set destination if navigation session has started
        if (navSessionStarted.getValue()) {

            // Tell server that destination is using walking mode
            if (this.currentMode.getValue() == TransportMode.WALK) {
                logger.info("setDestination setting destination on server walk mode "
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
                logger.info("setDestination setting destination on server public mode "
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
            logger.info("setDestination: ERROR setting destination "
                         + "navSessionStarted " + navSessionStarted.getValue()
                         + " currentSessionId: " + currentSessionId.getValue());
        }
    }

    /**
     * Helper for setting destination.
     * @param transportMode mode of transport
     * @return TransportMode enum
     */
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
     * Get the directions to current destination after firebase notifies us.
     */
    private void getDirections() {
        navigationService.getDirections(this.currentSessionId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                this.currentDirections.postValue(result.unwrap());
            }
        });
    }


    /**
     * Inform server to end nav session.
     * @param fromFirebaseNotification boolean if notification is from firebase
     */
    public void endNavSession(Boolean fromFirebaseNotification) {
        if (currentSessionId.getValue() != null) {
            navigationService.endNavigationSession(this.currentSessionId.getValue())
                .thenAccept(result -> {
                    if (result.isSuccessful()) {
                        toastService.toastShort("Successfully ended nav session "
                                                + currentSessionId.getValue());
                    } else {
                        if (fromFirebaseNotification) {
                            toastService.toastShort("Successfully ended nav session "
                                                    + currentSessionId.getValue());
                        } else {
                            toastService.toastLong("Error ending nav session "
                                                   + result.getErrorMessage());
                            logger.info("endNavSession" + result.getErrorMessage());
                        }
                    }
                });
            logger.info("endNavSession (startNavigationSession) exited nav session id: "
                         + currentSessionId.getValue());
        } else {
            logger.info("endNavSession: session id is null");
        }
    }


    /**
     * Update location of AP to server.
     * @param latLng location of ap
     */
    public void updateApLocation(LatLng latLng) {
        logger.info("updateApLocation entered");
        // Todo: Temp patch for crash
        if (currentSessionId.getValue() == null) {
            return;
        }

        if (latLng == null) {
            logger.info("updateApLocation error latLng is null");
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

            // Only update ap location to server if distance is more than the buffer
            if (newLocation.distanceTo(currentLocation) < UPDATE_DISTANCE_BUFFER) {
                logger.info("updateApLocation: ERROR ap location is only "
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

    /**
     * Helper to update location of ap to server.
     * @param latLng new coordinate of ap
     */
    private void updateApLocationToServer(LatLng latLng) {
        logger.info("updateApLocation updating ap location to server...");
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
     * @return latLng of current ap location
     */
    public LatLng getApLocation() {
        // Todo: Temp patch for crash
        if (currentSessionId.getValue() == null) {
            return null;
        }
        navigationService.getCurrentLocation(currentSessionId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.info("getApLocation successfully retrieved AP location");
                this.currentApLocation.postValue(new LatLng(result.unwrap().latitude,
                    result.unwrap().longitude));
                logger.info("getApLocation currentApLocation: " + currentApLocation.getValue());
            } else {
                logger.info("getApLocation failed to retrieved AP location");
            }
        });

        return currentApLocation.getValue();
    }


    /**
     * Get the Place to display destination details for both users.
     * @param placeId placeId of destination.
     */
    public void getPlaceFromPlaceId(String placeId) {
        if (placeId == null) {
            return;
        }
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
            .getPlaceById(googleApiClient, placeId);

        placeResult.setResultCallback(places -> {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {

                PlaceInfo placeInfo = PlaceInfo.fromGoogleApiPlace(place);
                this.currentDestination.postValue(placeInfo);
                this.currentAddress.postValue(place.getAddress().toString());
                this.currentRoutes.postValue(currentDirections.getValue().getDirectionsRoutes());
                routeIsSet.setValue(true);

            } catch (NullPointerException e) {
                logger.info("getPlaceFromPlaceId: NullPointerException: " + e.getMessage());
            }

            places.release();
        });
    }


    /**
     * Generate list of guide cards for current destination.
     */
    public void generateGuideCards() {

        ArrayList<GuideCard> guideCards = new ArrayList<>();

        Route route = currentRoutes.getValue().get(0); //always 1
        int routeLegSize = route.getRouteLegs().size() - 1;// usually 1

        // can't show guide cards, no legs in routead
        if (routeLegSize < 0) {
            return;
        }

        Leg leg = route.getRouteLegs().get(routeLegSize);
        int legStepSize = leg.getLegSteps().size() - 1; // can be any number

        for (int step = 0; step <= legStepSize; step++) {

            String maneuver = "";
            if (leg.getLegSteps().get(step).getStepManeuver() != null) {
                maneuver = leg.getLegSteps().get(step).getStepManeuver();
            }


            guideCards.add(new GuideCard(

                leg.getLegSteps().get(step).getStepDistance().getText(),
                leg.getLegSteps().get(step).getStepDuration().getText(),
                leg.getLegSteps().get(step).getStepHtmlInstructions(),
                maneuver,
                leg.getLegSteps().get(step).getStepTravelMode(),
                leg.getLegSteps().get(step).getStepStartLocation(),
                leg.getLegSteps().get(step).getStepEndLocation()

            ));
        }

        if (!guideCards.isEmpty()) {
            currentGuideCards.setValue(guideCards);
        }

    }


    /*
     * ------------------------ GOOGLE MAPS AUTO SUGGESTIONS -------------------
     */

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
                logger.info("onResult: Place query did not complete successfully: "
                          + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {
                logger.info("onSuggestionClicked: Entered try block");

                PlaceInfo placeInfo = PlaceInfo.fromGoogleApiPlace(place);
                this.currentDestination.setValue(placeInfo);

                this.currentPlaceId.setValue(placeId);
                this.currentAddress.setValue(place.getAddress().toString());

                //send to server the destination selected
                this.setDestination();

                logger.info("onResult: selected destination: " + placeInfo.toString());

            } catch (NullPointerException e) {
                logger.info("onResult: NullPointerException: " + e.getMessage());
            }

            places.release();
        });
    }





    /*
     * ------------------------ HANDLE UI ON CLICK EVENTS -------------------
     */

    /**
     * Handle switch control button clicked.
     */
    public void onGainControlButtonClicked() {
        navigationService.switchControl(this.currentSessionId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.info("onGainControlButtonClicked successfully switched control");

            } else {
                logger.info("onGainControlButtonClicked error");
            }
        });

        logger.info("onGainControlButtonClicked current user is ap: " + currentUserIsAp);

        if (this.currentUserIsAp) {
            carerHasControl.setValue(false);
        } else if (this.currentUserIsAp == false) {
            carerHasControl.setValue(true);
        }
    }


    /**
     * Toggle the status of favourite.
     */
    public void toggleFavoriteStatus() {
        //todo (iter3): set/unset favorite state of destination
    }


    /**
     * Switch control of user.
     * @param carerHasControl if carer has control
     */
    private void switchNavControl(Boolean carerHasControl) {

        // Carer
        if (!currentUserIsAp) {
            // currently have control
            if (this.carerHasControl.getValue()) {
                // now lost control
                if (!carerHasControl) {
                    this.carerHasControl.postValue(false);
                }
            } else { // currently dont have control
                // now gain control
                if (carerHasControl) {
                    this.carerHasControl.postValue(true);
                }
            }
        }

        // AP
        if (currentUserIsAp) {
            // currently have control
            if (!this.carerHasControl.getValue()) {
                // now lost control
                if (carerHasControl) {
                    this.carerHasControl.postValue(true);
                }
            } else { // currently dont have control
                // now gain control
                if (!carerHasControl) {
                    this.carerHasControl.postValue(false);
                }
            }
        }
    }

    /**
     * Update server that Ap went off track.
     */
    public void apWentOffTrack() {
        navigationService.updateApOffTrack(this.currentSessionId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.info("apWentOffTrack successfully updated server ap went off track");

            } else {
                logger.info("apWentOffTrack error updating server");
            }
        });
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
     * Handle close navigation session button clicked.
     */
    public void onCloseNavSessionButtonClicked() {
        endNavSession(false);
        navSessionEnded.postValue(true);
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

}
