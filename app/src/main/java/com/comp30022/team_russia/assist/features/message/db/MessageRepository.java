package com.comp30022.team_russia.assist.features.message.db;

import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.features.message.models.Message;

import java.util.List;

public interface MessageRepository {
    LiveData<List<Message>> getMessages(int associationId);
}
