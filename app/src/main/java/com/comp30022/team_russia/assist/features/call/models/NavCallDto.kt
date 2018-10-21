package com.comp30022.team_russia.assist.features.call.models

import com.google.gson.annotations.SerializedName

data class NavCallDto (
    /**
     * Call ID.
     */
    val id: Int = -1,
    val state: NavCallState = NavCallState.Pending,
    val sessionId: Int,
    @SerializedName("APId")
    val ApId: Int,
    val carerId: Int,
    @SerializedName("sync")
    val syncToken: Int,
    val carerIsInitiator: Boolean
)

enum class NavCallState {
    Pending,
    Ongoing,
    OngoingCamera,
    Terminated
}