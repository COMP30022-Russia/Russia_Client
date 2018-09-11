package com.comp30022.team_russia.assist.features.login.models;

import java.util.Date;

import kotlin.NotImplementedError;

/**
 * Domain model representing a user.
 */
public abstract class User {

    public enum UserType {
        Carer,
        AP
    }

    /**
     * The type of the user.
     */
    private final UserType userType;

    /**
     * Gets the type of the user.
     * @return The type of the user.
     */
    public UserType getUserType() {
        return this.userType;
    }

    /**
     * The username. Used as login credential.
     */
    private final String username;

    /**
     * Gets the user's username.
     * @return The user's username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * The password.
     * For simplicity, we are storing the password
     * as plaintext for now, disregarding security issues.
     */
    private final String password;

    /**
     * Gets the user's password.
     * @return
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * The user's real name.
     */
    private final String realName;

    /**
     * Gets the user's real name.
     * @return The user's real name.
     */
    public String getRealname() {
        return realName;
    }

    /**
     * Mobile phone number.
     */
    private final String mobileNumber;

    /**
     * Gets the user's mobile phone number.
     * @return The user's mobile phone number.
     */
    public String getMobileNumber() {
        return this.mobileNumber;
    }

    /**
     * The user's date of birth.
     */
    private final Date dateOfBirth;

    /**
     * Gets the user's date of birth.
     * @return The user's date of birth.
     */
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Gets the user's age (in years).
     * @return The user's age.
     */
    public int getAge() {
        throw new NotImplementedError();
    }

    public User(String username, String password, UserType userType, String realName, String mobileNumber, Date dateOfBirth) {
        this.username = username;
        this.password = password;
        assert userType != null;
        this.userType = userType;
        this.realName = realName;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
    }
}
