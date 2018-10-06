package com.comp30022.team_russia.assist.base;

/**
 * Represents a resource (e.g. subscription) that can be disposed when not needed.
 * The clean-up code of that resource should be implemented in the dispose() method.
 */
public interface Disposable {
    void dispose();
}