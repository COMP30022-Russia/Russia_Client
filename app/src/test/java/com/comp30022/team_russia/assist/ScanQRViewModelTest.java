package com.comp30022.team_russia.assist;

import java9.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;

import android.arch.lifecycle.Observer;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.assoc.vm.ScanQrViewModel;
import com.comp30022.team_russia.assist.util.LastCall;

import static org.mockito.Mockito.*;

/**
 * Unit test for ScanQRViewModel
 */
public class ScanQRViewModelTest extends TestBase {

    private ScanQrViewModel viewModel;

    private UserService userService;

    @Before
    public void setUp() {
        userService = mock(UserService.class);

        viewModel = new ScanQrViewModel(userService, executorService);

        when(userService.associateWith("test"))
            .thenReturn(CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_make_network_request_during_scan() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isBusy.observeForever(observer);

        viewModel.onScanResult("test");

        verify(observer, atLeastOnce()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void user_service_should_respond_to_scanned_code() {
        Observer<String> observer = mock(Observer.class);
        Observer<Boolean> observer1 = mock(Observer.class);

        viewModel.isBusy.observeForever(observer1);
        viewModel.toastMessage.observeForever(observer);

        viewModel.onScanResult("test");

        verify(observer, LastCall.lastCall()).onChanged("Error");
        verify(userService, atLeastOnce()).associateWith(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void network_request_should_end_when_user_service_returns() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isBusy.observeForever(observer);

        viewModel.onScanResult("test");

        verify(observer, LastCall.lastCall()).onChanged(false);
        verify(userService, atLeastOnce()).associateWith(any());
    }
}
