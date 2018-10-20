package com.comp30022.team_russia.assist.features.profile.services;

import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.Carer;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.media.models.MediaFileInfo;
import com.comp30022.team_russia.assist.features.media.services.MediaManager;
import com.comp30022.team_russia.assist.features.profile.models.ProfileDto;
import com.comp30022.team_russia.assist.features.profile.models.ProfilePictureCreationArgs;
import com.comp30022.team_russia.assist.features.profile.models.ProfilePictureQueryArgs;

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
import retrofit2.http.PATCH;

/**
 * Default implementation of {@link ProfileDetailsService}.
 */

public class ProfileDetailsServiceImpl implements ProfileDetailsService {

    private final LoggerInterface logger;
    private final RussiaProfileDetailsApi russiaProfileDetailsApi;
    private final AuthService authService;
    private User currentUser;
    private final MediaManager mediaManager;

    /**
     * Constructor.
     */
    @Inject
    public ProfileDetailsServiceImpl(AuthService authService,
                                     MediaManager mediaManager,
                                     Retrofit retrofit,
                                     LoggerFactory loggerFactory) {
        this.authService = authService;
        russiaProfileDetailsApi = retrofit.create(RussiaProfileDetailsApi.class);
        this.logger = loggerFactory.create(this.getClass().getSimpleName());
        this.mediaManager = mediaManager;
    }

    @Override
    public CompletableFuture<Boolean> update(ProfileDto updateInfo) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        russiaProfileDetailsApi.update(authService.getAuthToken(),updateInfo).enqueue(

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
        russiaProfileDetailsApi.updatePassword(authService.getAuthToken(),password).enqueue(
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
        russiaProfileDetailsApi.getDetails(authService.getAuthToken()).enqueue(
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

                                ProfileDetailsServiceImpl.this.currentUser = new AssistedPerson(
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

                                ProfileDetailsServiceImpl.this.currentUser = new Carer(
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
        int userId = authService.getCurrentUser().getUserId();
        return ensureMetadataExists(userId, true);
    }

    @Override
    public CompletableFuture<Boolean> updatePic(String filePath) {
        int userId = authService.getCurrentUser().getUserId();
        CompletableFuture<MediaFileInfo> future = new CompletableFuture<>();
        mediaManager.query(MediaManager.TYPE_PROFILE, new ProfilePictureQueryArgs(userId))
            .thenAcceptAsync(result -> {
                if (result.isSuccessful()) {
                    // already a metadata entry
                    MediaFileInfo fileInfo =  result.unwrap();
                    fileInfo.setAvailableRemote(false);
                    fileInfo.setProfileImagePendingUploadUri(filePath);
                    mediaManager.updateMetaData(fileInfo);
                    future.complete(fileInfo);
                } else {
                    // no metadata
                    mediaManager.createMedia(MediaManager.TYPE_PROFILE,
                        new ProfilePictureCreationArgs(authService.getCurrentUser().getUserId(),
                            filePath))
                        .thenAcceptAsync((x) -> future.complete(x));
                }
            });

        return future.thenApplyAsync((mediaFileInfo -> {
            if (mediaFileInfo == null) {
                return false;
            }
            mediaManager.forceDownload(mediaFileInfo.getId());
            return true;
        }));
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public LiveData<String> getProfilePicPath() {
        return mediaManager.getMediaLocalUriLiveData(MediaManager.TYPE_PROFILE,
            new ProfilePictureQueryArgs(currentUser.getUserId()));
    }

    /**
     * Get a specified user's profile picture.
     * @param userId user id of user to get profile picture of
     * @return ProfilePic object of specified user
     */
    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<ActionResult<LiveData<String>>> getUsersProfilePicture(int userId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }
        return ensureMetadataExists(userId, false).thenApplyAsync((ok) -> {
            if (ok  != null && ok) {
                return new ActionResult(mediaManager.getMediaLocalUriLiveData(
                    MediaManager.TYPE_PROFILE,
                    new ProfilePictureQueryArgs(userId)));

            } else {
                return ActionResult.failedCustomMessage("something went wrong");
            }
        });
    }

    private CompletableFuture<Boolean> ensureMetadataExists(int userId,
                                                            boolean shouldForceDownload) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        mediaManager.query(MediaManager.TYPE_PROFILE, new ProfilePictureQueryArgs(userId))
            .thenAcceptAsync(result -> {
                if (result.isSuccessful()) {
                    MediaFileInfo fileInfo = result.unwrap();

                    if (!fileInfo.isAvailableLocally() || shouldForceDownload) {
                        mediaManager.forceDownload(fileInfo.getId());
                    }

                    future.complete(true);

                } else {
                    // no metadata
                    mediaManager.createMedia(MediaManager.TYPE_PROFILE,
                        new ProfilePictureCreationArgs(
                            userId,
                            null))
                        .thenAcceptAsync((x) -> {
                            if (x == null) {
                                future.complete(false);
                                return;
                            }
                            mediaManager.forceDownload(x.getId());
                            future.complete(true);
                        });
                }
            });

        return future;
    }
}

interface RussiaProfileDetailsApi {
    @PATCH("/me/profile")
    Call<Map<String, String>> update(@Header("Authorization") String authToken,
                                     @Body ProfileDto info);

    @FormUrlEncoded
    @PATCH("/me/profile")
    Call<Map<String,String>> updatePassword(@Header("Authorization") String authToken,
                                            @Field("password") String password);

    @GET("/me/profile")
    Call<Map<String,String>> getDetails(@Header("Authorization") String authToken);
}
