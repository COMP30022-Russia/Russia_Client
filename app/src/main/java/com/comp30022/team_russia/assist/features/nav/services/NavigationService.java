package com.comp30022.team_russia.assist.features.nav.services;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.nav.models.Directions;
import com.comp30022.team_russia.assist.features.nav.models.NavSession;
import com.comp30022.team_russia.assist.features.nav.models.RecentDto;

import com.google.android.gms.maps.model.LatLng;

import java9.util.concurrent.CompletableFuture;

/**
 * Navigation Service used to update and retrieve information from server.
 */
public interface NavigationService {

    CompletableFuture<ActionResult<NavSession>> createNewNavigationSession(int assocId);

    CompletableFuture<ActionResult<NavSession>> getNavigationSession(int sessionId);

    CompletableFuture<ActionResult<NavSession>> getCurrentNavigationSession();

    CompletableFuture<ActionResult<Void>> endNavigationSession(int sessionId);

    CompletableFuture<ActionResult<Directions>> getDirections(int sessionId);

    CompletableFuture<ActionResult<Void>> setDestination(
        int sessionId, String placeId, String name, String mode);

    CompletableFuture<ActionResult<Void>> switchControl(int sessionId);

    CompletableFuture<ActionResult<Void>> updateCurrentLocation(int sessionId, LatLng latLng);

    CompletableFuture<ActionResult<LatLng>> getCurrentLocation(int sessionId);

    CompletableFuture<ActionResult<Void>> updateApOffTrack(int sessionId);

    CompletableFuture<ActionResult<RecentDto>> getDestinations(int userId, int limit);

    CompletableFuture<ActionResult<Void>> setFavourites(
        int userId, int destinationId, boolean toFav);
}
