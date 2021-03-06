package com.comp30022.team_russia.assist.features.nav.models;

import com.google.gson.annotations.SerializedName;

/**
 * Destination class
 * used to send location info to server.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DestinationDto {
    @SerializedName("placeID")
    private String placeId;

    @SerializedName("name")
    private String name;

    @SerializedName("mode")
    private String mode;

    /**
     * DestinationDto.
     */
    public DestinationDto(String placeId, String name, String mode) {
        this.placeId = placeId;
        this.name = name;
        this.mode = mode;
    }
}
