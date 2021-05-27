package com.asen.buffalo.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.asen.buffalo.digest.HashUtils;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * http工具类
 *
 * @author Asen
 * @since 1.1.1
 */
public class OkHttpClients {
    private static final Timeout DEFAULT_CONNECTION_TIMEOUT = new Timeout(5, TimeUnit.SECONDS);
    private static final Timeout DEFAULT_CALL_TIMEOUT = new Timeout(5, TimeUnit.SECONDS);
    private static final Timeout DEFAULT_READ_TIMEOUT = new Timeout(5, TimeUnit.SECONDS);
    private static final Map<String, OkHttpClient> CACHE_CLIENTS = new HashMap<>();
    /**
     * 指定Client
     */
    private OkHttpClient okHttpClient;
    private Timeout callTimeout = DEFAULT_CALL_TIMEOUT;
    private Timeout connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private Timeout readTimeOut = DEFAULT_READ_TIMEOUT;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Proxy proxy;
    private boolean retryOnFail = false;
    private String url;
    private HttpUrl httpUrl;
    private Request request;
    private RequestBody httpRequestBody;
    private String requestBody;
    private Response response;
    private Integer statusCode;
    private String body;
    private boolean success;

    public OkHttpClients() {
    }

    public OkHttpClients(Timeout callTimeout, Timeout connectionTimeout, Timeout readTimeOut) {
        this.callTimeout = callTimeout;
        this.connectionTimeout = connectionTimeout;
        this.readTimeOut = readTimeOut;
    }

