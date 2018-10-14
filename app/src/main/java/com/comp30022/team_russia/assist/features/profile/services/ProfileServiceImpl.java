package com.comp30022.team_russia.assist.features.profile.services;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.Carer;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.models.ProfileDto;
import com.comp30022.team_russia.assist.features.profile.models.ProfilePic;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java9.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;


/**
 * Default implementation of {@link ProfileService}.
 */

public class ProfileServiceImpl implements ProfileService {
    private final LoggerInterface logger;
    private ProfileApi russiaApi;
    private AuthService authService;
    private User currentUser;
    private ProfilePic profilePic;

    @Inject
    public ProfileServiceImpl(AuthService authService,
                              Retrofit retrofit,
                              LoggerFactory loggerFactory) {
        this.authService = authService;
        russiaApi = retrofit.create(ProfileApi.class);
        this.logger = loggerFactory.create(this.getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<Boolean> update(ProfileDto updateInfo) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.update(authService.getAuthToken(),updateInfo).enqueue(

            new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call,
                                       Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        logger.debug("update: Successful");
                        result.complete(true);
                        //login(registrationInfo.getUsername(), registrationInfo.getPassword());
                        return;
                    }
                    logger.debug("update: Not Successful");
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
    public CompletableFuture<Boolean> updatePassword(String password) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.updatePassword(authService.getAuthToken(),password).enqueue(
            new Callback<Map<String,String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call,
                                       Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        logger.debug("updatePassword: Password Update Successful");
                        result.complete(true);
                        return;
                    }
                    logger.debug("updatePassword: Not Successful");
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
    public CompletableFuture<Boolean> getDetails() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.getDetails(authService.getAuthToken()).enqueue(
            new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call,
                                       Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {

                        logger.debug("getDetails: Successful");

                        Map<String, String> body = response.body();

                        if (body.containsKey("name")) {
                            logger.debug("getDetails: Name" + body.get("name"));
                        }

                        User.UserType type = authService.getCurrentUser().getUserType();
                            if (type == User.UserType.AP) {

                                ProfileServiceImpl.this.currentUser = new AssistedPerson(
                                    Integer.parseInt(body.get("id")),
                                    body.get("username"),
                                    body.get("password"),
                                    body.get("name"),
                                    body.get("mobileNumber"),
                                    body.get("DOB"),
                                    body.get("emergencyContactName"),
                                    body.get("emergencyContactNumber")
                                );
                            } else {

                                ProfileServiceImpl.this.currentUser = new Carer(
                                    Integer.parseInt(body.get("id")),
                                    body.get("username"),
                                    body.get("password"),
                                    body.get("name"),
                                    body.get("mobileNumber"),
                                    body.get("DOB")
                                );
                            }
                            result.complete(true);
                            return;
                    }
                    result.complete(false);
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    result.complete(false);
                }
            }
        );
        return result;
    }

    @Override
    public CompletableFuture<Boolean> getPic() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.getPic(authService.getAuthToken()).enqueue(
            new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        InputStream is = response.body().byteStream();

                        ProfileServiceImpl.this.profilePic = new ProfilePic(
                            BitmapFactory.decodeStream(is));

                        logger.debug("getPic: Picture Response successful");
                        result.complete(true);
                        return;
                    }
                    result.complete(false);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    result.complete(false);
                }
            });
        return result;
    }

    @Override
    public CompletableFuture<Boolean> updatePic(File file) {
        // Create multipart form-data body
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part
            .createFormData("picture", file.getName(), reqFile);

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaApi.updatePic(authService.getAuthToken(),body).enqueue(
            new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.isSuccessful()) {
                        logger.debug("updatePic: Picture Updated");
                        result.complete(true);
                        return;
                    }
                    logger.debug("updatePic: Not Successful");
                    result.complete(false);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    logger.debug("updatePic: Failed");
                    result.complete(false);
                }
            }
        );
        return result;
    }


    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public ProfilePic getProfilePic() {
        return profilePic;
    }

}

interface ProfileApi {
    @PATCH("/me/profile")
    Call<Map<String, String>> update(@Header("Authorization") String authToken,
                                     @Body ProfileDto info);

    @FormUrlEncoded
    @PATCH("/me/profile")
    Call<Map<String,String>> updatePassword(@Header("Authorization") String authToken,
                                            @Field("password") String password);


    @GET("/me/profile/picture")
    Call<ResponseBody> getPic(@Header("Authorization") String authToken);


    @Multipart
    @POST("/me/profile/picture")
    Call<ResponseBody> updatePic(@Header("Authorization") String authToken,
                                 @Part MultipartBody.Part image);

    @GET("/me/profile")
    Call<Map<String,String>> getDetails(@Header("Authorization") String authToken);
}
