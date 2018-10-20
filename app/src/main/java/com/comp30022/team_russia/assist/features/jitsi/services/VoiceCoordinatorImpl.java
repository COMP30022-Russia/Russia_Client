package com.comp30022.team_russia.assist.features.jitsi.services;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;

import com.comp30022.team_russia.assist.base.DisposableCollection;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.jitsi.JitsiStartArgs;
import com.comp30022.team_russia.assist.features.jitsi.JitsiStartType;
import com.comp30022.team_russia.assist.features.jitsi.models.NavCallDto;
import com.comp30022.team_russia.assist.features.jitsi.models.VoiceEndedPushNoti;
import com.comp30022.team_russia.assist.features.jitsi.models.VoicePendingAcceptPushNoti;
import com.comp30022.team_russia.assist.features.jitsi.models.VoiceStartedPushNoti;
import com.comp30022.team_russia.assist.features.jitsi.models.VoiceStateChangedPushNoti;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.nav.services.NavigationService;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;

import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Implementation of {@link VoiceCoordinator}.
 */
public class VoiceCoordinatorImpl implements VoiceCoordinator {

    private static final int NO_CALL_ID = -1;

    private final LoggerInterface logger;
    private final PubSubHub pubSubHub;

    private final NavCallApi navCallApi;

    private final AuthService authService;

    private final NavigationService navigationService;


    /**
     * Remote back camera.
     */
    private final MutableLiveData<Boolean> remoteCameraOn = new MutableLiveData<>();

    // The states

    private NavCallDesiredState desiredState = NavCallDesiredState.Off;
    private final MutableLiveData<NavCallDesiredState> desiredStateLiveData
        = new MutableLiveData<>();

    private JitsiState currentJitsiState = JitsiState.Off;
    private int currentCallId = NO_CALL_ID;
    private int currentNavSessionId = -1;
    private int lastSyncNumber = -1;

    private Handler stateCheckHandler = new Handler();
    private Handler reconcileHandler = new Handler();

    private final DisposableCollection subscriptions = new DisposableCollection();

    /**
     * A {@link Runnable} that gets called periodically to execute the reconcile logic.
     * NOTE: In order not to overwhelm the system, this Runnable has limited execution frequency.
     * Don't replace with method reference! We need a singleton instance of this Runnable.
     */
    @SuppressWarnings("Convert2MethodRef")
    private final Runnable reconcileRunnable = () -> reconcileState();

    @Inject
    public VoiceCoordinatorImpl(PubSubHub pubSubHub, LoggerFactory loggerFactory,
                                Retrofit retrofit,
                                NavigationService navigationService,
                                AuthService authService) {
        logger = loggerFactory.getLoggerForClass(this.getClass());
        navCallApi = retrofit.create(NavCallApi.class);
        this.navigationService = navigationService;
        this.authService = authService;
        this.pubSubHub = pubSubHub;

        remoteCameraOn.postValue(false);

        desiredStateLiveData.postValue(desiredState);
        registerListeners();
        logger.info("Created");
    }

