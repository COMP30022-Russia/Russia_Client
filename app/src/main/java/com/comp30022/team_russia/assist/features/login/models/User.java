package com.comp30022.team_russia.assist.features.login.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Domain model representing a user.
 */
public abstract class User {
    /**
     * Format of the user's date of birth.
     */
    private static SimpleDateFormat dobFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * The type of the user.
     */
    private final UserType userType;

    /**
     * User id.
     */
    private final int id;

    /**
     * The username. Used as login credential.
     */
    private final String username;

    /**
     * The password.
     * For simplicity, we are storing the password
     * as plaintext for now, disregarding security issues.
     */
    private final String password;

    /**
     * The user's real name.
     */
    private final String realName;

    /**
     * Mobile phone number.
     */
    private final String mobileNumber;

    /**
     * The user's date of birth.
     */
    private final String dateOfBirth;

    /**
     * User types.
     */
    public enum UserType {
        Carer,
        AP
    }


    /**
     * User data.
     * @param id ID of the user.
     * @param username Username of the user.
     * @param password Password of the user.
     * @param userType Type of user.
     * @param realName Name of the user.
     * @param mobileNumber Mobile phone number of the user.
     * @param dateOfBirth Date of birth of the user.
     */
    public User(int id, String username, String password, UserType userType,
                String realName, String mobileNumber, String dateOfBirth) {
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
     * Gets the type of the user.
     * @return The type of the user.
     */
    public UserType getUserType() {
        return this.userType;
    }


    public int getUserId() {
        return this.id;
    }

    /**
     * Gets the user's username.
     * @return The user's username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets the user's password.
     * @return
     */
    public String getPassword() {
        return this.password;
    }


    /**
     * Gets the user's real name.
     * @return The user's real name.
     */
    public String getRealname() {
        return realName;
    }


    /**
     * Gets the user's mobile phone number.
     * @return The user's mobile phone number.
     */
    public String getMobileNumber() {
        return this.mobileNumber;
    }


    /**
     * Gets the user's date of birth.
     * @return The user's date of birth.
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Gets the user's age (in years).
     * @return The user's age.
     */
    public int getAge() {
        return getAgeFromDate(this.dateOfBirth);
    }


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

    /**
     * Parse Dob.
     * @param inDate Date to parse as String.
     * @return Parsed date as a Date.
     */
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


    /**
     * Convert date to age.
     * @param dobString date as a string
     * @return age
     */
    private int getAgeFromDate(String dobString) {

        Date date = null;
        try {
            date = dobFormat.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) {
            return 0;
        }

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int year = dob.get(Calendar.YEAR);
        int month = dob.get(Calendar.MONTH);
        int day = dob.get(Calendar.DAY_OF_MONTH);

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }
}
