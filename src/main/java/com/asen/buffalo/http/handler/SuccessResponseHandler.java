package com.asen.buffalo.http.handler;

/**
 * http成功返回(status大于等于200且status小于300)处理器
 *
 * @author Asen
 * @version 1.0.0
 * @since 2020/05/19
 */
public interface SuccessResponseHandler {
    /**
     * 具体处理逻辑
     *
     * @param url      请求路径
     * @param status   http请求返回状态码
     * @param response http请求返回httpEntity
     */
    void handle(String url, int status, String response);
}
