package com.comp30022.team_russia.assist.features.login.models;

import java.util.Date;

/**
 * Domain model representing an Assisted Person.
 */
public class AP extends User {

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
     * Home address.
     */
    private final String address;

    public String getAddress() {
        return address;
    }

    public AP(int id,
              String username,
              String password,
              String realName,
              String mobileNumber,
              Date dateOfBirth,
              String emergencyContactName,
              String emergencyContactNumber,
              String address) {
        super(id, username, password, UserType.AP, realName, mobileNumber, dateOfBirth);
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
        this.address = address;
    }
}
