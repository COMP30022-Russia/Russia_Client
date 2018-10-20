package com.comp30022.team_russia.assist.base;

import java9.util.concurrent.CompletableFuture;
import kotlin.jvm.functions.Function2;

/**
 * Represents an result from background operations.
 * Like "Maybe" in Haskell.
 * @param <T> The payload type.
 */
public class ActionResult<T> {

    // These numbers have nothing to do with HTTP response code. They just need to be unique.

    public static final int NO_ERROR = 0;
    public static final int NETWORK_ERROR = 1;
    public static final int NOT_AUTHENTICATED = 2;
    public static final int UNKNOWN_ERROR = 500;
    public static final int CUSTOM_ERROR = 501;
    public static final int FAILED_RETRY_EXCEEDED = 42;

    private final String errorMessage;
    private final int errorType;
    private final T payload;

    /**
     * Success constructor.
     * @param successContent The payload.
     */
    public ActionResult(T successContent) {
        errorType = NO_ERROR;
        errorMessage = null;
        payload = successContent;
    }

    /**
     * Error constructor with type and message.
     * @param errorType The type of the error.
     * @param errorMessage The error message.
     */
    public ActionResult(int errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.payload = null;
    }

    /**
     * Error constructor with type.
     * @param errorType The type of the error.
     */
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

    public static ActionResult failedCustomMessage(String errorMessage) {
        return new ActionResult(CUSTOM_ERROR, errorMessage);
    }

    //CHECKSTYLE.OFF: ALL
    public static <Params1, Params2, ReturnType> Function2<Params1, Params2, CompletableFuture<ActionResult<ReturnType>>> retry(Function2<Params1, Params2, CompletableFuture<ActionResult<ReturnType>>> func, int times) {

        Function2<Params1, Params2, CompletableFuture<ActionResult<ReturnType>>> wrapper = func;

        for (int i = 1; i < times; ++i) {
            final Function2<Params1, Params2, CompletableFuture<ActionResult<ReturnType>>> oldWrapper = wrapper;
            wrapper = (params1, params2) -> {
                CompletableFuture<ActionResult<ReturnType>> result = new CompletableFuture<>();
                func.invoke(params1, params2).thenAcceptAsync(
                    (returnTypeActionResult -> {
                        if (returnTypeActionResult.isSuccessful()) {
                            result.complete(returnTypeActionResult);
                        } else {
                            oldWrapper.invoke(params1, params2).thenAcceptAsync(returnTypeActionResult1 -> {
                                if (returnTypeActionResult1.isSuccessful()) {
                                    result.complete(returnTypeActionResult1);
                                } else {
                                    result.complete(new ActionResult<>(ActionResult.FAILED_RETRY_EXCEEDED));
                                }
                            });
                        }
                    })
                );
                return result;
            };
        }
        return wrapper;
    }
    //CHECKSTYLE.ON: ALL
}
