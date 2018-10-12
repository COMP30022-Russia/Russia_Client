package com.comp30022.team_russia.assist.features.jitsi.models

import com.google.gson.annotations.SerializedName

data class VoicePendingAcceptPushNoti (
        @SerializedName("sessionID")
        val sessionId: Int,
        @SerializedName("callID")
        val callId: Int,
        val senderName: String,
        @SerializedName("sync")
        val syncToken: Int
)

data class VoiceStartedPushNoti(
        @SerializedName("sessionID")
        val sessionId: Int,
        @SerializedName("callID")
        val callId: Int,
        @SerializedName("sync")
        val syncToken: Int
)

data class VoiceStateChangedPushNoti(
        @SerializedName("sessionID")
        val sessionId: Int,
        @SerializedName("callID")
        val callId: Int,
        @SerializedName("sync")
        val syncToken: Int,
        val state: String
)

data class VoiceEndedPushNoti(
        @SerializedName("sessionID")
        val sessionId: Int,
        @SerializedName("callID")
        val callId: Int,
        @SerializedName("sync")
        val syncToken: Int,
        val reason: String
)