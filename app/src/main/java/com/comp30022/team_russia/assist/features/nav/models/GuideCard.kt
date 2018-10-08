package com.comp30022.team_russia.assist.features.nav.models

/**
 * Represents a guide card object that displays information of current route.
 */
data class GuideCard(

        val distance: String,

        val duration: String,

        val instructions: String,

        val maneuver: String,

        val travelMode: String,

        val startLocation: Location,

        val endLocation: Location
)