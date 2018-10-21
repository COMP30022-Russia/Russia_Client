package com.comp30022.team_russia.assist.features.chat.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

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
    @Nullable
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
     * List of pictures.
     */
    @Ignore
    private List<PictureDto> pictures;

    /**
     * A single picture id for an image.
     */
    @Nullable
    private int pictureId;

    /**
     * Message type, Picture or Message.
     */
    @NonNull
    private String type;

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
                   Date createdAt,
                   int pictureId,
                   String type) {

        this.id = id;
        this.associationId = associationId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
        this.pictureId = pictureId;
        this.type = type;
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

    @Nullable
    public int getPictureId() {
        return pictureId;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public List<PictureDto> getPictures() {
        return pictures;
    }


    public void setPictureId(@Nullable int pictureId) {
        this.pictureId = pictureId;
    }
}
