package com.comp30022.team_russia.assist;

import java9.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;

import android.arch.lifecycle.Observer;

import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.models.RegistrationDto;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.login.vm.RegisterFormViewModel;
import com.comp30022.team_russia.assist.util.LastCall;
import com.comp30022.team_russia.assist.util.TestToastService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RegisterFormViewModelTest extends TestBase {

    private AuthService authService;

    private RegisterFormViewModel viewModel;

    @Before
    public void setUp() {
        ToastService toastService = new TestToastService();
        authService = mock(AuthService.class);

        viewModel = new RegisterFormViewModel(authService, toastService);

        // Define success case
        RegistrationDto test_reg = new RegistrationDto("stevenL",
            "test",
            User.UserType.AP,
            "Steven",
            "0412123123",
            "1996-06-10",
            "Dom",
            "0412345678");
        when(authService.register(test_reg)).thenReturn(CompletableFuture.completedFuture(true));
    }

    /**
     *  Input test data into every field.
     */
    private void set_valid_ap_fields(RegisterFormViewModel viewModel) {
        viewModel.username.setValue("stevenL");
        viewModel.password.setValue("test");
        viewModel.isAp.setValue(true);
        viewModel.name.setValue("Steven");
        viewModel.mobileNumber.setValue("0412123123");
        viewModel.birthDate.setValue("1996-06-10");
        viewModel.emergencyName.setValue("Dom");
        viewModel.emergencyNumber.setValue("0412345678");
    }

    private void set_incomplete_fields(RegisterFormViewModel viewModel) {
        viewModel.username.setValue("dommychi");
        viewModel.password.setValue("");
        viewModel.isAp.setValue(false);
        viewModel.name.setValue("Dom");
        viewModel.mobileNumber.setValue("0");
        viewModel.birthDate.setValue("");
        viewModel.emergencyName.setValue("");
        viewModel.emergencyNumber.setValue("");
    }

    private void set_ap_fields_with_missing_emergency_values(RegisterFormViewModel viewModel) {
        viewModel.username.setValue("a");
        viewModel.password.setValue("a");
        viewModel.isAp.setValue(true);
        viewModel.name.setValue("a");
        viewModel.mobileNumber.setValue("1");
        viewModel.birthDate.setValue("2000-01-01");
        viewModel.emergencyName.setValue("");
        viewModel.emergencyNumber.setValue("");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void name_should_be_valid() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isNameValid.observeForever(observer);

        viewModel.name.setValue("Steven");

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void birth_date_should_be_valid() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isBirthDateValid.observeForever(observer);

        viewModel.birthDate.setValue("1996-06-10");

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mobile_number_should_be_valid() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isMobileNumberValid.observeForever(observer);

        viewModel.mobileNumber.setValue("0412123123");

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void username_should_be_valid() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isUsernameValid.observeForever(observer);

        viewModel.username.setValue("stevenL");

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void password_should_be_valid() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isPasswordValid.observeForever(observer);

        viewModel.password.setValue("test");

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void emergency_name_should_be_valid() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isEmergencyNameValid.observeForever(observer);

        viewModel.emergencyName.setValue("Dom");

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void emergency_number_should_be_valid() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isEmergencyNumberValid.observeForever(observer);

        viewModel.emergencyNumber.setValue("0412345678");

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void all_fields_should_be_valid() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isAllFieldsValid.observeForever(observer);

        set_valid_ap_fields(viewModel);

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void all_fields_should_not_be_valid_if_not_complete() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isAllFieldsValid.observeForever(observer);

        set_incomplete_fields(viewModel);

        verify(observer, LastCall.lastCall()).onChanged(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void registration_should_make_a_network_request() {
        Observer<Boolean> isBusyObserver = mock(Observer.class);
        Observer<Boolean> isAllFieldsValidObserver = mock(Observer.class);

        viewModel.isBusy.observeForever(isBusyObserver);
        viewModel.isAllFieldsValid.observeForever(isAllFieldsValidObserver);

        set_valid_ap_fields(viewModel);
        viewModel.confirmClicked();

        verify(isBusyObserver, atLeastOnce()).onChanged(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void registration_success_should_end_network_request() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isBusy.observeForever(observer);
        // Need to observe this so it isn't null.
        viewModel.isAllFieldsValid.observeForever(observer);

        set_valid_ap_fields(viewModel);
        viewModel.confirmClicked();

        verify(observer, LastCall.lastCall()).onChanged(false);
        verify(authService, atLeastOnce()).register(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void registration_success_should_not_clear_fields() {
        // Observe all fields
        Observer<Boolean> observer = mock(Observer.class);
        viewModel.isAllFieldsValid.observeForever(observer);

        // Set all fields
        set_valid_ap_fields(viewModel);

        // Get values before submission
        String name = viewModel.name.getValue();
        String username = viewModel.username.getValue();
        String password = viewModel.password.getValue();
        String mobile_number = viewModel.mobileNumber.getValue();
        String emergency_name = viewModel.emergencyName.getValue();
        String emergency_number = viewModel.emergencyNumber.getValue();
        String birth_date = viewModel.birthDate.getValue();

        // Simulate register click
        viewModel.confirmClicked();
        verify(authService, atLeastOnce()).register(any());
        verify(observer, LastCall.lastCall()).onChanged(true);

        // Ensure that fields are not changed
        assertEquals(viewModel.name.getValue(), name);
        assertEquals(viewModel.username.getValue(), username);
        assertEquals(viewModel.password.getValue(), password);
        assertEquals(viewModel.mobileNumber.getValue(), mobile_number);
        assertEquals(viewModel.emergencyName.getValue(), emergency_name);
        assertEquals(viewModel.emergencyNumber.getValue(), emergency_number);
        assertEquals(viewModel.birthDate.getValue(), birth_date);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void registration_failure_should_not_clear_fields() {
        // Observe all fields
        Observer<Boolean> observer = mock(Observer.class);
        viewModel.isAllFieldsValid.observeForever(observer);

        // Set all fields
        set_incomplete_fields(viewModel);

        // Get values before submission
        String name = viewModel.name.getValue();
        String username = viewModel.username.getValue();
        String password = viewModel.password.getValue();
        String mobile_number = viewModel.mobileNumber.getValue();
        String emergency_name = viewModel.emergencyName.getValue();
        String emergency_number = viewModel.emergencyNumber.getValue();
        String birth_date = viewModel.birthDate.getValue();

        // Simulate register click, note that authService should not be called
        viewModel.confirmClicked();
        verify(observer, LastCall.lastCall()).onChanged(false);
        verify(authService, never()).register(any());

        // Ensure that fields are not changed
        assertEquals(viewModel.name.getValue(), name);
        assertEquals(viewModel.username.getValue(), username);
        assertEquals(viewModel.password.getValue(), password);
        assertEquals(viewModel.mobileNumber.getValue(), mobile_number);
        assertEquals(viewModel.emergencyName.getValue(), emergency_name);
        assertEquals(viewModel.emergencyNumber.getValue(), emergency_number);
        assertEquals(viewModel.birthDate.getValue(), birth_date);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void registration_failure_should_end_network_request() {
        Observer<Boolean> observer = mock(Observer.class);

        viewModel.isBusy.observeForever(observer);
        // Need to observe this so it isn't null.
        viewModel.isAllFieldsValid.observeForever(observer);

        set_incomplete_fields(viewModel);
        viewModel.confirmClicked();

        verify(observer, LastCall.lastCall()).onChanged(false);
    }

    /**
     * Verifies that emergency fields are not required for carer registrations.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void emergency_fields_should_not_be_required_for_carer() {
        Observer<Boolean> emergencyNameObserver = mock(Observer.class);
        Observer<Boolean> emergencyNumberObserver = mock(Observer.class);

        viewModel.isEmergencyNameValid.observeForever(emergencyNameObserver);
        viewModel.isEmergencyNumberValid.observeForever(emergencyNumberObserver);

        viewModel.isAp.setValue(false);
        viewModel.emergencyName.setValue("");
        viewModel.emergencyNumber.setValue("");

        verify(emergencyNameObserver, LastCall.lastCall()).onChanged(true);
        verify(emergencyNumberObserver, LastCall.lastCall()).onChanged(true);
    }

    /**
     * Verifies that emergency fields required for AP registrations.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void emergency_name_should_be_filled_for_ap() {
        Observer<Boolean> isEmergencyNameValidObserver = mock(Observer.class);
        Observer<Boolean> isEmergencyNumberValidObserver = mock(Observer.class);
        viewModel.isEmergencyNameValid.observeForever(isEmergencyNameValidObserver);
        viewModel.isEmergencyNumberValid.observeForever(isEmergencyNumberValidObserver);

        set_ap_fields_with_missing_emergency_values(viewModel);

        verify(isEmergencyNameValidObserver, LastCall.lastCall()).onChanged(false);
        verify(isEmergencyNumberValidObserver, LastCall.lastCall()).onChanged(false);
    }
}
