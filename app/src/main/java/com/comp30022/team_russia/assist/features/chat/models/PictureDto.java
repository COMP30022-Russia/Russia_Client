package com.comp30022.team_russia.assist.features.chat.models;

import com.google.gson.annotations.SerializedName;

/**
 * Picture Dto.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class PictureDto {
    @SerializedName("status")
    private String status;

    @SerializedName("id")
    private int pictureId;

    @SerializedName("associationId")
    private int assocId;

    @SerializedName("messageId")
    private int messageId;

    /**
     * Constructor.
     */
    public PictureDto(String status,
                      int pictureId,
                      int assocId,
                      int messageId) {
        this.status = status;
        this.pictureId = pictureId;
        this.assocId = assocId;
        this.messageId = messageId;
    }

    public int getPictureId() {
        return pictureId;
    }
}