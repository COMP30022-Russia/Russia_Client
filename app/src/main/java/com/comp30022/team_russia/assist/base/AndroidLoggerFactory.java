package com.comp30022.team_russia.assist.base;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

/**
 * Logger Factory implementation that builds on top of {@link android.util.Log}.
 */
public class AndroidLoggerFactory implements LoggerFactory {

    private Map<Class, LoggerInterface> cachedLoggers =  new ConcurrentHashMap<>();

    @Inject
    public AndroidLoggerFactory() {}

    @Override
    public LoggerInterface create(String tagName) {
        return new AndroidLogger(tagName);
    }

    @Override
    public LoggerInterface getLoggerForClass(Class theClass) {
        if (cachedLoggers.containsKey(theClass)) {
            return cachedLoggers.get(theClass);
        }
        LoggerInterface newLogger = new AndroidLogger(theClass.getSimpleName());
        cachedLoggers.put(theClass, newLogger);
        return newLogger;
    }

}

/**
 * Android Logger.
 */
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
