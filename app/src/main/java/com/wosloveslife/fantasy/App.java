package com.wosloveslife.fantasy;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Migration;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.manager.SettingConfig;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.PlayService;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/1/2.
 */
public class App extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        WLogger.d("onCreate : 程序启动 时间 = " + System.currentTimeMillis());
        super.onCreate();
        sContext = this;

        initDB();

        initKits();

        initManager();

        Intent intent = new Intent(this, PlayService.class);
        startService(intent);
    }

    private void initDB() {
        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .schemaVersion(Migration.SCHEMA_VERSION)
                .migration(new Migration())
                .build());
    }

    private void initKits() {
        Logger.init("Fantasy");

        Toaster.init(this);
    }

    private void initManager() {
        SPHelper.getInstance().init(this);
        SettingConfig.init(this);
        MusicManager.getInstance().init(this);
    }

    public static void executeOnComputationThread(Subscriber<? super Object> subscriber) {
        Observable.empty()
                .observeOn(Schedulers.computation())
                .subscribe(subscriber);
    }

    public static void executeOnIoThread(Subscriber<? super Object> subscriber) {
        Observable.empty()
                .observeOn(Schedulers.io())
                .subscribe(subscriber);
    }

    public static void executeOnMainThread(Subscriber<? super Object> subscriber) {
        Observable.empty()
                .subscribeOn(Schedulers.immediate())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public static Context getAppContent() {
        return sContext;
    }
}
