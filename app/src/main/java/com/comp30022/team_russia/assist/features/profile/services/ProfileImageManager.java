package com.comp30022.team_russia.assist.features.profile.services;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.media.models.MediaFileInfo;
import com.comp30022.team_russia.assist.features.media.types.MediaSubManager;
import com.comp30022.team_russia.assist.features.profile.models.ProfilePictureCreationArgs;
import com.comp30022.team_russia.assist.features.profile.models.ProfilePictureQueryArgs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import java9.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * ProfileImageManager.
 */
public class ProfileImageManager implements MediaSubManager {

    private static int MAX_IMAGE_SIZE = 400;

    private final AuthService authService;
    private final Application appContext;
    private final RussiaProfilePictureApi pictureApi;
    private final LoggerInterface logger;

    /**
     * Constructor.
     */
    @Inject
    public ProfileImageManager(AuthService authService,
                                    Application application,
                                    Retrofit retrofit,
                                    LoggerFactory loggerFactory) {
        this.authService = authService;
        this.appContext = application;
        this.pictureApi = retrofit.create(RussiaProfilePictureApi.class);
        this.logger = loggerFactory.getLoggerForClass(this.getClass());
    }

    @Override
    public MediaFileInfo onCreateMedia(Object payload, MediaFileInfo mediaFileInfo) {
        if (! (payload instanceof ProfilePictureCreationArgs)) {
            throw new IllegalArgumentException("invalid payload");
        }
        ProfilePictureCreationArgs args = (ProfilePictureCreationArgs) payload;
        mediaFileInfo.setProfileImageId(args.getUserId());
        if (args.getUri() == null) {
            // no local file to load
            // means this is a remote profile picture
            mediaFileInfo.setAvailableRemote(true);
        } else {
            mediaFileInfo.setProfileImagePendingUploadUri(args.getUri());
        }
        return mediaFileInfo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<ActionResult<Void>> onUploadMedia(MediaFileInfo fileInfo) {
        logger.debug("Uploading media id = " + fileInfo.getId());

        String path = fileInfo.getProfileImagePendingUploadUri();
        Integer userId = fileInfo.getProfileImageId();

        if (userId == null || path == null || path.isEmpty()) {
            return CompletableFuture
                .completedFuture(ActionResult.failedCustomMessage("Not for upload"));
        }

        logger.debug("filepath = " + path + " userId = " + userId);

        Bitmap bitmap;
        try {
            FileDescriptor fd = appContext
                .getContentResolver()
                .openFileDescriptor(Uri.parse(path),"r")
                .getFileDescriptor();

            bitmap = lessResolution(fd, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
        } catch (Exception e) {
            return CompletableFuture
                .completedFuture(ActionResult.failedCustomMessage("File not loaded"));
        }

        File file;
        // temporary file name
        String filename = String.format(
            Locale.ENGLISH, "profile_%d_squared_%s.jpg", userId, getTimeStamp());

        try {
            // https://stackoverflow.com/questions/45828401/how-to-post-a-bitmap-to-a-server-using-retrofit-android
            // create a file to write bitmap data
            file = new File(appContext.getFilesDir(), filename);
            file.createNewFile();
        } catch (IOException e) {
            bitmap.recycle();
            return CompletableFuture.completedFuture(ActionResult
                .failedCustomMessage("Failed to create file " + filename));
        }

        try {
            Bitmap squared = squareImage(bitmap);
            bitmap.recycle();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            squared.compress(Bitmap.CompressFormat.JPEG, 50 /*ignored for PNG*/, bos);
            byte[] bitmapData = bos.toByteArray();

            // write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            squared.recycle();
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                ActionResult.failedCustomMessage("Failed to write to file")
            );
        }
        //
        fileInfo.setAvailableLocally(true);
        fileInfo.setLocalUri(file.getAbsolutePath());

        // Create multipart form-data body
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part
            .createFormData("picture", file.getName(), reqFile);

        CompletableFuture<ActionResult<Void>> result = new CompletableFuture<>();
        pictureApi.updatePic(authService.getAuthToken(), body).enqueue(
            new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        logger.debug("updatePic: Picture Updated");
                        result.complete(new ActionResult(null));
                        return;
                    }
                    logger.debug("updatePic: Not Successful");
                    result.complete(ActionResult.failedCustomMessage("something wrong"));
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call,
                                      @NonNull Throwable t) {
                    logger.debug("updatePic: Failed");
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            }
        );
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<ActionResult<String>> onDownloadMedia(MediaFileInfo fileInfo) {
        logger.debug("Downloading media id = " + fileInfo.getId());

        Boolean remote = fileInfo.isAvailableRemote();
        Integer userId = fileInfo.getProfileImageId();

        if (userId == null || remote == null || !remote) {
            return CompletableFuture
                .completedFuture(ActionResult.failedCustomMessage("nothing to download"));
        }

        logger.debug("userId = " + userId);

        CompletableFuture<ActionResult<String>> result = new CompletableFuture<>();
        pictureApi.getUsersPicture(authService.getAuthToken(), userId).enqueue(
            new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {

                        String filename = String.format(Locale.ENGLISH,
                            "profile_%d_download_%s.jpg", userId, getTimeStamp());

                        try {
                            File file = new File(appContext.getFilesDir(), filename);
                            file.createNewFile();
                            BufferedSink sink = Okio.buffer(Okio.sink(file));
                            sink.writeAll(response.body().source());
                            sink.flush();
                            sink.close();
                            result.complete(new ActionResult<>(file.getAbsolutePath()));
                            logger.debug("getPic: Picture Response successful");

                        } catch (Exception e) {
                            logger.error("failed saving to file");
                            e.printStackTrace();
                            result.complete(ActionResult.failedCustomMessage("io error"));
                        }

                        return;
                    }
                    result.complete(ActionResult.failedCustomMessage("error in response"));
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    result.complete(new ActionResult<>(ActionResult.NETWORK_ERROR));
                }
            });
        return result;
    }

    @Override
    public SupportSQLiteQuery getQueryForMedia(Object identifiers) {
        ProfilePictureQueryArgs args = (ProfilePictureQueryArgs) identifiers;
        int userId = args.getUserId();

        SupportSQLiteQuery query = SupportSQLiteQueryBuilder.builder("media_files")
            .columns(new String[]{"*"})
            .selection("profile_id = :id", new Object[]{userId})
            .orderBy("id DESC")
            .limit("1")
            .create();

        logger.debug(query.getSql());
        return query;
    }

    /**
     * Turns picture into square if picture not already a square.
     */
    private Bitmap squareImage(Bitmap srcBmp) {
        //source: https://stackoverflow.com/questions/6908604/android-crop-center-of-bitmap
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                srcBmp,
                srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                0,
                srcBmp.getHeight(),
                srcBmp.getHeight()
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                srcBmp,
                0,
                srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                srcBmp.getWidth(),
                srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    /**
     * Gets a bitmap at limited resolution.
     * @param fd The {@link FileDescriptor} of the image file.
     * @param width Max width, in pixels.
     * @param height Max height, in pixels.
     * @return A bitmap not larger than the requested size.
     */
    public static Bitmap lessResolution(FileDescriptor fd, int width, int height) {
        //https://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android
        BitmapFactory.Options options = new BitmapFactory.Options();

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    private static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    private String getTimeStamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        return tsLong.toString();
    }
}

interface RussiaProfilePictureApi {
    @Multipart
    @POST("/me/profile/picture")
    Call<ResponseBody> updatePic(@Header("Authorization") String authToken,
                                 @Part MultipartBody.Part image);

    @GET("/me/profile/picture")
    Call<ResponseBody> getPic(@Header("Authorization") String authToken);

    @GET("/users/{id}/picture")
    Call<ResponseBody> getUsersPicture(
        @Header("Authorization") String authToken,
        @Path("id") int userId
    );
}