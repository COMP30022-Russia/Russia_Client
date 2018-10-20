package com.comp30022.team_russia.assist.features.nav.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.DisposableCollection;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.SingleLiveEvent;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.jitsi.services.VoiceCoordinator;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.nav.services.NavigationService;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;

import com.shopify.livedataktx.LiveDataKt;

import javax.inject.Inject;


/**
 * Sub-ViewModel for the voice call related features inside {@link NavigationViewModel}.
 */
public class NavVoiceCallViewModel extends ViewModel {

    private enum State {
        NoCall,
        Starting,
        Incoming,
        OnGoing,
        Stopping
    }

    public final MutableLiveData<Boolean> showVoiceCallButton = new MutableLiveData<>();

    public final LiveData<Boolean> showRearCameraButton;

    public final LiveData<Integer> voiceCallButtonIcon;

    public final LiveData<Integer> voiceCallButtonBackground;

    public final MediatorLiveData<State> voiceCallState = new MediatorLiveData<>();

    public final SingleLiveEvent<Void> showConfirmAcceptDialog = new SingleLiveEvent<>();

    public final SingleLiveEvent<Void> showConfirmEndDialog = new SingleLiveEvent<>();

    public final LiveData<Boolean> shouldAnimate;

    private final LiveData<Boolean> isVoiceCallOn;

    private final PubSubHub pubSubHub;

    private final LoggerInterface logger;

    private final NavigationService navigationService;

    private final VoiceCoordinator voiceCoordinator;

    private final AuthService authService;

    private final ToastService toastService;

    private final DisposableCollection subscriptions = new DisposableCollection();

    /**
     * Constructor.
     * @param pubSubHub {@link PubSubHub} instance.
     * @param loggerFactory {@link LoggerFactory} instance.
     * @param voiceCoordinator {@link VoiceCoordinator} instance.
     * @param authService {@link AuthService} instance.
     * @param navigationService {@link NavigationService} instance.
     * @param toastService {@link ToastService} instance.
     */
    @Inject
    public NavVoiceCallViewModel(PubSubHub pubSubHub,
                                 LoggerFactory loggerFactory,
                                 VoiceCoordinator voiceCoordinator,
                                 AuthService authService,
                                 NavigationService navigationService,
                                 ToastService toastService
    ) {
        this.pubSubHub = pubSubHub;
        this.logger = loggerFactory.getLoggerForClass(this.getClass());
        this.navigationService = navigationService;
        this.voiceCoordinator = voiceCoordinator;
        this.toastService = toastService;
        this.authService = authService;

        this.voiceCallState.addSource(voiceCoordinator.getState(), desiredState -> {
            if (desiredState != null) {
                switch (desiredState) {
                case Off:
                    voiceCallState.postValue(State.NoCall);
                    break;
                case Ongoing:
                    voiceCallState.postValue(State.OnGoing);
                    break;
                case RingingMe:
                    voiceCallState.postValue(State.Incoming);
                    break;
                case RingingOther:
                    voiceCallState.postValue(State.Starting);
                    break;
                case StartRequested:
                    voiceCallState.postValue(State.Starting);
                    break;
                default:
                    break;
                }
            }
        });
        showVoiceCallButton.postValue(true);
        voiceCallButtonIcon = LiveDataKt.map(voiceCallState,
            voiceCallState -> {
                switch (voiceCallState) {
                case NoCall:
                case Incoming:
                    return R.drawable.ic_call_holo_dark;
                case OnGoing:
                    return R.drawable.ic_call_end_black;
                case Starting:
                case Stopping:
                    return R.drawable.ic_call_wait;
                default:
                    return R.drawable.ic_call_holo_dark;
                }
            });
        voiceCallButtonBackground = LiveDataKt.map(voiceCallState,
            voiceCallState -> {
                switch (voiceCallState) {
                case Starting:
                case NoCall:
                    return R.drawable.bg_accent_circle;
                case Stopping:
                case OnGoing:
                    return R.drawable.bg_red_circle;
                default:
                    return R.drawable.bg_accent_circle;
                }
            }
        );

        showRearCameraButton = LiveDataKt.map(voiceCallState,
            state -> (state == State.OnGoing && authService.isLoggedInUnboxed()
                      && authService.getCurrentUser().getUserType() == User.UserType.Carer));
        isVoiceCallOn = LiveDataKt.map(voiceCallState,
            voiceCallState -> voiceCallState == State.OnGoing);
        shouldAnimate = LiveDataKt.map(voiceCallState,
            voiceCallState -> voiceCallState == State.Incoming);
        voiceCallState.postValue(State.NoCall);

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_COORDINATOR_ERROR_MESSAGE,
            new SubscriberCallback<String>() {
                @Override
                public void onReceived(String payload) {
                    toastService.toastShort(payload);
                }
            }));
    }

    /**
     * Handle start/stop call button clicked.
     */
    public void onToggleButtonClicked() {
        voiceCoordinator.setRemoteCameraOn(false);

        State currentState = voiceCallState.getValue();
        if (currentState == null) {
            currentState = State.NoCall;
        }

        switch (currentState) {
        case NoCall:
            logger.info("Toggle button clicked. Starting voice call.");
            navigationService.getCurrentNavigationSession()
                .thenAcceptAsync(navSessionActionResult -> {
                    if (navSessionActionResult.isSuccessful()) {
                        int navSessionId = navSessionActionResult.unwrap().getId();
                        voiceCoordinator.startCallForSession(navSessionId);
                    }
                });
            break;
        case Incoming:
            showConfirmAcceptDialog.call();
            break;
        case Starting:
        case OnGoing:
            showConfirmEndDialog.call();
            break;
        default:
            logger.warn("Unknown currentState " + currentState.toString());
        }
    }


    /**
     * Handle show back camera button clicked.
     */
    public void onShowRearCameraButtonClicked() {
        Boolean tmp = voiceCoordinator.getIsRemoteCameraOn().getValue();
        boolean unboxed = tmp != null && tmp;
        voiceCoordinator.setRemoteCameraOn(!unboxed);
    }

    public void onDecline() {
        voiceCoordinator.declineIncomingCall();
    }

    public void onAccept() {
        voiceCoordinator.setRemoteCameraOn(false);
        voiceCoordinator.acceptIncomingCall();
    }

    public void onEnd() {
        voiceCoordinator.stopOngoingCall();
    }

    public void enable() {

    }

    public void disable() {

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        subscriptions.dispose();
    }
}
