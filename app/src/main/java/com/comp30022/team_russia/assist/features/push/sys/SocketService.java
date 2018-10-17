package com.comp30022.team_russia.assist.features.push.sys;

/**
 * Socket service for data messages.
 */
public interface SocketService {
    public void connect(String firebaseToken);

    public boolean isConnected();

    public void disconnect();
}