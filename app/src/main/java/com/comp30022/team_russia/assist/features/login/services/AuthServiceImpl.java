package com.comp30022.team_russia.assist.features.login.services;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.Carer;
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

    /**
     * Constructor.
     * @param retrofit Retrofit instance.
     */
    private Gson gson = new Gson();

    @Inject
    public AuthServiceImpl(Retrofit retrofit, PubSubHub notificationHub) {
        russiaApi = retrofit.create(RussiaLoginRegisterApi.class);
        isLoggedInLiveData = LiveDataKt.map(authToken, value ->
                authToken.getValue() != null
                        && !authToken.getValue().isEmpty());
        authToken.postValue(null);


        notificationHub.configureTopic(PubSubTopics.FIREBASE_TOKEN, FirebaseTokenData.class,
            new PayloadToObjectConverter<FirebaseTokenData>() {
                @Override
                public FirebaseTokenData fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, FirebaseTokenData.class);
                }

                @Override
                public String toString(FirebaseTokenData payload) {
                    return gson.toJson(payload);
                }
            });

        // listen for new Firebase tokens (could be unchanged)
        notificationHub.subscribe(PubSubTopics.FIREBASE_TOKEN,
            new SubscriberCallback<FirebaseTokenData>() {
                @Override
                public void onReceived(FirebaseTokenData payload) {
                    AuthServiceImpl.this.updateFirebaseToken(payload);
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> login(String username, String password) {
        if (isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.login(username, password).enqueue(
            new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call,
                                       Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> body = response.body();
                        if (body.containsKey("token")) {
                            AuthServiceImpl.this.authToken
                                .postValue(body.get("token"));
                            String type = body.get("type");
                            if (type.equals("AP")) {
                                AuthServiceImpl.this.currentUser = new AssistedPerson(
                                    Integer.parseInt(body.get("id")),
                                    body.get("username"),
                                    body.get("password"),
                                    body.get("name"),
                                    body.get("mobileNumber"),
                                    User.parseDoB(body.get("DOB")),
                                    body.get("emergencyContactName"),
                                    body.get("emergencyContactNumber"),
                                    body.get("address")
                                );
                            } else {
                                AuthServiceImpl.this.currentUser = new Carer(
                                    Integer.parseInt(body.get("id")),
                                    body.get("username"),
                                    body.get("password"),
                                    body.get("name"),
                                    body.get("mobileNumber"),
                                    User.parseDoB(body.get("DOB"))
                                );
                            }
                            result.complete(true);
                            return;
                        }
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
            return CompletableFuture.completedFuture(ActionResult.failedNotAutenticated());
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
    Call<Map<String, String>> login(@Field("username") String username,
                                    @Field("password") String password);

    @FormUrlEncoded
    @POST("me/token")
    Call<Void> updateFirebaseToken(@Header("Authorization") String authToken,
                                   @Field("instanceID") String instanceId,
                                   @Field("token") String newToken);
}