    private void registerListeners() {
        // Jitsi State changes (from JitsiMeetHolder)
        subscriptions.add(pubSubHub.subscribe(PubSubTopics.JITSI_IDLE,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    updateJitsiState(JitsiState.Idle);
                }
            }));
        subscriptions.add(pubSubHub.subscribe(PubSubTopics.JITSI_JOINED,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    updateJitsiState(JitsiState.Ongoing);
                }
            }));
        subscriptions.add(pubSubHub.subscribe(PubSubTopics.JITSI_FAILED,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    reportFailure();
                    updateJitsiState(JitsiState.Error);
                }
            }));
        subscriptions.add(pubSubHub.subscribe(PubSubTopics.JITSI_LEFT,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    // Jitsi participant left (Both local and remote?)
                    if (VoiceCoordinatorImpl.this.desiredState == NavCallDesiredState.Off) {
                        // this is expected
                        updateJitsiState(JitsiState.Idle);
                    } else {
                        // Call is supposed to be ongoing, shouldn't left, hence an error.
                        logger.warn("Jitsi participant left during conference");
                        updateJitsiState(JitsiState.Error);
                    }
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.JITSI_STOPPED,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    updateJitsiState(JitsiState.Off);
                }
            }));

        // NavSession call state changes (from Firebase data messages).
        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_CALL_PENDING_ME,
            new SubscriberCallback<VoicePendingAcceptPushNoti>() {
                @Override
                public void onReceived(VoicePendingAcceptPushNoti payload) {
                    ensureSyncTokenValid(payload.getCallId(), payload.getSyncToken(), () -> {
                        currentNavSessionId = payload.getSessionId();
                        updateDesiredState(NavCallDesiredState.RingingMe);
                        // @todo: display message
                        logger.info(String.format("%s is calling me...", payload.getSenderName()));
                    });
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_CALL_PENDING_OTHER,
            new SubscriberCallback<VoicePendingAcceptPushNoti>() {
                @Override
                public void onReceived(VoicePendingAcceptPushNoti payload) {
                    ensureSyncTokenValid(payload.getCallId(), payload.getSyncToken(), () -> {
                        currentNavSessionId = payload.getSessionId();
                        updateDesiredState(NavCallDesiredState.RingingOther);
                        lastSyncNumber = payload.getSyncToken();
                    });
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_CALL_STARTED,
            new SubscriberCallback<VoiceStartedPushNoti>() {
                @Override
                public void onReceived(VoiceStartedPushNoti payload) {
                    ensureSyncTokenValid(payload.getCallId(), payload.getSyncToken(), () -> {
                        publishToastMessage("Voice call started");
                        currentNavSessionId = payload.getSessionId();
                        updateDesiredState(NavCallDesiredState.Ongoing);
                    });
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_CALL_TERMINATED,
            new SubscriberCallback<VoiceEndedPushNoti>() {
                @Override
                public void onReceived(VoiceEndedPushNoti payload) {
                    ensureSyncTokenValid(payload.getCallId(), payload.getSyncToken(), () -> {
                        currentNavSessionId = payload.getSessionId();
                        updateDesiredState(NavCallDesiredState.Off);
                        logger.info(String.format("Nav call stopped because: %s",
                            payload.getReason()));
                    });
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_CALL_STATE_CHANGED,
            new SubscriberCallback<VoiceStateChangedPushNoti>() {
                @Override
                public void onReceived(VoiceStateChangedPushNoti payload) {
                    ensureSyncTokenValid(payload.getCallId(), payload.getSyncToken(), () -> {
                        currentNavSessionId = payload.getSessionId();
                        switch (payload.getState()) {
                        case "Ongoing":
                            updateDesiredState(NavCallDesiredState.Ongoing);
                            break;
                        case "Terminated":
                            updateDesiredState(NavCallDesiredState.Off);
                            break;
                        default:
                            logger.warn("Unknown state: " + payload.getState());
                            break;
                        }
                    });
                }
            }));

    }

    private synchronized void ensureSyncTokenValid(int newCallId, int newSyncToken, Runnable r) {
        if (newCallId > currentCallId
            || (newCallId == currentCallId
                && newSyncToken > lastSyncNumber)) {
            currentCallId = newCallId;
            lastSyncNumber = newSyncToken;
            r.run();
        } else {
            logger.warn("Outdated state, ignoring...");
        }
    }


    private void updateDesiredState(NavCallDesiredState newDesiredState) {
        if (desiredState != newDesiredState) {
            logger.info(String.format(Locale.ENGLISH,
                "NavSession: %d Call: %d", currentNavSessionId, currentCallId));
            logger.info(String.format("Desired State: [%s] -> [%s]", desiredState.toString(),
                newDesiredState.toString()));
            desiredState = newDesiredState;
            desiredStateLiveData.postValue(desiredState);

        }
        reconcileHandler.removeCallbacks(reconcileRunnable);
        reconcileHandler.postDelayed(reconcileRunnable, 2000);
    }

    private void updateJitsiState(JitsiState newJitsiState) {
        if (currentJitsiState != newJitsiState) {
            logger.info(String.format("Jitsi State: [%s] -> [%s]", currentJitsiState.toString(),
                newJitsiState.toString()));
            currentJitsiState = newJitsiState;
        }
        reconcileHandler.removeCallbacks(reconcileRunnable);
        reconcileHandler.postDelayed(reconcileRunnable, 2000);
    }

    private synchronized void reconcileState() {
        logger.info(String.format(Locale.ENGLISH,
            "Reconciling state: desired=[%s] jitsi=[%s] NavSession=%d Call=%d",
            desiredState.toString(),
            currentJitsiState.toString(),
            currentNavSessionId,
            currentCallId));

        User.UserType userType = User.UserType.Carer;
        if (!authService.isLoggedInUnboxed()) {
            desiredState = NavCallDesiredState.Off;
        } else {
            userType = authService.getCurrentUser().getUserType();
        }
        switch (desiredState) {
        case Off:
        case StartRequested:
            switch (currentJitsiState) {
            case Error:
            case Ongoing:
            case Idle:
                logger.info("reconcileState: asking Jitsi to stop...");
                pubSubHub.publish(PubSubTopics.JITSI_PLEASE_STOP, null);
                break;
            case Off:
                break;
            default:
                logger.error("Unexpected currentJitsiState");
                break;
            }
            break;
        case Ongoing:
            switch (currentJitsiState) {
            case Error:
            case Off:
            case Idle: {
                JitsiStartArgs startArgs = new JitsiStartArgs(
                    userType == User.UserType.AP
                        ? JitsiStartType.VideoBackCamera : JitsiStartType.Voice,
                    String.format(Locale.ENGLISH, "%d", this.currentCallId));
                logger.info(String.format("reconcileState: asking Jitsi to start. Room=%s",
                    startArgs.getRoom()));
                pubSubHub.publish(PubSubTopics.JITSI_PLEASE_START, startArgs);
                break;
            }
            case Ongoing:
                // @todo: ensure Jitsi is in the correct room.
                break;
            default:
                logger.error("Unexpected currentJitsiState");
                break;
            }
            break;
        case RingingOther:
            switch (currentJitsiState) {
            case Off:
            case Idle:
                logger
                    .info("reconcileState: ringing other people. can pre-start Jitsi for warm up.");
                JitsiStartArgs startArgs = new JitsiStartArgs(
                    userType == User.UserType.AP
                        ? JitsiStartType.VideoBackCamera : JitsiStartType.Voice,
                    String.format(Locale.ENGLISH, "%d", this.currentCallId));
                logger.info(String.format("reconcileState: asking Jitsi to start. Room=%s",
                    startArgs.getRoom()));
                pubSubHub.publish(PubSubTopics.JITSI_PLEASE_START, startArgs);
                break;
            case Error:
                logger.warn("Asking Jitsi to stop...");
                pubSubHub.publish(PubSubTopics.JITSI_PLEASE_STOP, null);
                break;
            case Ongoing:
                // @todo: ensure Jitsi is in the correct room.
                logger.warn("Jitsi appears to be already running...");
                break;
            default:
                logger.error("Unexpected currentJitsiState");
                break;
            }
            break;
        case RingingMe:
            switch (currentJitsiState) {
            case Off:
                break;
            case Idle:
            case Error:
            case Ongoing:
                logger.warn("Asking Jitsi to stop...");
                pubSubHub.publish(PubSubTopics.JITSI_PLEASE_STOP, null);
                break;
            default:
                logger.error("Unexpected currentJitsiState");
                break;
            }
            break;
        default:
            logger.error("Unexpected desiredState");
            break;
        }
        logger.info("Reconciling finished.");
        reconcileHandler.removeCallbacks(reconcileRunnable);
        reconcileHandler.postDelayed(reconcileRunnable, 2000);
    }

    private void reportFailure() {
        if (this.desiredState == NavCallDesiredState.Ongoing) {
            logger.info("Reporting Jitsi failure...");
            navCallApi.reportJitsiFailure(authService.getAuthToken(), currentCallId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            try {
                                logger.error(String.format("Failed to report Jitsi failure: %s",
                                    response.raw().body().string()));
                            } catch (Exception e) {
                                logger.error("Failed to report Jitsi failure.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        logger.error("Failed to report Jitsi failure.");
                    }
                });
        }
    }

    @Override
    public void initialise() {
        logger.info("Init");

        stateCheckHandler.post(new Runnable() {
            @Override
            public void run() {
                checkCallState();
                stateCheckHandler.postDelayed(this, 5000);
            }
        });

        reconcileHandler.removeCallbacks(reconcileRunnable);
        reconcileHandler.post(reconcileRunnable);
    }

    /**
     * Proactively check call state.
     */
    private void checkCallState() {
        logger.info("Checking call state of current navigation....");
        navigationService.getCurrentNavigationSession()
            .thenAcceptAsync(navSessionActionResult -> {
                if (navSessionActionResult.isSuccessful()
                    && navSessionActionResult.unwrap().getId() > 0) {
                    NavCallDto result = navSessionActionResult.unwrap().getCall();
                    synchronized (this) {
                        if (navSessionActionResult.unwrap().getId() > currentNavSessionId) {
                            currentNavSessionId = navSessionActionResult.unwrap().getId();
                            currentCallId = -1;
                            lastSyncNumber = -1;
                        }
                    }
                    if (result == null) {
                        updateDesiredState(NavCallDesiredState.Off);
                        return;
                    }
                    ensureSyncTokenValid(result.getId(), result.getSyncToken(), () -> {
                        currentNavSessionId = result.getSessionId();

                        switch (result.getState()) {
                        case Ongoing:
                            updateDesiredState(NavCallDesiredState.Ongoing);
                            break;
                        case Pending: {
                            User.UserType userType = authService
                                .getCurrentUser().getUserType();
                            boolean amInitiator =
                                (result.getCarerIsInitiator()
                                 && userType == User.UserType.Carer)
                                || (!result.getCarerIsInitiator()
                                    && userType == User.UserType.AP);
                            if (amInitiator) {
                                updateDesiredState(NavCallDesiredState.RingingOther);
                            } else {
                                updateDesiredState(NavCallDesiredState.RingingMe);
                            }
                            break;
                        }
                        case Terminated:
                            updateDesiredState(NavCallDesiredState.Off);
                            break;
                        default:
                            break;
                        }
                    });
                } else if (navSessionActionResult.isSuccessful()
                           && navSessionActionResult.unwrap().getId() <= 0) {
                    logger.warn("No nav sessions..");
                    synchronized (this) {
                        currentNavSessionId = -1;
                        currentCallId = -1;
                        lastSyncNumber = -1;
                    }
                    updateDesiredState(NavCallDesiredState.Off);
                } else {
                    logger.warn("Error during checkCallState. Probably network issue.");
                }
            });
    }

    @Override
    public void destroy() {
        this.subscriptions.dispose();
    }

    @Override
    public void acceptIncomingCall() {
        if (desiredState == NavCallDesiredState.RingingMe) {
            if (authService.isLoggedInUnboxed()) {
                navCallApi.acceptNavCall(authService.getAuthToken(), this.currentCallId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!response.isSuccessful()) {
                                String errorMsg;
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception e) {
                                    errorMsg = "Unknown error";
                                }
                                publishToastMessage(String.format("Failed to accept the call: %s",
                                    errorMsg));
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            publishToastMessage("Network error. Try again.");
                        }
                    });
            }
        } else {
            logger.warn("No incoming call...");
        }
    }

    @Override
    public void declineIncomingCall() {
        if (desiredState == NavCallDesiredState.RingingMe) {
            if (authService.isLoggedInUnboxed()) {
                navCallApi.declineNavCall(authService.getAuthToken(), this.currentCallId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!response.isSuccessful()) {
                                String errorMsg;
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception e) {
                                    errorMsg = "Unknown error";
                                }
                                publishToastMessage(String.format("Failed to decline the call: %s",
                                    errorMsg));
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            publishToastMessage("Network error. Try again.");
                        }
                    });
            }
        } else {
            logger.warn("No incoming call...");
        }
    }

    @Override
    public void stopOngoingCall() {
        if (desiredState == NavCallDesiredState.Ongoing
            || desiredState == NavCallDesiredState.RingingOther) {
            if (authService.isLoggedInUnboxed()) {
                navCallApi.endNavCall(authService.getAuthToken(), this.currentCallId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!response.isSuccessful()) {
                                String errorMsg;
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception e) {
                                    errorMsg = "Unknown error";
                                }
                                publishToastMessage(String.format("Failed to stop the call: %s",
                                    errorMsg));
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            publishToastMessage("Network error. Try again.");
                        }
                    });
            }
        } else {
            logger.warn("No ongoing call...");
        }
    }

    @Override
    public void startCallForSession(int sessionId) {
        if (desiredState != NavCallDesiredState.Ongoing) {
            updateDesiredState(NavCallDesiredState.StartRequested);
            publishToastMessage("Starting voice call...");
            if (authService.isLoggedInUnboxed()) {
                navCallApi.startNavCall(authService.getAuthToken(), sessionId)
                    .enqueue(new Callback<NavCallDto>() {
                        @Override
                        public void onResponse(Call<NavCallDto> call,
                                               Response<NavCallDto> response) {
                            if (!response.isSuccessful()) {
                                String errorMsg;
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception e) {
                                    errorMsg = "Unknown error";
                                }
                                publishToastMessage(
                                    String.format("Failed to start call: %s", errorMsg));
                            } else {
                                updateDesiredState(NavCallDesiredState.RingingOther);
                            }
                        }

                        @Override
                        public void onFailure(Call<NavCallDto> call, Throwable t) {
                            updateDesiredState(NavCallDesiredState.Off);
                        }
                    });
            }
        } else {
            logger.warn("Has ongoing call, cannot start another");
        }
    }

    @Override
    public LiveData<NavCallDesiredState> getState() {
        return desiredStateLiveData;
    }

    /**
     * Publish a toast message.
     * (Somewhere else in the UI it gets picked up and displayed as a toast message).
     * @param message The message to display.
     */
    public void publishToastMessage(String message) {
        pubSubHub.publish(PubSubTopics.NAV_COORDINATOR_ERROR_MESSAGE, message);
    }


    @Override
    public LiveData<Boolean> getIsRemoteCameraOn() {
        return remoteCameraOn;
    }

    @Override
    public void setRemoteCameraOn(boolean isOn) {
        this.remoteCameraOn.postValue(isOn);
    }
}


interface NavCallApi {
    @POST("navigation/{id}/call")
    Call<NavCallDto> startNavCall(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId
    );

    @POST("calls/{id}/accept")
    Call<Void> acceptNavCall(
        @Header("Authorization") String authToken,
        @Path("id") int callId
    );


    @POST("calls/{id}/end")
    Call<Void> endNavCall(
        @Header("Authorization") String authToken,
        @Path("id") int callId
    );

    @POST("calls/{id}/reject")
    Call<Void> declineNavCall(
        @Header("Authorization") String authToken,
        @Path("id") int callId
    );

    @POST("calls/{id}/failure")
    Call<Void> reportJitsiFailure(
        @Header("Authorization") String authToken,
        @Path("id") int callId
    );


    @GET("calls/{id}")
    Call<NavCallDto> getCallState(
        @Header("Authorization") String authToken,
        @Path("id") int callId
    );
}


enum JitsiState {
    Off,
    Idle,
    Starting,
    Ongoing,
    Error
}