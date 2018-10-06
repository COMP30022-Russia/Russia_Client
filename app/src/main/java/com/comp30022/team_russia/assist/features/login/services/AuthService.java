package com.comp30022.team_russia.assist.features.login.services;

import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.login.models.RegistrationDto;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.push.models.FirebaseTokenData;

import java9.util.concurrent.CompletableFuture;

/**
 * Authentication (Login and Registration) Service Interface
 *
 * <p>This should be implemented as a stateful service. The implementation should be used as a
 * singleton throughout the app.
 */
public interface AuthService {
    /**
     * Logs a user in.
     *
     * @param username The username.
     * @param password The password.
     * @return Is the login successful.
     */
    CompletableFuture<Boolean> login(String username, String password);

    /**
     * Whether the application is in an authenticated state.
     *
     * @return True if logged in.
     */
    LiveData<Boolean> isLoggedIn();

    /**
     * Whether the application is in an authentication state.
     * Unboxed value of @{link isLoggedIn};
     *
     * @todo fixme
     */
    boolean isLoggedInUnboxed();

    String getAuthToken();

    /**
     * Logs out a user.
     *
     * @return Is the logout operation successful.
     */
    CompletableFuture<Boolean> logout();

    /**
     * Gets the current username.
     *
     * @return The current username;
     */
    String getCurrentUsername();

    /**
     * Gets the current logged-in user.
     *
     * @return The current user.
     */
    User getCurrentUser();

    /**
     * Registers a new user.
     *
     * @param registrationInfo DTO containing the fields of registration form.
     * @return True if successful.
     */
    CompletableFuture<Boolean> register(RegistrationDto registrationInfo);

    CompletableFuture<ActionResult<Void>> updateFirebaseToken(FirebaseTokenData newTokenData);

}
