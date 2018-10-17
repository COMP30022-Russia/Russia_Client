package com.comp30022.team_russia.assist.features.push.models

import com.google.gson.annotations.SerializedName
data class NewEmergencyStartPushNotification (
        @SerializedName("eventID") var eventId: Int = -1,
        @SerializedName("senderID") var senderId: Int = -1,
        @SerializedName("senderName") var senderName: String,
        @SerializedName("mobileNumber") var mobileNumber: String = ""
)