package com.wosloveslife.fantasy;

import android.app.Application;

import com.wosloveslife.fantasy.manager.MusicManager;

/**
 * Created by zhangh on 2017/1/2.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initManager();
    }

    private void initManager() {
        MusicManager.getInstance().init(this);
    }
}
