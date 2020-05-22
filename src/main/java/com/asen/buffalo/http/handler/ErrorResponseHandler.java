package com.asen.buffalo.http.handler;

/**
 * http请求错误返回(status小于200或者status大于等于300)处理器
 *
 * @author Asen
 * @version 1.0.0
 * @since 2020/05/19
 */
public interface ErrorResponseHandler {
    /**
     * 具体处理逻辑
     *
     * @param url      请求路径
     * @param status   http请求返回状态码
     * @param response http请求返回httpEntity
     */
    void handle(String url, int status, String response);
}