package com.comp30022.team_russia.assist.features.login.models;

import java.util.Date;

/**
 * Domain model representing a Carer.
 */
public class Carer extends User {
    public Carer(String username, String password, String realName, String mobileNumber, Date dateOfBirth) {
        super(username, password, UserType.Carer, realName, mobileNumber, dateOfBirth);
    }
}
