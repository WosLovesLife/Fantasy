package com.wosloveslife.fantasy;

import android.app.Application;
import android.content.Intent;

import com.wosloveslife.fantasy.manager.MusicManager;

/**
 * Created by zhangh on 2017/1/2.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initManager();

        Intent intent = new Intent(this, PlayService.class);
        startService(intent);
    }

    private void initManager() {
        MusicManager.getInstance().init(this);
    }
}
