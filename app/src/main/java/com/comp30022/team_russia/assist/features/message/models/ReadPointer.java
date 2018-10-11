package com.comp30022.team_russia.assist.features.message.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Represents a chat history read pointer of a conversation.
 */
@Entity(tableName = "chat_read_pointers")
public class ReadPointer {

    /**
     * Association Id.
     */
    @PrimaryKey
    @NonNull
    private int id;

    /**
     * The last message ID of the association that has been
     * read.
     */
    private int lastReadId;

    public ReadPointer(int id, int lastReadId) {
        this.id = id;
        this.lastReadId = lastReadId;
    }

    public int getId() {
        return this.id;
    }

    public int getLastReadId() {
        return this.lastReadId;
    }

}
