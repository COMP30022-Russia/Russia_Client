package com.comp30022.team_russia.assist.features.nav.service;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.nav.models.NavSession;
import com.comp30022.team_russia.assist.features.nav.models.Route;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import java9.util.concurrent.CompletableFuture;

/**
 * Navigation Service used to update and retrieve information from server.
 */
public interface NavigationService {

    CompletableFuture<ActionResult<NavSession>> createNewNavigationSession(int assocId);

    CompletableFuture<ActionResult<NavSession>> getNavigationSession(int sessionId);

    CompletableFuture<ActionResult<NavSession>> getCurrentNavigationSession();

    CompletableFuture<ActionResult<Void>> endNavigationSession(int sessionId);

    CompletableFuture<ActionResult<List<Route>>> getRoute(int sessionId);

    CompletableFuture<ActionResult<Void>> setDestination(int sessionId, String placeId,
                                                         String name, String mode);

    CompletableFuture<ActionResult<Void>> switchControl(int sessionId);

    CompletableFuture<ActionResult<Void>> updateCurrentLocation(int sessionId, LatLng latLng);

    CompletableFuture<ActionResult<LatLng>> getCurrentLocation(int sessionId);
}
