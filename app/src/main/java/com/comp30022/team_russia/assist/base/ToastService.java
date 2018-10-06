package com.comp30022.team_russia.assist.base;

/**
 * Service for showing Android System Toast messages.
 */
public interface ToastService {
    /**
     * Show a short-duration toast message.
     *
     * @param message The text to show.
     */
    void toastShort(String message);

    /**
     * Show a long-duration toast message.
     *
     * @param message The text to show.
     */
    void toastLong(String message);
}