package com.comp30022.team_russia.assist.features.media.services;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.os.Handler;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.db.RussiaDatabase;
import com.comp30022.team_russia.assist.features.media.db.MediaFileDao;
import com.comp30022.team_russia.assist.features.media.models.MediaFileInfo;
import com.comp30022.team_russia.assist.features.media.models.ProgressDto;
import com.comp30022.team_russia.assist.features.media.types.MediaSubManager;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import java9.util.concurrent.CompletableFuture;

import javax.inject.Inject;

/**
 * Implementation of {@link MediaManager}.
 */
public class MediaManagerImpl implements MediaManager {

    /**
     * Check for pending uploads every minute.
     */
    private static final int UPLOAD_QUEUE_PERIOD = 60 * 1000;

    private final MediaFileDao mediaFileDao;

    private final LoggerInterface logger;

    private final ConcurrentHashMap<String, MediaSubManager> typeDelegates
        = new ConcurrentHashMap<>();


    private final Handler uploadQueueHandler = new Handler();

    @SuppressWarnings("Convert2MethodRef")
    private final Runnable uploadQueueRunnable = () -> processUploadQueue();

    //CHECKSTYLE.OFF: ALL
    private final MutableLiveData<Boolean> _isBusy = new MutableLiveData<>();
    //CHECKSTYLE.ON: ALL


    @Inject
    public MediaManagerImpl(RussiaDatabase db, LoggerFactory loggerFactory) {
        this.mediaFileDao = db.mediaFileDao();
        this.logger = loggerFactory.getLoggerForClass(this.getClass());
        _isBusy.postValue(false);
        uploadQueueHandler.post(uploadQueueRunnable);
    }

    @Override
    public void registerMediaType(String type, MediaSubManager typeDelegate) {
        if (typeDelegate == null) {
            logger.error("null typeDelegate");
            return;
        }
        logger.debug("registering delegate for type " + type);
        this.typeDelegates.put(type, typeDelegate);
    }

    @Override
    public CompletableFuture<MediaFileInfo> createMedia(String type, Object metadata) {
        CompletableFuture<MediaFileInfo> result = new CompletableFuture<>();
        AsyncTask.execute(() -> {
            long id = mediaFileDao.insertOrUpdate(new MediaFileInfo(
                null,
                type,
                false,
                false,
                null,
                null,
                null,
                null,
                null));
            MediaFileInfo initialInfo = mediaFileDao.getById((int)id);
            logger.info("Created empty MediaFileInfo id=" + initialInfo.getId() + " "
                        + initialInfo.toString());
            MediaFileInfo modifiedInfo = getDelegate(type).onCreateMedia(metadata, initialInfo);
            mediaFileDao.insertOrUpdate(modifiedInfo);
            logger.info("Updated MediaFileInfo id=" + modifiedInfo.getId() + " "
                        + modifiedInfo.toString());
            if (modifiedInfo.isAvailableRemote() && !modifiedInfo.isAvailableLocally()) {
                AsyncTask.execute(() -> downloadMedia(modifiedInfo));
            }
            uploadQueueHandler.removeCallbacks(uploadQueueRunnable);
            uploadQueueHandler.post(uploadQueueRunnable);
            result.complete(modifiedInfo);
        });
        return result;
    }

    @Override
    public void updateMetaData(MediaFileInfo newFileInfo) {
        if (newFileInfo == null || newFileInfo.getId() == null) {
            logger.warn("updateMetaData: null input");
            return;
        }
        AsyncTask.execute(() -> {
            mediaFileDao.insertOrUpdate(newFileInfo);
            logger.debug("updateMetaData: " + newFileInfo.toString() + " updated");
            if (newFileInfo.isAvailableRemote() && !newFileInfo.isAvailableLocally()) {
                AsyncTask.execute(() -> downloadMedia(newFileInfo));
            }
            uploadQueueHandler.removeCallbacks(uploadQueueRunnable);
            uploadQueueHandler.post(uploadQueueRunnable);
        });
    }

    @Override
    public LiveData<Boolean> isAvailableRemoteLiveData(int mediaId) {
        return mediaFileDao.getIsAvailableRemote(mediaId);
    }

    @Override
    public LiveData<Boolean> isAvailableRemoteLiveData(String type, Object identifier) {
        MediatorLiveData<Boolean> tmp = new MediatorLiveData<>();
        tmp.postValue(false);
        query(type, identifier).thenAcceptAsync((result) -> {
            if (result.isSuccessful()) {
                MediaFileInfo fileInfo = result.unwrap();
                tmp.addSource(mediaFileDao.getIsAvailableRemote(fileInfo.getId()), tmp::postValue);
            } else {
                // metadata non-exist, we need to create
                logger.warn("isAvailableRemoteLiveData: Non existent media");
            }
        });
        return tmp;
    }

    @Override
    public LiveData<String> getMediaLocalUriLiveData(int mediaId) {
        // @todo: schedule download;
        return mediaFileDao.getLocalUri(mediaId);
    }

    @Override
    public LiveData<String> getMediaLocalUriLiveData(String type, Object identifier) {
        MediatorLiveData<String> tmp = new MediatorLiveData<>();
        tmp.postValue(null);
        query(type, identifier).thenAcceptAsync((result) -> {
            if (result.isSuccessful()) {
                MediaFileInfo fileInfo = result.unwrap();
                tmp.addSource(mediaFileDao.getLocalUri(fileInfo.getId()), tmp::postValue);
            } else {
                // metadata non-exist, we need to create
                logger.warn("getMediaLocalUriLiveData: Non existent media");
            }
        });
        return tmp;
    }

