package com.wosloveslife.dao.store;

import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by leonard on 17/6/17.
 */

public class BaseStore {
    public static Observable<Realm> getRealm() {
        return Observable.create(new Observable.OnSubscribe<Realm>() {
            @Override
            public void call(Subscriber<? super Realm> subscriber) {
                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.beginTransaction();
                    subscriber.onNext(realm);
                    if (realm.isInTransaction()) {
                        realm.commitTransaction();
                    }
                } finally {
                    if (realm.isInTransaction()) {
                        realm.cancelTransaction();
                    }
                }
                subscriber.onCompleted();
            }
        });
    }

    public static Observable<Realm> getRealmTx() {
        return Observable.create(new Observable.OnSubscribe<Realm>() {
            @Override
            public void call(Subscriber<? super Realm> subscriber) {
                subscriber.onNext(Realm.getDefaultInstance());
                subscriber.onCompleted();
            }
        });
    }
}
