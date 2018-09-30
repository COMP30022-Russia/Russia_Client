package com.comp30022.team_russia.assist.util;

import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;

public class TestLoggerFactory implements LoggerFactory {
    @Override
    public LoggerInterface create(String tagName) {
        return new TestLogger(tagName);
    }
}

class TestLogger implements LoggerInterface {
    private final String tagName;

    TestLogger(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public void info(String message) {
        System.out.print(String.format("[%s] INFO %s", this.tagName, message));
    }

    @Override
    public void error(String message) {
        System.out.print(String.format("[%s] ERR %s", this.tagName, message));
    }

    @Override
    public void warn(String message) {
        System.out.print(String.format("[%s] WARN %s", this.tagName, message));
    }

    @Override
    public void debug(String message) {
        System.out.print(String.format("[%s] DEB %s", this.tagName, message));
    }
}