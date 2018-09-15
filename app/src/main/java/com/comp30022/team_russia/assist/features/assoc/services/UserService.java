package com.comp30022.team_russia.assist.features.assoc.services;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDTO;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.assoc.models.UserProfileDTO;

import java.util.List;
import java9.util.concurrent.CompletableFuture;

/**
 * User management service interface.
 * Responsible for association management and user profiles.
 */
public interface UserService {

    /**
     * Gets a one-time authentication token for user association.
     * @return The token.
     */
    CompletableFuture<ActionResult<String>> getAssociateToken();

    /**
     * Associates the current user with another user based on the associate token.
     * @param token The associate token obtained from the other user.
     * @return Whether the operation is successful.
     */
    CompletableFuture<ActionResult<Void>> associateWith(String token);

    /**
     * Gets the users that are associated with the current user.
     * @return A list of associated users.
     */
    CompletableFuture<List<AssociationDTO>> getAssociatedUsers();

    /**
     * Updates the profile of the current user.
     * @param updatedInfo The DTO containing altered fields.
     * @return Whether the operation is successful.
     */
    CompletableFuture<Boolean> updateProfile(UserProfileDTO updatedInfo);

    /**
     * Gets a user by id.
     * @param userId The ID of the user.
     * @return The user.
     */
    CompletableFuture<User> getUser(String userId);


    CompletableFuture<ActionResult<User>> getUserFromAssociation(int associationId);
}
