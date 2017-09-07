package com.wosloveslife.fantasy.v2.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.services.PlayService;
import com.yesing.blibrary_wos.utils.systemUtils.SystemServiceUtils;

/**
 * Created by zhangh on 2017/9/3.
 */

public class Controller implements IPlayer {
    private static Controller sController;
    private final State mState;
    private Context mContext;

    private Controller() {
        mState = new State();
    }

    public static Controller getInstance() {
        if (sController == null) {
            synchronized (Controller.class) {
                if (sController == null) {
                    sController = new Controller();
                }
            }
        }
        return sController;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        if (!SystemServiceUtils.isServiceRunning(context, PlayService.class.getName())) {
            bindService();
        }
    }

    public void onAppStop() {

    }

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
    public void seekTo(long progress) {
        if (mPlayBinder != null) {
            mPlayBinder.seekTo(progress);
        }
    }

    @Override
    public State getState() {
        return mState;
    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    private PlayService.PlayBinder mPlayBinder;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /** bindService()方法执行后, 绑定成功时回调 */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d("连接到播放服务");
            mPlayBinder = (PlayService.PlayBinder) service;
            mState.setPlayBinder((PlayService.PlayBinder) service);

            mPlayBinder.addListener(new ExoPlayerEventListenerAdapter() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    super.onPlayerStateChanged(playWhenReady, playbackState);


                    if (getState().isPlaying()) {
                        mPlayBinder.play(MusicManager.getInstance().getMusicConfig().mCurrentMusic);
                    } else if (mPlayBinder.isPlaying()) {
                        mPlayBinder.pause();
                    }
                }
            });
        }

        /** 和服务断开连接后回调(比如unbindService()方法执行后) */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d("和服务断开连接");
        }
    };

    private void bindService() {
        mContext.bindService(new Intent(mContext, PlayService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        mContext.unbindService(mServiceConnection);
    }
}
