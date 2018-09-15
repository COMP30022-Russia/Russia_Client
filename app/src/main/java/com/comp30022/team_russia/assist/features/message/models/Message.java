package com.comp30022.team_russia.assist.features.message.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.support.annotation.NonNull;

import java.util.Date;

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

    public int getId() { return id; }

    public String getContent() {
        return content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public int getAssociationId() { return associationId; }
}
