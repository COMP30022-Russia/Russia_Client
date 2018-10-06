package com.comp30022.team_russia.assist.features.nav.models;


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

    // todo: to save nav session details in this class instead of having variables in vm

    public NavSession(int id,
                      Boolean active,
                      Boolean carerHasControl,
                      String state,
                      String transportMode,
                      Directions route,
                      int destinationId) {

        this.id = id;
        this.active = active;
        this.carerHasControl = carerHasControl;
        this.state = state;
        this.transportMode = transportMode;
        this.route = route;
        this.destinationId = destinationId;
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
}