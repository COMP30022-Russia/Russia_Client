package com.comp30022.team_russia.assist.features.emergency.sys;

import android.content.Context;
import android.media.MediaPlayer;

// adopted from https://stackoverflow.com/questions/18254870/play-a-sound-from-res-raw

/**
 * Audio Player.
 */
public class AudioPlayer {

    private MediaPlayer mediaPlayer;

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void play(Context c, int rid) {
        stop();

        mediaPlayer = MediaPlayer.create(c, rid);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stop();
            }
        });

        mediaPlayer.start();
    }


    public void playLooping(Context c, int rid) {
        stop();

        mediaPlayer = MediaPlayer.create(c, rid);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stop();
            }
        });

        mediaPlayer.start();
    }

}