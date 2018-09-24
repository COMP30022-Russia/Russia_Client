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

    public Association(int id, int userId) {
        this.id = id;
        this.userId = userId;
    }

    public int getId() {
        return this.id;
    }

    public int getUserId() {
        return this.userId;
    }
}
