package com.comp30022.team_russia.assist.features.nav.models;

import com.comp30022.team_russia.assist.features.call.models.NavCallDto;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an instance of a navigation session between an AP and a Carer,
 * only one instance at a time.
 */
public class NavSession {
    int id;
    Boolean active;
    Boolean carerHasControl;
    String state;
    String transportMode;
    Directions route;
    int destinationId;

    @SerializedName("Call")
    NavCallDto call;

    // todo: to save nav session details in this class instead of having variables in vm

    /**
     * Nav Session.
     */
    public NavSession(int id,
                      Boolean active,
                      Boolean carerHasControl,
                      String state,
                      String transportMode,
                      Directions route,
                      int destinationId,
                      NavCallDto call) {

        this.id = id;
        this.active = active;
        this.carerHasControl = carerHasControl;
        this.state = state;
        this.transportMode = transportMode;
        this.route = route;
        this.destinationId = destinationId;
        this.call = call;
    }

    public int getId() {
        return id;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getCarerHasControl() {
        return carerHasControl;
    }

    public String getState() {
        return state;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public Directions getRoute() {
        return route;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public NavCallDto getCall() {
        return call;
    }
}