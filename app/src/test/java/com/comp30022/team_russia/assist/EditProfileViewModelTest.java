package com.comp30022.team_russia.assist;

import java9.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.models.ProfileDto;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsServiceImpl;
import com.comp30022.team_russia.assist.features.profile.vm.EditProfileViewModel;
import com.comp30022.team_russia.assist.util.LastCall;
import com.comp30022.team_russia.assist.util.TestToastService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class EditProfileViewModelTest extends TestBase {

    private AuthService authService;

    private ToastService toastService;

    private ProfileDetailsService profileDetailsService;

    private EditProfileViewModel viewModel;

    private AssistedPerson testAP;

    @Before
    public void setUp() {
        toastService = new TestToastService();
        authService = mock(AuthService.class);
        profileDetailsService = mock(ProfileDetailsServiceImpl.class);

        MutableLiveData<Boolean> trueLiveData = new MutableLiveData<>();
        trueLiveData.setValue(true);

        when(authService.isLoggedIn()).thenReturn(trueLiveData);

        when(profileDetailsService.update(any())).thenReturn(CompletableFuture.completedFuture(true));
        when(profileDetailsService.updatePassword(any())).thenReturn(CompletableFuture.completedFuture(true));

        testAP = new AssistedPerson(1, "x", "123", "X",
            "12345678900", "2000-01-01", "y",
            "09876543211");

        when(authService.getCurrentUser()).thenReturn(testAP);
        when(profileDetailsService.getCurrentUser()).thenReturn(testAP);

        viewModel = new EditProfileViewModel(authService, toastService, profileDetailsService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void confirm_btn_makes_network_request() {
        Observer<Boolean> isBusy_observer = mock(Observer.class);
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);

        viewModel.isBusy.observeForever(isBusy_observer);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);

        viewModel.confirmClicked();

        verify(isBusy_observer, atLeastOnce()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void confirm_btn_ends_network_request_on_success() {
        Observer<Boolean> isBusy_observer = mock(Observer.class);
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);

        viewModel.isBusy.observeForever(isBusy_observer);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);

        viewModel.confirmClicked();

        verify(isBusy_observer, LastCall.lastCall()).onChanged(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updating_profile_should_exit_edit_mode() {
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);
        Observer<Boolean> shouldExitEditMode_observer = mock(Observer.class);

        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);
        viewModel.shouldExitEditMode.observeForever(shouldExitEditMode_observer);

        viewModel.confirmClicked();

        verify(shouldExitEditMode_observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updating_name_should_change_name_in_profile() {
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);

        viewModel.name.setValue("A");

        viewModel.confirmClicked();

        // Confirm the update method uses the new value for name.
        ArgumentCaptor<ProfileDto> captor = ArgumentCaptor.forClass(ProfileDto.class);
        verify(profileDetailsService, atLeastOnce()).update(captor.capture());

        assertEquals("A", captor.getValue().name);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updating_mobile_number_should_change_mobile_number_in_profile() {
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);

        viewModel.mobileNumber.setValue("123");

        viewModel.confirmClicked();

        // Confirm the update method uses the new value for mobile number.
        ArgumentCaptor<ProfileDto> captor = ArgumentCaptor.forClass(ProfileDto.class);
        verify(profileDetailsService, atLeastOnce()).update(captor.capture());

        assertEquals("123", captor.getValue().mobileNumber);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updating_dob_should_change_dob_in_profile() {
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);
        
        viewModel.birthDate.setValue("2018-08-31");

        viewModel.confirmClicked();

        // Confirm the update method uses the new value for date of birth.
        ArgumentCaptor<ProfileDto> captor = ArgumentCaptor.forClass(ProfileDto.class);
        verify(profileDetailsService, atLeastOnce()).update(captor.capture());

        assertEquals("2018-08-31", captor.getValue().DOB);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updating_emergency_name_should_change_emergency_name_in_profile() {
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);

        viewModel.emergencyName.setValue("B");

        viewModel.confirmClicked();

        // Confirm the update method uses the new value for emergency name.
        ArgumentCaptor<ProfileDto> captor = ArgumentCaptor.forClass(ProfileDto.class);
        verify(profileDetailsService, atLeastOnce()).update(captor.capture());

        assertEquals("B", captor.getValue().emergencyContactName);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updating_emergency_number_should_change_emergency_number_in_profile() {
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);

        viewModel.emergencyNumber.setValue("000");

        viewModel.confirmClicked();

        // Confirm the update method uses the new value for emergency contact number.
        ArgumentCaptor<ProfileDto> captor = ArgumentCaptor.forClass(ProfileDto.class);
        verify(profileDetailsService, atLeastOnce()).update(captor.capture());

        assertEquals("000", captor.getValue().emergencyContactNumber);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updating_password_should_change_password() {
        Observer<Boolean> isAllFieldsValid_observer = mock(Observer.class);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValid_observer);

        viewModel.password.setValue("000000");

        viewModel.confirmClicked();

        // Confirm the updatePassword method uses the new value for password.
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(profileDetailsService, atLeastOnce()).updatePassword(captor.capture());

        assertEquals("000000", captor.getValue());
    }
}
