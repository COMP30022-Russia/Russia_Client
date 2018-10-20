package com.comp30022.team_russia.assist.features.assoc.models

data class UserResponseDto (
    var id: Int = 0,
    var name: String? = null,
    var username: String? = null,
    var type: String? = null,
    var mobileNumber: String? = null,
    var emergencyContactName: String? = null,
    var emergencyContactNumber: String? = null,
    var DOB: String? = null
)
