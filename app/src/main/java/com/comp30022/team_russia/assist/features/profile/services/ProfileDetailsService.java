package com.comp30022.team_russia.assist.features.profile.services;

import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.profile.models.ProfileDto;

import java9.util.concurrent.CompletableFuture;

/**
 * Profile Service Interface
 *
 * <p>This should be implemented as a stateful service. The implementation should be used as a
 * singleton throughout the app.
 */
public interface ProfileDetailsService {

    /**
     * Updates profile details to server.
     * @param updateInfo DTO containing the fields of update form.
     * @return True if successful.
     */
    CompletableFuture<Boolean> update(ProfileDto updateInfo);

    /**
     * Updates password details to server.
     * @return True if successful.
     */
    CompletableFuture<Boolean> updatePassword(String password);


    /**
     * Update current user's profile picture.
     * @param filePath file path
     * @return success / failure
     */
    CompletableFuture<Boolean> updatePic(String filePath);

    /**
     * Updates details from server.
     * @return True if successful.
     */
    CompletableFuture<Boolean> getDetails();

    /**
     * Check if can get current user's profile picture.
     * @return if there is a picture to get
     */
    CompletableFuture<Boolean> getPic();

    /**
     * Gets the current logged-in user.
     * @return The current user.
     */
    User getCurrentUser();

    /**
     * Get current user's profile picture.
     * @return ProfilePic object
     */
    LiveData<String> getProfilePicPath();

    /**
     * Get a specified user's profile picture.
     * @param userId user id of user to get profile picture of
     * @return ProfilePic object of specified user
     */
    CompletableFuture<ActionResult<LiveData<String>>> getUsersProfilePicture(int userId);
}
