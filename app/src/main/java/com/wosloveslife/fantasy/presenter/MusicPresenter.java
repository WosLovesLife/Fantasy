package com.wosloveslife.fantasy.presenter;

import android.content.Context;

import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.baidu.BaiduMusic;
import com.wosloveslife.fantasy.baidu.BaiduSearchMusic;
import com.wosloveslife.fantasy.net.ApiManager;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import java.io.IOException;
import java.util.List;

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

    public Observable<BaiduSearchMusic> searchMusic(String query) {
        return ApiManager.getInstance()
                .getMyOrderListDataApi()
                .searchMusic(ApiManager.SEARCH_METHOD, query)
                .subscribeOn(Schedulers.io());
    }

    /** todo 优化代码结构,使用rx */
    public Observable<BaiduLrc> searchLrc(String query) {
        return searchMusic(query).map(new Func1<BaiduSearchMusic, BaiduLrc>() {
            @Override
            public BaiduLrc call(BaiduSearchMusic baiduSearchMusic) {
                if (baiduSearchMusic == null) return null;

                BaiduLrc baiduLrc = null;
                List<BaiduMusic> song = baiduSearchMusic.getSong();
                if (song != null && song.size() > 0) {
                    try {
                        baiduLrc = ApiManager.getInstance()
                                .getBaiduLrcApi()
                                .callSearchLrc(ApiManager.LRC_METHOD, baiduSearchMusic.getSong().get(0).getSongid())
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
                .getBaiduLrcApi()
                .searchLrc(ApiManager.LRC_METHOD, musicId)
                .subscribeOn(Schedulers.io());
    }
}
