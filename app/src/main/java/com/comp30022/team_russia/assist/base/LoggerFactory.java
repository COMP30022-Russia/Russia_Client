package com.comp30022.team_russia.assist.base;

/**
 * Logger Factory creates a LoggerInterface instance for a particular class to use.
 */
public interface LoggerFactory {
    LoggerInterface create(String tagName);
}