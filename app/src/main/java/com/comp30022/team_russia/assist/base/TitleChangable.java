package com.comp30022.team_russia.assist.base;

/**
 * This is a marker interface that tags an {@link Activity} as able to change the
 * title on the AppBar.
 */
public interface TitleChangable {
    void updateTitle(String title);
}
