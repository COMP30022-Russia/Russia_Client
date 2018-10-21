package com.comp30022.team_russia.assist;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.base.pubsub.PubSubHub;
import com.comp30022.team_russia.assist.features.call.services.VoiceCoordinator;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.nav.services.NavigationService;
import com.comp30022.team_russia.assist.features.nav.services.NavigationServiceImpl;
import com.comp30022.team_russia.assist.features.nav.vm.NavVoiceCallViewModel;
import com.comp30022.team_russia.assist.util.TestLoggerFactory;
import com.comp30022.team_russia.assist.util.TestPubSubHub;
import com.comp30022.team_russia.assist.util.TestToastService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class NavVoiceCallViewModelTest extends TestBase {

    private PubSubHub pubSubHub;

    private LoggerFactory loggerFactory;

    private VoiceCoordinator voiceCoordinator;

    private AuthService authService;

    private NavigationService navigationService;

    private ToastService toastService;

    private NavVoiceCallViewModel viewModel;

    @Before
    public void setUp() {
        pubSubHub = new TestPubSubHub();
        loggerFactory = new TestLoggerFactory();
        voiceCoordinator = mock(VoiceCoordinator.class);
        authService = mock(AuthService.class);
        navigationService = mock(NavigationServiceImpl.class);
        toastService = new TestToastService();

        viewModel = new NavVoiceCallViewModel(pubSubHub, loggerFactory, voiceCoordinator, authService, navigationService, toastService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void on_decline_should_decline_call() {
        viewModel.onDecline();

        verify(voiceCoordinator, atLeastOnce()).declineIncomingCall();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void on_accept_should_accept_call() {
        viewModel.onAccept();

        verify(voiceCoordinator, atLeastOnce()).acceptIncomingCall();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void on_accept_should_not_enable_remote_camera() {
        viewModel.onAccept();

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(voiceCoordinator, atLeastOnce()).setRemoteCameraOn(captor.capture());

        assertEquals(false, captor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void on_end_should_terminate_the_call() {
        viewModel.onEnd();

        verify(voiceCoordinator, atLeastOnce()).stopOngoingCall();
    }
}
