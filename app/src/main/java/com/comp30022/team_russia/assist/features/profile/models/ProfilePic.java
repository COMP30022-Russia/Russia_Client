package com.comp30022.team_russia.assist.features.profile.models;

import android.graphics.Bitmap;

/**
 * Profile Pic.
 */
public class ProfilePic {

    private final Bitmap profilePic;

    public Bitmap getProfilePicture() {
        return profilePic;
    }

    public ProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }
}
