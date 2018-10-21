package com.comp30022.team_russia.assist.features.home.models

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri

/**
 * Wrapper for a LiveData containing the image Uri for profile image on home contact list.
 */
class ContactListProfileImageWrapper : ViewModel() {
    val uri: MediatorLiveData<Uri> = MediatorLiveData()
}