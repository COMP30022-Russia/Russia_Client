package com.comp30022.team_russia.assist.base

import android.os.Bundle

/**
 * The information needed to perform a navigation (page navigation, not geo navigation).
 */
data class NavigationEventArgs(
    /**
     * The Resource Id of the action in the navigation graph.
     */
    var actionId: Int = 0,

    /**
     * Whether this navigation need to clear the stack (i.e. remove history so that the user
     * cannot return by pressing back).
     */
    var shouldClearStack: Boolean = false,

    /**
     * The Android Bundle to be passed to the destination Fragment.
     */
    var bundle: Bundle? = null
) {
}
