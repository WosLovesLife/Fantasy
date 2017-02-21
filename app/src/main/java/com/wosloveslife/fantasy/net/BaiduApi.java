package com.wosloveslife.fantasy.net;

import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.baidu.BaiduMusicInfo;
import com.wosloveslife.fantasy.baidu.BaiduSearch;

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
    public static final String PLAY_METHOD = "baidu.ting.song.play";

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

    BaiduSearch.BaiduSearchMusicApi mBaiduSearchMusicApi;
    BaiduLrc.BaiduLrcApi mBaiduLrcApi;
    BaiduMusicInfo.BaiduMusicInfoApi mBaiduMusicInfoApi;

    public BaiduSearch.BaiduSearchMusicApi getBaiduSearchMusicApi() {
        if (mBaiduSearchMusicApi == null) {
            synchronized (mSyncBlock) {
                if (mBaiduSearchMusicApi == null) {
                    mBaiduSearchMusicApi = createBase().create(BaiduSearch.BaiduSearchMusicApi.class);
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

    public BaiduMusicInfo.BaiduMusicInfoApi getBaiduMusicInfocApi() {
        if (mBaiduMusicInfoApi == null) {
            synchronized (mSyncBlock) {
                if (mBaiduMusicInfoApi == null) {
                    mBaiduMusicInfoApi = createBase().create(BaiduMusicInfo.BaiduMusicInfoApi.class);
                }
            }
        }
        return mBaiduMusicInfoApi;
    }
}
