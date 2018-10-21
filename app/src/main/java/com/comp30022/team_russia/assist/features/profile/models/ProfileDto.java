package com.comp30022.team_russia.assist.features.profile.models;

//CHECKSTYLE.OFF: AbbreviationAsWordInNameCheck
//CHECKSTYLE.OFF: MemberNameCheck

/**
 * Data Transform Object representing the fields in the user registration form.
 */
public class ProfileDto {

    /**
     * The user's real name / display name.
     */
    public final String name;

    /**
     * Mobile phone number.
     */
    public final String mobileNumber;

    /**
     * The user's date of birth.
     */
    public final String DOB;

    /**
     * Emergency contact person name.
     */
    public final String emergencyContactName;

    /**
     * Emergency contact person mobile number.
     */
    public final String emergencyContactNumber;

    /**
     * Constructor.
     * @param name Name of the user.
     * @param mobileNumber Mobile number of the user.
     * @param dateOfBirth Date of birth.
     * @param emergencyContactName Emergency contact name.
     * @param emergencyContactNumber Emergency contact number.
     */
    public ProfileDto(
                           String name,
                           String mobileNumber,
                           String dateOfBirth,
                           String emergencyContactName,
                           String emergencyContactNumber) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.DOB = dateOfBirth;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
    }
}

