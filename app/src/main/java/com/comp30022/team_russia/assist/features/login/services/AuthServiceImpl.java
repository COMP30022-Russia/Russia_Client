package com.comp30022.team_russia.assist.features.login.services;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.Carer;
import com.comp30022.team_russia.assist.features.login.models.RegistrationDto;
import com.comp30022.team_russia.assist.features.login.models.User;
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
    @Inject
    public AuthServiceImpl(Retrofit retrofit) {
        russiaApi = retrofit.create(RussiaLoginRegisterApi.class);
        isLoggedInLiveData = LiveDataKt.map(authToken, value ->
            authToken.getValue() != null
                && !authToken.getValue().isEmpty());
        authToken.postValue(null);
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
}
