package com.wosloveslife.fantasy.presenter;

import android.content.Context;
import android.text.format.DateFormat;

import com.wosloveslife.fantasy.baidu.BaiduAlbum;
import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.baidu.BaiduMusic;
import com.wosloveslife.fantasy.baidu.BaiduSearch;
import com.wosloveslife.fantasy.net.ApiManager;
import com.wosloveslife.fantasy.net.BaiduApi;
import com.wosloveslife.fantasy.net.XiamiApi;
import com.wosloveslife.fantasy.utils.AliUtils;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by YesingBeijing on 2016/9/26.
 */
public class MusicPresenter extends BasePresenter {

    public MusicPresenter(Context context) {
        super(context);
    }

    public Observable<BaiduSearch> searchFromBaidu(String query) {
        return ApiManager.getInstance()
                .getBaiduApi()
                .getBaiduSearchMusicApi()
                .searchMusic(BaiduApi.SEARCH_METHOD, query)
                .subscribeOn(Schedulers.io());
    }

    @Deprecated
    public Request searchMusicFromXiami(String query) {
        String timestamp = DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date()).toString();
        Map<String, String> params = new HashMap<>();
        params.put("method", XiamiApi.SEARCH_METHOD);
        params.put("app_key", XiamiApi.APP_KEY);
        params.put("sign_method", XiamiApi.SIGN_METHOD);
        params.put("v", XiamiApi.V);
        params.put("timestamp", timestamp);
        params.put("partner_id", XiamiApi.PARTNER_ID);
        params.put("format", XiamiApi.FORMAT);
        params.put("force_sensitive_param_fuzzy", XiamiApi.FORCE_SENSITIVE_PARAM_FUZZY);
        params.put("key", "赵雷");

        Request request = null;
        try {
            request = ApiManager.getInstance()
                    .getXiamiApi()
                    .getXiamiMusicSearchApi()
                    .searchMusic(XiamiApi.SEARCH_METHOD, XiamiApi.APP_KEY, XiamiApi.SIGN_METHOD,
                            XiamiApi.V, timestamp, XiamiApi.PARTNER_ID, XiamiApi.FORMAT,
                            XiamiApi.FORCE_SENSITIVE_PARAM_FUZZY, null, null, "赵雷",
                            AliUtils.signTopRequest(params, XiamiApi.APP_SECRET, "HMAC"))
                    .request();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

    /** todo 优化代码结构,使用rx */
    public Observable<BaiduLrc> searchLrc(String query) {
        return searchFromBaidu(query).map(new Func1<BaiduSearch, BaiduLrc>() {
            @Override
            public BaiduLrc call(BaiduSearch baiduSearchMusic) {
                if (baiduSearchMusic == null) return null;

                BaiduLrc baiduLrc = null;
                List<BaiduMusic> song = baiduSearchMusic.getSong();
                if (song != null && song.size() > 0) {
                    try {
                        baiduLrc = ApiManager.getInstance()
                                .getBaiduApi()
                                .getBaiduLrcApi()
                                .callSearchLrc(BaiduApi.LRC_METHOD, baiduSearchMusic.getSong().get(0).getSongid())
                                .execute()
                                .body();
                    } catch (IOException e) {
                        WLogger.w("call : 请求歌词发生错误， 可忽略 e = " + e);
                    }
                }
                return baiduLrc;
            }
        });
    }

    public Observable<BaiduLrc> getLrc(String musicId) {
        return ApiManager.getInstance()
                .getBaiduApi()
                .getBaiduLrcApi()
                .searchLrc(BaiduApi.LRC_METHOD, musicId)
                .subscribeOn(Schedulers.io());
    }

    public Observable<BaiduAlbum> searchAlbum(String query) {
        return searchFromBaidu(query).map(new Func1<BaiduSearch, BaiduAlbum>() {
            @Override
            public BaiduAlbum call(BaiduSearch baiduSearchMusic) {
                if (baiduSearchMusic == null) return null;

                BaiduAlbum baiduAlbum = null;
                List<BaiduAlbum> album = baiduSearchMusic.getAlbum();
                if (album != null && album.size() > 0) {
                    baiduAlbum = album.get(0);
                }
                return baiduAlbum;
            }
        });
    }
}
