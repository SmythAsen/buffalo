package com.asen.buffalo.http;

/**
 * @description:
 * @author: Asen
 * @since: 2020-05-19 16:05:57
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