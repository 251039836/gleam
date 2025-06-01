package gleam.util.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.net.WWWFormCodec;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import gleam.util.json.JsonUtil;

/**
 * @author by Redback
 * @date 2021/08/18
 */
public class HttpClientUtil {

    private final static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * 同步 HttpClient
     */
    private static final CloseableHttpClient httpClient = buildHttpClient();
    /**
     * 异步 HttpClient
     */
    private static final CloseableHttpAsyncClient httpAsyncClient = buildAsyncClient();

    public static final ContentType APPLICATION_FORM_URLENCODED_UTF8 = ContentType.create("application/x-www-form-urlencoded", StandardCharsets.UTF_8);

    /**
     * 异步 http get 请求
     * 
     * @param url                        URL
     * @param httpStringResponseCallback 异步回调
     * @throws Exception exception
     */
    public static void asyncGet(String url, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        asyncGet(url, Lists.newArrayList(), httpStringResponseCallback);
    }

    /**
     * 异步 http get 请求
     * 
     * @param url                        URL
     * @param param                      请求参数
     * @param httpStringResponseCallback 异步回调
     * @throws Exception exception
     */
    public static void asyncGet(String url, Map<String, Object> param, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        URI uri = buildUri(url, param);
        SimpleHttpRequest request = SimpleRequestBuilder.get(uri).setCharset(StandardCharsets.UTF_8).build();
        asyncHttpRequest(request, httpStringResponseCallback);
    }

    /**
     * 异步 http get 请求
     * 
     * @param url                        URL
     * @param params                     请求参数
     * @param httpStringResponseCallback 异步回调
     * @throws Exception exception
     */
    public static void asyncGet(String url, List<NameValuePair> params, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        URI uri = buildUri(url, params);
        SimpleHttpRequest request = SimpleRequestBuilder.get(uri).setCharset(StandardCharsets.UTF_8).build();
        asyncHttpRequest(request, httpStringResponseCallback);
    }

