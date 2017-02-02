package com.wosloveslife.fantasy.presenter;

import android.content.Context;

import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.baidu.BaiduMusic;
import com.wosloveslife.fantasy.baidu.BaiduSearchMusic;
import com.wosloveslife.fantasy.net.ApiManager;

import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
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
    public void searchLrc(String query, final Scheduler scheduler, final Subscriber<BaiduLrc> subscriber) {
        searchMusic(query).subscribe(new SubscriberAdapter<BaiduSearchMusic>() {
            @Override
            public void onNext(BaiduSearchMusic baiduSearchMusic) {
                super.onNext(baiduSearchMusic);
                if (baiduSearchMusic != null) {
                    List<BaiduMusic> song = baiduSearchMusic.getSong();
                    if (song != null && song.size() > 0) {
                        getLrc(baiduSearchMusic.getSong().get(0).getSongid())
                                .observeOn(scheduler)
                                .subscribe(subscriber);
                    } else {
                        subscriber.onNext(null);
                    }
                } else {
                    subscriber.onNext(null);
                }
                subscriber.onCompleted();
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
