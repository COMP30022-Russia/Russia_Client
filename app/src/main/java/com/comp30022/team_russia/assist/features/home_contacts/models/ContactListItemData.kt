package com.comp30022.team_russia.assist.features.home_contacts.models

/**
 * Represents the data in a list item on the Home screen (i.e. Contact List).
 */
class ContactListItemData
(
    /**
     * Association ID.
     */
    val associationId: Int,
    /**
     * The user ID. Used to distinguish different users.
     */
    val userId: Int,
    /**
     * The real name/display name of the user.
     */
    val name: String,
    /**
     * The last message of the conversation with that user.
     * This provides a preview of the conversation on the home screen.
     */
    val lastMessage: String,
    /**
     * Whether or not there are unread messages in the conversation.
     * Used to display the "unread dot".
     */
    val hasUnread: Boolean = false
    ) {

    override fun equals(other: Any?): Boolean {
        val obj = other as ContactListItemData?
        return (obj!!.associationId == this.associationId
                && obj.name == this.name
                && obj.userId == this.userId
                && obj.lastMessage == this.lastMessage
                && obj.hasUnread == this.hasUnread)
    }
}
