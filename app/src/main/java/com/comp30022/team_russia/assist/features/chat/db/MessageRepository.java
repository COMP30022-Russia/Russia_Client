package com.comp30022.team_russia.assist.features.chat.db;

import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.features.chat.models.Message;

import java.util.List;

/**
 * Repository for {@link Message}.
 */
public interface MessageRepository {
    LiveData<List<Message>> getMessages(int associationId);

    void syncMessages(int associationId);

    void updateReadPointer(int associationId, int newPointer);
}
