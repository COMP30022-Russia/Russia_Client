package com.comp30022.team_russia.assist.features.push.sys;

import com.comp30022.team_russia.assist.ConfigurationManager;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Map;

import javax.inject.Inject;

/**
 * Background service for receiving socket messages.
 */
public class RussiaSocketService implements SocketService {
    /**
     * Logger.
     */
    private final LoggerInterface logger;

    /**
     * Socket.io socket instance.
     */
    private Socket socket;

    /**
     * {@link PubSubHub} instance.
     */
    private final PubSubHub pubSubHub;

    /**
     * {@link AuthService} instance.
     */
    private final AuthService authService;

    /**
     * Constructor.
     * @param loggerFactory DI injected.
     * @param pubSubHub DI injected.
     * @param authService DI injected.
     */
    @Inject
    public RussiaSocketService(LoggerFactory loggerFactory,
                               PubSubHub pubSubHub,
                               AuthService authService) {
        logger = loggerFactory.getLoggerForClass(this.getClass());
        this.pubSubHub = pubSubHub;
        this.authService = authService;
    }

    /**
     * Establish connection to socket server.
     * @param firebaseToken Firebase token.
     */
    public void connect(String firebaseToken) {
        // If socket is already connected
        if ((socket != null && socket.connected()) || !authService.isLoggedInUnboxed()) {
            return;
        }

        try {
            // Create socket
            if (socket == null) {
                // Specify connection options and connect
                String baseUrl = "socket";
                IO.Options opts = new IO.Options();
                opts.query = String.format("auth_token=%s&firebase_token=%s",
                    authService.getAuthToken().replace("Bearer ", ""),
                    firebaseToken);
                opts.forceNew = true;

                socket = IO.socket(ConfigurationManager.getInstance().getProperty(
                    "SERVER_URL") + baseUrl, opts);
            }

            // Connect
            socket.connect();

            // On connect
            socket.on(Socket.EVENT_CONNECT, args -> logger.info("Socket connected"));
            // On error
            socket.on(Socket.EVENT_ERROR, args -> logger.error("Socket error"));
            // On disconnection
            socket.on(Socket.EVENT_DISCONNECT, args -> logger.info("Socket disconnected"));

            // On receiving data message
            socket.on("data_message", args -> {
                // Extract payload
                String stringPayload = (String) args[0];
                Gson gson = new Gson();

                // Deserialize json to HashMap
                // Adapted from https://stackoverflow.com/questions/14944419
                Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> payload = gson.fromJson(stringPayload, stringStringMap);

                if (payload.containsKey("type") && payload.containsKey("data")) {
                    String type = payload.get("type");
                    String data = payload.get("data");
                    pubSubHub.publish(type, data);
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Is socket connected.
     * @return Boolean value indicating whether socket is connected.
     */
    public boolean isConnected() {
        if (socket == null) {
            return false;
        }
        return socket.connected();
    }

    /**
     * Disconnect from socket.
     */
    public void disconnect() {
        if (socket == null || !socket.connected()) {
            return;
        }
        socket.close();
    }
}
