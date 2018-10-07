package com.comp30022.team_russia.assist.features.login.models

import com.google.gson.annotations.SerializedName

data class LoginResultDto (
    val id: Int,
    val type: String,
    val token: String?,
    val username: String,
    val name: String,
    val mobileNumber: String,
    @SerializedName("DOB")
    val dateOfBirth: String,
    val emergencyContactName: String?,
    val emergencyContactNumber: String?,
    val address: String?
)