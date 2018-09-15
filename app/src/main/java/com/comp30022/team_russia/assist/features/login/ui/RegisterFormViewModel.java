package com.comp30022.team_russia.assist.features.login.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.features.login.models.RegistrationDTO;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.shopify.livedataktx.LiveDataKt;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;


/**
 * ViewModel for RegisterAP / RegisterCarer screen.
 */
public class RegisterFormViewModel extends BaseViewModel {

    /**
     * Whether the registration form is for AP (true) or Carer (false).
     */
    public final MutableLiveData<Boolean> isAP = new MutableLiveData<>();

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
     * The "Home address" field.
     */
    public final MutableLiveData<String> homeAddress = new MutableLiveData<>();

    /**
     * Whether the "Home address" field is valid.
     */
    public final LiveData<Boolean> isHomeAddressValid;

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

    /**
     * Constructor.
     */
    @Inject
    public RegisterFormViewModel(AuthService authService) {
        this.authService = authService;
        isAP.setValue(false);

        clearFields();
        isBusy.postValue(false);
        // Setup validation
        // Both
        isNameValid = LiveDataKt.map(name, value -> !value.isEmpty());
        isBirthDateValid = LiveDataKt.map(birthDate, User::isValidDOB);
        isMobileNumberValid = LiveDataKt.map(mobileNumber,
            value -> !value.isEmpty());
        isUsernameValid = LiveDataKt.map(username, value -> !value.isEmpty());
        isPasswordValid = LiveDataKt.map(password, value -> !value.isEmpty());
        // AP-only fields
        isEmergencyNameValid = combineLatest(isAP, emergencyName,
            (isAP, value) -> !isAP || !value.isEmpty());
        isEmergencyNumberValid = combineLatest(isAP, emergencyNumber,
            (isAP, value) -> !isAP || !value.isEmpty());
        isHomeAddressValid = combineLatest(isAP, homeAddress,
            (isAP, value) -> !isAP || !value.isEmpty());


        isAllFieldsValid = combineLatest(
            // the sources (to compute from)
            isNameValid,
            isBirthDateValid,
            isMobileNumberValid,
            isUsernameValid,
            isPasswordValid,
            isEmergencyNameValid,
            isEmergencyNumberValid,
            isHomeAddressValid,
            // mapper functions that maps (bool, bool, ..., bool) to bool.
            (nameValid,
             birthDateValid,
             mobileNumberValid,
             usernameValid,
             passwordValid,
             emNameValid,
             emNumberValid,
             homeAddrValid) ->
                nameValid != null &&
                    birthDateValid != null &&
                    mobileNumberValid != null &&
                    usernameValid != null &&
                    passwordValid != null &&
                    emNameValid != null &&
                    emNumberValid != null &&
                    homeAddrValid != null &&
                    nameValid &&
                    birthDateValid &&
                    mobileNumberValid &&
                    usernameValid &&
                    passwordValid &&
                    emNameValid &&
                    emNumberValid &&
                    homeAddrValid
        );

        isConfirmButtonEnabled = combineLatest(isAllFieldsValid, isBusy,
            (fieldsValue, busy) -> fieldsValue != null
                && busy != null && fieldsValue && !busy);

    }

    /**
     * Handler for clicking on the "Confirm" button.
     */
    public void confirmClicked() {
        Log.d("", "");
        if (isAllFieldsValid.getValue() == true) {
            isBusy.postValue(true);

            authService.register(getRegistrationDTO()).thenAccept(isOK -> {
               if (isOK) {
                   Log.i("","OK");
                   clearFields();
               }
               isBusy.postValue(false);
            });
        }
    }

    private RegistrationDTO getRegistrationDTO() {
        try {
            return new RegistrationDTO(this.username.getValue(),
                this.password.getValue(),
                this.isAP.getValue() ? User.UserType.AP : User.UserType.Carer,
                this.name.getValue(),
                this.mobileNumber.getValue(),
                User.parseDOB(this.birthDate.getValue()),
                this.emergencyName.getValue(),
                this.emergencyNumber.getValue(),
                this.homeAddress.getValue()
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
        homeAddress.setValue("");
        birthDate.setValue("");
    }

}
