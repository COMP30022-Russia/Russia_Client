package com.comp30022.team_russia.assist;

import org.junit.Before;
import org.junit.Test;

import android.arch.lifecycle.Observer;

import com.comp30022.team_russia.assist.base.NavigationEventArgs;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.login.vm.RegisterChooseTypeViewModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RegisterChooseTypeViewModelTest extends TestBase {

    private AuthService authService;

    private RegisterChooseTypeViewModel viewModel;

    @Before
    public void setUp() {
        authService = mock(AuthService.class);

        viewModel = new RegisterChooseTypeViewModel();
    }

    @Test
    public void should_be_AP() {
        Observer<NavigationEventArgs> observer = mock(Observer.class);

        viewModel.navigateAction.observeForever(observer);
        viewModel.onUserTypeSelected(true);

        // verify that "navigationAction" is fired
        verify(observer, atLeastOnce()).onChanged(any());

        NavigationEventArgs args = viewModel.navigateAction.getValue();
        assertEquals(R.id.action_register_typechosen, args.getActionId());
    }
}
