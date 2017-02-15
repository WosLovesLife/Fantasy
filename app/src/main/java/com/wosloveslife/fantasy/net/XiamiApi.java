package com.wosloveslife.fantasy.net;

import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.bean.xiami.XiamiMusic;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zhangh on 2017/2/14.
 */
@Deprecated
public class XiamiApi {
    private static XiamiApi sXiamiApi;
    private final Object mSyncBlock = new Object();

//    public static final String HOST = "http://gw.api.taobao.com/";  // 正式地址不能用错误码11没有权限
    public static final String HOST = "http://gw.api.tbsandbox.com/"; // 沙箱模式地址 但是可以获取到数据.
    public static final String SEARCH_METHOD = "alibaba.xiami.api.search.summary.get";
    public static final String LRC_METHOD = "baidu.ting.song.lry";

    public static final String APP_KEY = "23634366";
    public static final String APP_SECRET = "e1194c4b4a97d688473b6f3d0ef8876e";
    public static final String SIGN_METHOD = "hmac";
    public static final String V = "2.0";
    public static final String PARTNER_ID = "top-apitools";
    public static final String FORMAT = "json";
    public static final String FORCE_SENSITIVE_PARAM_FUZZY = "true";

    private XiamiApi() {
    }

    public static XiamiApi getInstance() {
        if (sXiamiApi == null) {
            synchronized (ApiManager.class) {
                if (sXiamiApi == null) {
                    sXiamiApi = new XiamiApi();
                }
            }
        }
        return sXiamiApi;
    }

    public Retrofit createBase() {
        return new Retrofit.Builder()
                .baseUrl(HOST)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(ApiManager.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    XiamiMusic.SearchApi mXiamiMusicSearchApi;
    BaiduLrc.BaiduLrcApi mBaiduLrcApi;

    public XiamiMusic.SearchApi getXiamiMusicSearchApi() {
        if (mXiamiMusicSearchApi == null) {
            synchronized (mSyncBlock) {
                if (mXiamiMusicSearchApi == null) {
                    mXiamiMusicSearchApi = createBase().create(XiamiMusic.SearchApi.class);
                }
            }
        }
        return mXiamiMusicSearchApi;
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
