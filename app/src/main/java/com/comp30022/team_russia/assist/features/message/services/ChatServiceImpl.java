package com.comp30022.team_russia.assist.features.message.services;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.message.models.Message;
import com.comp30022.team_russia.assist.features.message.models.Picture;
import com.comp30022.team_russia.assist.features.message.models.PictureDto;

import java.io.File;
import java.io.InputStream;
import java.util.List;

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
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Default implementation of {@link ChatService}.
 */
public class ChatServiceImpl implements ChatService {

    private final LoggerInterface logger;

    private final AuthService authService;

    private final RussiaMessagingApi messagingApi;

    /**
     * Constructor.
     * @param authService The {@link AuthService} instance, injected by DI.
     * @param retrofit The {@link Retrofit} instance, injected by DI.
     * @param loggerFactory The {@link LoggerFactory} instance, injected by DI.
     */
    @Inject
    public ChatServiceImpl(AuthService authService,
                           Retrofit retrofit,
                           LoggerFactory loggerFactory) {

        this.authService = authService;
        messagingApi = retrofit.create(RussiaMessagingApi.class);
        this.logger = loggerFactory.getLoggerForClass(this.getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<ActionResult<Void>> sendChatMessage(int associationId,
                                                                 String msg) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("ChatService", "Not authenticated");
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        if (msg == null) {
            return CompletableFuture.completedFuture(ActionResult
                .failedCustomMessage("Message cannot be null."));
        }
        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        messagingApi.sendChatMessage(authService.getAuthToken(),
            associationId, msg).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<>(null));
                    } else {
                        result.complete(ActionResult
                            .failedCustomMessage("Error in response: "
                                                 + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<ActionResult<List<Message>>> getHistory(int associationId, int limit,
                                                                     int beforeId, int afterId) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("ChatService", "Not authenticated");
            // @todo: better error handling
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }
        return getHistoryHelper(messagingApi.getChatMessages(
            authService.getAuthToken(), associationId, limit, beforeId, afterId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<ActionResult<List<Message>>> getHistory(int associationId, int limit) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("ChatService", "Not authenticated");
            // @todo: better error handling
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }
        return getHistoryHelper(messagingApi.getChatMessages(
            authService.getAuthToken(), associationId, limit));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<ActionResult<List<Message>>> getHistory(int associationId) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("ChatService", "Not authenticated");
            // @todo: better error handling
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }
        return getHistoryHelper(messagingApi.getChatMessages(
            authService.getAuthToken(), associationId));
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<ActionResult<List<Message>>> getHistoryHelper(
        Call<ChatHistoryDto> call) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("ChatService", "Not authenticated");
            // @todo: better error handling
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<List<Message>>> result = new CompletableFuture<>();

        call.enqueue(new Callback<ChatHistoryDto>() {
            @Override
            public void onResponse(@NonNull Call<ChatHistoryDto> call,
                                   @NonNull Response<ChatHistoryDto> response) {
                if (response.isSuccessful()) {
                    if (response.body().messages != null
                        && !response.body().messages.isEmpty()) {

                        List<Message> messages = response.body().messages;

                        for (Message message : messages) {
                            if (message.getType().equals("Picture")) {
                                if (! message.getPictures().isEmpty()) {
                                    message.setPictureId(message.getPictures()
                                        .get(0).getPictureId());
                                } else {
                                    Log.e("ChatService", "Could not load image in chat");
                                }
                            }
                        }

                        result.complete(new ActionResult<>(response.body().messages));
                        return;
                    }
                }
                Log.e("ChatService", "Error during request");
                result.complete(
                    ActionResult
                        .failedCustomMessage("Error response: " + response.raw().toString()));
            }

            @Override
            public void onFailure(@NonNull Call<ChatHistoryDto> call, @NonNull Throwable t) {
                result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
            }
        });
        return result;
    }

    /**
     * Get picture place holder from server.
     * @param associationId association id
     * @param imageCount number of images to be sent
     * @return List of PictureDto
     */
    @Override
    public CompletableFuture<ActionResult<List<PictureDto>>> getPictureIdPlaceholder(
        int associationId, int imageCount) {

        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<List<PictureDto>>> result = new CompletableFuture<>();

        messagingApi.getPictureId(authService.getAuthToken(), associationId, imageCount)
            .enqueue(new Callback<Message>() {
                @Override
                public void onResponse(@NonNull Call<Message> call,
                                       @NonNull Response<Message> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        result.complete(new ActionResult<>(response.body().getPictures()));

                        logger.info("sendImages: successfully got pictureIds");

                    } else {
                        logger.error("sendImages: FAILED to get pictureIds");
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Message> call,
                                      @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));

                }
            });

