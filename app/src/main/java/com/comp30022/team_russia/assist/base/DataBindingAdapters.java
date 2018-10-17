package com.comp30022.team_russia.assist.base;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.ImageView;

import com.comp30022.team_russia.assist.R;

/**
 * Data Binding Adapters.
 * These enables us to use data binding expression in the xml files on properties not natively
 * supported.
 */
public class DataBindingAdapters {

    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, String imageUri) {
        if (imageUri == null) {
            view.setImageURI(null);
        } else {
            view.setImageURI(Uri.parse(imageUri));
        }
    }

    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, Uri imageUri) {
        view.setImageURI(imageUri);
    }

    @BindingAdapter("android:src")
    public static void setImageDrawable(ImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter("android:background")
    public static void setBackgroundResource(ImageView imageView, int resource) {
        Drawable d = null;
        if (resource != 0) {
            d = ResourcesCompat.getDrawable(imageView.getResources(), resource, null);
        }
        imageView.setBackground(d);

    }

    @BindingAdapter("android:profile")
    public static void setProfileImage(ImageView view, Bitmap image) {
        if (image == null) {
            view.setImageResource(R.drawable.ic_profile_placeholder);
        } else {
            view.setImageBitmap(image);
        }
    }

    @BindingAdapter("android:profile")
    public static void setProfileImage(ImageView view, Uri imageUri) {
        if (imageUri == null) {
            view.setImageURI(null);
            view.setImageResource(R.drawable.ic_profile_placeholder);
        } else {
            view.setImageURI(imageUri);
        }
    }
}
