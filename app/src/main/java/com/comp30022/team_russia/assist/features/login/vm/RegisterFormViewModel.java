package com.comp30022.team_russia.assist.features.login.vm;

import static com.comp30022.team_russia.assist.base.LiveDataHelpers.combineLatest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.models.RegistrationDto;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;

import com.shopify.livedataktx.LiveDataKt;

import javax.inject.Inject;


/**
 * ViewModel for RegisterAP / RegisterCarer screen.
 */
public class RegisterFormViewModel extends BaseViewModel {

    /**
     * Whether the registration form is for AP (true) or Carer (false).
     */
    public final MutableLiveData<Boolean> isAp = new MutableLiveData<>();

    /**
     * The "Name" field.
     */
    public final MutableLiveData<String> name = new MutableLiveData<>();

    /**
     * Whether the "Name" field is valid.
     */
    public final LiveData<Boolean> isNameValid;

    /**
     * The "Birth date" field.
     */
    public final MutableLiveData<String> birthDate = new MutableLiveData<>();

    /**
     * Whether the "Birth date" field is valid.
     */
    public final LiveData<Boolean> isBirthDateValid;

    /**
     * The "Mobile number" field.
     */
    public final MutableLiveData<String> mobileNumber = new MutableLiveData<>();

    /**
     * Whether the "Mobile number" field is valid.
     */
    public final LiveData<Boolean> isMobileNumberValid;

    /**
     * The "Username" field.
     */
    public final MutableLiveData<String> username = new MutableLiveData<>();

    /**
     * Whether the "Username" field is valid.
     */
    public final LiveData<Boolean> isUsernameValid;

    /**
     * The "Password" field.
     */
    public final MutableLiveData<String> password = new MutableLiveData<>();

    /**
     * Whether the "Password" field is valid.
     */
    public final LiveData<Boolean> isPasswordValid;

    /**
     * The "Emergency contact name" field.
     */
    public final MutableLiveData<String> emergencyName = new MutableLiveData<>();

    /**
     * Whether the "Emergency contact name" field is valid.
     */
    public final LiveData<Boolean> isEmergencyNameValid;

    /**
     * The "Emergency contact number" field.
     */
    public final MutableLiveData<String> emergencyNumber = new MutableLiveData<>();

    /**
     * Whether the "Emergency contact number" field is valid.
     */
    public final LiveData<Boolean> isEmergencyNumberValid;

    /**
     * Whether or not all the fields are valid (i.e. can be submitted).
     */
    public final LiveData<Boolean> isAllFieldsValid;


    public final LiveData<Boolean> isConfirmButtonEnabled;

    public final MutableLiveData<Boolean> isBusy = new MutableLiveData<>();

    /**
     * Authentication service.
     */
    private final AuthService authService;

    private final ToastService toastService;

    /**
     * Constructor.
     */
    @Inject
    public RegisterFormViewModel(AuthService authService, ToastService toastService) {
        this.authService = authService;
        this.toastService = toastService;

        isAp.setValue(false);

        clearFields();
        isBusy.postValue(false);
        // Setup validation
        // Both
        isNameValid = LiveDataKt.map(name, value -> !value.isEmpty());
        isBirthDateValid = LiveDataKt.map(birthDate, User::isValidDoB);
        isMobileNumberValid = LiveDataKt.map(mobileNumber,
            value -> !value.isEmpty());
        isUsernameValid = LiveDataKt.map(username, value -> !value.isEmpty());
        isPasswordValid = LiveDataKt.map(password, value -> !value.isEmpty());
        // AP-only fields
        isEmergencyNameValid = combineLatest(isAp, emergencyName,
            (isAp, value) -> !isAp || !value.isEmpty());
        isEmergencyNumberValid = combineLatest(isAp, emergencyNumber,
            (isAp, value) -> !isAp || !value.isEmpty());


        isAllFieldsValid = combineLatest(
            // the sources (to compute from)
            isNameValid,
            isBirthDateValid,
            isMobileNumberValid,
            isUsernameValid,
            isPasswordValid,
            isEmergencyNameValid,
            isEmergencyNumberValid,
            // mapper functions that maps (bool, bool, ..., bool) to bool.
            (nameValid,
             birthDateValid,
             mobileNumberValid,
             usernameValid,
             passwordValid,
             emNameValid,
             emNumberValid) ->
                nameValid != null
                && birthDateValid != null
                && mobileNumberValid != null
                && usernameValid != null
                && passwordValid != null
                && emNameValid != null
                && emNumberValid != null
                && nameValid
                && birthDateValid
                && mobileNumberValid
                && usernameValid
                && passwordValid
                && emNameValid
                && emNumberValid
        );

        isConfirmButtonEnabled = combineLatest(isAllFieldsValid, isBusy,
            (fieldsValue, busy) -> fieldsValue != null
                                   && busy != null && fieldsValue && !busy);

    }

    /**
     * Handler for clicking on the "Confirm" button.
     */
    public void confirmClicked() {
        if (isAllFieldsValid.getValue() != null && isAllFieldsValid.getValue()) {
            isBusy.postValue(true);
            toastService.toastShort("Registering...");

            authService.register(getRegistrationDto()).thenAccept(result -> {
                if (result.isSuccessful()) {
                    toastService.toastShort("Registered successfully");
                } else {
                    if (result.getErrorType() == ActionResult.CUSTOM_ERROR
                        && result.getErrorMessage().equals("Username is taken")) {
                        toastService.toastShort("Username is taken");
                    } else {
                        toastService.toastShort("Registration failed");
                    }
                }
                isBusy.postValue(false);
            });
        }
    }

    private RegistrationDto getRegistrationDto() {
        try {
            return new RegistrationDto(this.username.getValue(),
                this.password.getValue(),
                this.isAp.getValue() ? User.UserType.AP : User.UserType.Carer,
                this.name.getValue(),
                this.mobileNumber.getValue(),
                this.birthDate.getValue(),
                this.emergencyName.getValue(),
                this.emergencyNumber.getValue()
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void clearFields() {
        name.setValue("");
        username.setValue("");
        password.setValue("");
        mobileNumber.setValue("");
        password.setValue("");
        emergencyName.setValue("");
        emergencyNumber.setValue("");
        birthDate.setValue("");
    }

}
