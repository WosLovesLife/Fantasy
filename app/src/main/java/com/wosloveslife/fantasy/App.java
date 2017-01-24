package com.wosloveslife.fantasy;

import android.app.Application;
import android.content.Intent;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.PlayService;

/**
 * Created by zhangh on 2017/1/2.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initKits();

        initManager();

        Intent intent = new Intent(this, PlayService.class);
        startService(intent);
    }

    private void initKits() {
        Logger.init("Fantasy");
    }

    private void initManager() {
        SPHelper.getInstance().init(this);
        MusicManager.getInstance().init(this);
    }
}
