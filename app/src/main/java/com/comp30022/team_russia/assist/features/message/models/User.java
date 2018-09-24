package com.comp30022.team_russia.assist.features.message.models;

/**
 * Represents an user in the contact list.
 */
public class User {
    //CHECKSTYLE.OFF: MemberNameCheck
    private int user_id;
    //CHECKSTYLE.ON: MemberNameCheck
    private String name;

    public User(int id, String name) {
        this.user_id = id;
        this.name = name;
    }

    // Add an empty constructor so we can later parse JSON into User using Jackson
    public User() {
    }

    public int getUserId() {
        return user_id;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "User{"
               + "name='" + name + '\'' + '}';
    }
}