    public OkHttpClients(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public static OkHttpClients create() {
        return new OkHttpClients();
    }

    public static OkHttpClients create(OkHttpClient okHttpClient) {
        return new OkHttpClients(okHttpClient);
    }

    public static OkHttpClients create(Timeout callTimeout, Timeout connectionTimeout, Timeout readTimeOut) {
        return new OkHttpClients(callTimeout, connectionTimeout, readTimeOut);
    }

    public OkHttpClients get() throws Exception {
        init();
        Response response = getOkHttpClient().newCall(request.newBuilder().get().build()).execute();
        parseResponse(response);
        return this;
    }

    public OkHttpClients post() throws Exception {
        init();
        buildRequestBody();
        Response response = getOkHttpClient().newCall(request.newBuilder().post(httpRequestBody).build()).execute();
        parseResponse(response);
        return this;
    }

    public OkHttpClients post(Request request) throws Exception {
        this.request = request;
        init();
        Response response = getOkHttpClient().newCall(this.request).execute();
        parseResponse(response);
        return this;
    }

    public OkHttpClients post(RequestBody body) throws Exception {
        init();
        Response response = getOkHttpClient().newCall(request.newBuilder().post(body).build()).execute();
        parseResponse(response);
        return this;
    }

    public OkHttpClients method(String method) throws Exception {
        init();
        Response response = getOkHttpClient().newCall(request.newBuilder().method(method, httpRequestBody).build()).execute();
        parseResponse(response);
        return this;
    }

    private void init() {
        buildUrl();
        buildRequest();
    }

    private void buildUrl() {
        if (Objects.nonNull(this.httpUrl) || StringUtils.isBlank(this.url)) {
            return;
        }
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        params.forEach(builder::addEncodedQueryParameter);
        httpUrl = builder.build();
    }

    private void buildRequest() {
        Request.Builder builder;
        if (Objects.nonNull(this.request)) {
            builder = this.request.newBuilder();
        } else {
            builder = new Request.Builder();
        }
        if (Objects.nonNull(httpUrl)) {
            builder.url(httpUrl);
        }
        headers.forEach(builder::addHeader);
        request = builder.build();
    }

    private void buildRequestBody() {
        if (Objects.nonNull(httpRequestBody)) {
            return;
        }
        String contentType = headers.getOrDefault("Content-Type", "application/json");
        if (StringUtils.isNotBlank(requestBody)) {
            httpRequestBody = RequestBody.create(requestBody, MediaType.parse(contentType));
        }
    }

    private void parseResponse(Response response) throws IOException {
        this.response = response;
        this.statusCode = response.code();
        this.success = response.isSuccessful();
        if (response.isSuccessful()) {
            this.body = new String(Objects.requireNonNull(response.body()).bytes(), StandardCharsets.UTF_8);
        }
    }

    public OkHttpClient getOkHttpClient() throws Exception {
        if (Objects.nonNull(okHttpClient)) {
            return okHttpClient;
        }
        StringJoiner keyJoiner = new StringJoiner("-");
        keyJoiner.add(callTimeout.toString())
                .add(connectionTimeout.toString())
                .add(readTimeOut.toString())
                .add(String.valueOf(retryOnFail));
        if (Objects.nonNull(proxy)) {
            keyJoiner.add(proxy.toString());
        }
        String key = HashUtils.md5(keyJoiner.toString());
        if (CACHE_CLIENTS.containsKey(key)) {
            return CACHE_CLIENTS.get(key);
        }
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .callTimeout(callTimeout.getTimeout(), callTimeout.getTimeUnit())
                .connectTimeout(connectionTimeout.getTimeout(), connectionTimeout.getTimeUnit())
                .readTimeout(readTimeOut.getTimeout(), readTimeOut.getTimeUnit())
                .retryOnConnectionFailure(retryOnFail)
                .proxy(proxy)
                .hostnameVerifier((s, sslSession) -> true)
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .build();
        CACHE_CLIENTS.put(key, okHttpClient);
        return okHttpClient;
    }

    public OkHttpClients callTimeout(Integer timeout, TimeUnit timeUnit) {
        this.callTimeout = new Timeout(timeout, timeUnit);
        return this;
    }

    public OkHttpClients connectionTimeout(Integer timeout, TimeUnit timeUnit) {
        this.connectionTimeout = new Timeout(timeout, timeUnit);
        return this;
    }

    public OkHttpClients readTimeOut(Integer timeout, TimeUnit timeUnit) {
        this.readTimeOut = new Timeout(timeout, timeUnit);
        return this;
    }

    public OkHttpClients addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public OkHttpClients addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public OkHttpClients addParam(String name, String value) {
        this.params.put(name, value);
        return this;
    }

    public OkHttpClients addParams(Map<String, String> params) {
        this.params.putAll(params);
        return this;
    }

    public OkHttpClients url(String url) {
        this.url = url;
        return this;
    }

    public OkHttpClients httpUrl(HttpUrl httpUrl) {
        this.httpUrl = httpUrl;
        return this;
    }

    public OkHttpClients httpRequest(Request httpRequest) {
        this.request = httpRequest;
        return this;
    }

    public OkHttpClients requestBody(RequestBody requestBody) {
        this.httpRequestBody = requestBody;
        return this;
    }

    public OkHttpClients requestBody(Object body) {
        this.requestBody = JSONObject.toJSONString(body, SerializerFeature.BrowserCompatible);
        return this;
    }

    public OkHttpClients multipartBody(MultipartBody multipart) {
        this.httpRequestBody = multipart;
        return this;
    }

    public OkHttpClients requestBody(String requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public OkHttpClients proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public OkHttpClients proxy(Proxy.Type type, String host, int port) {
        this.proxy = new Proxy(type, new InetSocketAddress(host, port));
        return this;
    }

    public OkHttpClients retryOnFail(boolean retryOnFail) {
        this.retryOnFail = retryOnFail;
        return this;
    }

    public Response response() {
        return this.response;
    }

    public String body() {
        return body;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public boolean success() {
        return success;
    }

    public JSONObject toJSONObject() {
        if (success && StringUtils.isNotBlank(body)) {
            return JSON.parseObject(body);
        }
        return null;
    }

    public <T> T toObject(Class<T> clz) {
        if (success && StringUtils.isNotBlank(body)) {
            return JSON.parseObject(body, clz);
        }
        return null;
    }
}
