package com.comp30022.team_russia.assist.base;

/**
 * Logger interface.
 */
public interface LoggerInterface {
    void info(String message);

    void error(String message);

    void warn(String message);

    void debug(String message);
}