package com.comp30022.team_russia.assist;

import org.junit.Before;
import org.junit.Test;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.NavigationEventArgs;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsServiceImpl;
import com.comp30022.team_russia.assist.features.profile.vm.ProfileViewModel;
import com.comp30022.team_russia.assist.util.LastCall;
import com.comp30022.team_russia.assist.util.TestLoggerFactory;
import com.comp30022.team_russia.assist.util.TestToastService;

import java9.util.concurrent.CompletableFuture;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ProfileViewModelTest extends TestBase {

    private AuthService authService;

    private ProfileDetailsService profileDetailsService;

    private ToastService toastService;

    private LoggerFactory loggerFactory;

    private ProfileViewModel viewModel;

    private AssistedPerson testAP;

    @Before
    public void setUp() {
        toastService = new TestToastService();
        authService = mock(AuthService.class);
        profileDetailsService = mock(ProfileDetailsServiceImpl.class);
        loggerFactory = new TestLoggerFactory();

        /* Need some kind of initialisation. */
        MutableLiveData<Boolean> trueLiveData = new MutableLiveData<>();
        trueLiveData.setValue(true);

        when(authService.isLoggedIn()).thenReturn(trueLiveData);

        when(profileDetailsService.getDetails()).thenReturn(CompletableFuture.completedFuture(true));
        when(profileDetailsService.getPic()).thenReturn(CompletableFuture.completedFuture(true));
        when(profileDetailsService.updatePic(any())).thenReturn(CompletableFuture.completedFuture(true));

        testAP = new AssistedPerson(1, "x", "123", "X",
            "12345678900", "2000-01-01", "y",
            "09876543211");

        when(authService.getCurrentUser()).thenReturn(testAP);
        when(profileDetailsService.getCurrentUser()).thenReturn(testAP);

        viewModel = new ProfileViewModel(authService, profileDetailsService, toastService, loggerFactory);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reload_should_update_isAp() {
        Observer<Boolean> isAp_observer = mock(Observer.class);

        viewModel.isAp.observeForever(isAp_observer);

        viewModel.reload();

        verify(isAp_observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reload_should_update_name() {
        Observer<String> name_observer = mock(Observer.class);

        viewModel.name.observeForever(name_observer);

        viewModel.reload();

        verify(name_observer, LastCall.lastCall()).onChanged("X");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reload_should_update_birth_date() {
        Observer<String> birth_date_observer = mock(Observer.class);

        viewModel.birthDate.observeForever(birth_date_observer);

        viewModel.reload();

        verify(birth_date_observer, LastCall.lastCall()).onChanged("2000-01-01");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reload_should_update_mobile_number() {
        Observer<String> mobile_number_observer = mock(Observer.class);

        viewModel.mobileNumber.observeForever(mobile_number_observer);

        viewModel.reload();

        verify(mobile_number_observer, LastCall.lastCall()).onChanged("12345678900");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reload_should_update_emergency_name_if_user_isAp() {
        Observer<Boolean> isAp_observer = mock(Observer.class);
        Observer<String> emergency_name_observer = mock(Observer.class);

        viewModel.isAp.observeForever(isAp_observer);
        viewModel.emergencyName.observeForever(emergency_name_observer);

        viewModel.reload();

        verify(isAp_observer, LastCall.lastCall()).onChanged(true);
        verify(emergency_name_observer, LastCall.lastCall()).onChanged("y");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reload_should_update_emergency_number_if_user_isAp() {
        Observer<Boolean> isAp_observer = mock(Observer.class);
        Observer<String> emergency_number_observer = mock(Observer.class);

        viewModel.isAp.observeForever(isAp_observer);
        viewModel.emergencyNumber.observeForever(emergency_number_observer);

        viewModel.reload();

        verify(isAp_observer, LastCall.lastCall()).onChanged(true);
        verify(emergency_number_observer, LastCall.lastCall()).onChanged("09876543211");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void update_pic_should_change_profile_pic() {
        viewModel.updatePic("test");

        // Confirm the updatePic method uses the correct path.
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(profileDetailsService, atLeastOnce()).updatePic(captor.capture());

        assertEquals("test", captor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void edit_profile_btn_should_navigate_to_edit_profile() {
        Observer<NavigationEventArgs> navigateAction_observer = mock(Observer.class);
        viewModel.navigateAction.observeForever(navigateAction_observer);

        viewModel.onEditProfileButtonClicked();

        verify(navigateAction_observer, atLeastOnce()).onChanged(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void logout_btn_should_log_user_out_of_app() {
        viewModel.logout();
        verify(authService, atLeastOnce()).logout();
    }
}