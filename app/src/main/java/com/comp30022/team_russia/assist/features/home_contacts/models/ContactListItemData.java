package com.comp30022.team_russia.assist.features.home_contacts.models;

import android.support.annotation.NonNull;

/**
 * Represents the data in a list item on the Home screen (i.e. Contact List).
 */
public class ContactListItemData {

    public final int id;


    /**
     * The real name/display name of the user.
     */
    @NonNull
    public final String name;

    /**
     * The username. Used to distinguish different users.
     */
    @NonNull
    public final String username;

    /**
     * The last message of the conversation with that user.
     * This provides a preview of the conversation on the home screen.
     */
    @NonNull
    public final String lastMessage;

    public ContactListItemData(int id, String name, String username, String lastMessage) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.lastMessage = lastMessage;
    }

    @Override
    public boolean equals(Object obj) {
        ContactListItemData other = (ContactListItemData) obj;
        return (other.id == this.id) &&
            other.name.equals(this.name) &&
            other.username.equals(this.username) &&
            other.lastMessage.equals(this.lastMessage);
    }
}
