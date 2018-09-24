package com.comp30022.team_russia.assist.base.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.comp30022.team_russia.assist.features.message.db.MessageDao;
import com.comp30022.team_russia.assist.features.message.models.Association;
import com.comp30022.team_russia.assist.features.message.models.Message;

/**
 * Room database declaration class.
 */
@Database(entities = {
        Association.class,
        Message.class
    },
    version = 1,
    exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class RussiaDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();
}
