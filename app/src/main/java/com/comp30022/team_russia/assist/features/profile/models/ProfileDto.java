package com.comp30022.team_russia.assist.features.profile.models;

//CHECKSTYLE.OFF: AbbreviationAsWordInNameCheck
//CHECKSTYLE.OFF: MemberNameCheck
import java.util.Date;

/**
 * Data Transform Object representing the fields
 * in the user registration form.
 */
public class ProfileDto {

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
     * Emergency contact person mobile number.
     */
    String emergencyContactNumber;

    public ProfileDto(
                           String name,
                           String mobileNumber,
                           Date dateOfBirth,
                           String emergencyContactName,
                           String emergencyContactNumber) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.DOB = dateOfBirth;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
    }
}

