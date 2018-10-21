package com.comp30022.team_russia.assist.features.chat.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.comp30022.team_russia.assist.R;

/**
 * MessageImageFullScreenViewer.
 */
public class MessageImageFullScreenViewer extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_image_full_screen);



        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bmp);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}
