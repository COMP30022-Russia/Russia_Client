package com.comp30022.team_russia.assist.features.message.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Representation an Association in Room database.
 */
@Entity(tableName = "association_table")
public class Association {

    /**
     * Association Id.
     */
    @PrimaryKey
    @NonNull
    private int id;

    /**
     * Id of the other user in the association.
     */
    @NonNull
    private int userId;

    @NonNull
    private boolean isActive;

    /**
     * Constructor.
     * @param id The Association ID.
     * @param userId The ID of the user on the other side of the Association.
     * @param isActive Is the association active.
     */
    public Association(int id, int userId, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.isActive = isActive;
    }

    public int getId() {
        return this.id;
    }

    public int getUserId() {
        return this.userId;
    }

    public boolean getIsActive() {
        return this.isActive;
    }
}
