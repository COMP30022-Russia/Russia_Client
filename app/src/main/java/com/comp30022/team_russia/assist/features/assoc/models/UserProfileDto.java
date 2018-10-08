//CHECKSTYLE.OFF: AbbreviationAsWordInNameCheck
//CHECKSTYLE.OFF: MemberNameCheck

package com.comp30022.team_russia.assist.features.assoc.models;

/**
 * Data Transfer Object for profile update.
 * The fields to be updated are non-null.
 */
public class UserProfileDto {
    /**
     * Username.
     */
    String username;

    /**
     * Password.
     */
    String password;

    /**
     * Name.
     */
    String name;

    /**
     * Mobile number.
     */
    String mobileNumber;

    /**
     * Date of birth.
     */
    String DOB;

    /**
     * Emergency Contact number.
     */
    String emergencyContactNumber;

    /**
     * Emergency Contact person name.
     */
    String emergencyContactName;
}
