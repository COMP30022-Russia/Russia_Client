package com.comp30022.team_russia.assist.base.persist;

import android.app.Application;

/**
 * A key-value store for simple persistence.
 */
public interface KeyValueStore {
    void initialise(Application applicationContext);

    void setString(String key, String value);

    String getString(String key, String defaultValue);

    // Helper methods for easier login persistence

    void saveAuthToken(String token);

    boolean hasAuthToken();

    String getAuthToken();

    void clearAuthToken();
}
