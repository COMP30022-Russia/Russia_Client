//CHECKSTYLE.OFF: AbbreviationAsWordInNameCheck
//CHECKSTYLE.OFF: MemberNameCheck

package com.comp30022.team_russia.assist.features.login.models;

import java.util.Date;


/**
 * Data Transform Object representing the fields
 * in the user registration form.
 */
public class RegistrationDto {

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
    String DOB;

    /**
     * Emergency contact person name.
     */
    String emergencyContactName;

    /**
     * Emergency contact person mobile number.
     */
    String emergencyContactNumber;

    public RegistrationDto(String username,
                           String password,
                           User.UserType type,
                           String name,
                           String mobileNumber,
                           String dateOfBirth, String emergencyContactName,
                           String emergencyContactNumber) {
        this.username = username;
        this.password = password;
        this.type = type == User.UserType.AP ? "AP" : "Carer";
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.DOB = dateOfBirth;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}