        return result;
    }


    /**
     * Send images one by one.
     * @param associationId association id
     * @param pictureId picture id of image to send
     * @param file file image
     * @return success/failure
     */
    @Override
    public CompletableFuture<ActionResult<Void>> sendImage(
        int associationId, int pictureId, File file) {

        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();

        // Create multipart form-data body
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imageBody = MultipartBody.Part
            .createFormData("picture", file.getName(), reqFile);

        messagingApi.sendImage(authService.getAuthToken(), associationId, pictureId,
            imageBody).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {

                        result.complete(new ActionResult<>(ActionResult.NO_ERROR));
                        logger.info("sendImages: successfully sent pictureId "
                                    + pictureId);

                    } else {

                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                        logger.error("sendImages: FAILED to send pictureId "
                                     + pictureId);
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
     * Get images from server.
     * @param associationId association id
     * @param pictureId picture id
     * @return the Picture object (image)
     */
    @Override
    public CompletableFuture<ActionResult<Picture>> getImage(int associationId, int pictureId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<Picture>> result = new CompletableFuture<>();

        messagingApi.getImage(authService.getAuthToken(), associationId, pictureId).enqueue(
            new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {

                        InputStream inputStream = response.body().byteStream();

                        result.complete(new ActionResult<>(
                            new Picture(BitmapFactory.decodeStream(inputStream))));

                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));

                }
            });

        return result;
    }

    /**
     * TODO NOT YET IMPLEMENTED.
     * Get all the Pictures from server
     * @param associationId association id
     * @return list of Pictures
     */
    @Override
    public CompletableFuture<ActionResult<List<Picture>>> getAllImages(int associationId) {
        if (!authService.isLoggedInUnboxed()) {
            return CompletableFuture.completedFuture(
                new ActionResult<>(ActionResult.NOT_AUTHENTICATED));
        }

        CompletableFuture<ActionResult<List<Picture>>> result = new CompletableFuture<>();

        messagingApi.getAllImages(authService.getAuthToken(), associationId).enqueue(
            new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {

                        InputStream inputStream = response.body().byteStream();
                        //todo NO IDEA TO HOW TAKE INPUT STREAM OF LIST OF IMAGES
                        //todo its a list of images here
                        //todo convert inputstream to image or any other representation
                        //result.complete(new ActionResult<>(image));

                    } else {
                        result.complete(new ActionResult<>(ActionResult.CUSTOM_ERROR,
                            "Error in response: " + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));

                }
            });

        return result;
    }

}

interface RussiaMessagingApi {
    @FormUrlEncoded
    @POST("associations/{id}/chat")
    Call<Void> sendChatMessage(
        @Header("Authorization") String authToken,
        @Path("id") int associationId,
        @Field("content") String message);

    @GET("associations/{id}/chat")
    Call<ChatHistoryDto> getChatMessages(
        @Header("Authorization") String authToken,
        @Path("id") int associationId,
        @Query("limit") int limit,
        @Query("before") int beforeId,
        @Query("after") int afterId);

    @GET("associations/{id}/chat")
    Call<ChatHistoryDto> getChatMessages(
        @Header("Authorization") String authToken,
        @Path("id") int associationId,
        @Query("limit") int limit);

    @GET("associations/{id}/chat")
    Call<ChatHistoryDto> getChatMessages(
        @Header("Authorization") String authToken,
        @Path("id") int associationId);

    @FormUrlEncoded
    @POST("associations/{id}/chat/picture")
    Call<Message> getPictureId(
        @Header("Authorization") String authToken,
        @Path("id") int associationId,
        @Field("count") int count);

    @Multipart
    @POST("associations/{assocId}/chat/picture/{pictureId}")
    Call<Void> sendImage(
        @Header("Authorization") String authToken,
        @Path("assocId") int associationId,
        @Path("pictureId") int pictureId,
        @Part MultipartBody.Part image);

    @GET("associations/{assocId}/chat/picture/{pictureId}")
    Call<ResponseBody> getImage(
        @Header("Authorization") String authToken,
        @Path("assocId") int associationId,
        @Path("pictureId") int pictureId);

    @GET("associations/{assocId}/chat")
    Call<ResponseBody> getAllImages(
        @Header("Authorization") String authToken,
        @Path("id") int associationId);
}

class ChatHistoryDto {
    List<Message> messages;

    ChatHistoryDto(List<Message> messages) {
        this.messages = messages;
    }
}
