package com.comp30022.team_russia.assist.features.login.services;

import com.comp30022.team_russia.assist.features.login.models.RegistrationDTO;
import com.comp30022.team_russia.assist.features.login.models.User;

import java.util.Map;

import java9.util.concurrent.CompletableFuture;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

public class AuthServiceImpl implements AuthService {

    private String authToken = null;
    private RussiaApi russiaApi;


    public AuthServiceImpl() {
        Retrofit retrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://10.0.2.2:3000/")
            .build();

        russiaApi = retrofit.create(RussiaApi.class);
    }

    @Override
    public CompletableFuture<Boolean> login(String username, String password) {
        if (this.isLoggedIn()) {
            return CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.login(username, password).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> body = response.body();
                    result.complete(true);
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
    public boolean isLoggedIn() {
        return this.authToken != null;
    }

    @Override
    public CompletableFuture<Boolean> logout() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public String getCurrentUsername() {
        return null;
    }

    @Override
    public User getCurrentUser() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public CompletableFuture<Boolean> register(RegistrationDTO registrationInfo) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.register(registrationInfo).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                result.complete(true);
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
interface RussiaApi {
    @POST("users/register")
    Call<Map<String, String>> register(@Body RegistrationDTO info);

    @FormUrlEncoded
    @POST("users/login")
    Call<Map<String, String>> login(@Field("username") String username, @Field("password") String password);
}
