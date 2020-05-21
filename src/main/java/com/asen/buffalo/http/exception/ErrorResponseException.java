package com.asen.buffalo.http.exception;

/**
 * @description: Http异常返回
 * @author: Asen
 * @since: 2020/05/21
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
