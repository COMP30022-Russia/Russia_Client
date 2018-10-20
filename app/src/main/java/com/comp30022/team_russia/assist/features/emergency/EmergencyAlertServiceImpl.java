package com.comp30022.team_russia.assist.features.emergency.services;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.login.services.AuthService;

import com.google.gson.annotations.SerializedName;
import java9.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * Retrofit api calls for navigation.
 */
interface RussiaEmergencyAlertApi {
    @POST("me/emergency")
    Call<Void> sendEmergency(
        @Header("Authorization") String authToken);

    @GET("emergencies/{id}")
    Call<EmergencyAlertDto> getEmergency(
        @Header("Authorization") String authToken,
        @Path("id") int eventId);

    @POST("emergencies/{id}")
    Call<Void> handleEmergency(
        @Header("Authorization") String authToken,
        @Path("id") int eventId);
}

class EmergencyAlertDto {
    @SerializedName("id")
    private String eventId;

    @SerializedName("carerId")
    private String carerId;

    @SerializedName("handled")
    private Boolean handled;

    @SerializedName("resolverId")
    private String resolverId;

    public EmergencyAlertDto(String eventId,
                             String carerId,
                             Boolean handled,
                             String resolverId) {
        this.eventId = eventId;
        this.carerId = carerId;
        this.handled = handled;
        this.resolverId = resolverId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCarerId() {
        return carerId;
    }

    public Boolean getHandled() {
        return handled;
    }

    public String getResolverId() {
        return resolverId;
    }
}

/**
 * Implementation of Emergency Alert Service.
 */
public class EmergencyAlertServiceImpl implements EmergencyAlertService {

    private AuthService authService;

    private RussiaEmergencyAlertApi emergencyAlertApi;

    private LoggerInterface logger;

    @Inject
    public EmergencyAlertServiceImpl(AuthService authService, Retrofit retrofit,
                                     LoggerFactory loggerFactory) {
        this.authService = authService;
        emergencyAlertApi = retrofit.create(RussiaEmergencyAlertApi.class);
        this.logger = loggerFactory.getLoggerForClass(this.getClass());
    }

    @Override
    public CompletableFuture<ActionResult<Void>> sendEmergency() {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();

        emergencyAlertApi.sendEmergency(authService.getAuthToken()).enqueue(
            new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(ActionResult.NO_ERROR));
                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });

        return result;
    }

    @Override
    public CompletableFuture<ActionResult<EmergencyAlertDto>> getEmergency(int eventId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<EmergencyAlertDto>> result = new CompletableFuture<>();

        emergencyAlertApi.getEmergency(authService.getAuthToken(), eventId).enqueue(
            new Callback<EmergencyAlertDto>() {
                @Override
                public void onResponse(Call<EmergencyAlertDto> call,
                                       Response<EmergencyAlertDto> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(response.body()));
                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(Call<EmergencyAlertDto> call, Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });

        return result;
    }

    @Override
    public CompletableFuture<ActionResult<Void>> handleEmergency(int eventId) {
        logger.debug("handleEmergency: start.  eventID = " + eventId);
        if (!authService.isLoggedInUnboxed()) {
            logger.debug("handleEmergency: failed not logged in.  eventID = " + eventId);
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();

        emergencyAlertApi.handleEmergency(authService.getAuthToken(), eventId).enqueue(
            new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(ActionResult.NO_ERROR));
                        logger.debug("handleEmergency: success.  eventID = " + eventId);
                    } else {
                        String error =  response.raw().toString();
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + error));
                        logger.error("handleEmergency: failed error in response. "
                                     + " eventID = " + eventId);
                        logger.error(error);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                    logger.error("handleEmergency: failed network error.  eventID = " + eventId);
                }
            });

        return result;
    }
}