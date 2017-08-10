package com.wosloveslife.fantasy.adapter;

import com.yesing.blibrary_wos.utils.assist.WLogger;

import rx.Subscriber;

/**
 * Created by zhangh on 2017/2/1.
 */

public class SubscriberAdapter<T> extends Subscriber<T> {
    @Override
    public void onCompleted() {
        WLogger.d("onCompleted :  ");
    }

    @Override
    public void onError(Throwable e) {
        WLogger.w("SubscriberAdapter onError : ", e);
    }

    @Override
    public void onNext(T t) {
        WLogger.d("onNext : t = " + t);
    }
}
