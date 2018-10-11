package com.comp30022.team_russia.assist;

import java9.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;

import android.arch.lifecycle.Observer;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.assoc.vm.GenerateQrViewModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class GenerateQRViewModelTest extends TestBase {

    private GenerateQrViewModel viewModel;

    private UserService userService;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        userService = mock(UserService.class);
    }

    /**
     * Should retrieve QR when initialising VM.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void should_display_qr() {
        // Get userService to return token
        when(userService.getAssociateToken()).thenReturn(
            CompletableFuture.completedFuture(
                new ActionResult<>("my token")));
        viewModel = new GenerateQrViewModel(userService, executorService);

        // Verify token
        Observer<String> observer = mock(Observer.class);
        viewModel.token.observeForever(observer);
        assertEquals("my token", viewModel.token.getValue());

        // Ensure userService is called
        verify(userService, atLeastOnce()).getAssociateToken();
    }

    /**
     * Should retrieve QR when initialising VM.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void should_report_error_on_failure() {
        // Get userService to return error
        when(userService.getAssociateToken()).thenReturn(
            CompletableFuture.completedFuture(new ActionResult<>(ActionResult.NETWORK_ERROR)));
        viewModel = new GenerateQrViewModel(userService, executorService);

        // Ensure that viewModel reports that error has occurred
        Observer<Boolean> observer = mock(Observer.class);
        viewModel.hasError.observeForever(observer);
        assertEquals(true, viewModel.hasError.getValue());
        verify(userService, atLeastOnce()).getAssociateToken();
    }
}
