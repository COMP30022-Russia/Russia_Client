package com.comp30022.team_russia.assist.features.home_contacts.models;

import android.support.annotation.NonNull;

/**
 * Represents the data in a list item on the Home screen (i.e. Contact List).
 */
public class ContactListItemData {

    /**
     * Association ID.
     */
    public final int associationId;
    
    /**
     * The real name/display name of the user.
     */
    @NonNull
    public final String name;

    /**
     * The user ID. Used to distinguish different users.
     */
    @NonNull
    public final int userId;

    /**
     * The last message of the conversation with that user.
     * This provides a preview of the conversation on the home screen.
     */
    @NonNull
    public final String lastMessage;

    /**
     * Constructor.
     * @param associationId The association ID.
     * @param userId The user ID.
     * @param name The real name / display name of the user.
     * @param lastMessage The latest message of the chat with this user.
     */
    public ContactListItemData(int associationId, int userId, String name, String lastMessage) {
        this.associationId = associationId;
        this.name = name;
        this.userId = userId;
        this.lastMessage = lastMessage;
    }

    @Override
    public boolean equals(Object obj) {
        ContactListItemData other = (ContactListItemData) obj;
        return (other.associationId == this.associationId)
            && other.name.equals(this.name)
            && other.userId == this.userId
            && other.lastMessage.equals(this.lastMessage);
    }
}
