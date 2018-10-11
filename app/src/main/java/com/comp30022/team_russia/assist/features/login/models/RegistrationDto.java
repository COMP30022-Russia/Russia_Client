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


    /**
     * Registration DTO Constructor.
     * @param username Username of the new user.
     * @param password Password of the new user.
     * @param type User type of the new user.
     * @param name Name of the new user.
     * @param mobileNumber Mobile phone number of the new user.
     * @param dateOfBirth Date of birth of the new user.
     * @param emergencyContactName Name of the new user's emergency contact.
     * @param emergencyContactNumber Mobile phone number of the new user's emergency contact.
     */
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

    /**
     * Indicates whether another object is equal to the current instance of RegistrationDTO.
     * @param obj Object to compare with.
     * @return Boolean value indicating equality.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RegistrationDto)) {
            return false;
        }

        RegistrationDto registrationDto = (RegistrationDto) obj;
        return (
            this.DOB.equals(registrationDto.DOB)
            && this.emergencyContactName.equals(registrationDto.emergencyContactName)
            && this.emergencyContactNumber.equals(registrationDto.emergencyContactNumber)
            && this.mobileNumber.equals(registrationDto.mobileNumber)
            && this.name.equals(registrationDto.name)
            && this.password.equals(registrationDto.password)
            && this.type.equals(registrationDto.type)
            && this.username.equals(registrationDto.username));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}