    @Override
    public void forceDownload(int mediaId) {
        AsyncTask.execute(() -> {
            MediaFileInfo fileInfo = mediaFileDao.getById(mediaId);
            if (fileInfo == null) {
                logger.warn("Cannot find MediaFileInfo with id=" + mediaId);
                return;
            }
            downloadMedia(fileInfo);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<ActionResult<MediaFileInfo>> query(String type, Object identifies) {

        CompletableFuture<ActionResult<MediaFileInfo>> result =
            new CompletableFuture<>();

        AsyncTask.execute(() -> {
            MediaFileInfo fileInfo =
                mediaFileDao.getByRawQuery(getDelegate(type).getQueryForMedia(identifies));
            if (fileInfo == null) {
                result.complete(ActionResult.failedCustomMessage("Not found"));
            }
            result.complete(new ActionResult<>(fileInfo));
        });
        return result;
    }

    @Override
    public LiveData<Boolean> isBusy() {
        return _isBusy;
    }

    @Override
    public LiveData<ProgressDto> getProgress() {
        return null;
    }

    private MediaSubManager getDelegate(String type) {
        MediaSubManager delegate = typeDelegates.get(type);
        if (delegate == null) {
            logger.warn("Delegate for type " + type + " does not exist!");
        }
        return delegate;
    }

    private void downloadMedia(MediaFileInfo fileInfo) {
        if (fileInfo == null) {
            return;
        }
        int mediaId = fileInfo.getId();
        getDelegate(fileInfo.getType()).onDownloadMedia(fileInfo)
            .thenAcceptAsync((result) -> {
                // @todo: replace with StringBuilder
                if (result != null && result.isSuccessful()) {
                    logger.info("downloading of media id=" + mediaId + " succeeded.");
                    AsyncTask.execute(() -> {
                        // result contains the local file URI if successful
                        mediaFileDao.setLocalUri(mediaId, result.unwrap());
                    });
                } else {
                    logger.warn("downloading of media id=" + mediaId + " failed.");
                    if (result.getErrorType() == ActionResult.NETWORK_ERROR) {
                        logger.warn("due to network error.");
                    } else {
                        logger.warn("due to unknown error.");
                    }
                }
            }).exceptionally((e) -> {
                logger.error("downloading of media id=" + mediaId + " failed spectacularly:");
                e.printStackTrace();
                return null;
            });
    }

    private CompletableFuture<Void> uploadMedia(MediaFileInfo fileInfo) {
        if (fileInfo == null) {
            return CompletableFuture.completedFuture(null);
        }
        int mediaId = fileInfo.getId();
        return getDelegate(fileInfo.getType()).onUploadMedia(fileInfo)
            .thenAcceptAsync((result) -> {
                // @todo: replace with StringBuilder
                if (result != null && result.isSuccessful()) {
                    logger.info("uploading of media id=" + mediaId + " succeeded.");
                    AsyncTask.execute(() -> {
                        mediaFileDao.insertOrUpdate(fileInfo);
                        mediaFileDao.setRemoteAvailable(mediaId);
                    });
                } else {
                    logger.warn("uploading of media id=" + mediaId + " failed.");
                    if (result.getErrorType() == ActionResult.NETWORK_ERROR) {
                        logger.warn("due to network error.");
                    } else {
                        logger.warn("due to unknown error.");
                    }
                }
            }).exceptionally((e) -> {
                logger.error("uploading of media id=" + mediaId + " failed spectacularly:");
                e.printStackTrace();
                return null;
            });
    }

    private void processUploadQueue() {
        logger.debug("processUploadQueue begin");
        AsyncTask.execute(() -> {
            List<MediaFileInfo> pendingUploads = mediaFileDao.getPendingUploads();
            if (pendingUploads == null || pendingUploads.isEmpty()) {
                logger.info("No pending uploads");
                uploadQueueHandler.removeCallbacks(uploadQueueRunnable);
                uploadQueueHandler.postDelayed(uploadQueueRunnable, UPLOAD_QUEUE_PERIOD);
            } else {
                int total = pendingUploads.size();
                logger.info(total + " pending uploads");

                CompletableFuture<Void> nextUpload = uploadMedia(pendingUploads.get(0));
                for (int i = 1; i < total; ++i) {
                    final int iCopy = i;
                    nextUpload = nextUpload.thenAcceptAsync(a ->
                        logger.info(
                            String.format(Locale.ENGLISH,
                                "Completed %d / %d of the upload queue", iCopy - 1, total)))
                    .thenCombineAsync(uploadMedia(pendingUploads.get(i)),
                        (a, b) -> {
                            // do nothing
                            logger.debug("combiner after i = " + iCopy);
                            return null;
                        });
                }

                nextUpload.thenAcceptAsync((x) -> {
                    logger.info("Finished processing upload queue.");
                    uploadQueueHandler.removeCallbacks(uploadQueueRunnable);
                    uploadQueueHandler.postDelayed(uploadQueueRunnable, UPLOAD_QUEUE_PERIOD);
                }).exceptionally((e) -> {
                    logger.error("Error during processing of upload queue:");
                    e.printStackTrace();
                    uploadQueueHandler.removeCallbacks(uploadQueueRunnable);
                    uploadQueueHandler.postDelayed(uploadQueueRunnable, UPLOAD_QUEUE_PERIOD);
                    return null;
                });
            }
        });
    }
}
