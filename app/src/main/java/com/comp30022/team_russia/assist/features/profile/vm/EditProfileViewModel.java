package com.comp30022.team_russia.assist.features.profile.vm;

import android.arch.lifecycle.LiveData;

import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.SingleLiveEvent;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.models.ProfileDto;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;

import com.shopify.livedataktx.LiveDataKt;

import javax.inject.Inject;

/**
 * The edit profile view model.
 */

public class EditProfileViewModel extends BaseViewModel {
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
     * The "Mobile number" field.
     */
    public final MutableLiveData<String> mobileNumber = new MutableLiveData<>();

    /**
     * Whether the "Mobile number" field is valid.
     */
    public final LiveData<Boolean> isMobileNumberValid;

    /**
     * The "Birth date" field.
     */
    public final MutableLiveData<String> birthDate = new MutableLiveData<>();
    /**
     * Whether the "Birth date" field is valid.
     */
    public final LiveData<Boolean> isBirthDateValid;
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



    public final SingleLiveEvent<Boolean> shouldExitEditMode = new SingleLiveEvent<>();

    /**
     * Authentication service.
     */
    private final AuthService authService;

    private final ToastService toastService;

    private final ProfileDetailsService profileDetailsService;

    /**
     * Constructor.
     */
    @Inject
    public EditProfileViewModel(AuthService authService,
                                ToastService toastService,
                                ProfileDetailsService profService) {

        this.authService = authService;
        this.toastService = toastService;
        this.profileDetailsService = profService;

        User user = profileDetailsService.getCurrentUser();

        shouldExitEditMode.setValue(false);

        name.setValue(user.getRealname());
        mobileNumber.setValue(user.getMobileNumber());
        password.setValue("Placeholder");
        birthDate.setValue(user.getDateOfBirth());
        if (authService.getCurrentUser().getUserType() == User.UserType.AP) {
            isAp.setValue(true);
            AssistedPerson ap = (AssistedPerson) profService.getCurrentUser();
            emergencyName.setValue(ap.getEmergencyContactName());
            emergencyNumber.setValue(ap.getEmergencyContactNumber());
        } else {
            isAp.setValue(false);
            emergencyName.setValue("NOT AP");
            emergencyNumber.setValue("NOT AP");
        }


        isBusy.postValue(false);
        // Setup validation
        // Both
        isNameValid = LiveDataKt.map(name, value -> !value.isEmpty());
        isMobileNumberValid = LiveDataKt.map(mobileNumber,
            value -> !value.isEmpty());
        isBirthDateValid = LiveDataKt.map(birthDate, User::isValidDoB);
        isPasswordValid = LiveDataKt.map(password, value -> !value.isEmpty());

        // AP-only fields
        if (user.getUserType() == User.UserType.AP) {
            isEmergencyNameValid = combineLatest(isAp, emergencyName,
                (isAp, value) -> !isAp || !value.isEmpty());
            isEmergencyNumberValid = combineLatest(isAp, emergencyNumber,
                (isAp, value) -> !isAp || !value.isEmpty());

            isAllFieldsValid = combineLatest(
                // the sources (to compute from)
                isNameValid,
                isMobileNumberValid,
                isPasswordValid,
                isBirthDateValid,
                isEmergencyNameValid,
                isEmergencyNumberValid,
                // mapper functions that maps (bool, bool, ..., bool) to bool.
                (nameValid,
                 mobileNumberValid,
                 passwordValid,
                 birthDateViewValid,
                 emNameValid,
                 emNumberValid) ->
                    nameValid != null
                    && mobileNumberValid != null
                    && passwordValid != null
                    && birthDateViewValid != null
                    && emNameValid != null
                    && emNumberValid != null
                    && nameValid
                    && mobileNumberValid
                    && passwordValid
                    && birthDateViewValid
                    && emNameValid
                    && emNumberValid
            );
        } else {
            isEmergencyNameValid = LiveDataKt.map(emergencyName,
                value -> value.matches("IS AP"));
            isEmergencyNumberValid = LiveDataKt.map(emergencyNumber,
                value -> value.matches("IS AP"));
            isAllFieldsValid = combineLatest(
                // the sources (to compute from)
                isNameValid,
                isMobileNumberValid,
                isPasswordValid,
                isBirthDateValid,
                // mapper functions that maps (bool, bool, ..., bool) to bool.
                (nameValid,
                 mobileNumberValid,
                 passwordValid,
                 birthDateViewValid) ->
                    nameValid != null
                    && mobileNumberValid != null
                    && passwordValid != null
                    && birthDateViewValid != null
                    && nameValid
                    && mobileNumberValid
                    && passwordValid
                    && birthDateViewValid
            );
        }


        isConfirmButtonEnabled = combineLatest(isAllFieldsValid, isBusy,
            (fieldsValue, busy) -> fieldsValue != null
                                   && busy != null && fieldsValue && !busy);

    }

    public void confirmClicked() {
        if (isAllFieldsValid.getValue()) {
            isBusy.postValue(true);
            toastService.toastShort("Updating...");
            profileDetailsService.update(getProfileDto()).thenAccept(isOK -> {
                if (isOK) {
                    toastService.toastShort("Updated successfully.");
                    shouldExitEditMode.setValue(true);
                } else {
                    toastService.toastShort("Update failed");
                    return;
                }

                isBusy.postValue(false);
            });

            if (!password.getValue().equals("Placeholder")) {
                isBusy.postValue(true);
                toastService.toastShort("Updating...");
                profileDetailsService.updatePassword(password.getValue()).thenAccept(isOK -> {
                    if (isOK) {
                        toastService.toastShort("Updated Password successfully.");


                        shouldExitEditMode.setValue(true);



                    } else {
                        toastService.toastShort("Update Password failed");
                        return;
                    }
                    isBusy.postValue(false);
                });
            }
        }
    }

    private ProfileDto getProfileDto() {
        try {
            return new ProfileDto(
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

    /**
     * Handle event confirm button clicked.
     */
    public void onEditProfileConfirmButtonClicked() {
        confirmClicked();
    }
}
