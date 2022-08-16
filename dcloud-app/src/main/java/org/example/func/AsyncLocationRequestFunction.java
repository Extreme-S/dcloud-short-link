package org.example.func;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.example.model.ShortLinkWideDO;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class AsyncLocationRequestFunction extends RichAsyncFunction<ShortLinkWideDO, String> {

    private static final String IP_PARSE_URL = "https://restapi.amap.com/v3/ip?ip=%s&output=json&key=5af1ef5456407cd8ae5fa8405272d5ec";

    private CloseableHttpAsyncClient httpAsyncClient;

    @Override
    public void open(Configuration parameters) throws Exception {
        this.httpAsyncClient = createAsyncHttpClient();
    }

    @Override
    public void close() throws Exception {
        if (httpAsyncClient != null) {
            httpAsyncClient.close();
        }
    }

    @Override
    public void asyncInvoke(ShortLinkWideDO shortLinkWideDO, ResultFuture<String> resultFuture) throws Exception {
        //获取http的get请求
        String url = String.format(IP_PARSE_URL, shortLinkWideDO.getIp());
        HttpGet httpGet = new HttpGet(url);
        Future<HttpResponse> future = httpAsyncClient.execute(httpGet, null);

        CompletableFuture<ShortLinkWideDO> completableFuture = CompletableFuture.supplyAsync(new Supplier<ShortLinkWideDO>() {
            @Override
            public ShortLinkWideDO get() {
                try {
                    HttpResponse response = future.get();
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                        String result = EntityUtils.toString(entity, "UTF-8");
                        JSONObject locationObj = JSON.parseObject(result);
                        shortLinkWideDO.setProvince(locationObj.getString("province"));
                        shortLinkWideDO.setCity(locationObj.getString("city"));
                        return shortLinkWideDO;
                    }
                } catch (InterruptedException | ExecutionException | IOException e) {
                    log.error("ip解析错误,value={},msg={}", shortLinkWideDO, e.getMessage());
                }
                return null;
            }
        });

        completableFuture.thenAccept(new Consumer<ShortLinkWideDO>() {
            @Override
            public void accept(ShortLinkWideDO shortLinkWideDO) {
                resultFuture.complete(Collections.singleton(JSON.toJSONString(shortLinkWideDO)));
            }
        });

//        completableFuture.thenAccept( (dbResult) -> {
//            resultFuture.complete(Collections.singleton(JSON.toJSONString(shortLinkWideDO)));
//        });


    }

    private CloseableHttpAsyncClient createAsyncHttpClient() {
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(20000)            //返回数据的超时时间
                    .setConnectTimeout(10000)           //连接上服务器的超时时间
                    .setConnectionRequestTimeout(1000)  //从连接池中获取连接的超时时间
                    .build();

            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
            PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
            connManager.setMaxTotal(500);               //设置连接池最大是500个连接
            connManager.setDefaultMaxPerRoute(300);     //MaxPerRoute是对maxTotal的细分，每个主机的并发最大是300，route是指域名

            CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
                    .setConnectionManager(connManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            httpClient.start();
            return httpClient;

        } catch (IOReactorException e) {
            log.error("初始化 CloseableHttpAsyncClient异常:{}", e.getMessage());
            return null;
        }

    }
}
