package com.comp30022.team_russia.assist.features.emergency.sys;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Audio Player Wrapper.
 * Adopted from https://stackoverflow.com/questions/18254870/play-a-sound-from-res-raw
 */
public class AudioPlayer {

    private MediaPlayer mediaPlayer;

    /**
     * Stops the currently playing audio.
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Play an audio.
     * @param c The current Context.
     * @param rid The audio file resource ID.
     */
    public void play(Context c, int rid) {
        stop();

        mediaPlayer = MediaPlayer.create(c, rid);
        mediaPlayer.setOnCompletionListener(mediaPlayer -> stop());

        mediaPlayer.start();
    }


    /**
     * Starts playing an audio repeatedly.
     * @param c The current Context.
     * @param rid The audio file resource ID.
     */
    public void playLooping(Context c, int rid) {
        stop();

        mediaPlayer = MediaPlayer.create(c, rid);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnCompletionListener(mediaPlayer -> stop());

        mediaPlayer.start();
    }

}