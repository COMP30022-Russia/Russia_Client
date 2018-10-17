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

    /**
     * Picture id of a single image.
     */
    public final int pictureId;

    /**
     * Constructor.
     * @param id Unique ID of the message.
     * @param isSentByMe Whether the message is sent by the current user.
     * @param content The content of the message.
     * @param friendlyDateTime The human friendly date displayed next to the chat bubble.
     * @param senderDisplayName The display name of the sender.
     */
    public MessageListItemData(int id, boolean isSentByMe, String content,
                               String friendlyDateTime, String senderDisplayName, int pictureId) {
        this.id = id;
        this.isSentByMe = isSentByMe;
        this.content = content != null ? content : "";
        this.friendlyDateTime = friendlyDateTime != null ? friendlyDateTime : "";
        this.senderDisplayName = senderDisplayName != null ? senderDisplayName : "";
        this.pictureId = pictureId;
    }

    @Override
    public boolean equals(Object obj) {
        MessageListItemData other = (MessageListItemData) obj;
        return id == other.id && isSentByMe == other.isSentByMe
            && content.equals(other.content)
            && friendlyDateTime.equals(other.friendlyDateTime)
            && senderDisplayName.equals(other.senderDisplayName)
            && pictureId == other.pictureId;
    }
}
