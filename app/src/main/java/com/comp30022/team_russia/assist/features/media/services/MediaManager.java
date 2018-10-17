package com.comp30022.team_russia.assist.features.media.services;

import android.arch.lifecycle.LiveData;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.media.models.MediaFileInfo;
import com.comp30022.team_russia.assist.features.media.models.ProgressDto;
import com.comp30022.team_russia.assist.features.media.types.MediaSubManager;

import java9.util.concurrent.CompletableFuture;

/**
 * MediaManager.
 */
public interface MediaManager {

    public static final String TYPE_PROFILE  = "profile";
    public static final String TYPE_CHAT     = "picture_in_chat";

    void registerMediaType(String type, MediaSubManager typeDelegate);

    /**
     * Creates the Metadata for a media. Making it trackable by {@link MediaManager}.
     * @param type The type of the media.
     * @param metadata Additional information for the {@link MediaSubManager} to process.
     */
    CompletableFuture<MediaFileInfo> createMedia(String type, Object metadata);

    void updateMetaData(MediaFileInfo newFileInfo);

    LiveData<Boolean> isAvailableRemoteLiveData(int mediaId);

    LiveData<Boolean> isAvailableRemoteLiveData(String type, Object identifier);

    /**
     * Returns a {@link LiveData} object that can be observed to obtain the latest local
     * URI of a media file.
     * @param mediaId The ID of the media.
     * @return LiveData containing the URI of the local file / cache of the specified media.
     */
    LiveData<String> getMediaLocalUriLiveData(int mediaId);

    LiveData<String> getMediaLocalUriLiveData(String type, Object identifier);

    /**
     * Forces re-download of the media with the specified mediaId.
     * This is useful for profile image updates.
     * @param mediaId The ID of the media to re-download.
     */
    void forceDownload(int mediaId);

    CompletableFuture<ActionResult<MediaFileInfo>> query(String type, Object identifies);

    LiveData<Boolean> isBusy();

    LiveData<ProgressDto> getProgress();
}
