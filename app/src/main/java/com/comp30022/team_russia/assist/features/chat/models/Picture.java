package com.comp30022.team_russia.assist.features.chat.models;

import android.graphics.Bitmap;

/**
 * Picture class.
 */
public class Picture {

    private final Bitmap picture;

    public Bitmap getPicture() {
        return picture;
    }

    public Picture(Bitmap picture) {
        this.picture = picture;
    }
}
