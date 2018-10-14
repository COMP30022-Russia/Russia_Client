package com.comp30022.team_russia.assist.features.profile.services;

import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.profile.models.ProfileDto;
import com.comp30022.team_russia.assist.features.profile.models.ProfilePic;

import java.io.File;
import java9.util.concurrent.CompletableFuture;

/**
 * Profile Service Interface
 *
 * <p>This should be implemented as a stateful service. The implementation should be used as a
 * singleton throughout the app.
 */
public interface ProfileService {

    /**
     * Updates profile details to server.
     *
     * @param updateInfo DTO containing the fields of update form.
     * @return True if successful.
     */
    CompletableFuture<Boolean> update(ProfileDto updateInfo);


    /**
     * Updates password details to server.
     *
     * @return True if successful.
     */
    CompletableFuture<Boolean> updatePassword(String password);


    CompletableFuture<Boolean> updatePic(File image);

    /**
     * Updates details from server.
     *
     * @return True if successful.
     */
    CompletableFuture<Boolean> getDetails();

    CompletableFuture<Boolean> getPic();

    /**
     * Gets the current logged-in user.
     *
     * @return The current user.
     */
    User getCurrentUser();

    ProfilePic getProfilePic();
}
