package com.comp30022.team_russia.assist;

import org.junit.Before;
import org.junit.Test;

import android.arch.lifecycle.Observer;

import com.comp30022.team_russia.assist.base.NavigationEventArgs;
import com.comp30022.team_russia.assist.features.login.vm.RegisterChooseTypeViewModel;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RegisterChooseTypeViewModelTest extends TestBase {

    private RegisterChooseTypeViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new RegisterChooseTypeViewModel();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_be_AP() {
        Observer<NavigationEventArgs> observer = mock(Observer.class);

        viewModel.navigateAction.observeForever(observer);
        viewModel.onUserTypeSelected(true);

        // verify that "navigationAction" is fired
        verify(observer, atLeastOnce()).onChanged(any());

        NavigationEventArgs args = viewModel.navigateAction.getValue();
        assertEquals(R.id.action_register_typechosen, Objects.requireNonNull(args).getActionId());
    }
}
