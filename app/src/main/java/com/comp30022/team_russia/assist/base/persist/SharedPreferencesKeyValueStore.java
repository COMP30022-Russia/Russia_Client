package com.comp30022.team_russia.assist.base.persist;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;

import javax.inject.Inject;

/**
 * Implementation of {@link KeyValueStore} using Android's SharedPreferences.
 */
public class SharedPreferencesKeyValueStore implements KeyValueStore {

    private static final String AUTH_TOKEN_KEY = "AUTH_TOKEN";
    private static final String LOGGED_OUT = "LOGGED_OUT";

    private final LoggerInterface logger;

    private Application application;
    private SharedPreferences preferences;

    private boolean initialised = false;

    @Inject
    public SharedPreferencesKeyValueStore(LoggerFactory loggerFactory) {
        logger = loggerFactory.create(this.getClass().getSimpleName());

        logger.info("Created");
    }

    @Override
    public void initialise(Application applicationContext) {
        if (initialised) {
            logger.warn("Attempting to initialise a second time.");
            return;
        }
        application = applicationContext;
        preferences = application.getSharedPreferences(
            application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        initialised = true;
    }

    @Override
    public void setString(String key, String value) {
        if (!initialised) {
            logger.warn("setString: not initialised.");
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public String getString(String key, String defaultValue) {
        if (!initialised) {
            logger.warn("getString: not initialised.");
            return defaultValue;
        }
        return preferences.getString(key, defaultValue);
    }

    @Override
    public void saveAuthToken(String token) {
        setString(AUTH_TOKEN_KEY, token);
    }

    @Override
    public boolean hasAuthToken() {
        return !getString(AUTH_TOKEN_KEY, LOGGED_OUT).equals(LOGGED_OUT);
    }

    @Override
    public String getAuthToken() {
        String tmp = getString(AUTH_TOKEN_KEY, LOGGED_OUT);
        return tmp.equals(LOGGED_OUT) ? null : tmp;
    }

    @Override
    public void clearAuthToken() {
        setString(AUTH_TOKEN_KEY, LOGGED_OUT);
    }


}
