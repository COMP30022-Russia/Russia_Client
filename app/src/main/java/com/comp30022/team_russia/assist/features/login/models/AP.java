package com.comp30022.team_russia.assist.features.login.models;

import java.util.Date;

/**
 * Domain model representing an Assisted Person.
 */
public class AP extends User {

    private final String emergencyContactNumber;

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    private final String emergencyContactName;
    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    private final String address;

    public String getAddress() {
        return address;
    }

    public AP(String username,
              String password,
              String realName,
              String mobileNumber,
              Date dateOfBirth,
              String emergencyContactName,
              String emergencyContactNumber,
              String address) {
        super(username, password, UserType.AP, realName, mobileNumber, dateOfBirth);
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
        this.address = address;
    }
}
