package com.wosloveslife.fantasy.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.interfaces.IPlay;

/**
 * Created by zhangh on 2017/6/19.
 */

public class PlayerController implements IPlay {
    @Nullable
    private PlayService.PlayBinder mPlayBinder;
    private static PlayerController sPlayerController;
    private Context mContext;

    private PlayerController() {
    }

    public static PlayerController getInstance() {
        if (sPlayerController == null) {
            synchronized (PlayerController.class) {
                if (sPlayerController == null) {
                    sPlayerController = new PlayerController();
                }
            }
        }
        return sPlayerController;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void registerPlayService() {
        mContext.bindService(new Intent(mContext, PlayService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unregisterPlayService() {
        mContext.unbindService(mServiceConnection);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /** bindService()方法执行后, 绑定成功时回调 */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d("连接到播放服务");
            mPlayBinder = (PlayService.PlayBinder) service;
        }

        /** 和服务断开连接后回调(比如unbindService()方法执行后) */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d("和服务断开连接");
        }
    };

    public interface OnBind2PlayServiceListener {
        void onBind(PlayService.PlayBinder player);
    }

    //==============================================================================================

    @Override
    public void play(Audio audio) {
        if (mPlayBinder != null) {
            mPlayBinder.play(audio);
        }
    }

    @Override
    public void pause() {
        if (mPlayBinder != null) {
            mPlayBinder.pause();
        }
    }

    @Override
    public void next() {
        if (mPlayBinder != null) {
            mPlayBinder.next();
        }
    }

    @Override
    public void previous() {
        if (mPlayBinder != null) {
            mPlayBinder.previous();
        }
    }

    @Override
    public void seekTo(long progress) {
        if (mPlayBinder != null) {
            mPlayBinder.seekTo(progress);
        }
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferState() {
        return 0;
    }

    @Override
    public void addListener(ExoPlayer.EventListener listener) {
        if (mPlayBinder != null) {
            mPlayBinder.addListener(listener);
        }
    }

}
