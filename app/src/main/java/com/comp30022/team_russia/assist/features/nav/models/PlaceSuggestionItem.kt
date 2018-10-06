package com.comp30022.team_russia.assist.features.nav.models

/**
 * Represents a destination that is searched on the map.
 */
data class PlaceSuggestionItem(
        /**
         * Place name.
         */
        val name: String,

        /**
         * Address.
         */
        val address: String,

        /**
         * Place id
         */
        val googleMapsPlaceId: String,

        /**
         * Suggestion type
         */
        val type: PlaceSuggestionItemType
)

enum class PlaceSuggestionItemType {
    NORMAL, FAVOURITED, RECENT
}