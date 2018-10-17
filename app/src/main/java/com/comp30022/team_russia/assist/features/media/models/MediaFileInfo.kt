package com.comp30022.team_russia.assist.features.media.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

/**
 * Represents a local DB item representing a media file.
 */
@Entity(tableName = "media_files")
data class MediaFileInfo(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id: Int?,

    @NonNull
    /**
     * The type of the media.
     * Can be, e.g., profile image, chat image.
     */
    var type: String,

    @ColumnInfo(name = "remote")
    @NonNull
    var isAvailableRemote: Boolean = false,
    @NonNull
    @ColumnInfo(name = "local")
    var isAvailableLocally: Boolean = false,

    @ColumnInfo(name = "cache_path")
    var localUri: String?,

    // The followings are additional data for each MediaTypeDelegate
    @ColumnInfo(name = "profile_id")
    var profileImageId: Int?,
    var profileImagePendingUploadUri: String?,
    var chatImageId: Int?,
    var chatId: Int?
)