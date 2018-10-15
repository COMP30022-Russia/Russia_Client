package com.comp30022.team_russia.assist.features.nav.models

import com.google.gson.annotations.SerializedName

/**
 * Recent data class used to retrieve favourite and recent destinations from server.
 */
data class RecentDto(
        @SerializedName("recents") val recents: List<Recents>,
        @SerializedName("favourites") val favourites: List<Favourites>
)

class Recents(
        @SerializedName("placeID") val recentPlaceId: String,
        @SerializedName("userId") val recentUserId: Integer,
        @SerializedName("name") val recentName: String
)

class Favourites(
        @SerializedName("placeID") val favouritePlaceId: String,
        @SerializedName("userId") val favouriteUserId: Integer,
        @SerializedName("name") val favouriteName: String,
        @SerializedName("favourite") val favourited: Boolean
)

