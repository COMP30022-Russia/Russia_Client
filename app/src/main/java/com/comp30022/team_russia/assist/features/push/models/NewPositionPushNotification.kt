package com.comp30022.team_russia.assist.features.push.models

import com.google.gson.annotations.SerializedName

data class NewPositionPushNotification (
        @SerializedName("sessionID") var sessionId: Int = -1,
        @SerializedName("lat") var lat: Double = -1.0,
        @SerializedName("lon") var lon: Double = -1.0,
        /**
         * This sync token is different from rest of nav messages.
         * This is based on server side timestamp.
         */
        @SerializedName("sync") var sync: Long
)