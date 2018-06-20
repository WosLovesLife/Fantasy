package com.wosloveslife.fantasy;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Migration;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.manager.CustomConfiguration;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.manager.PlayerController;
import com.wosloveslife.fantasy.services.PlayService;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import io.realm.Realm;
import io.realm.RealmConfiguration;

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

        initPlayerController();

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
        CustomConfiguration.init(this);
        MusicManager.Companion.getInstance().init(this);
    }

    private void initPlayerController() {
        PlayerController.Companion.init(this);
    }

    public static Context getAppContent() { // todo rename to getAppContext
        return sContext;
    }
}
