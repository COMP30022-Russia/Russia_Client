package com.comp30022.team_russia.assist.features.assoc.db;

import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.features.chat.models.Association;
import com.comp30022.team_russia.assist.features.chat.models.UserContact;
import com.comp30022.team_russia.assist.features.home.models.ContactListItemData;

import java.util.List;

/**
 * Users (profiles) and Associations local cache.
 */
public interface UserAssociationCache {

    /**
     * Replaces the locally-cached set of association with the new ones.
     * @param newAssociations The new set of {@link Association}s.
     */
    void replaceAssociations(List<Association> newAssociations);

    /**
     * Updates locally-cached User profiles in batch.
     * @param users The set of user profiles {@link UserContact} to be cached.
     */
    void batchUpdateUserProfiles(List<UserContact> users);

    /**
     * Updates a single User profile in the local cache.
     * @param user The user profile to be cached.
     */
    void insertOrUpdateUserProfile(UserContact user);

    /**
     * Updates a single Association.
     * @param association The association to be updated.
     */
    void insertOrUpdateAssociation(Association association);

    /**
     * Gets a {@link LiveData} representing the latest contact list to be displayed, along
     * with the latest message from each contact.
     * @return The contact list.
     */
    LiveData<List<ContactListItemData>> getContactList();

    /**
     * Clears all Users and Associations cache.
     */
    void clear();

}
