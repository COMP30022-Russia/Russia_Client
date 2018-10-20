package com.comp30022.team_russia.assist.features.push.sys;

/**
 * Socket service for data messages.
 */
public interface SocketService {
    /**
     * Establish connection to socket server.
     * @param firebaseToken Firebase token.
     */
    void connect(String firebaseToken);

    /**
     * Is socket connected.
     * @return Boolean value indicating whether socket is connected.
     */
    boolean isConnected();

    /**
     * Disconnect from socket.
     */
    void disconnect();
}