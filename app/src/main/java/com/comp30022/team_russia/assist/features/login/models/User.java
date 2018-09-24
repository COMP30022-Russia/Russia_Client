package com.comp30022.team_russia.assist.features.login.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kotlin.NotImplementedError;

/**
 * Domain model representing a user.
 */
public abstract class User {

    /**
     * User types.
     */
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

    private final int id;

    public int getUserId() {
        return this.id; 
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

    public User(int id, String username, String password, UserType userType,
                String realName, String mobileNumber, Date dateOfBirth) {
        this.id = id;
        this.username = username;
        this.password = password;
        assert userType != null;
        this.userType = userType;
        this.realName = realName;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
    }


    /**
     * Format of the user's date of birth.
     */
    private static SimpleDateFormat dobFormat = new SimpleDateFormat("dd-MM-yyyy");

    /**
     * Checks if a string is a valid date.
     */
    public static boolean isValidDoB(String inDate) {
        dobFormat.setLenient(false);
        try {
            Date inputDate = dobFormat.parse(inDate);

            Date todayDate = new Date();
            if (todayDate.compareTo(inputDate) > 0) {
                return true;
            }
        } catch (ParseException pe) {
            return false;
        }
        return false;
    }

    public static Date parseDoB(String inDate) {
        if (isValidDoB(inDate)) {
            try {
                return dobFormat.parse(inDate);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
