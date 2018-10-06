package com.comp30022.team_russia.assist.features.nav.models

import com.google.gson.annotations.SerializedName

/**
 * Directions data class used to retrieve route GoogleJSON Object from server.
 */
data class Directions(
        @SerializedName("status") val directionsStatus: String,
        @SerializedName("geocoded_waypoints") val directionsGeocodedWaypoints: List<GeoCodedWaypoints>,
        @SerializedName("routes") val directionsRoutes: List<Route>
)

data class GeoCodedWaypoints(
        @SerializedName("geocoder_status") val geocoderStatus: String,
        @SerializedName("place_id") val place_id: String,
        @SerializedName("types") val types: List<String>
)

data class Route(
        @SerializedName("bounds") val routeBounds: Bound,
        @SerializedName("overview_polyline") val routePolylineOverview: OverViewPolyLine,
        @SerializedName("summary") val routeSummary: String,
        @SerializedName("legs") val routeLegs: List<Leg>
)

data class Bound(
        @SerializedName("northeast") val northeast: Location,
        @SerializedName("southwest") val southwest: Location
)

data class OverViewPolyLine(
        @SerializedName("points") val overViewPolyLinePoints: String
)

data class Leg(
        @SerializedName("distance") val legDistance: TextValue,
        @SerializedName("duration") val legDuration: TextValue,
        @SerializedName("end_address") val legEndAddress: String,
        @SerializedName("end_location") val legEndLocation: Location,
        @SerializedName("start_address") val legStartAddress: String,
        @SerializedName("start_location") val legStartLocation: Location,
        @SerializedName("steps") val legSteps: List<Step>
)

class Step(
        @SerializedName("distance") val stepDistance: TextValue,
        @SerializedName("duration") val stepDuration: TextValue,
        @SerializedName("end_location") val stepEndLocation: Location,
        @SerializedName("html_instructions") val stepHtmlInstructions: String,
        @SerializedName("polyline") val stepPolyline: PolyLine,
        @SerializedName("start_location") val stepStartLocation: Location,
        @SerializedName("travel_mode") val stepTravelMode: String,
        @SerializedName("maneuver") val stepManeuver: String
)

class TextValue(
        @SerializedName("text") val text: String,
        @SerializedName("value") val value: String
)

data class PolyLine(
        @SerializedName("points") val polyLinePoints: String
)

data class Location(
        @SerializedName("lat") val locationLat: Float,
        @SerializedName("lon") val locationLon: Float
)
