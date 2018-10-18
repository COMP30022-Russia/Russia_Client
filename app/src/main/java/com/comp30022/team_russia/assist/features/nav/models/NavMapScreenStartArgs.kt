package com.comp30022.team_russia.assist.features.nav.models

import com.google.gson.annotations.SerializedName

/**
 * Arguments needed to start the Nav Map UI (NavigationFragment).
 */
data class NavMapScreenStartArgs (
        var senderName: String = "Unknown",
        var sessionId: Int = -1,
        var associationId: Int = -1,
        var apInitiated: Boolean
)