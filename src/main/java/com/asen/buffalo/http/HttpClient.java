package com.asen.buffalo.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.asen.buffalo.http.exception.ErrorResponseException;
import com.asen.buffalo.http.handler.ErrorResponseHandler;
import com.asen.buffalo.http.handler.SuccessResponseHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description:
 * @author: Asen
 * @create: 2019-07-22 11:00:36
 */
@Slf4j
public class HttpClient {

    /**
     * 链接建立的超时时间 ms
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;
    /**
     * 响应超时时间 ms
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    /**
     * 每个路由的最大连接数
     */
    private static final int DEFAULT_DEFAULT_MAX_PER_ROUTE = 50;
    /**
     * 最大连接数
     */
    private static final int DEFAULT_DEFAULT_MAX_TOTAL = 200;
    /**
     * 重试次数，默认0
     */
    private static final int DEFAULT_RETRY_COUNT = 0;
    /**
     * 从connection pool中获得一个connection的超时时间 ms
     */
    private static final int DEFAULT_CONNECTION_WAIT_TIMEOUT = 300;

    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String AUTHORIZATION_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private static final String DEFAULT_SHARED_KEY = "DEFAULT_SHARED_KEY";
    private static final Map<String, HttpClient> CREATED_HTTP_CLIENTS = new HashMap<>();
    private static final Lock LOCK = new ReentrantLock();

