package com.comp30022.team_russia.assist.features.login.models;

import java.util.Date;


/**
 * Data Transform Object representing the fields
 * in the user registration form.
 */
public class RegistrationDTO {

    /**
     * The type of the user.
     */
    String type;

    /**
     * The username. Used as login credential.
     */
    String username;

    /**
     * The password.
     */
    String password;

    /**
     * The user's real name.
     */
    String name;

    /**
     * Mobile phone number.
     */
    String mobileNumber;

    /**
     * The user's date of birth.
     */
    Date DOB;

    /**
     * Emergency contact person name.
     */
    String emergencyContactName;

    /**
     * Emergency contact person mobile number;
     */
    String emergencyContactNumber;

    /**
     * Home address.
     */
    String address;

    public RegistrationDTO(String username,
                           String password,
                           User.UserType type,
                           String name,
                           String mobileNumber,
                           Date dateOfBirth, String emergencyContactName, String emergencyContactNumber, String address) {
        this.username = username;
        this.password = password;
        this.type = type == User.UserType.AP ? "AP" : "Carer";
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.DOB = dateOfBirth;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
        this.address = address;
    }

}

