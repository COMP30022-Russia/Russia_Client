package com.comp30022.team_russia.assist.features.assoc.services;

import android.util.Log;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDTO;
import com.comp30022.team_russia.assist.features.assoc.models.UserProfileDTO;
import com.comp30022.team_russia.assist.features.assoc.models.UserResponseDTO;
import com.comp30022.team_russia.assist.features.login.models.AP;
import com.comp30022.team_russia.assist.features.login.models.Carer;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;

import java.util.ArrayList;
import java.util.List;
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
import retrofit2.http.Query;

public class UserServiceImpl implements UserService {

    private final AuthService authService;
    private final RussiaUsersApi usersApi;

    @Inject
    public UserServiceImpl(AuthService authService, Retrofit retrofit) {
        this.authService = authService;
        usersApi = retrofit.create(RussiaUsersApi.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<ActionResult<String>> getAssociateToken() {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("UserService", "Not authenticated");
            return CompletableFuture.completedFuture(ActionResult.failedNotAutenticated());
        }

        CompletableFuture<ActionResult<String>> result = new CompletableFuture<>();
        usersApi.getAssociateToken(authService.getAuthToken())
            .enqueue(new Callback<AssociationTokenDTO>() {
                @Override
                public void onResponse(Call<AssociationTokenDTO> call,
                                       Response<AssociationTokenDTO> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(response.body().token));
                        return;
                    }
                    Log.e("UserService", "Failed to get associate token");
                    result.complete(ActionResult.failedCustomMessage("Whatever"));
                }

                @Override
                public void onFailure(Call<AssociationTokenDTO> call, Throwable t) {
                    result.complete(ActionResult.failedNetworkError());
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
            .enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(null));
                        return;
                    }
                    result.complete(ActionResult.failedCustomMessage("Raw response" + response.raw()));
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    result.complete(ActionResult.failedNetworkError());
                }
            });
        return result;
    }

    @Override
    public CompletableFuture<List<AssociationDTO>> getAssociatedUsers() {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("UserService", "Not authenticated");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        CompletableFuture<List<AssociationDTO>> result = new CompletableFuture<>();
        usersApi.getAssociations(authService.getAuthToken())
            .enqueue(new Callback<List<AssociationDTO>>() {
                @Override
                public void onResponse(Call<List<AssociationDTO>> call,
                                       Response<List<AssociationDTO>> response) {
                    if (response.isSuccessful())
                    {
                        result.complete(response.body());
                        return;
                    }
                    result.complete(new ArrayList<>());
                }

                @Override
                public void onFailure(Call<List<AssociationDTO>> call, Throwable t) {
                    result.complete(new ArrayList<>());
                }
            });
        return result;
    }

    @Override
    public CompletableFuture<Boolean> updateProfile(UserProfileDTO updatedInfo) {
        return null;
    }

    @Override
    public CompletableFuture<User> getUser(String userId) {
        return null;
    }

    @Override
    public CompletableFuture<ActionResult<User>> getUserFromAssociation(int associationId) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("UserService", "Not authenticated");
            return CompletableFuture.completedFuture(ActionResult.failedNotAutenticated());
        }

        CompletableFuture<ActionResult<User>> result = new CompletableFuture<>();
        usersApi.getAssociation(authService.getAuthToken(), associationId)
            .enqueue(new Callback<AssociationDTO>() {
                @Override
                public void onResponse(Call<AssociationDTO> call, Response<AssociationDTO> response) {
                    if (response.isSuccessful()) {
                        UserResponseDTO userData = response.body().user;
                        if (userData.type.equals("AP")) {
                            result.complete(new ActionResult<>(new AP(
                                userData.id,
                                userData.username,
                                "",
                                userData.name,
                                userData.mobileNumber,
                                userData.DOB,
                                userData.emergencyContactName,
                                userData.emergencyContactNumber,
                                userData.address
                            )));
                        } else {
                            result.complete(new ActionResult<>(new Carer(
                                userData.id,
                                userData.username,
                                "",
                                userData.name,
                                userData.mobileNumber,
                                userData.DOB
                            )));
                        }
                        return;
                    }
                    result.complete(ActionResult.failedCustomMessage("Failed to get association"));
                }

                @Override
                public void onFailure(Call<AssociationDTO> call, Throwable t) {
                    result.complete(ActionResult.failedNetworkError());
                }
            });

        return result;
    }
}

interface RussiaUsersApi {
    @GET("me/associations")
    Call<List<AssociationDTO>> getAssociations(
        @Header("Authorization") String authToken
    );

    @GET("me/association_token")
    Call<AssociationTokenDTO> getAssociateToken(
        @Header("Authorization") String authToken
    );

    @FormUrlEncoded
    @POST("me/associate")
    Call<Void> doAssociation(
        @Header("Authorization") String authToken,
        @Field("token") String token
    );

    @GET("associations/{id}")
    Call<AssociationDTO> getAssociation(
        @Header("Authorization") String authToken,
        @Path("id") int id);
}

class AssociationTokenDTO {
    String token;
}