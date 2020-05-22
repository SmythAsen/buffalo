package com.asen.buffalo.http.exception;

/**
 * Http异常返回
 *
 * @author Asen
 * @version 1.0.0
 * @since 2020/05/19
 */
public class ErrorResponseException extends RuntimeException {

    public ErrorResponseException() {
    }

    public ErrorResponseException(String message) {
        super(message);
    }

    public ErrorResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorResponseException(Throwable cause) {
        super(cause);
    }

    public ErrorResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
