package com.comp30022.team_russia.assist.features.assoc.services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.assoc.db.UserAssociationCache;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDto;
import com.comp30022.team_russia.assist.features.assoc.models.DoAssociationResponseDto;
import com.comp30022.team_russia.assist.features.assoc.models.UserResponseDto;
import com.comp30022.team_russia.assist.features.chat.models.Association;
import com.comp30022.team_russia.assist.features.chat.models.UserContact;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.Carer;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;

import java.util.ArrayList;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import java9.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Default implementation of {@link UserService}.
 */
public class UserServiceImpl implements UserService {

    private final AuthService authService;
    private final RussiaUsersApi usersApi;
    private final UserAssociationCache usersCache;

    /**
     * User Service Implementation Constructor.
     * @param authService AuthService used by the UserService.
     * @param retrofit Retrofit used by the UserService.
     */
    @Inject
    public UserServiceImpl(AuthService authService,
                           Retrofit retrofit,
                           UserAssociationCache usersCache) {
        this.authService = authService;
        this.usersCache = usersCache;
        usersApi = retrofit.create(RussiaUsersApi.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<ActionResult<String>> getAssociateToken() {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("UserService", "Not authenticated");
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<String>> result = new CompletableFuture<>();
        usersApi.getAssociateToken(authService.getAuthToken())
            .enqueue(new Callback<AssociationTokenDto>() {
                @Override
                public void onResponse(@NonNull Call<AssociationTokenDto> call,
                                       @NonNull Response<AssociationTokenDto> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(response.body().token));
                        return;
                    }
                    Log.e("UserService", "Failed to get associate token");
                    result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                        "Failed to get auth token"));
                }

                @Override
                public void onFailure(@NonNull Call<AssociationTokenDto> call,
                                      @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<ActionResult<Void>> associateWith(String token) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("UserService", "Not authenticated");
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        usersApi.doAssociation(authService.getAuthToken(), token)
            .enqueue(new Callback<DoAssociationResponseDto>() {
                @Override
                public void onResponse(@NonNull Call<DoAssociationResponseDto> call,
                                       @NonNull Response<DoAssociationResponseDto> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(null));
                        // update local cache for association
                        DoAssociationResponseDto newAssociation = response.body();
                        User.UserType userType = authService.getCurrentUser().getUserType();

                        usersCache.insertOrUpdateAssociation(
                            new Association(
                                newAssociation.getId(),
                                userType == User.UserType.AP
                                    ? newAssociation.getCarerId() : newAssociation.getAPId(),
                                newAssociation.getActive()
                            )
                        );
                        return;
                    }
                    result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                        "Raw response" + response.raw()));
                }

                @Override
                public void onFailure(@NonNull Call<DoAssociationResponseDto> call,
                                      @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });

        return result;
    }

    @Override
    public CompletableFuture<List<AssociationDto>> getAssociatedUsers() {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("UserService", "Not authenticated");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        CompletableFuture<List<AssociationDto>> result = new CompletableFuture<>();
        usersApi.getAssociations(authService.getAuthToken())
            .enqueue(new Callback<List<AssociationDto>>() {
                @Override
                public void onResponse(@NonNull Call<List<AssociationDto>> call,
                                       @NonNull Response<List<AssociationDto>> response) {
                    if (response.isSuccessful()) {
                        List<AssociationDto> associations = response.body();
                        result.complete(associations);
                        // update local cache

                        usersCache.replaceAssociations(
                            StreamSupport.stream(associations).map(associationDto
                                -> new Association(
                                associationDto.getId(),
                                associationDto.getUser().getId(),
                                true)).collect(Collectors.toList())
                        );

                        usersCache.batchUpdateUserProfiles(
                            StreamSupport.stream(associations).map(associationDto
                                -> new UserContact(
                                    associationDto.getUser().getId(),
                                    associationDto.getUser().getName()
                            )).collect(Collectors.toList())
                        );

                        return;
                    }
                    result.complete(new ArrayList<>());
                }

                @Override
                public void onFailure(@NonNull Call<List<AssociationDto>> call,
                                      @NonNull Throwable t) {
                    result.complete(new ArrayList<>());
                }
            });
        return result;
    }

    @Override
    public CompletableFuture<User> getUser(String userId) {
        return null;
    }

    @Override
    public CompletableFuture<ActionResult<User>> getUserFromAssociation(int associationId) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("UserService", "Not authenticated");
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<User>> result = new CompletableFuture<>();
        usersApi.getAssociation(authService.getAuthToken(), associationId)
            .enqueue(new Callback<AssociationDto>() {
                @Override
                public void onResponse(@NonNull Call<AssociationDto> call,
                                       @NonNull Response<AssociationDto> response) {
                    if (response.isSuccessful()) {
                        UserResponseDto userData = response.body().getUser();
                        if (userData.getType().equals("AP")) {
                            result.complete(new ActionResult<>(new AssistedPerson(
                                userData.getId(),
                                userData.getUsername(),
                                "",
                                userData.getName(),
                                userData.getMobileNumber(),
                                userData.getDOB(),
                                userData.getEmergencyContactName(),
                                userData.getEmergencyContactNumber()
                            )));

                        } else {
                            result.complete(new ActionResult<>(new Carer(
                                userData.getId(),
                                userData.getUsername(),
                                "",
                                userData.getName(),
                                userData.getMobileNumber(),
                                userData.getDOB()
                            )));
                        }
                        // update local cache.
                        usersCache.insertOrUpdateUserProfile(new UserContact(
                            userData.getId(),
                            userData.getName()));
                        return;
                    }
                    result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                        "Failed to get association"));
                }

                @Override
                public void onFailure(@NonNull Call<AssociationDto> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });

        return result;
    }
}

/**
 * RussiaUsersApi.
 */
interface RussiaUsersApi {
    @GET("me/associations")
    Call<List<AssociationDto>> getAssociations(
        @Header("Authorization") String authToken
    );

    @GET("me/association_token")
    Call<AssociationTokenDto> getAssociateToken(
        @Header("Authorization") String authToken
    );

    @FormUrlEncoded
    @POST("me/associate")
    Call<DoAssociationResponseDto> doAssociation(
        @Header("Authorization") String authToken,
        @Field("token") String token
    );

    @GET("associations/{id}")
    Call<AssociationDto> getAssociation(
        @Header("Authorization") String authToken,
        @Path("id") int id);
}

/**
 * AssociationTokenDto.
 */
class AssociationTokenDto {
    String token;
}