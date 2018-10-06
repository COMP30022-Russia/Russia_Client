package com.comp30022.team_russia.assist.features.nav.models;

import com.google.gson.annotations.SerializedName;

/**
 * LocationDto class similar to LatLng
 * used to send positional info to server.
 */
public class LocationDto {

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    public LocationDto(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

}