    private static final ScheduledExecutorService SCHEDULED_CLOSED_EXECUTOR = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("http conn-closed-thread-%s")
                    .priority(Thread.NORM_PRIORITY)
                    .daemon(false).build(),
            (r, e) -> log.error("monitor push reject task error={}", e.toString()));

    private static final List<HttpClientConnectionManager> HTTP_CLIENT_CONNECTION_MANAGERS = Lists.newArrayList();

    static {
        SCHEDULED_CLOSED_EXECUTOR.schedule(() -> HTTP_CLIENT_CONNECTION_MANAGERS.forEach(HttpClientConnectionManager::closeExpiredConnections), 5, TimeUnit.SECONDS);
    }

    /**
     * 连接超时时间/ms
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    /**
     * 链接建立的超时时间/ms
     */
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    /**
     * 每个路由的最大连接数
     */
    private int maxPreRote = DEFAULT_DEFAULT_MAX_PER_ROUTE;
    /**
     * 最大连接数
     */
    private int maxTotal = DEFAULT_DEFAULT_MAX_TOTAL;
    /**
     * 重试次数，默认0
     */
    private int retryCount = DEFAULT_RETRY_COUNT;
    /**
     * 从connection pool中获得一个connection的超时时间 ms
     */
    private int connectionWaitTimeout = DEFAULT_CONNECTION_WAIT_TIMEOUT;

    /**
     * 请求的url
     */
    private String url;
    /**
     * 认证
     */
    private String authorization;

    /**
     * 令牌
     */
    private String bearerToken;
    /**
     * 請求body
     */
    private String body;
    /**
     * 编码
     */
    private String contentType = CONTENT_TYPE_APPLICATION_JSON;
    /**
     * 请求头
     */
    private Map<String, String> headers = Maps.newHashMap();
    /**
     * 用户设置参数
     */
    private Map<String, String> params = Maps.newHashMap();
    /**
     * 查询数组
     */
    private Map<String, List<String>> arrayParams = Maps.newHashMap();
    /**
     * 传输文件
     */
    private Map<String, File> files = Maps.newHashMap();
    /**
     * MultipartEntityBuilder 中的textBody
     */
    private Map<String, String> textBody = Maps.newHashMap();
    /**
     * 查询参数
     */
    private Map<String, String> queryParams = Maps.newHashMap();

    /**
     * 代理ip
     */
    private String proxyHost;
    /**
     * 代理端口
     */
    private Integer proxyPort;
    /**
     * 代理模式：http或者https
     */
    private String proxySchemeName;

    /**
     * 请求返回的结果
     */
    private String result;
    /**
     * 请求状态
     */
    private Integer httpStatusCode;
    /**
     * 返回状态不为200的异常返回结果
     */
    private String errorResult;

    /**
     * 错误返回处理
     */
    private ErrorResponseHandler errorResponseHandler = (url, status, response) -> {
        log.error("http request failed! url: {}, status: {}, response: {}", url, status, response);
        throw new ErrorResponseException("http request failed");
    };
    private SuccessResponseHandler successResponseHandler = (url, status, response) -> {
    };


    private HttpClient() {
    }

    private HttpClient(int socketTimeout, int connectionTimeout, int maxPreRote, int maxTotal, int retryCount, int connectionWaitTimeout) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.maxPreRote = maxPreRote;
        this.maxTotal = maxTotal;
        this.retryCount = retryCount;
        this.connectionWaitTimeout = connectionWaitTimeout;
    }

    public static HttpClient create(String cachedKey) {
        HttpClient httpClient = CREATED_HTTP_CLIENTS.get(cachedKey);
        if (Objects.nonNull(httpClient)) {
            return httpClient;
        }
        try {
            LOCK.lock();
            httpClient = CREATED_HTTP_CLIENTS.get(cachedKey);
            if (Objects.isNull(httpClient)) {
                httpClient = new HttpClient();
                CREATED_HTTP_CLIENTS.put(cachedKey, httpClient);
            }
        } finally {
            LOCK.unlock();
        }
        return httpClient;
    }


    public static HttpClient create(String cachedKey, int socketTimeout, int connectionTimeout, int maxPreRote, int maxTotal, int retryCount, int connectionWaitTimeout) {
        HttpClient httpClient = CREATED_HTTP_CLIENTS.get(cachedKey);
        if (Objects.nonNull(httpClient)) {
            return httpClient;
        }
        try {
            LOCK.lock();
            httpClient = CREATED_HTTP_CLIENTS.get(cachedKey);
            if (Objects.isNull(httpClient)) {
                httpClient = new HttpClient(socketTimeout, connectionTimeout, maxPreRote, maxTotal, retryCount, connectionWaitTimeout);
                CREATED_HTTP_CLIENTS.put(cachedKey, httpClient);
            }
        } finally {
            LOCK.unlock();
        }
        return httpClient;
    }

    public static HttpClient create() {
        return create(DEFAULT_SHARED_KEY);
    }

    public static HttpClient create(int socketTimeout, int connectionTimeout, int maxPreRote, int maxTotal, int retryCount, int connectionWaitTimeout) {
        return create(DEFAULT_SHARED_KEY, socketTimeout, connectionTimeout, maxPreRote, maxTotal, retryCount, connectionWaitTimeout);
    }

    /**
     * 设置连接超时时间 ms
     *
     * @param socketTimeout
     * @return
     */
    public HttpClient socketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * 链接建立的超时时间/ms
     *
     * @param connectionTimeout
     * @return
     */
    public HttpClient connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * 设置每个路由的最大连接数
     *
     * @param maxPreRote
     * @return
     */
    public HttpClient maxPreRote(int maxPreRote) {
        this.maxPreRote = maxPreRote;
        return this;
    }

    /**
     * 设置最大连接数
     *
     * @param maxTotal
     * @return
     */
    public HttpClient maxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    /**
     * 设置重试次数，默认0
     *
     * @param retryCount
     * @return
     */
    public HttpClient retryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /**
     * 设置从connection pool中获得一个connection的超时时间 ms
     *
     * @param connectionWaitTimeout
     * @return
     */
    public HttpClient connectionWaitTimeout(int connectionWaitTimeout) {
        this.connectionWaitTimeout = connectionWaitTimeout;
        return this;
    }

    /**
     * 请求地址
     *
     * @param url
     * @return
     */
    public HttpClient url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 返回请求结果
     *
     * @return
     */
    public String result() {
        return this.result;
    }

    /**
     * 获取请求返回状态码
     *
     * @return
     */
    public Integer httpStatusCode() {
        return this.httpStatusCode;
    }

    /**
     * 获取异常返回结果
     *
     * @return
     */
    public String errorResult() {
        return this.errorResult;
    }

    /**
     * 添加多个请求参数
     *
     * @param params
     * @return
     */
    public HttpClient params(Map<String, String> params) {
        if (Objects.nonNull(params) && params.size() > 0) {
            this.params.putAll(params);
        }
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public HttpClient param(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            this.params.put(key, value);
        }
        return this;
    }

    /**
     * 添加多个数组参数
     *
     * @param arrayParams
     * @return
     */
    public HttpClient arrayParams(Map<String, List<String>> arrayParams) {
        if (Objects.nonNull(arrayParams) && arrayParams.size() > 0) {
            this.arrayParams.putAll(arrayParams);
        }
        return this;
    }

    /**
     * 添加数组参数
     *
     * @param key
     * @param values
     * @return
     */
    public HttpClient arrayParam(String key, List<String> values) {
        if (StringUtils.isNotBlank(key) && Objects.nonNull(values)) {
            this.arrayParams.put(key, values);
        }
        return this;
    }

    /**
     * 添加多个查询参数
     *
     * @param queryParams
     * @return
     */
    public HttpClient queryParams(Map<String, String> queryParams) {
        if (Objects.nonNull(queryParams) && queryParams.size() > 0) {
            this.queryParams.putAll(queryParams);
        }
        return this;
    }

    /**
     * 添加查询参数
     *
     * @param key
     * @param value
     * @return
     */
    public HttpClient queryParam(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            this.queryParams.put(key, value);
        }
        return this;
    }

    /**
     * 添加认证
     *
     * @param authorization
     * @return
     */
    public HttpClient authorization(String authorization) {
        this.authorization = authorization;
        return this;
    }

    /**
     * 添加多个请求头
     *
     * @param headers
     * @return
     */
    public HttpClient headers(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * 添加请求头
     *
     * @param key
     * @param value
     * @return
     */
    public HttpClient header(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            this.headers.put(key, value);
        }
        return this;
    }

    /**
     * 添加请求body
     *
     * @param body
     * @return
     */
    public HttpClient body(String body) {
        if (StringUtils.isNotBlank(body)) {
            this.body = body;
        }
        return this;
    }

    /**
     * 添加bearer 认证方式token
     *
     * @param bearerToken
     * @return
     */
    public HttpClient bearerToken(String bearerToken) {
        if (StringUtils.isNotBlank(bearerToken)) {
            this.bearerToken = bearerToken;
        }
        return this;
    }

    /**
     * 添加请求body
     *
     * @param body
     * @return
     */
    public HttpClient body(Object body) {
        if (Objects.nonNull(body)) {
            this.body = JSONObject.toJSONString(body, SerializerFeature.BrowserCompatible);
        }
        return this;
    }

    /**
     * 设置请求编码
     *
     * @param contentType
     * @return
     */
    public HttpClient contentType(String contentType) {
        if (StringUtils.isNotBlank(contentType)) {
            this.contentType = contentType;
        }
        return this;
    }

    /**
     * 添加请求文件
     *
     * @param name
     * @param file
     * @return
     */
    public HttpClient file(String name, File file) {
        if (StringUtils.isNotBlank(name) && Objects.nonNull(file)) {
            this.files.put(name, file);
        }
        return this;
    }

    /**
     * 添加多个文件
     *
     * @param files
     * @return
     */
    public HttpClient files(Map<String, File> files) {
        if (Objects.nonNull(files)) {
            for (Map.Entry<String, File> entry : files.entrySet()) {
                this.files.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * 添加multipart请求是textbody参数
     *
     * @param name
     * @param value
     * @return
     */
    public HttpClient multipartTextBody(String name, String value) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
            this.textBody.put(name, value);
        }
        return this;
    }

    /**
     * 添加multipart请求是textbody参数
     *
     * @param textBody
     * @return
     */
    public HttpClient multipartTextBody(Map<String, String> textBody) {
        if (Objects.nonNull(textBody) && textBody.size() > 0) {
            this.textBody.putAll(textBody);
        }
        return this;
    }

    /**
     * 设置代理
     *
     * @param proxyHost
     * @param proxyPort
     * @param proxySchemeName
     * @return
     */
    public HttpClient proxy(String proxyHost, Integer proxyPort, String proxySchemeName) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxySchemeName = proxySchemeName;
        return this;
    }

    /**
     * 当http请求错误(status<200 || status>=300)返回时处理逻辑
     *
     * @param errorResponseHandler 逻辑处理器
     * @return this
     */
    public HttpClient errorResponseHandler(ErrorResponseHandler errorResponseHandler) {
        this.errorResponseHandler = errorResponseHandler;
        return this;
    }

    /**
     * 当http请求正确(200<=status<300)时返回时处理逻辑
     *
     * @param successResponseHandler
     * @return
     */
    public HttpClient successResponseHandler(SuccessResponseHandler successResponseHandler) {
        this.successResponseHandler = successResponseHandler;
        return this;
    }

    /**
     * 发送POST请求
     *
     * @return
     */
    public HttpClient post() {
        this.validate();
        try {
            CloseableHttpClient httpClient = this.getHttpClient();
            HttpPost post = buildHttpPost();
            httpExecute(httpClient, post);
            httpClient.close();
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送GET请求
     *
     * @return
     */
    public HttpClient get() {
        this.validate();
        try {
            CloseableHttpClient httpClient = this.getHttpClient();
            HttpGet get = buildHttpGet();
            httpExecute(httpClient, get);
            httpClient.close();
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析返回结果为指定类
     *
     * @param clz
     * @param <T>
     * @return
     */
    public <T> T toObject(Class<T> clz) {
        if (StringUtils.isNotBlank(this.result)) {
            return JSON.parseObject(this.result, clz);
        }
        log.error("the http result is null, or you not sent the http request!");
        return null;
    }

    /**
     * 解析返回结果为JSONObject
     *
     * @return
     */
    public JSONObject toJsonObject() {
        if (StringUtils.isNotBlank(this.result)) {
            return JSON.parseObject(this.result);
        }
        log.error("the http result is null, or you not sent the http request!");
        return null;
    }


    /**
     * 构建httpPost
     *
     * @return
     */
    private HttpPost buildHttpPost() throws URISyntaxException, UnsupportedEncodingException {
        HttpPost post = new HttpPost(buildUrl());
        if (headers.size() > 0) {
            headers.forEach(post::addHeader);
        }
        if (StringUtils.isNotBlank(this.authorization)) {
            post.addHeader(AUTHORIZATION_NAME, this.authorization);
        }
        if (StringUtils.isNotBlank(this.bearerToken)) {
            post.setHeader(AUTHORIZATION_NAME, BEARER_TOKEN_PREFIX + this.bearerToken);
        }
        HttpEntity httpEntity;
        if (files.size() > 0) {
            httpEntity = buildMultipartEntity();
            post.setEntity(httpEntity);
        }
        if (StringUtils.isNotBlank(body)) {
            httpEntity = buildNormalEntity();
            post.setEntity(httpEntity);
        }
        return post;
    }

    private HttpGet buildHttpGet() throws URISyntaxException {
        HttpGet get = new HttpGet(buildUrl());
        if (headers.size() > 0) {
            headers.forEach(get::addHeader);
        }
        if (StringUtils.isNotBlank(this.authorization)) {
            get.addHeader(AUTHORIZATION_NAME, this.authorization);
        }
        if (StringUtils.isNotBlank(this.bearerToken)) {
            get.setHeader(AUTHORIZATION_NAME, BEARER_TOKEN_PREFIX + this.bearerToken);
        }
        return get;
    }

    /**
     * 构建url
     *
     * @return
     * @throws URISyntaxException
     */
    private URI buildUrl() throws URISyntaxException {
        this.validate();
        // 添加参数
        URIBuilder builder = new URIBuilder(this.url);
        if (this.queryParams.size() > 0) {
            builder.setCustomQuery(this.buildQueryParams());
        }
        if (this.params.size() > 0) {
            builder.addParameters(buildParams());
        }
        if (this.arrayParams.size() > 0) {
            builder.addParameters(buildArrayParams());
        }
        return builder.build();
    }

    /**
     * 构建常用entitiy
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    private HttpEntity buildNormalEntity() throws UnsupportedEncodingException {
        StringEntity result = new StringEntity(body);
        result.setContentEncoding(StandardCharsets.UTF_8.name());
        result.setContentType(contentType);
        return result;
    }

    /**
     * 构建multipartEntity
     *
     * @return
     */
    private HttpEntity buildMultipartEntity() {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // 添加文件
        if (this.files.size() > 0) {
            FileBody fileBody;
            for (Map.Entry<String, File> entry : this.files.entrySet()) {
                fileBody = new FileBody(entry.getValue());
                builder.addPart(entry.getKey(), fileBody);
            }
        }
        if (this.textBody.size() > 0) {
            for (Map.Entry<String, String> entry : this.textBody.entrySet()) {
                builder.addTextBody(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }


    private void httpExecute(CloseableHttpClient httpclient, HttpUriRequest request) {
        String path = request.getURI().toString();
        log.debug("http request url: {}", path);
        HttpEntity httpEntity = null;
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(request);
            if (Objects.isNull(response)) {
                throw new RuntimeException("call api exception no response");
            }
            this.httpStatusCode = response.getStatusLine().getStatusCode();
            httpEntity = response.getEntity();
            if (Objects.nonNull(httpEntity)) {
                this.result = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
            }
            if (HttpStatus.SC_OK <= this.httpStatusCode && this.httpStatusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
                log.debug("http request success, url: {},status: {}", path, this.httpStatusCode);
                successResponseHandler.handle(url, this.httpStatusCode, this.result);
                return;
            }
            this.errorResult = this.result;
            errorResponseHandler.handle(path, this.httpStatusCode, this.errorResult);
            log.error("http request failed! url: {}, status: {}, exception: {}", path, this.httpStatusCode, this.errorResult);
        } catch (SocketTimeoutException e) {
            log.error("http timeout request url : {} .", path);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("http exception request url: {} ", path, e);
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(response)) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            request.abort();
            EntityUtils.consumeQuietly(httpEntity);
        }
    }


    /**
     * 参数校验
     */
    private void validate() {
        if (StringUtils.isBlank(this.url)) {
            throw new RuntimeException("the url can not be null");
        }
        if (files.size() > 0 && StringUtils.isNotBlank(body)) {
            throw new RuntimeException("the body and file can not be setting both");
        }
    }

    /**
     * 构建参数
     *
     * @return
     */
    private List<NameValuePair> buildParams() {
        List<NameValuePair> paramList = Lists.newArrayList();
        for (Map.Entry<String, String> entry : this.params.entrySet()) {
            paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return paramList;
    }

    /**
     * 构建数组类型的参数
     *
     * @return
     */
    private List<NameValuePair> buildArrayParams() {
        List<NameValuePair> paramList = Lists.newArrayList();
        for (Map.Entry<String, List<String>> entry : this.arrayParams.entrySet()) {
            entry.getValue().forEach(value -> paramList.add(new BasicNameValuePair(entry.getKey(), value)));
        }
        return paramList;
    }

    private String buildQueryParams() {
        StringJoiner joiner = new StringJoiner("&");
        String queryParamNode;
        for (Map.Entry<String, String> entry : this.queryParams.entrySet()) {
            queryParamNode = entry.getKey() + "=" + entry.getValue();
            joiner.add(queryParamNode);
        }
        return joiner.toString();
    }


    private CloseableHttpClient getHttpClient() {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(((chain, authType) -> true)).build();
            //不进行主机名验证
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslConnectionSocketFactory)
                    .build();
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(this.maxTotal);
            cm.setDefaultMaxPerRoute(this.maxPreRote);
            HTTP_CLIENT_CONNECTION_MANAGERS.add(cm);

            RequestConfig.Builder configBuilder = RequestConfig.custom()
                    .setSocketTimeout(this.socketTimeout)
                    .setConnectTimeout(this.connectionTimeout)
                    .setConnectionRequestTimeout(this.connectionWaitTimeout);

            // 设置代理
            if (StringUtils.isNotBlank(this.proxyHost) && Objects.nonNull(this.proxyPort)) {
                HttpHost proxy = new HttpHost(this.proxyHost, this.proxyPort, this.proxySchemeName);
                configBuilder.setProxy(proxy);
            }
            return HttpClients.custom()
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .setDefaultCookieStore(new BasicCookieStore())
                    .setConnectionManager(cm)
                    .setDefaultRequestConfig(configBuilder.build())
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(this.retryCount, false))
                    .build();
        } catch (Exception e) {
            log.error("get httpclient error !{}", e.getMessage(), e);
        }
        return HttpClients.createDefault();
    }
}
