package com.wosloveslife.fantasy.net;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.App;
import com.wosloveslife.fantasy.utils.NetWorkUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhangh on 2017/2/2.
 */

public class ApiManager {
    private static ApiManager sApiManager;

    //======================================基础网络配置 - start====================================
    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetWorkUtil.isNetWorkAvailable(App.getAppContent())) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                Logger.d("no network");
            }

            Response originalResponse = chain.proceed(request);

            if (NetWorkUtil.isNetWorkAvailable(App.getAppContent())) {
                int maxAge = 60; // 在线缓存在1分钟内可读取
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 * 60 * 24 * 28; // 离线时缓存保存4周
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }
    };
    private static File httpCacheDirectory = new File(App.getAppContent().getCacheDir(), "OnlineRetailer");
    private static int cacheSize = 20 * 1024 * 1024; // 10 MiB
    private static Cache cache = new Cache(httpCacheDirectory, cacheSize);
    private OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
            .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();
    //=====================================基础网络配置 - end=======================================

    private ApiManager() {
    }

    public static ApiManager getInstance() {
        if (sApiManager == null) {
            synchronized (ApiManager.class) {
                if (sApiManager == null) {
                    sApiManager = new ApiManager();
                }
            }
        }
        return sApiManager;
    }

    OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public BaiduApi getBaiduApi() {
        return BaiduApi.getInstance();
    }

    public XiamiApi getXiamiApi() {
        return XiamiApi.getInstance();
    }
}
