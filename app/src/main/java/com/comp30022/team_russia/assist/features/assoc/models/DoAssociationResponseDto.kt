package com.comp30022.team_russia.assist.features.assoc.models

import com.google.gson.annotations.SerializedName

/**
 * Represents the response object of POST /me/associate
 */
data class DoAssociationResponseDto(
    val active: Boolean,
    /**
     * The association ID.
     */
    val id: Int,
    @SerializedName("APId")
    val aPId: Int,
    val carerId: Int
)