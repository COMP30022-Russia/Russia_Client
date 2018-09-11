package com.comp30022.team_russia.assist.features.login;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.shopify.livedataktx.LiveDataKt;
//http://www.java2s.com/Tutorial/Java/0120__Development/CheckifaStringisavaliddate.htm
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    //Check if valid date
    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);
        try {
            Date inputDate = dateFormat.parse(inDate);

            Date todayDate = new Date();
            if (todayDate.compareTo(inputDate) > 0){
                return true;
            }
        } catch (ParseException pe) {
            return false;
        }
        return false;

    }
    /**
     * Constructor.
     */
    public RegisterFormViewModel() {
        // initialize fields
        isAP.setValue(false);
        name.setValue("");
        password.setValue("");
        mobileNumber.setValue("");
        password.setValue("");
        emergencyName.setValue("");
        emergencyNumber.setValue("");
        homeAddress.setValue("");


        // Setup validation
        // Both
        isNameValid = LiveDataKt.map(name, value -> !value.isEmpty());
        //isBirthDateValid = LiveDataKt.map(birthDate, value -> !value.isEmpty());
        isBirthDateValid = LiveDataKt.map(birthDate, value -> isValidDate(value));
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

    }

    /**
     * Handler for clicking on the "Confirm" button.
     */
    public void confirmClicked() {
        Log.d("", "");
    }
}
