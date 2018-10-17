package com.comp30022.team_russia.assist.features.media.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.comp30022.team_russia.assist.features.media.models.MediaFileInfo;

import java.util.List;

/**
 * MediaFileDao.
 */
@Dao
public interface MediaFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdate(MediaFileInfo mediaFileInfo);

    @Query("SELECT * FROM media_files WHERE id = :mediaId LIMIT 1")
    MediaFileInfo getById(int mediaId);

    @RawQuery(observedEntities = MediaFileInfo.class)
    MediaFileInfo getByRawQuery(SupportSQLiteQuery query);

    @Query("SELECT remote "
           + "FROM media_files "
           + "WHERE id = :mediaId "
           + "LIMIT 1")
    LiveData<Boolean> getIsAvailableRemote(int mediaId);

    @Query("SELECT local "
           + "FROM media_files "
           + "WHERE id = :mediaId "
           + "LIMIT 1")
    LiveData<Boolean> getIsAvailableLocal(int mediaId);


    @Query("SELECT cache_path "
           + "FROM media_files "
           + "WHERE id = :mediaId "
           + "  AND local = 1 "
           + "LIMIT 1")
    LiveData<String> getLocalUri(int mediaId);

    @Query("UPDATE media_files "
           + "SET remote = 1 "
           + "WHERE id = :mediaId")
    void setRemoteAvailable(int mediaId);

    @Query("UPDATE media_files "
           + "SET local = 1,"
           + "    cache_path = :localUri "
           + "WHERE id = :mediaId")
    void setLocalUri(int mediaId, String localUri);


    @Query("SELECT * FROM media_files WHERE remote = 0")
    List<MediaFileInfo> getPendingUploads();

    @Query("DELETE FROM media_files")
    void clear();
}
