package com.comp30022.team_russia.assist.features.push.models

import com.google.gson.annotations.SerializedName

data class NewRoutePushNotification (
        @SerializedName("sessionID") var sessionId: Int = -1
)