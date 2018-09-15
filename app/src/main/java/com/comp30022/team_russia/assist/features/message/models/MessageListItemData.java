package com.comp30022.team_russia.assist.features.message.models;

/**
 * Representation of the displayed message.
 */
public class MessageListItemData {
    /**
     * Message Id.
     */
    public final int id;

    /**
     * Is the message sent by me (current user) or the opposite side.
     */
    public final boolean isSentByMe;

    public final String content;

    public final String senderDisplayName;

    /**
     * The date / time display alongside the message.
     * Need to be smart and short.
     */
    public final String friendlyDateTime;

    //@todo: profile image id

    public MessageListItemData(int id, boolean isSentByMe, String content,
                               String friendlyDateTime, String senderDisplayName) {
        this.id = id;
        this.isSentByMe = isSentByMe;
        this.content = content != null ? content : "";
        this.friendlyDateTime = friendlyDateTime != null ? friendlyDateTime : "";
        this.senderDisplayName = senderDisplayName != null ? senderDisplayName : "";
    }

    @Override
    public boolean equals(Object obj) {
        MessageListItemData other = (MessageListItemData) obj;
        return id == other.id && isSentByMe == other.isSentByMe
            && content.equals(other.content)
            && friendlyDateTime.equals(other.friendlyDateTime)
            && senderDisplayName.equals(other.senderDisplayName);
    }
}
