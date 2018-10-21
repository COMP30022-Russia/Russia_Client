package com.comp30022.team_russia.assist.features.location.services;

import com.comp30022.team_russia.assist.base.ActionResult;

import com.google.android.gms.maps.model.LatLng;

import java9.util.concurrent.CompletableFuture;

/**
 * Location service to get location of ap outside of navigation.
 */
public interface RealTimeLocationService {
    CompletableFuture<ActionResult<Void>> updateApCurrentLocation(LatLng latLng);

    CompletableFuture<ActionResult<LatLng>> getApCurrentLocation(int userId);
}
