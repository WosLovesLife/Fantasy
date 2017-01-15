package com.wosloveslife.fantasy.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wosloveslife.fantasy.interfaces.IPlay;

/**
 * Created by zhangh on 2017/1/15.
 */

public class PlayerServices extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    private void init() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MBinder();
    }

    class MBinder extends Binder implements IPlay{

        @Override
        public void play() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void next() {

        }

        @Override
        public void previous() {

        }

        @Override
        public void setProgress(int progress) {

        }
    }
}
