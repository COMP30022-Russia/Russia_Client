package com.comp30022.team_russia.assist.features.push.models

import com.google.gson.annotations.SerializedName

data class NewPicturePushNotification (
        @SerializedName("associationID") var associationId: Int = -1,
        @SerializedName("pictureID") var pictureId: Int = -1
)
