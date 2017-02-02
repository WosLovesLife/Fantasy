package com.wosloveslife.fantasy.net;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.App;
import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.baidu.BaiduSearchMusic;
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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zhangh on 2017/2/2.
 */

public class ApiManager {
    private static final String BAIDU_HOST = "http://tingapi.ting.baidu.com/";
    public static final String SEARCH_METHOD = "baidu.ting.search.catalogSug";
    public static final String LRC_METHOD = "baidu.ting.song.lry";

    private static ApiManager sApiManager;

    private final Object mSyncBlock = new Object();
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

    BaiduSearchMusic.BaiduSearchMusicApi mBaiduSearchMusicApi;
    BaiduLrc.BaiduLrcApi mBaiduLrcApi;

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

    private Retrofit createBase() {
        return new Retrofit.Builder()
                .baseUrl(BAIDU_HOST)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    //============================================Apis==============================================

    public BaiduSearchMusic.BaiduSearchMusicApi getMyOrderListDataApi() {
        if (mBaiduSearchMusicApi == null) {
            synchronized (mSyncBlock) {
                if (mBaiduSearchMusicApi == null) {
                    mBaiduSearchMusicApi = createBase().create(BaiduSearchMusic.BaiduSearchMusicApi.class);
                }
            }
        }
        return mBaiduSearchMusicApi;
    }

    public BaiduLrc.BaiduLrcApi getBaiduLrcApi() {
        if (mBaiduLrcApi == null) {
            synchronized (mSyncBlock) {
                if (mBaiduLrcApi == null) {
                    mBaiduLrcApi = createBase().create(BaiduLrc.BaiduLrcApi.class);
                }
            }
        }
        return mBaiduLrcApi;
    }
}
