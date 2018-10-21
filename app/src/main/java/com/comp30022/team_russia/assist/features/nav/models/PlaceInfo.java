package com.comp30022.team_russia.assist.features.nav.models;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

/**
 * Represents a place retrieved from google maps api.
 */
public class PlaceInfo {

    private static final String TAG = "PlaceInfo";
    private String name;
    private String address;
    private String phoneNumber;
    private String id;
    private Uri websiteUri;
    private LatLng latlng;
    private float rating;
    private String attributions;


    /**
     * Place Info Data.
     */
    public PlaceInfo(String name, String address, String phoneNumber, String id, Uri websiteUri,
                     LatLng latlng, float rating, String attributions) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.websiteUri = websiteUri;
        this.latlng = latlng;
        this.rating = rating;
        this.attributions = attributions;
    }

    public PlaceInfo() {

    }

    /**
     * Convert result from google api to a PlaceInfo object.
     * @param place The Place object.
     * @return PlaceInfo object.
     */
    public static PlaceInfo fromGoogleApiPlace(Place place) {
        Log.e(TAG, "fromGoogleApiPlace");
        Log.i(TAG, "fromGoogleApiPlace" + place.toString());
        PlaceInfo placeInfo = new PlaceInfo();
        placeInfo.setName(place.getName().toString());
        placeInfo.setAddress(place.getAddress().toString());
        placeInfo.setId(place.getId());
        placeInfo.setLatlng(place.getLatLng());
        placeInfo.setRating(place.getRating());
        placeInfo.setPhoneNumber(place.getPhoneNumber().toString());
        placeInfo.setWebsiteUri(place.getWebsiteUri());
        return placeInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getWebsiteUri() {
        return websiteUri;
    }

    public void setWebsiteUri(Uri websiteUri) {
        this.websiteUri = websiteUri;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAttributions() {
        return attributions;
    }

    public void setAttributions(String attributions) {
        this.attributions = attributions;
    }

    @Override
    public String toString() {
        return "PlaceInfo{"
               + "name='" + name + '\''
               + ", currentAddress='" + address + '\''
               + ", phoneNumber='" + phoneNumber + '\''
               + ", id='" + id + '\''
               + ", websiteUri=" + websiteUri
               + ", latlng=" + latlng
               + ", rating=" + rating
               + ", attributions='" + attributions + '\'' + '}';
    }
}