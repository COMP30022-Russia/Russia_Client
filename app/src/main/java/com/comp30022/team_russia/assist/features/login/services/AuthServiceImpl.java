package com.comp30022.team_russia.assist.features.login.services;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.base.persist.KeyValueStore;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.Carer;
import com.comp30022.team_russia.assist.features.login.models.LoginResultDto;
import com.comp30022.team_russia.assist.features.login.models.RegistrationDto;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.FirebaseTokenData;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;

import com.google.gson.Gson;
import com.shopify.livedataktx.LiveDataKt;

import java.util.Map;

import java9.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Default implementation of {@link AuthService}.
 */
public class AuthServiceImpl implements AuthService {

    private final MutableLiveData<String> authToken = new MutableLiveData<>();
    private RussiaLoginRegisterApi russiaApi;
    private final LiveData<Boolean> isLoggedInLiveData;
    private User currentUser;

    private final KeyValueStore keyValueStore;
    private final ToastService toastService;
    private final PubSubHub pubSubHub;

    private Gson gson = new Gson();

    /**
     * AuthServiceImpl Constructor.
     * @param retrofit Retrofit required for the AuthService.
     * @param notificationHub NotificationHub for the AuthService.
     */
    @Inject
    public AuthServiceImpl(Retrofit retrofit, PubSubHub notificationHub,
                           KeyValueStore keyValueStore, ToastService toastService) {
        russiaApi = retrofit.create(RussiaLoginRegisterApi.class);
        this.keyValueStore = keyValueStore;
        this.pubSubHub = notificationHub;
        this.toastService = toastService;

        isLoggedInLiveData = LiveDataKt.map(authToken, value ->
            authToken.getValue() != null
            && !authToken.getValue().isEmpty());

        restoreLoginState();



        // listen for new Firebase tokens (could be unchanged)
        this.pubSubHub.subscribe(PubSubTopics.FIREBASE_TOKEN,
            new SubscriberCallback<FirebaseTokenData>() {
                @Override
                public void onReceived(FirebaseTokenData payload) {
                    AuthServiceImpl.this.updateFirebaseToken(payload);
                }
            });
    }

    private void restoreLoginState() {
        if (keyValueStore.hasAuthToken()) {
            toastService.toastShort("Re-logging you in...");
            String storedAuthToken = keyValueStore.getAuthToken();
            // Even if we have persisted auth token, we still need to make an HTTP request to:
            //  1) Verify that the authToken is still valid
            //  2) Get the latest user profile
            // Only after that can we consider the app "logged in".
            russiaApi.getProfile("Bearer " + storedAuthToken).enqueue(
                new Callback<LoginResultDto>() {
                    @Override
                    public void onResponse(Call<LoginResultDto> call,
                                           Response<LoginResultDto> response) {
                        if (response.isSuccessful()) {
                            LoginResultDto body = response.body();
                            if (body.getType().equals("AP")) {
                                AuthServiceImpl.this.currentUser = new AssistedPerson(
                                    body.getId(),
                                    body.getUsername(),
                                    "",
                                    body.getName(),
                                    body.getMobileNumber(),
                                    body.getDateOfBirth(),
                                    body.getEmergencyContactName(),
                                    body.getEmergencyContactNumber()
                                );
                            } else {
                                AuthServiceImpl.this.currentUser = new Carer(
                                    body.getId(),
                                    body.getUsername(),
                                    "",
                                    body.getName(),
                                    body.getMobileNumber(),
                                    body.getDateOfBirth()
                                );
                            }
                            authToken.postValue(storedAuthToken);
                            toastService.toastShort("Welcome back!");
                            pubSubHub.publish(PubSubTopics.LOGGED_IN, (Void)null);
                        } else {
                            keyValueStore.clearAuthToken();
                            authToken.postValue(null);
                            toastService
                                .toastShort("Your credential has expired. Please login again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResultDto> call, Throwable t) {
                        keyValueStore.clearAuthToken();
                        authToken.postValue(null);
                        toastService
                            .toastShort("Your credential has expired. Please login again.");
                    }
                });
        } else {
            this.authToken.postValue(null);
        }
    }

    @Override
    public CompletableFuture<Boolean> login(String username, String password) {
        if (isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.login(username, password)
            .enqueue(new Callback<LoginResultDto>() {
                @Override
                public void onResponse(Call<LoginResultDto> call,
                                       Response<LoginResultDto> response) {
                    if (response.isSuccessful()) {
                        LoginResultDto body = response.body();
                        if (body.getToken() != null) {
                            String newAuthToken = body.getToken();
                            AuthServiceImpl.this.keyValueStore.saveAuthToken(newAuthToken);
                            AuthServiceImpl.this.authToken.postValue(newAuthToken);
                            if (body.getType().equals("AP")) {
                                AuthServiceImpl.this.currentUser = new AssistedPerson(
                                    body.getId(),
                                    body.getUsername(),
                                    "",
                                    body.getName(),
                                    body.getMobileNumber(),
                                    body.getDateOfBirth(),
                                    body.getEmergencyContactName(),
                                    body.getEmergencyContactNumber()
                                );
                            } else {
                                AuthServiceImpl.this.currentUser = new Carer(
                                    body.getId(),
                                    body.getUsername(),
                                    "",
                                    body.getName(),
                                    body.getMobileNumber(),
                                    body.getDateOfBirth()
                                );
                            }
                            pubSubHub.publish(PubSubTopics.LOGGED_IN, null);
                            result.complete(true);
                            return;
                        }
                    }
                    result.complete(false);
                }

                @Override
                public void onFailure(Call<LoginResultDto> call, Throwable t) {
                    result.complete(false);

                }
            });
        return result;
    }

    @Override
    public LiveData<Boolean> isLoggedIn() {
        return this.isLoggedInLiveData;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean isLoggedInUnboxed() {
        try {
            return this.isLoggedIn().getValue();
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public String getAuthToken() {
        return "Bearer " + this.authToken.getValue();
    }

    @Override
    public CompletableFuture<Boolean> logout() {
        this.authToken.postValue("");
        this.keyValueStore.clearAuthToken();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public CompletableFuture<Boolean> register(RegistrationDto registrationInfo) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.register(registrationInfo).enqueue(

            new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call,
                                       Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        result.complete(true);
                        login(registrationInfo.getUsername(), registrationInfo.getPassword());
                        return;
                    }
                    result.complete(false);
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    result.complete(false);
                }
            });
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<ActionResult<Void>> updateFirebaseToken(
        FirebaseTokenData newTokenData) {
        if (!this.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(ActionResult.failedNotAuthenticated());
        }


        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        russiaApi.updateFirebaseToken(this.getAuthToken(),
            newTokenData.getInstanceId(), newTokenData.getToken())
            .enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult(null));
                        return;
                    }
                    result.complete(ActionResult
                        .failedCustomMessage("Failed to update Firebase token."));
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    result.complete(ActionResult.failedNetworkError());
                }
            });
        return result;
    }
}

/**
 * Helper interface to use Retrofit with.
 */
interface RussiaLoginRegisterApi {
    @POST("users/register")
    Call<Map<String, String>> register(@Body RegistrationDto info);

    @FormUrlEncoded
    @POST("users/login")
    Call<LoginResultDto> login(@Field("username") String username,
                               @Field("password") String password);

    @GET("me/profile")
    Call<LoginResultDto> getProfile(@Header("Authorization") String authToken);

    @FormUrlEncoded
    @POST("me/token")
    Call<Void> updateFirebaseToken(@Header("Authorization") String authToken,
                                   @Field("instanceID") String instanceId,
                                   @Field("token") String newToken);
}