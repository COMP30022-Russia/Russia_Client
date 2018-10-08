package com.comp30022.team_russia.assist.features.message.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Representation of a chat message in the database.
 */
@Entity(tableName = "message_table")
public class Message {
    @PrimaryKey
    @NonNull
    private int id;

    /**
     * Message body.
     */
    @NonNull
    private String content;

    @NonNull
    private int associationId;

    /**
     * Id of the sender of the message.
     */
    @NonNull
    private int authorId;

    /**
     * When message was created.
     */
    @NonNull
    private Date createdAt;

    /**
     * Constructor.
     * @param id The unique message id.
     * @param associationId The ID of the association this message belongs to.
     * @param authorId The user ID of the author.
     * @param content The message content.
     * @param createdAt When the message was created.
     */
    public Message(int id,
                   int associationId,
                   int authorId,
                   String content,
                   Date createdAt) {
        this.id = id;
        this.associationId = associationId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public int getAssociationId() {
        return associationId;
    }
}
