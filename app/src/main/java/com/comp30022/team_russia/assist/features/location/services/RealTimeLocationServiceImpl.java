package com.comp30022.team_russia.assist.features.location.services;

import android.support.annotation.NonNull;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.nav.models.LocationDto;

import com.google.android.gms.maps.model.LatLng;

import java9.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit api calls for navigation.
 */
interface RussiaRealTimeLocationApi {
    @POST("me/location")
    Call<Void> setApLocation(
        @Header("Authorization") String authToken,
        @Body LocationDto position);

    @GET("users/{id}/location")
    Call<LocationDto> getApLocation(
        @Header("Authorization") String authToken,
        @Path("id") int userId);
}

/**
 * Implementation of real time location service.
 */
public class RealTimeLocationServiceImpl implements RealTimeLocationService {
    /**
     * Auth service.
     */
    private AuthService authService;

    private RussiaRealTimeLocationApi realTimeLocationApi;

    /**
     * Implementation of real time location service.
     *
     * @param authService Auth service
     * @param retrofit    Retrofit
     */
    @Inject
    public RealTimeLocationServiceImpl(AuthService authService,
                                       Retrofit retrofit) {
        this.authService = authService;
        realTimeLocationApi = retrofit.create(RussiaRealTimeLocationApi.class);
    }

    /**
     * Updating AP location, expects body of { lat: number, lon: number }.
     *
     * @param latLng current location of AP.
     * @return ActionResult indicating result of set location call.
     */
    @Override
    public CompletableFuture<ActionResult<Void>> updateApCurrentLocation(LatLng latLng) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        Callback<Void> callback =
            new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(ActionResult.NO_ERROR));
                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            };

        realTimeLocationApi.setApLocation(authService.getAuthToken(),
            new LocationDto(latLng.latitude, latLng.longitude)).enqueue(callback);
        return result;
    }

    /**
     * Get location of Ap.
     *
     * @param userId ID of caller.
     * @return current location of Ap.
     */
    @Override
    public CompletableFuture<ActionResult<LatLng>> getApCurrentLocation(int userId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<LatLng>> result = new CompletableFuture<>();
        realTimeLocationApi.getApLocation(authService.getAuthToken(), userId).enqueue(
            new Callback<LocationDto>() {
                @Override
                public void onResponse(@NonNull Call<LocationDto> call,
                                       @NonNull Response<LocationDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.complete(
                            new ActionResult<>(new LatLng(
                                response.body().getLat(),
                                response.body().getLon())));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LocationDto> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });
        return result;
    }
}
