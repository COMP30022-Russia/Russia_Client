package com.comp30022.team_russia.assist.base;

import android.util.Log;

import javax.inject.Inject;

/**
 * Logger Factory implementation that builds on top of {@link android.util.Log}.
 */
public class AndroidLoggerFactory implements LoggerFactory {
    @Inject
    public AndroidLoggerFactory() {}

    @Override
    public LoggerInterface create(String tagName) {
        return new AndroidLogger(tagName);
    }
}

class AndroidLogger implements LoggerInterface {

    private final String tagName;

    AndroidLogger(String tagName) {
        this.tagName = tagName;
    }

    public void debug(String message) {
        Log.d(this.tagName, message);
    }

    public void info(String message) {
        Log.i(this.tagName, message);
    }

    public void error(String message) {
        Log.e(this.tagName, message);
    }

    public void warn(String message) {
        Log.w(tagName, message);
    }

}
