package com.comp30022.team_russia.assist.features.login.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;
import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.SingleLiveEvent;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.services.AuthService;

import javax.inject.Inject;

/**
 * ViewModel for Login screen.
 */
public class LoginViewModel extends BaseViewModel {
    /**
     * The username.
     */
    public final MutableLiveData<String> username = new MutableLiveData<>();

    /**
     * The password.
     */
    public final MutableLiveData<String> password = new MutableLiveData<>();

    /**
     * Whether or not the user input (i.e. username and password) is valid.
     */
    public final LiveData<Boolean> isInputValid;

    /**
     * Whether or not the Login button should be enabled.
     */
    public final LiveData<Boolean> isLoginButtonEnabled;

    /**
     * Whether we are authenticating against the server.
     * Should show an indicator and disable inputs if busy.
     */
    public final MutableLiveData<Boolean> isBusy = new MutableLiveData<>();

    /**
     * Authentication Service.
     */
    private final AuthService authService;

    private final ToastService toastService;

    /**
     * Constructor.
     */
    @Inject
    public LoginViewModel(AuthService authService, ToastService toastService) {
        this.authService = authService;
        this.toastService = toastService;
        username.setValue("");
        password.setValue("");
        isBusy.setValue(false);

        // Set up the "computed properties": these fields are calculated
        // dynamically based on the
        // user input.

        // The input is valid if both username and password are non-empty
        // strings.
        isInputValid = combineLatest(username, password,
            (usernameValue, passwordValue)
                -> usernameValue.length() > 0 && passwordValue.length() > 0);

        // Should disable the Login button if the view model is busy or if the
        // input is invalid.
        isLoginButtonEnabled = combineLatest(isInputValid, isBusy,
            (inputValid, busy) -> inputValid && !busy);
    }

    /**
     * Handler for clicking on "Login" button.
     */
    public void loginClicked() {
        isBusy.setValue(true);
        toastService.toastShort("Logging in...");
        authService.login(username.getValue(), password.getValue())
            .thenAccept((isOk) -> {
                //Log.println(Log.INFO, "", "Login result = " + isOk);
                // Use postValue instead of setValue because we are on a background
                // thread.
                isBusy.postValue(false);
                if (isOk) {
                    username.postValue("");
                    password.postValue("");
                    toastService.toastShort("Logged in successfully!");
                } else {
                    toastService.toastShort("Login failed.");
                }
            });
    }

    /**
     * Handler for clicking "Register" link.
     */
    public void registerClicked() {
        navigateTo(R.id.action_register);
    }
}
