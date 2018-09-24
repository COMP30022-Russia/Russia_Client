package com.comp30022.team_russia.assist.features.message.services;

import android.util.Log;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.message.models.Message;

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

/**
 * Default implementation of {@link ChatService}.
 */
public class ChatServiceImpl implements ChatService {

    private AuthService authService;
    private RussiaMessagingApi messagingApi;

    @Inject
    public ChatServiceImpl(AuthService authService, Retrofit retrofit) {
        this.authService = authService;
        messagingApi = retrofit.create(RussiaMessagingApi.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<ActionResult<Void>> sendChatMessage(int associationId,
                                                                 String msg) {
        if (!authService.isLoggedInUnboxed()) {
            Log.e("ChatService", "Not authenticated");
            return CompletableFuture.completedFuture(ActionResult
                .failedNotAutenticated());
        }

        if (msg == null) {
            return CompletableFuture.completedFuture(ActionResult
                .failedCustomMessage("Message cannot be null."));
        }
        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        messagingApi.sendChatMessage(authService.getAuthToken(),
            associationId, msg).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        result.complete(new ActionResult<Void>(null));
                    } else {
                        result.complete(ActionResult
                            .failedCustomMessage("Error in response: "
                                + response.raw().toString()));
                    }
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    result.complete(ActionResult.failedNetworkError());
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
            return CompletableFuture.completedFuture(ActionResult.failedNotAutenticated());
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
            return CompletableFuture.completedFuture(ActionResult.failedNotAutenticated());
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
            return CompletableFuture.completedFuture(ActionResult.failedNotAutenticated());
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
            return CompletableFuture.completedFuture(ActionResult.failedNotAutenticated());
        }

        CompletableFuture<ActionResult<List<Message>>> result = new CompletableFuture<>();

        call.enqueue(new Callback<ChatHistoryDto>() {
                @Override
                public void onResponse(Call<ChatHistoryDto> call,
                                       Response<ChatHistoryDto> response) {
                    if (response.isSuccessful()) {
                        if (response.body().messages != null
                            && !response.body().messages.isEmpty()) {
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
                public void onFailure(Call<ChatHistoryDto> call, Throwable t) {
                    result.complete(ActionResult.failedNetworkError());
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
}

class ChatHistoryDto {
    List<Message> messages;

    ChatHistoryDto(List<Message> messages) {
        this.messages = messages;
    }

}

