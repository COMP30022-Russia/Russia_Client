package com.comp30022.team_russia.assist.features.login.models;


/**
 * Domain model representing an Assisted Person.
 */
public class AssistedPerson extends User {

    /**
     * Emergency contact number.
     */
    private final String emergencyContactNumber;

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    /**
     * Emergency contact person name.
     */
    private final String emergencyContactName;

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    /**
     * Constructs an AP object.
     * @param id The id of the User.
     * @param username Username.
     * @param password Password.
     * @param realName The real name.
     * @param mobileNumber Mobile number.
     * @param dateOfBirth Date of birth.
     * @param emergencyContactName Emergency contact name.
     * @param emergencyContactNumber Emergency contact number.
     */
    public AssistedPerson(int id,
                          String username,
                          String password,
                          String realName,
                          String mobileNumber,
                          String dateOfBirth,
                          String emergencyContactName,
                          String emergencyContactNumber
                          ) {
        super(id, username, password, UserType.AP, realName, mobileNumber, dateOfBirth);
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
    }
}
