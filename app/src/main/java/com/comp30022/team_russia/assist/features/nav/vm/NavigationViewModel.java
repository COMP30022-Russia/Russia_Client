package com.comp30022.team_russia.assist.features.nav.vm;

import static com.comp30022.team_russia.assist.features.push.NavApLocationUpdateTokenDeduplicator.ensureApLocSyncTokenValid;
import static com.comp30022.team_russia.assist.features.push.NavSyncTokenDeduplicator.ensureNavSyncTokenValid;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.DisposableCollection;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.jitsi.services.VoiceCoordinator;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.nav.models.Directions;
import com.comp30022.team_russia.assist.features.nav.models.GuideCard;
import com.comp30022.team_russia.assist.features.nav.models.Leg;
import com.comp30022.team_russia.assist.features.nav.models.NavSession;
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
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.shopify.livedataktx.LiveDataKt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;



/**
 * Navigation View Model.
 */
public class NavigationViewModel extends BaseViewModel {

    /* Only update ap location to server if distance is more than 0.5m difference */
    private static final double UPDATE_DISTANCE_BUFFER = 0.5;

    public boolean apInitiated;

    public boolean apLocationSyncedWithServer;

    public boolean isCurrentUserAp() {
        return currentUserIsAp;
    }

    private boolean currentUserIsAp;

    private int currentUserId;

    public List<Recents> recentDestinations = new ArrayList<>();

    public final MutableLiveData<String> currentSearchText = new MutableLiveData<>();

    public final MutableLiveData<PlaceInfo> currentDestination = new MutableLiveData<>();

    public final MutableLiveData<TransportMode> currentMode = new MutableLiveData<>();

    public final MutableLiveData<Boolean> carerHasControl = new MutableLiveData<>();

    public final LiveData<Boolean> currentUserHasControl;

    /**
     * Gets whether the current user has control, as a primitive boolean value.
     * @return Whether the current user has control.
     */
    public boolean getCurrentUserHasControlUnboxed() {
        Boolean tmp = currentUserHasControl.getValue();
        boolean tmpUnboxed = tmp != null && tmp;
        return tmpUnboxed;
    }

    public final MutableLiveData<LatLng> currentApLocation = new MutableLiveData<>();

    public final MutableLiveData<List<Route>> currentRoutes = new MutableLiveData<>();

    public final MutableLiveData<Directions> currentDirections = new MutableLiveData<>();

    public final MutableLiveData<Boolean> routeIsSet = new MutableLiveData<>();

    public final MutableLiveData<Boolean> navSessionStarted = new MutableLiveData<>();

    public final MutableLiveData<Boolean> navSessionEnded = new MutableLiveData<>();

    public final MutableLiveData<Boolean> apIsOffTrack = new MutableLiveData<>();

    /**
     * Gets if the AP Off Track Dialog is Still Shown.
     * @return true if the AP Off Track Dialog is Still Shown.
     */
    public boolean getApOffTrackDialogStillShownUnboxed() {
        Boolean tmp = apOffTrackDialogStillShown.getValue();
        return tmp != null && tmp;
    }

    public final MutableLiveData<Boolean> apOffTrackDialogStillShown = new MutableLiveData<>();

    /**
     * Sub-ViewModel for voice call related UI logic.
     */
    public final NavVoiceCallViewModel voiceCallVm;

    private final VoiceCoordinator voiceCoordinator;


    /* local variables */
    public final MutableLiveData<Integer> assocId = new MutableLiveData<>();

    public int getCurrentSessionId() {
        return currentSessionId;
    }

    public void setCurrentSessionId(int currentSessionId) {
        logger.debug("Setting currentSessionId to " + currentSessionId);
        this.currentSessionId = currentSessionId;
    }

    /**
     * The current session Id.
     */
    private int currentSessionId = -1;

    private final MutableLiveData<String> currentPlaceId = new MutableLiveData<>();

    private final MutableLiveData<String> currentAddress = new MutableLiveData<>();

    // Services

    private final GoogleApiClient googleApiClient;

    private final Application russiaApp;

    private final AuthService authService;

    private final NavigationService navigationService;

    private final PubSubHub pubSubHub;

    private final LoggerInterface logger;

    private final ToastService toastService;

    private final DisposableCollection subscriptions = new DisposableCollection();


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
        this.voiceCoordinator = voiceCoordinator;

