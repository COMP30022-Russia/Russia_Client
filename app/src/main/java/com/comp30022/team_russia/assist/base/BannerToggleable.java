package com.comp30022.team_russia.assist.base;

/**
 * A marker interface to mark an Activity as being able to toggle on/off a banner,
 * like the "ON GOING NAVIGATION SESSION" banner.
 */
public interface BannerToggleable {

    /**
     * Called when the Navigation Fragment is visible.
     */
    void enterNavScreen();

    /**
     * Called when the Navigation Fragment becomes invisible.
     */
    void leaveNavScreen();
}
