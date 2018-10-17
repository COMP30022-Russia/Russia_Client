package com.comp30022.team_russia.assist.features.profile.models

data class ProfilePictureCreationArgs(
    /**
     * ID of the user this picture belongs to.
     */
    val userId: Int,
    /**
     * Local file to upload. Can be null.
     */
    val uri: String?
)