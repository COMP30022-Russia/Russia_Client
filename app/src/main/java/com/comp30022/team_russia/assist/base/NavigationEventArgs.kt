package com.comp30022.team_russia.assist.base

import android.os.Bundle

data class NavigationEventArgs (
    var actionId: Int = 0,
    var shouldClearStack: Boolean = false,
    var bundle: Bundle? = null
) {

}
