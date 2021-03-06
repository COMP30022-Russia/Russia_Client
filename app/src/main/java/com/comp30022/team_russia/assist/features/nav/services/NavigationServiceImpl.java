package com.comp30022.team_russia.assist.features.nav.services;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.nav.models.DestinationDto;
import com.comp30022.team_russia.assist.features.nav.models.Directions;
import com.comp30022.team_russia.assist.features.nav.models.LocationDto;
import com.comp30022.team_russia.assist.features.nav.models.NavSession;
import com.comp30022.team_russia.assist.features.nav.models.RecentDto;

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
interface RussiaNavigationApi {
    @POST("associations/{id}/navigation")
    Call<NavSession> createNewNavigationSession(
        @Header("Authorization") String authToken,
        @Path("id") int associationId);

    @GET("navigation/{id}")
    Call<NavSession> getNavigationSession(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId);

    @GET("me/navigation")
    Call<NavSession> getCurrentNavigationSession(
        @Header("Authorization") String authToken);

    @POST("navigation/{id}/end")
    Call<Void> endNavigationSession(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId);

    @GET("navigation/{id}/route")
    Call<Directions> getRoute(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId);

    @POST("navigation/{id}/destination")
    Call<Void> setDestination(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId,
        @Body DestinationDto destination);

    @POST("navigation/{id}/control")
    Call<Void> switchControl(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId);

    @POST("navigation/{id}/location")
    Call<Void> updateCurrentLocation(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId,
        @Body LocationDto position);

    @GET("navigation/{id}/location")
    Call<LocationDto> getLocation(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId);

    @POST("navigation/{id}/off_track")
    Call<Void> updateOffTrack(
        @Header("Authorization") String authToken,
        @Path("id") int navSessionId);

    @GET("users/{id}/destinations")
    Call<RecentDto> getDestinations(
        @Header("Authorization") String authToken,
        @Path("id") int userId
        /*todo limit*/);

    @POST("users/{userId}/destinations/{destinationId}")
    Call<Void> setFavourite(
        @Header("Authorization") String authToken,
        @Path("userId") int userId,
        @Path("destinationId") int destinationId
        /*todo boolean*/);
}

/**
 * Implementation of Navigation Service.
 */
public class NavigationServiceImpl implements NavigationService {


    private static final long DELAY = 1000; // 1 seconds

    private final Handler checkStateHandler = new Handler();

    private final Runnable checkStateRunnable = () -> checkState();

    private AuthService authService;

    private final MutableLiveData<NavSession> currentNavSession = new MutableLiveData<>();

    private RussiaNavigationApi navigationApi;

    /**
     * Constructor.
     */
    @Inject
    public NavigationServiceImpl(AuthService authService, Retrofit retrofit) {
        this.authService = authService;
        navigationApi = retrofit.create(RussiaNavigationApi.class);

        checkStateHandler.post(checkStateRunnable);
    }


    /**
     * Check if nav session exist every 5 seconds.
     */
    private void checkState() {
        getCurrentNavigationSession().thenAccept(result ->
            checkStateHandler.postDelayed(checkStateRunnable, DELAY));
    }


