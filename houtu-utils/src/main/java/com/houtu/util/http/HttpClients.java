package com.houtu.util.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.houtu.util.common.IntrospectorUtils;
import com.houtu.util.common.JsonUtils;
import com.houtu.util.prop.HttpClientProperties;
import jakarta.annotation.Nonnull;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class HttpClients {

    final static String CONTENT_TYPE_NAME = "Content-Type";

    static CloseableHttpClient defaultHttpClient = org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();

    public HttpClients(HttpClientProperties httpClientProperties) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // 创建连接池
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // 设置最大连接数
        cm.setMaxTotal(httpClientProperties.getPool().getMaxTotal());
        // 设置每个路由的默认最大连接
        cm.setDefaultMaxPerRoute(httpClientProperties.getPool().getMaxPerRoute());

        org.apache.hc.client5.http.config.RequestConfig requestConfig = org.apache.hc.client5.http.config.RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(httpClientProperties.getRequest().getConnectTimeout()))
                .setResponseTimeout(Timeout.ofSeconds(httpClientProperties.getRequest().getResponseTimeout()))
                .build();
//        SSLContext sslContext = SSLContextBuilder
//                .create()
//                .loadTrustMaterial(TrustAllStrategy.INSTANCE)
//                .build();
        defaultHttpClient = HttpClientBuilder.create()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setProxy(StringUtils.hasLength(httpClientProperties.getProxy().getHostname())
                        && httpClientProperties.getProxy().getPort() > 0 ? new HttpHost(httpClientProperties.getProxy().getHostname(), httpClientProperties.getProxy().getPort()) : null)
                .build();
    }

    public static HttpResponseData get(String url) {
        return get(url, null);
    }

    public static HttpResponseData get(String url, RequestConfig requestConfig) {
        if (requestConfig == null) {
            requestConfig = RequestConfig.build();
        }
        HttpGet httpGet = new HttpGet(buildURIFromParams(url, requestConfig));
        addHeader(httpGet, requestConfig);
        return execute(requestConfig.getHttpClient(), httpGet);
    }

    public static HttpResponseData post(String url, RequestConfig requestConfig) {
        AtomicReference<ContentType> contentTypeRef = new AtomicReference<>();
        if (requestConfig.getHeaders() != null) {
            requestConfig.getHeaders().entrySet().forEach(es -> {
                Object hvalue = es.getValue();
                if (CONTENT_TYPE_NAME.equalsIgnoreCase(es.getKey()) && hvalue != null) {
                    contentTypeRef.set(ContentType.parse(String.valueOf(es.getValue())));
                }
            });
        }
        if (contentTypeRef.get() == null) {
            if (requestConfig.getData() != null) {
                contentTypeRef.set(ContentType.APPLICATION_JSON);
            } else if (requestConfig.getMultipart() != null) {
                contentTypeRef.set(ContentType.MULTIPART_FORM_DATA);
            } else {
                contentTypeRef.set(ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8).withCharset(StandardCharsets.UTF_8));
            }
        }
        ContentType contentType = contentTypeRef.get();

        HttpPost httpPost;

        HttpEntity httpEntity;
        if (ContentType.APPLICATION_JSON.isSameMimeType(contentType)) {
            httpPost = new HttpPost(buildURIFromParams(url, requestConfig));
            httpPost.setEntity(new StringEntity(requestConfig.getData(), contentType));
        } else if (ContentType.MULTIPART_FORM_DATA.isSameMimeType(contentType)) {
            httpPost = new HttpPost(buildURIFromParams(url, requestConfig));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            if (requestConfig.getMultipart() != null) {
                requestConfig.getMultipart().getFileByteArrays().forEach((k, v) -> {
                    if (v != null) {
                        builder.addBinaryBody(k, v);
                    }
                });
                requestConfig.getMultipart().getFileInputStreams().forEach((k, v) -> {
                    if (v != null) {
                        builder.addBinaryBody(k, v);
                    }
                });
                requestConfig.getMultipart().getParams().forEach((k, v) -> {
                    if (v != null) {
                        builder.addTextBody(k, String.valueOf(v));
                    }
                });
            }
            httpEntity = builder.build();
            httpPost.setEntity(httpEntity);
        } else if (ContentType.APPLICATION_FORM_URLENCODED.isSameMimeType(contentType)) {
            httpPost = new HttpPost(url);
            Map<String, Object> params = requestConfig.getParams();
            List<NameValuePair> paramList = new ArrayList<>(params.size());
            params.entrySet().stream()
                    .filter(p -> p.getKey() != null && p.getValue() != null)
                    .forEach(p -> paramList.add(new BasicNameValuePair(p.getKey(), String.valueOf(p.getValue()))));
            Charset charset = contentType.getCharset();
            if (charset == null) {
                charset = StandardCharsets.UTF_8;
            }
            httpEntity = new UrlEncodedFormEntity(paramList, charset);
            httpPost.setEntity(httpEntity);
        } else {
            throw new RuntimeException("not health Content-Type - " + contentType);
        }
        addHeader(httpPost, requestConfig);
        return execute(requestConfig.getHttpClient(), httpPost);
    }

    public static HttpResponseData execute(CloseableHttpClient httpClient, HttpUriRequestBase httpRequest) {
        Assert.notNull(httpRequest, "httpRequest can not be null");
        if (httpClient == null) {
            httpClient = defaultHttpClient;
        }
        try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
            return new HttpClients.HttpResponseData(response.getCode(),
                    response.getReasonPhrase(),
                    EntityUtils.toString(response.getEntity()),
                    response.getHeaders(),
                    response.getEntity().getContentEncoding());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static URI buildURIFromParams(String url, RequestConfig requestConfig) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (requestConfig.getParams() != null) {
                Map<String, Object> params = requestConfig.getParams();
                List<NameValuePair> paramList = new ArrayList<>(params.size());
                params.entrySet().stream()
                        .filter(p -> p.getValue() != null)
                        .forEach(p -> paramList.add(new BasicNameValuePair(p.getKey(), String.valueOf(p.getValue()))));
                uriBuilder.setParameters(paramList);
            }
            return uriBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static void addHeader(HttpUriRequestBase httpRequest, RequestConfig requestConfig) {
        Map<String, Object> headerMap = requestConfig.getHeaders();
        if (headerMap == null) {
            return;
        }
        // 设置请求头信息
        headerMap.entrySet().stream()
                .filter((e) -> e.getKey() != null && e.getValue() != null)
                .forEach((e) -> {
                    httpRequest.setHeader(e.getKey(), String.valueOf(e.getValue()));
                });
    }

    public static class RequestConfig {
        private CloseableHttpClient httpClient;
        // 请求头参数
        private Map<String, Object> headers = new HashMap<>();
        // 请求普通参数
        private Map<String, Object> params = new HashMap<>();
        // 请求Body参数
        private String data;
        // 请求Form文件上传配置
        private MultipartConfig multipart;

        public static RequestConfig build() {
            return new RequestConfig();
        }

        public CloseableHttpClient getHttpClient() {
            return httpClient;
        }

        public Map<String, Object> getHeaders() {
            return headers;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public String getData() {
            return this.data;
        }

        public MultipartConfig getMultipart() {
            return multipart;
        }

        public RequestConfig httpClient(CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public RequestConfig headers(Map<String, Object> headers) {
            this.headers = headers;
            return this;
        }

        public RequestConfig headers(Object headers) {
            this.headers = IntrospectorUtils.toMap(headers);
            return this;
        }

        public RequestConfig header(String key, Object value) {
            Assert.hasText(key, "key must not be empty");
            if (this.headers != null) {
                this.headers.put(key, value);
            }
            return this;
        }

        public RequestConfig params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public RequestConfig params(Object params) {
            this.params = IntrospectorUtils.toMap(params);
            return this;
        }

        public RequestConfig param(String key, Object value) {
            Assert.hasText(key, "key must not be empty");
            if (this.params != null) {
                this.params.put(key, value);
            }
            return this;
        }

        public RequestConfig data(String data) {
            this.data = data;
            return this;
        }

        public MultipartConfig multipart() {
            this.multipart = new MultipartConfig(this);
            return multipart;
        }
    }

    public static class MultipartConfig {
        private RequestConfig requestConfig;

        MultipartConfig(RequestConfig requestConfig) {
            this.requestConfig = requestConfig;
        }

        private Map<String, InputStream> fileInputStreams = new HashMap<>();
        private Map<String, byte[]> fileByteArrays = new HashMap<>();
        private Map<String, Object> params = new HashMap<>();

        Map<String, byte[]> getFileByteArrays() {
            return fileByteArrays;
        }

        Map<String, InputStream> getFileInputStreams() {
            return fileInputStreams;
        }

        Map<String, Object> getParams() {
            return params;
        }

        public MultipartConfig fileInputStream(String name, InputStream inputStream) {
            Assert.hasText(name, "name must not be empty");
            this.fileInputStreams.put(name, inputStream);
            return this;
        }

        public MultipartConfig fileByteArray(String name, byte[] byteArray) {
            Assert.hasText(name, "name must not be empty");
            this.fileByteArrays.put(name, byteArray);
            return this;
        }

        public MultipartConfig params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public MultipartConfig params(Object params) {
            this.params = IntrospectorUtils.toMap(params);
            return this;
        }

        public MultipartConfig param(String key, Object value) {
            Assert.hasText(key, "key must not be empty");
            if (this.params != null) {
                this.params.put(key, value);
            }
            return this;
        }

        public RequestConfig build() {
            return this.requestConfig;
        }
    }

    public static class HttpResponseData {

        // 响应头信息
        private Map<String, String> headerMap;
        // 响应状态码
        private int statusCode;
        // 响应状态描述
        private String statusText;
        // 响应内容
        private String content;
        // 响应内容编码
        private Charset charset;

        private HttpResponseData(int statusCode, String statusText, String content, Header[] headers, String charset) {
            this(statusCode, statusText, content, headers, charset, null);
        }

        private HttpResponseData(int statusCode, String statusText, String content, Header[] headers, String charset, Object requestData) {
            headerMap = new HashMap<>(headers.length);
            Arrays.stream(headers).forEach(p -> headerMap.put(p.getName(), p.getValue()));
            this.statusCode = statusCode;
            this.statusText = statusText;
            this.content = content;
            this.charset = charset == null ? StandardCharsets.UTF_8 : Charset.forName(charset);
        }

        public Map<String, String> getHeaderMap() {
            return headerMap;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getStatusText() {
            return statusText;
        }

        public String getContent() {
            return content;
        }

        public Charset getCharset() {
            return charset;
        }

        public @Nonnull <T> T convert(Class<T> clazz) {
            if (statusCode == HttpStatus.SC_OK) {
                if (CharSequence.class.isAssignableFrom(clazz)) {
                    return (T) content;
                }
                return JsonUtils.parseObject(content, clazz);
            }
            throw convertException();
        }

        public <T> T convert(TypeReference<T> typeReference) {
            if (statusCode == HttpStatus.SC_OK) {
                Class<?> rawType = JsonUtils.getRawType(typeReference);
                System.out.println(rawType);
                if (CharSequence.class.isAssignableFrom(rawType)) {
                    return (T) content;
                }
                return JsonUtils.parseObject(content, typeReference);
            }
            throw convertException();
        }

        RuntimeException convertException() {
            org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
            this.headerMap.entrySet().forEach(p -> httpHeaders.put(p.getKey(), Arrays.asList(p.getValue())));
            return HttpClientErrorException.create(org.springframework.http.HttpStatus.valueOf(statusCode),
                    statusText,
                    httpHeaders,
                    content == null ? null : content.getBytes(charset), charset);
        }
    }
}
