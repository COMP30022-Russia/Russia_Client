package com.comp30022.team_russia.assist.features.media.types;

import android.arch.persistence.db.SupportSQLiteQuery;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.media.models.MediaFileInfo;

import java9.util.concurrent.CompletableFuture;

/**
 * A manager for a particular class of media (e.g. Profile pictures; chat pictures).
 */
public interface MediaSubManager {

    /**
     * Call when a new "managed" media file is requested.
     * @param payload Additional metadata about the media.
     * @param mediaFileInfo The initial {@link MediaFileInfo} that should be modified.
     * @return The modified {@link MediaFileInfo}.
     */
    MediaFileInfo onCreateMedia(Object payload, MediaFileInfo mediaFileInfo);

    /**
     * Called when {@link MediaManager} requests that a media file be downloaded.
     * @param fileInfo file info
     * @return success / failure
     */
    CompletableFuture<ActionResult<Void>> onUploadMedia(MediaFileInfo fileInfo);

    CompletableFuture<ActionResult<String>> onDownloadMedia(MediaFileInfo fileInfo);

    /**
     * Generates a query.
     * @param identifiers identifier
     * @return SupportSQLiteQuery
     */
    SupportSQLiteQuery getQueryForMedia(Object identifiers);
}