    /**
     * Create new navigation session.
     *
     * @param assocId ID of association.
     * @return ActionResult with ID of created navigation session.
     */
    @Override
    public CompletableFuture<ActionResult<NavSession>> createNewNavigationSession(int assocId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<NavSession>> result = new CompletableFuture<>();
        navigationApi.createNewNavigationSession(authService.getAuthToken(), assocId).enqueue(
            new Callback<NavSession>() {
                @Override
                public void onResponse(@NonNull Call<NavSession> call,
                                       @NonNull Response<NavSession> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentNavSession.postValue(response.body());
                        result.complete(
                            new ActionResult<>(response.body()));
                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NavSession> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });
        return result;
    }

    /**
     * Get an existing navigation session using sessionID.
     *
     * @param sessionId ID of session.
     * @return ActionResult with specified navigation session or an error.
     */
    @Override
    public CompletableFuture<ActionResult<NavSession>> getNavigationSession(int sessionId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<NavSession>> result = new CompletableFuture<>();
        navigationApi.getNavigationSession(authService.getAuthToken(), sessionId).enqueue(
            new Callback<NavSession>() {
                @Override
                public void onResponse(@NonNull Call<NavSession> call,
                                       @NonNull Response<NavSession> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentNavSession.postValue(response.body());
                        result.complete(new ActionResult<>(response.body()));
                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NavSession> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });
        return result;
    }

    /**
     * Get the only an existing navigation session.
     *
     * @return ActionResult with ID of existing navigation session.
     */
    @Override
    public CompletableFuture<ActionResult<NavSession>> getCurrentNavigationSession() {
        // todo Should this return the body (i.e. the navigation session) also?
        // todo Since the route is returned here also.
        // todo use this to restore the session
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<NavSession>> result = new CompletableFuture<>();
        navigationApi.getCurrentNavigationSession(authService.getAuthToken()).enqueue(
            new Callback<NavSession>() {
                @Override
                public void onResponse(@NonNull Call<NavSession> call,
                                       @NonNull Response<NavSession> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentNavSession.postValue(response.body());
                        result.complete(new ActionResult<>(response.body()));
                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NavSession> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });
        return result;
    }

    /**
     * End a navigation session using sessionID.
     *
     * @param sessionId ID of session.
     * @return ActionResult indicating result of end navigation session API call.
     */
    @Override
    public CompletableFuture<ActionResult<Void>> endNavigationSession(int sessionId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        navigationApi.endNavigationSession(authService.getAuthToken(), sessionId).enqueue(
            new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        currentNavSession.postValue(null);
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
            });
        return result;
    }


    /**
     * Get the directions generated from server.
     *
     * @param sessionId ID of session.
     * @return ActionResult with the directions.
     */
    @Override
    public CompletableFuture<ActionResult<Directions>> getDirections(int sessionId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Directions>> result = new CompletableFuture<>();
        navigationApi.getRoute(authService.getAuthToken(), sessionId).enqueue(
            new Callback<Directions>() {
                @Override
                public void onResponse(@NonNull Call<Directions> call,
                                       @NonNull Response<Directions> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.complete(new ActionResult<>(response.body()));

                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Directions> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });
        return result;
    }



    /**
     * Sets the destination of navigation.
     *
     * @param sessionId ID of session.
     * @param placeId   Google Maps placeId of destination.
     * @param name      Name of destination.
     * @param mode      Transport mode.
     * @return ActionResult indicating result of set destination call.
     */
    @Override
    public CompletableFuture<ActionResult<Void>> setDestination(int sessionId, String placeId,
                                                                String name, String mode) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
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
        navigationApi.setDestination(authService.getAuthToken(), sessionId,
            new DestinationDto(placeId, name, mode)).enqueue(callback);
        return result;
    }

    /**
     * Switch control of navigation session.
     *
     * @param sessionId ID of session.
     * @return ActionResult indicating result of switch control call.
     */
    @Override
    public CompletableFuture<ActionResult<Void>> switchControl(int sessionId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        navigationApi.switchControl(authService.getAuthToken(), sessionId).enqueue(
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
            });
        return result;
    }

    /**
     * Updating AP location, expects body of { lat: number, lon: number }.
     *
     * @param sessionId ID of session.
     * @param latLng    A LatLng object of the current location of the AP.
     * @return ActionResult indicating result of set location call.
     */
    @Override
    public CompletableFuture<ActionResult<Void>> updateCurrentLocation(int sessionId,
                                                                       LatLng latLng) {
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

        navigationApi.updateCurrentLocation(authService.getAuthToken(), sessionId,
            new LocationDto(latLng.latitude, latLng.longitude)).enqueue(callback);
        return result;
    }

    /**
     * Get current location of AP.
     *
     * @param sessionId ID of session.
     * @return ActionResult with current location of AP.
     */
    @Override
    public CompletableFuture<ActionResult<LatLng>> getCurrentLocation(int sessionId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<LatLng>> result = new CompletableFuture<>();
        navigationApi.getLocation(authService.getAuthToken(), sessionId).enqueue(
            new Callback<LocationDto>() {
                @Override
                public void onResponse(@NonNull Call<LocationDto> call,
                                       @NonNull Response<LocationDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.complete(
                            new ActionResult<>(new LatLng(
                                response.body().getLat(),
                                response.body().getLon())));
                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LocationDto> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });
        return result;
    }


    /**
     * Update server that AP went off track.
     * @param sessionId current session ID
     * @return nothing
     */
    @Override
    public CompletableFuture<ActionResult<Void>> updateApOffTrack(int sessionId) {
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

        navigationApi.updateOffTrack(authService.getAuthToken(), sessionId).enqueue(callback);
        return result;
    }

    /**
     * Get the recents & favourites destination from server.
     * @param userId user id.
     * @param limit max number of recents.
     * @return list of destinations.
     */
    @Override
    public CompletableFuture<ActionResult<RecentDto>> getDestinations(int userId, int limit) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<RecentDto>> result = new CompletableFuture<>();

        navigationApi.getDestinations(authService.getAuthToken(), userId /*todo limit*/).enqueue(
            new Callback<RecentDto>() {
                @Override
                public void onResponse(@NonNull Call<RecentDto> call,
                                       @NonNull Response<RecentDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.complete(new ActionResult<>(response.body()));
                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RecentDto> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));

                }
            });
        return result;
    }

    /**
     * Set or unset a destination to a favourite.
     * @param userId user id
     * @param destinationId destination id
     * @param toFav to set ot unset
     * @return success of failure
     */
    @Override
    public CompletableFuture<ActionResult<Void>> setFavourites(int userId,
                                                               int destinationId, boolean toFav) {

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

        navigationApi.setFavourite(authService.getAuthToken(), userId, destinationId/*todo toFav*/)
            .enqueue(callback);
        return result;
    }

    @Override
    public LiveData<NavSession> getCurrentNavSessionLiveData() {

        return currentNavSession;
    }
}
