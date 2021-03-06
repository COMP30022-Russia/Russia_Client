package com.comp30022.team_russia.assist.base.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.comp30022.team_russia.assist.features.assoc.db.UserDao;
import com.comp30022.team_russia.assist.features.chat.db.MessageDao;
import com.comp30022.team_russia.assist.features.chat.models.Association;
import com.comp30022.team_russia.assist.features.chat.models.Message;
import com.comp30022.team_russia.assist.features.chat.models.ReadPointer;
import com.comp30022.team_russia.assist.features.chat.models.UserContact;
import com.comp30022.team_russia.assist.features.media.db.MediaFileDao;
import com.comp30022.team_russia.assist.features.media.models.MediaFileInfo;

/**
 * Room database declaration class.
 */
@Database(entities = {
        Association.class,
        Message.class,
        UserContact.class,
        ReadPointer.class,
        MediaFileInfo.class
    },
    version = 5,
    exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class RussiaDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();

    public abstract UserDao userDao();

    public abstract MediaFileDao mediaFileDao();
}
