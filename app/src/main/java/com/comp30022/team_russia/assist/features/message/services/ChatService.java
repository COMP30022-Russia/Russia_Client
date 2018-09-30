package com.comp30022.team_russia.assist.features.message.services;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.message.models.Message;

import java.util.List;

import java9.util.concurrent.CompletableFuture;

/**
 * Chat service interface. This is a stateless service.
 */
public interface ChatService {

    /**
     * Send a chat message.
     * @param associationId The ID of the association between the current user and the target user.
     * @param msg The message.
     * @return Whether the operation is successful.
     */
    CompletableFuture<ActionResult<Void>> sendChatMessage(int associationId, String msg);


    /**
     * Gets the chat history in a given time range.
     *
     * @param associationId The association ID, used to identify conversation.
     * @param limit         The maximum number of chat messages to return.
     * @param beforeId      The start message Id of the history to query for.
     * @param afterId       The end message Id of the history to query for.
     * @return A list of chat messages.
     */
    CompletableFuture<ActionResult<List<Message>>> getHistory(int associationId, int limit,
                                                              int beforeId, int afterId);

    CompletableFuture<ActionResult<List<Message>>> getHistory(int associationId, int limit);

    CompletableFuture<ActionResult<List<Message>>> getHistory(int associationId);
}
