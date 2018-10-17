package com.comp30022.team_russia.assist.features.push.models

import com.google.gson.annotations.SerializedName

data class NewNavStartPushNotification (
        @SerializedName("senderName") var senderName: String = "Unknown",
        @SerializedName("sessionID") var sessionId: Int = -1,
        @SerializedName("associationID") var associationId: Int = -1,
        @SerializedName("sync") var sync: Int
)