package com.comp30022.team_russia.assist.features.chat.db;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.comp30022.team_russia.assist.base.db.RussiaDatabase;
import com.comp30022.team_russia.assist.features.chat.models.Message;
import com.comp30022.team_russia.assist.features.chat.models.ReadPointer;
import com.comp30022.team_russia.assist.features.chat.services.ChatService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Implementation of {@link MessageRepository}.
 */
public class MessageRepositoryImpl implements MessageRepository {

    private final RussiaDatabase db;
    private final MessageDao messageDao;
    private final ChatService chatService;

    /**
     * Message Repository Implementation Constructor.
     * @param db Database that stores the messages.
     * @param chatService Chat service used by the Message Repository.
     */
    @Inject
    public MessageRepositoryImpl(RussiaDatabase db, ChatService chatService) {
        this.db = db;
        this.chatService = chatService;
        messageDao = this.db.messageDao();
    }

    /**
     * Retrieve messages from the local database.
     * @param associationId associationID to retrieve.
     * @return List of messages with the specified associationID.
     */
    public LiveData<List<Message>> getMessagesFromLocalDb(int associationId) {
        return messageDao.getAllMessages(associationId);
    }

    /**
     * Retrieve messages from the server.
     * @param associationId associationID to retrieve.
     * @return List of messages with the specified associationID.
     */
    public LiveData<List<Message>> getMessages(int associationId) {
        // get local data first
        LiveData localSource = messageDao.getAllMessages(associationId);
        // also send a request to API in the background
        syncMessages(associationId);
        return localSource;
    }

    @Override
    public void syncMessages(int associationId) {
        chatService.getHistory(associationId).thenAcceptAsync(result -> {
            if (result.isSuccessful()) {
                // check last
                List<Message> messages = result.unwrap();
                if (!messages.isEmpty()) {
                    // compare the last message ID with the last message saved
                    Collections.sort(messages, (a,b) -> a.getId() - b.getId());
                    Message lastMessage = messages.get(messages.size() - 1);
                    int lastSaveMessageIdInt = messageDao.getLastMessageId(associationId);

                    if (lastSaveMessageIdInt < lastMessage.getId()) {
                        // we need to insert all new (not-yet-saved) messages to local cache
                        for (Message message: messages) {
                            if (message.getId() > lastSaveMessageIdInt) {
                                new InsertAsyncTask(messageDao).execute(message);
                            }
                        }
                        if (messages.get(0).getId() > lastSaveMessageIdInt) {
                            int distance = messages.get(0).getId() - lastSaveMessageIdInt;
                            // not enough, there are more unseen messages
                            chatService.getHistory(associationId, distance + 1,
                                messages.get(0).getId(), lastSaveMessageIdInt)
                                .thenAccept(result2 -> {
                                    if (result2.isSuccessful()) {
                                        List<Message> messages2 = result2.unwrap();
                                        if (!messages2.isEmpty()) {
                                            for (Message message: messages2) {
                                                new InsertAsyncTask(messageDao).execute(message);
                                            }
                                        }
                                    }
                                });
                            // @todo: repeatedly retrieve (in batches) unseen messages,
                            // until all is saved
                            // @todo: we need a more elegant way to avoid callback hell.
                        }

                    }
                }

            }
            // @todo: retries when network error
        });
    }

    @Override
    public void updateReadPointer(int associationId, int newPointer) {
        new UpdateReadPointerAsyncTask(messageDao)
            .execute(new ReadPointer(associationId, newPointer));
    }

    private static class InsertAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao asyncTaskDao;

        InsertAsyncTask(MessageDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Message... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateReadPointerAsyncTask extends AsyncTask<ReadPointer, Void, Void> {

        private final MessageDao dao;

        UpdateReadPointerAsyncTask(MessageDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(ReadPointer... readPointers) {
            ReadPointer readPointer = readPointers[0];
            int lastReadId = dao.getLastReadId(readPointer.getId());
            if (readPointer.getLastReadId() > lastReadId) {
                dao.updateLastReadId(readPointer);
            }
            return null;
        }
    }
}



