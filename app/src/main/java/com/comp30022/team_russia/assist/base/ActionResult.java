package com.comp30022.team_russia.assist.base;


/**
 * Represents an result from background operations.
 * Like "Maybe" in Haskell.
 * @param <T>
 */
public class ActionResult<T> {

    public static final int NO_ERROR = 0;
    public static final int NETWORK_ERROR = 1;
    public static final int NOT_AUTHENTICATED = 2;
    public static final int SERVERSIDE_ERROR = 3;
    public static final int UNKNOWN_ERROR = 500;
    public static final int CUSTOM_ERROR = 501;

    private final String errorMessage;
    private final int errorType;
    private final T payload;

    public ActionResult(T successContent) {
        errorType = NO_ERROR;
        errorMessage = null;
        payload = successContent;
    }

    public ActionResult(int errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.payload = null;
    }

    public ActionResult(int errorType) {
        this(errorType, "");
    }

    public boolean isSuccessful() {
        return errorType == NO_ERROR;
    }

    public int getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T unwrap() {
        return payload;
    }


    public static ActionResult failedNetworkError() {
        return new ActionResult(NETWORK_ERROR);
    }

    public static ActionResult failedNotAutenticated() {
        return new ActionResult(NOT_AUTHENTICATED);
    }

    public static ActionResult failedCustomMessage(String errorMessage) {
        return new ActionResult(CUSTOM_ERROR, errorMessage);
    }
}
