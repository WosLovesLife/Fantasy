package com.wosloveslife.fantasy.net;

import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.baidu.BaiduSearchMusic;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zhangh on 2017/2/14.
 */

public class BaiduApi {
    private static BaiduApi sBaiduApi;
    private final Object mSyncBlock = new Object();

    public static final String HOST = "http://tingapi.ting.baidu.com/";
    public static final String SEARCH_METHOD = "baidu.ting.search.catalogSug";
    public static final String LRC_METHOD = "baidu.ting.song.lry";

    private BaiduApi() {
    }

    public static BaiduApi getInstance() {
        if (sBaiduApi == null) {
            synchronized (ApiManager.class) {
                if (sBaiduApi == null) {
                    sBaiduApi = new BaiduApi();
                }
            }
        }
        return sBaiduApi;
    }

    public Retrofit createBase() {
        return new Retrofit.Builder()
                .baseUrl(HOST)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(ApiManager.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    BaiduSearchMusic.BaiduSearchMusicApi mBaiduSearchMusicApi;
    BaiduLrc.BaiduLrcApi mBaiduLrcApi;

    public BaiduSearchMusic.BaiduSearchMusicApi getBaiduSearchMusicApi() {
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
