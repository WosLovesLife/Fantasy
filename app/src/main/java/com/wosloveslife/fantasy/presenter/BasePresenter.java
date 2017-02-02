package com.wosloveslife.fantasy.presenter;

import android.content.Context;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * 基础Presenter类
 * Created by YesingBeijing on 2016/9/14.
 */
public class BasePresenter {
    private CompositeSubscription mCompositeSubscription;

    protected Context mContext;

    public BasePresenter(Context context) {
        mContext = context;
    }

    public void addSubscription(Subscription subscription) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }

        mCompositeSubscription.add(subscription);
    }

    public void unsubscrible() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
    }
}
