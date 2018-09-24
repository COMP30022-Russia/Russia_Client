package com.comp30022.team_russia.assist.features.message.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.comp30022.team_russia.assist.features.message.models.Message;

import java.util.List;

/**
 * Data access object for {@link Message}.
 */
@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Message message);

    @Query("DELETE FROM message_table")
    void clear();

    @Query("SELECT * from message_table WHERE associationId = :associationId "
           + "ORDER BY id ASC")
    LiveData<List<Message>> getAllMessages(int associationId);

    @Query("SELECT * from message_table WHERE associationId = :associationId "
           + "ORDER BY id DESC LIMIT 1")
    LiveData<Message> getLastMessage(int associationId);

    @Query("SELECT id from message_table WHERE associationId = :associationId "
           + "ORDER BY id DESC LIMIT 1")
    int getLastMessageId(int associationId);
}
