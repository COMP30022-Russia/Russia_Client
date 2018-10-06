package com.comp30022.team_russia.assist.features.push.models

import com.google.gson.annotations.SerializedName

data class NewNavControlPushNotification (
        @SerializedName("sessionID") var sessionId: Int = -1,
        @SerializedName("carerHasControl") var carerHasControl: Boolean
)