        voiceCallVm = new NavVoiceCallViewModel(pubSubHub,
            loggerFactory,
            voiceCoordinator,
            authService,
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


        currentUserHasControl = LiveDataKt.map(carerHasControl, ccHasControl -> {
            boolean carerHasControlUnboxed = ccHasControl != null && ccHasControl;
            return (!currentUserIsAp && carerHasControlUnboxed)
                    || (currentUserIsAp && !carerHasControlUnboxed);
        });

        // initial values
        currentUserId = authService.getCurrentUser().getUserId();
        currentUserIsAp = (authService.getCurrentUser().getUserType() == User.UserType.AP);
        navSessionStarted.setValue(false);
        routeIsSet.setValue(false);
        apLocationSyncedWithServer = false;
        apIsOffTrack.setValue(false);
        apOffTrackDialogStillShown.setValue(false);

        currentSearchText.setValue("");


        setUpFirebaseDataMessageListeners();
    }


    /**
     * Listen to Firebase / Socket data messages.
     */
    private void setUpFirebaseDataMessageListeners() {

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_END,
            new SubscriberCallback<NewGenericPushNotification>() {
                @Override
                public void onReceived(NewGenericPushNotification payload) {
                    logger.debug("NAV_END " + payload.toString());
                    ensureNavSyncTokenValid(payload.getSessionId(), payload.getSync(),
                        () -> onNavigationEnded(payload.getSessionId()));
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NEW_AP_LOCATION,
            new SubscriberCallback<NewPositionPushNotification>() {
                @Override
                public void onReceived(NewPositionPushNotification payload) {
                    ensureApLocSyncTokenValid(payload.getSync(),
                        () -> onNewApLocation(payload));
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NEW_ROUTE,
            new SubscriberCallback<NewGenericPushNotification>() {
                @Override
                public void onReceived(NewGenericPushNotification payload) {
                    logger.debug("NEW_ROUTE " + payload.toString());
                    ensureNavSyncTokenValid(payload.getSessionId(), payload.getSync(),
                        () -> onNewRouteGenerated(payload.getSessionId()));
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_CONTROL_SWTICH,
            new SubscriberCallback<NewNavControlPushNotification>() {
                @Override
                public void onReceived(NewNavControlPushNotification payload) {
                    logger.debug("NAV_SWITCH " + payload.toString());
                    ensureNavSyncTokenValid(payload.getSessionId(), payload.getSync(),
                        () -> onControlSwitched(payload.getSessionId(),
                            payload.getCarerHasControl()));
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_OFF_TRACK,
            new SubscriberCallback<NewGenericPushNotification>() {
                @Override
                public void onReceived(NewGenericPushNotification payload) {
                    logger.debug("NAV_OFF_TRACK " + payload.toString());
                    ensureNavSyncTokenValid(payload.getSessionId(), payload.getSync(), () -> {
                        onApOffTrack(payload.getSessionId());

                    });
                }
            }));
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
                updateStateBasedOnNavSession(existingIdResult.unwrap());

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
                updateStateBasedOnNavSession(newIdResult.unwrap());
            } else {
                logger.info("startNavigationSession: failed to get new nav session");
            }
        });
    }

    /**
     * Updates the state of private variables and {@link LiveData}s, base on a {@link NavSession}
     * object.
     * @param newNavSession The new nav session  object.
     */
    private void updateStateBasedOnNavSession(NavSession newNavSession) {
        try {
            setCurrentSessionId(newNavSession.getId());

            // @todo: check if the session is active
            navSessionStarted.postValue(true);

            carerHasControl.postValue(newNavSession.getCarerHasControl());

            if (newNavSession.getRoute() != null
                && (!newNavSession.getRoute().getDirectionsRoutes().isEmpty())) {
                currentDirections.postValue(newNavSession.getRoute());
            }

            if (convertStringToTransportMode(newNavSession.getTransportMode()) != null) {
                currentMode.postValue(convertStringToTransportMode(
                    newNavSession.getTransportMode()));
                logger.debug("newNavSession.getTransportMode(): "
                             + newNavSession.getTransportMode());
            }

            logger.info("updateStateBasedOnNavSession: success");
        } catch (Exception e) {
            logger.error("updateStateBasedOnNavSession: error");
            e.printStackTrace();
        }

    }

    /**
     * Set destination of current nav session on server
     * Note: spelling of currentMode is important (for server)
     * Eg. "Walking", "PT"
     * @param modeOfTransport mode to transport with
     */
    public void updateDestination(TransportMode modeOfTransport) {


        if (currentSessionId <= 0) {
            logger.info("updateDestination currentSessionId is null");
            return;
        } else if (currentPlaceId.getValue() == null) {
            logger.info("updateDestination currentPlaceId is null");
            return;
        } else if (currentAddress.getValue() == null) {
            logger.info("updateDestination currentAddress is null");
            return;
        }

        // Only set destination if navigation session has started
        if (navSessionStarted.getValue()) {

            // Tell server that destination is using walking mode
            if (modeOfTransport == TransportMode.WALK) {
                logger.info("updateDestination setting destination on server walk mode.");

                navigationService.setDestination(currentSessionId,
                    this.currentPlaceId.getValue(), this.currentAddress.getValue(), "Walking")
                    .thenAccept(result -> {
                        if (result.isSuccessful()) {
                            // do nothing, route is shown on screen
                        } else {
                            toastService.toastShort("Could not set destination, try again");
                        }
                    });
            } else if (modeOfTransport == TransportMode.PUBLIC_TRANSPORT) {
                // Tell server that destination is using public transport mode
                logger.info("updateDestination setting destination on server public mode");

                navigationService.setDestination(currentSessionId,
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
            toastService.toastShort("Could not set destination");
            logger.info("updateDestination: ERROR setting destination "
                         + "navSessionStarted " + navSessionStarted.getValue()
                         + " currentSessionId: " + getCurrentSessionId());
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
        navigationService.getDirections(this.currentSessionId).thenAccept(result -> {
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
        if (currentSessionId > 0) {
            navigationService.endNavigationSession(this.currentSessionId)
                .thenAccept(result -> {
                    if (result.isSuccessful()) {
                        toastService.toastShort("Successfully ended nav session "
                                                + currentSessionId);
                    } else {
                        if (fromFirebaseNotification) {
                            toastService.toastShort("Successfully ended nav session "
                                                    + currentSessionId);
                        } else {
                            toastService.toastLong("Error ending nav session "
                                                   + result.getErrorMessage());
                            logger.info("endNavSession" + result.getErrorMessage());
                        }
                    }
                });
            logger.info("endNavSession (startNavigationSession) exited nav session id: "
                         + currentSessionId);
        } else {
            logger.info("endNavSession: session id is null");
        }
    }


    /**
     * Update location of AP to server.
     * @param latLng incoming location of ap from Geo device.
     */
    public void updateApLocation(LatLng latLng) {
        logger.info("updateApLocation entered");

        // Todo: Temp patch for crash
        if (currentSessionId <= 0) {
            return;
        }



        if (latLng == null) {
            logger.info("updateApLocation error latLng is null");
            return;
        }

        // server already has history of ap location
        if (apLocationSyncedWithServer) {

            final LatLng currentApLocationLatLng = currentApLocation.getValue();
            if (currentApLocationLatLng == null) {
                // no previous AP location, update anyway
                updateApLocationToServer(latLng);
            } else {
                // compare with prev location, only update when not too close
                Location newLocation = new Location("");
                newLocation.setLatitude(latLng.latitude);
                newLocation.setLongitude(latLng.longitude);
                Location currentLocation = new Location("");
                currentLocation.setLatitude(currentApLocationLatLng.latitude);
                currentLocation.setLongitude(currentApLocationLatLng.longitude);

                // Only update ap location to server if distance is more than the buffer
                if (newLocation.distanceTo(currentLocation) < UPDATE_DISTANCE_BUFFER) {
                    logger.info("updateApLocation: ERROR ap location is only "
                                + "1m different from previous");
                    return;
                } else {
                    updateApLocationToServer(latLng);
                }
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
        navigationService.updateCurrentLocation(currentSessionId, latLng)
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
        if (currentSessionId <= 0) {
            return null;
        }
        navigationService.getCurrentLocation(currentSessionId).thenAccept(result -> {
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
     * Generate list of guide cards for current destination from {@link Route}s.
     */
    public List<GuideCard> generateGuideCards(List<Route> routes) {

        ArrayList<GuideCard> guideCards = new ArrayList<>();

        Route route = routes.get(0); //always 1
        int routeLegSize = route.getRouteLegs().size() - 1;// usually 1

        // can't show guide cards, no legs in routead
        if (routeLegSize < 0) {
            return new ArrayList<>();
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


        return guideCards;
    }

    /*
     * ------------------------ Handlers of Firebase Events -------------------
     */

    /**
     * Called when a nav_end data message is received.
     * @param endedSessionId The ID of the ended session.
     */
    private void onNavigationEnded(final int endedSessionId) {
        final int currentSessionId = this.getCurrentSessionId();
        if (endedSessionId == currentSessionId) {
            // end nav session
            endNavSession(true);
            navSessionEnded.postValue(true);
            logger.info("firebase notification received for nav end");
        } else {
            logger.warn(
                String.format(Locale.ENGLISH,
                    "Not same session %d (Incoming) != %d (VM), ignoring ",
                    endedSessionId, currentSessionId));
        }
    }

    /**
     * Called when a nav_new_route data message is received.
     * @param sessionId  The ID of the nav session concerned.
     */
    private void onNewRouteGenerated(int sessionId) {

        if (sessionId == getCurrentSessionId()) {
            getDirections();
            logger.info("firebase notification received for new route");
        } else {
            logger.warn(
                String.format(Locale.ENGLISH,
                    "Not same session %d (Incoming) != %d (VM), ignoring ",
                    sessionId, getCurrentSessionId()));
        }
    }

    /**
     * Called when a nav_switch_control data message is received.
     * @param sessionId The ID of the nav session concerned.
     * @param carerHasControl Whether the Carer has the control now.
     */
    private void onControlSwitched(int sessionId, boolean carerHasControl) {
        if (sessionId == getCurrentSessionId()) {
            switchNavControl(carerHasControl);
            logger.info("firebase notification received for switching control");
        } else {
            logger.warn(
                String.format(Locale.ENGLISH,
                    "Not same session %d (Incoming) != %d (VM), ignoring ",
                    sessionId, currentSessionId));
        }
    }

    private void onNewApLocation(NewPositionPushNotification payload) {
        LatLng latLng = new LatLng(payload.getLat(), payload.getLon());
        currentApLocation.postValue(latLng);
        logger.info("firebase notification received for new ap location");
    }

    private void onApOffTrack(int sessionId) {
        if (sessionId == getCurrentSessionId()) {
            apIsOffTrack.postValue(true);
            logger.info("firebase notification received for ap offtrack");
        } else {
            logger.warn(
                String.format(Locale.ENGLISH,
                    "Not same session %d (Incoming) != %d (VM), ignoring ",
                    sessionId, getCurrentSessionId()));
        }
    }

    /*
     * ------------------------ GOOGLE MAPS AUTO SUGGESTIONS -------------------
     */

    /**
     * Handles sending destination to server when a destination is selected on the list.
     * @param item The {@link PlaceSuggestionItem} being clicked.
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
                this.updateDestination(this.currentMode.getValue());

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
        navigationService.switchControl(this.currentSessionId).thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.info("onGainControlButtonClicked successfully switched control");

            } else {
                logger.info("onGainControlButtonClicked error");
            }
        });

        logger.info("onGainControlButtonClicked current user is ap: " + currentUserIsAp);

        if (this.currentUserIsAp) {
            carerHasControl.setValue(false);
        } else {
            carerHasControl.setValue(true);
        }
    }

    /**
     * Called when the transport mode tab has been switched by the user.
     * @param newMode The new transport mode.
     */
    public void onModeChanged(TransportMode newMode) {
        logger.debug("Changing transport mode to " + newMode);
        currentMode.postValue(newMode);
        updateDestination(newMode);
    }

    /**
     * Switch control of user.
     * @param carerHasControl if carer has control
     */
    private void switchNavControl(boolean carerHasControl) {
        logger.info("switchNavControl: carer has "
                    + (carerHasControl ? "" : " NO ") + "control now");
        this.carerHasControl.postValue(carerHasControl);
    }

    /**
     * Update server that Ap went off track.
     * Show Ap that they are off track by showing banner.
     */
    public void apWentOffTrack() {
        logger.info("AP went off track!");
        apOffTrackDialogStillShown.postValue(true);
        navigationService.updateApOffTrack(this.currentSessionId).thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.info("apWentOffTrack successfully updated server ap went off track");

            } else {
                logger.info("apWentOffTrack error updating server");
            }
        });
    }

    /**
     * Mark "AP off track" as dismissed.
     */
    public void dismissApOffTrack() {
        apIsOffTrack.postValue(false);
        apOffTrackDialogStillShown.postValue(false);
    }

    /**
     * Start a call when Ap needs help after arriving destination.
     */
    public void startCall() {
        voiceCoordinator.startCallForSession(currentSessionId);
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
        subscriptions.dispose();
        googleApiClient.disconnect();
        logger.debug("onCleared.  Byebye!");
    }

}