    public static void asyncHttpRequest(SimpleHttpRequest request, HttpStringResponseCallback httpStringResponseCallback) {
        if (request == null) {
            throw new NullPointerException("request is null...");
        }
        if (httpStringResponseCallback == null) {
            throw new NullPointerException(String.format("async http request [%s] callback is null", request.getRequestUri()));
        }
        httpAsyncClient.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void cancelled() {
                // do nothing
            }

            @Override
            public void completed(SimpleHttpResponse response) {
                SimpleBody simpleBody = response.getBody();
                String content = getBodyText(simpleBody);
                httpStringResponseCallback.success(content);
            }

            @Override
            public void failed(Exception ex) {
                logger.error("async [{}] request[{}] error", request.getMethod(), request.getRequestUri(), ex);
                httpStringResponseCallback.failed(ex);
            }
        });

    }

    /**
     * 异步 http post 请求
     * 
     * @param url                        Url
     * @param httpStringResponseCallback 请求回调
     * @throws Exception exception
     */
    public static void asyncPost(String url, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        asyncPost(url, Collections.emptyList(), httpStringResponseCallback);
    }

    public static void asyncPost(String url, List<NameValuePair> params, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        asyncPost(url, Collections.emptyMap(), params, httpStringResponseCallback);
    }

    /**
     * 异步发送 http post 请求
     * 
     * @param url                        URL
     * @param headers                    头部
     * @param params                     表单参数
     * @param httpStringResponseCallback 异步回调
     * @throws Exception exception
     */
    public static void asyncPost(String url, Map<String, String> headers, List<NameValuePair> params, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        URI uri = buildUri(url, Lists.newArrayList());
        SimpleRequestBuilder builder = SimpleRequestBuilder.post(uri).setCharset(StandardCharsets.UTF_8).addHeader("Content-Type", APPLICATION_FORM_URLENCODED_UTF8.toString());
        // 组装SimpleHttpRequest
        final String content = WWWFormCodec.format(params, APPLICATION_FORM_URLENCODED_UTF8.getCharset());
        builder.setBody(content, APPLICATION_FORM_URLENCODED_UTF8);
        SimpleHttpRequest request = builder.build();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry);
            }
        }
        asyncHttpRequest(request, httpStringResponseCallback);
    }

    public static void asyncPost(String url, Map<String, Object> param, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        asyncPost(url, null, param, httpStringResponseCallback);
    }

    /**
     * 异步发送 http post 请求
     * 
     * @param url                        URL
     * @param headers                    头部
     * @param param                      表单参数
     * @param httpStringResponseCallback 异步回调
     * @throws Exception exception
     */
    public static void asyncPost(String url, Map<String, String> headers, Map<String, Object> param, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        URI uri = buildUri(url, Collections.emptyList());
        SimpleRequestBuilder builder = SimpleRequestBuilder.post(uri).setCharset(StandardCharsets.UTF_8).addHeader("Content-Type", APPLICATION_FORM_URLENCODED_UTF8.toString());
        List<NameValuePair> parameters;
        if (param != null) {
            parameters = new ArrayList<>(param.size());
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                String name = entry.getKey();
                String value = String.valueOf(entry.getValue());
                parameters.add(new BasicNameValuePair(name, value));
            }
        } else {
            parameters = Collections.emptyList();
        }
        // 组装SimpleHttpRequest
        final String content = WWWFormCodec.format(parameters, APPLICATION_FORM_URLENCODED_UTF8.getCharset());
        builder.setBody(content, APPLICATION_FORM_URLENCODED_UTF8);
        SimpleHttpRequest request = builder.build();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry);
            }
        }
        asyncHttpRequest(request, httpStringResponseCallback);
    }

    /**
     * 异步发送 json
     * 
     * @param url                        Url
     * @param data                       json object
     * @param httpStringResponseCallback 异步回调接口
     * @throws Exception exception
     */
    public static void asyncPostWithJson(String url, Object data, HttpStringResponseCallback httpStringResponseCallback) throws Exception {
        URI uri = buildUri(url, Lists.newArrayList()); // TODO
        SimpleRequestBuilder builder = SimpleRequestBuilder.post(uri).setCharset(StandardCharsets.UTF_8).addHeader("Content-Type", APPLICATION_FORM_URLENCODED_UTF8.toString());
        if (data != null) {
            String jsonStr;
            if (data instanceof String) {
                jsonStr = String.valueOf(data);
            } else {
                jsonStr = JsonUtil.toJson(data);
            }
            builder.setBody(jsonStr, ContentType.APPLICATION_JSON);
        }
        SimpleHttpRequest request = builder.build();
        asyncHttpRequest(request, httpStringResponseCallback);
    }

    /**
     * 建构一个异步的 HttpClient
     * 
     * @return CloseableHttpAsyncClient
     */
    private static CloseableHttpAsyncClient buildAsyncClient() {
//        Header header = new BasicHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
        RequestConfig requestConfig = buildLongKeepRequestConfig();
        AsyncClientConnectionManager connectionManager = buildHttpAsyncClientConnectionManager();
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
//                .setDefaultHeaders(Collections.singleton(header))
                .setConnectionManager(connectionManager).evictExpiredConnections().setDefaultRequestConfig(requestConfig).build();
        httpclient.start();
        return httpclient;
    }

    /**
     * 建构一个异步的连接管理器
     * 
     * @return AsyncClientConnectionManager
     */
    private static AsyncClientConnectionManager buildHttpAsyncClientConnectionManager() {
        return PoolingAsyncClientConnectionManagerBuilder.create().setValidateAfterInactivity(TimeValue.of(30, TimeUnit.SECONDS))//// 30 秒清理无效连接
                .setMaxConnTotal(50)// 总连接数
                .setMaxConnPerRoute(100)//// 单个连接的并发数量
                .build();

    }

    /**
     * 建构一个同步的 HttpClient
     * 
     * @return CloseableHttpClient
     */
    private static CloseableHttpClient buildHttpClient() {
//        Header header = new BasicHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
        RequestConfig requestConfig = buildRequestConfig();
        HttpClientConnectionManager httpClientConnectionManager = buildHttpClientConnectionManager();
        return HttpClients.custom().setConnectionManager(httpClientConnectionManager).evictExpiredConnections().setDefaultRequestConfig(requestConfig)
//                .setDefaultHeaders(Collections.singleton(header))
                .build();
    }

    /**
     * 建构一个连接管理器
     * 
     * @return HttpClientConnectionManager
     */
    private static HttpClientConnectionManager buildHttpClientConnectionManager() {
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        connectionManagerBuilder.setValidateAfterInactivity(TimeValue.of(30, TimeUnit.SECONDS))// 30 秒清理无效连接
                .setMaxConnTotal(50) // 总连接数
                .setMaxConnPerRoute(50);// 单个连接的并发数量
        SSLContext sslContext = null;
        try {
            sslContext = SSLContextBuilder.create().build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
            connectionManagerBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error("buildHttpsClientConnectionManager error.", e);
        }
        return connectionManagerBuilder.build();

    }

    /**
     * 建构一个长时间连接的请求配置
     * 
     * @return RequestConfig
     */
    private static RequestConfig buildLongKeepRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(35000, TimeUnit.MILLISECONDS)// 连接主机服务超时时间
                .setConnectionRequestTimeout(35000, TimeUnit.MILLISECONDS)// 请求超时时间
                .setResponseTimeout(60000, TimeUnit.MILLISECONDS)// 读取超时
                .build();
    }

    /**
     * 建构一个请求配置
     * 
     * @return RequestConfig
     */
    private static RequestConfig buildRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(35000, TimeUnit.MILLISECONDS)// 连接主机服务超时时间
                .setConnectionRequestTimeout(35000, TimeUnit.MILLISECONDS)// 请求超时时间
                .setResponseTimeout(60000, TimeUnit.MILLISECONDS)// 读取超时
                .setDefaultKeepAlive(30, TimeUnit.SECONDS)// 如果空闲保持过长，不知后台顶不顶得住。实际上有些业务可以一直保持长连接
                .build();
    }

    /**
     * 建构一个 URI
     * 
     * @param url    String uri
     * @param params uri 参数
     * @return URI
     * @throws URISyntaxException exception
     */
    private static URI buildUri(String url, List<NameValuePair> params) throws URISyntaxException {
        URI uri;
        URIBuilder uriBuilder = new URIBuilder(url);
        uriBuilder.setCharset(StandardCharsets.UTF_8);

        if (params != null && !params.isEmpty()) {
            uriBuilder.addParameters(params);
        }

        uri = uriBuilder.build();
        return uri;
    }

    /**
     * 建构一个 URI
     * 
     * @param url   String uri
     * @param param uri 参数
     * @return URI
     * @throws URISyntaxException exception
     */
    private static URI buildUri(String url, Map<String, Object> param) throws URISyntaxException {
        URI uri;
        URIBuilder uriBuilder = new URIBuilder(url);
        uriBuilder.setCharset(StandardCharsets.UTF_8);
        if (param != null && !param.isEmpty()) {
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                String key = entry.getKey();
                String value = String.valueOf(entry.getValue());
                uriBuilder.addParameter(key, value);
            }
        }
        uri = uriBuilder.build();
        return uri;
    }

    /**
     * 通过 http get 获取数据
     * 
     * @param url Url
     * @return String 字符串
     * @throws Exception exception
     */
    public static String get(String url) throws Exception {
        return get(url, null);
    }

    /**
     * 通过 http get 获取数据
     * 
     * @param url    资源 url
     * @param params 参数
     * @return String 字符串
     * @throws Exception exception
     */
    public static String get(String url, List<NameValuePair> params) throws Exception {
        URI uri = buildUri(url, params);
        HttpGet httpGet = new HttpGet(uri);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("do get[{}] error", url, e);
            throw e;
        }

    }

    /**
     * 获取 simple 中的字符串，为什么要重写，因为后台不大规范，不指明 content-type 的字符编码，默认用 utf-8
     * 
     * @param simpleBody SimpleBody
     * @return String
     */
    private static String getBodyText(SimpleBody simpleBody) {
        if (simpleBody.isBytes()) {
            ContentType contentType = simpleBody.getContentType();
            final Charset charset = (contentType != null ? contentType : ContentType.DEFAULT_TEXT).getCharset();
            return new String(simpleBody.getBodyBytes(), charset != null ? charset : StandardCharsets.UTF_8);
        } else {
            return simpleBody.getBodyText();
        }
    }

    /**
     * 通过 post 发送数据
     * 
     * @param url      Url
     * @param paramMap 表单参数
     * @return String 字符串
     * @throws Exception exception
     */
    public static String post(String url, Map<String, Object> paramMap) throws Exception {
        return post(url, null, paramMap);
    }

    /**
     * 同步的 http post 请求
     * 
     * @param url     URL
     * @param headers 请求头参数
     * @param param   表达参数
     * @return String 字符串数据
     * @throws Exception exception
     */
    public static String post(String url, Map<String, String> headers, Map<String, Object> param) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        if (param != null) {
            List<NameValuePair> pairs = new ArrayList<>();
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
        }
        httpPost.addHeader("Content-Type", APPLICATION_FORM_URLENCODED_UTF8);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry);
            }
        }
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            EntityUtils.consume(entity);
            return result;
        } catch (IOException | ParseException e) {
            logger.error("doPost[{}] error.", url, e);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送 http 请求， 以 json 的形式
     * 
     * @param url  url
     * @param data 要发送的数据
     * @return string
     * @throws Exception exception
     */
    public static String postWithJson(String url, Object data) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON);
        String jsonStr;
        if (data instanceof String) {
            jsonStr = String.valueOf(data);
        } else {
            jsonStr = JsonUtil.toJson(data);
        }
        httpPost.setEntity(new StringEntity(jsonStr, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("do post[{}] json error", url, e);
            throw e;
        }
    }

}