package com.asen.buffalo.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @description:
 * @author: huangys01@missfresh.cn
 * @create: 2019-07-22 11:00:36
 */
@Slf4j
public class HttpClient {

    public static final int HTTP_STATUS_OK_MIN = 200;
    public static final int HTTP_STATUS_OK_MAX = 300;
    public static final String UTF8_ENCODING = "UTF-8";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String AUTHORIZATION_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
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
    private Map<String, List<String>> arrayParams = Maps.newHashMap();
    /**
     * 传输文件
     */
    private Map<String, File> files = Maps.newHashMap();
    /**
     * MultipartEntityBuilder 中的textBody
     */
    private Map<String, String> textBody = Maps.newHashMap();

    private Map<String, String> queryParams = Maps.newHashMap();

    private String proxyHost;
    private Integer proxyPort;
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
    private String exceptionResult;

    /**
     * 连接超时时间
     */
    private Integer timeout;

    public static HttpClient create() {
        return new HttpClient();
    }

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
     * 返回请求结果
     *
     * @return
     */
    public String result() {
        return this.result;
    }

    public Integer httpStatusCode() {
        return this.httpStatusCode;
    }

    /**
     * 获取异常返回结果
     *
     * @return
     */
    public String exceptionResult() {
        return this.exceptionResult;
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
        result.setContentEncoding(UTF8_ENCODING);
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


    private String httpExecute(CloseableHttpClient httpclient, HttpUriRequest httpUriRequest) throws IOException {
        String ret = null;
        CloseableHttpResponse response = httpclient.execute(httpUriRequest);
        this.httpStatusCode = response.getStatusLine().getStatusCode();
        if (HTTP_STATUS_OK_MIN <= this.httpStatusCode && this.httpStatusCode < HTTP_STATUS_OK_MAX) {
            if (Objects.nonNull(response.getEntity())) {
                ret = EntityUtils.toString(response.getEntity(), UTF8_ENCODING);
            }
            response.close();
        } else {
            String exceptionResult;
            if (Objects.nonNull(response.getEntity())) {
                exceptionResult = EntityUtils.toString(response.getEntity(), UTF8_ENCODING);
                log.error(exceptionResult);
                this.exceptionResult = exceptionResult;
            }
            log.error("http response error!,code :{}", this.httpStatusCode);
        }
        this.result = ret;
        return ret;
    }

    public HttpClient url(String url) {
        this.url = url;
        return this;
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
     * 设置连接超时时间
     *
     * @param timeout
     * @return
     */
    public HttpClient timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    private CloseableHttpClient getHttpClient() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            //不进行主机名验证
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslConnectionSocketFactory)
                    .build();

            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(100);
            HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .setDefaultCookieStore(new BasicCookieStore())
                    .setConnectionManager(cm);

            ;
            RequestConfig.Builder configBuilder = RequestConfig.custom();
            if (StringUtils.isNotBlank(this.proxyHost) && Objects.nonNull(this.proxyPort)) {
                HttpHost proxy = new HttpHost(this.proxyHost, this.proxyPort, this.proxySchemeName);
                configBuilder.setProxy(proxy);
            }

            if (Objects.nonNull(this.timeout)) {
                configBuilder.setSocketTimeout(this.timeout)
                        .setConnectTimeout(this.timeout)
                        .setConnectionRequestTimeout(this.timeout);
            }

            RequestConfig defaultRequestConfig = configBuilder.build();
            httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
            return httpClientBuilder.build();
        } catch (Exception e) {
            log.error("get httpclient error !{}", e);
        }
        return HttpClients.createDefault();
    }
}