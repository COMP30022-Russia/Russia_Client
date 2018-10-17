package com.comp30022.team_russia.assist.features.media.types;

import android.arch.persistence.db.SupportSQLiteQuery;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.features.media.models.MediaFileInfo;

import java9.util.concurrent.CompletableFuture;


/**
 * ChatPicturesTypeDelegate.
 */
public class ChatPicturesTypeDelegate implements MediaSubManager {

    @Override
    public MediaFileInfo onCreateMedia(Object payload, MediaFileInfo mediaFileInfo) {
        return null;
    }

    @Override
    public CompletableFuture<ActionResult<Void>> onUploadMedia(MediaFileInfo fileInfo) {
        return null;
    }

    @Override
    public CompletableFuture<ActionResult<String>> onDownloadMedia(MediaFileInfo fileInfo) {
        return null;
    }

    @Override
    public SupportSQLiteQuery getQueryForMedia(Object identifiers) {
        return null;
    }
}
