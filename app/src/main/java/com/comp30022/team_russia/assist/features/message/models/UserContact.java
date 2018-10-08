package com.comp30022.team_russia.assist.features.message.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Represents an user in the contact list.
 */
@Entity(tableName = "user_profile_table")
public class UserContact {

    @NonNull
    @PrimaryKey
    private int id;

    /**
     * User's display name.
     */
    @NonNull
    private String name;

    public UserContact(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{"
               + "name='" + name + '\'' + '}';
    }
}
