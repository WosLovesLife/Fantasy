package com.wosloveslife.fantasy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.helper.AudioHelper;
import com.wosloveslife.fantasy.helper.NotificationHelper;
import com.wosloveslife.fantasy.interfaces.IPlay;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.wosloveslife.fantasy.v2.player.Controller;
import com.wosloveslife.fantasy.v2.player.PlayerException;

/**
 * Created by zhangh on 2017/1/17.
 */

public class PlayService extends Service {

    //=============
    Context mContext;

    //=============ExoPlayer相关
    private SimpleExoPlayer mPlayer;

    private NotificationHelper mNotificationHelper;
    private AudioHelper mAudioHelper;
    private PlayerEngine mPlayerEngine;

    public PlayService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public class PlayBinder extends Binder implements IPlay {
        public void play(Audio audio) {
            PlayService.this.play(audio);
        }

        @Override
        public void pause() {
            PlayService.this.pause();
        }

        /**
         * 跳转进度
         */
        @Override
        public void seekTo(long progress) {
            PlayService.this.seekTo(progress);
        }

        public SimpleExoPlayer getExoPlayer() {
            return PlayService.this.getExoPlayer();
        }
    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    //========================================播放相关-start====================================

    /**
     * 播放给定的某一首歌曲资源<br/>
     * 将歌曲资源序列化存储本地<br/>
     * 如果资源为null,尝试暂停现有播放<br/>
     *
     * @param audio 要播放的歌曲资源
     */
    public void play(Audio audio) {
        MusicManager.getInstance().getMusicConfig().mCurrentMusic = audio;
        if (audio != null) {
            /* 这里有一个未知的Bug，调用过ExoPlayer.seekTo()方法后,跳转歌曲的一瞬间进度会闪烁一下,
             * 导致歌词控件同步也会迅速滚动歌词一下, 因此这里在播放一首歌之前先将之前的歌的进度归零 */
            mPlayer.seekTo(0);
            if (mPlayerEngine.prepare(audio.getPath())) {
                play();
            } else {
                pause();
            }
        } else {
            pause();
        }
    }

    /**
     * 开始播放
     */
    public void play() {
        Audio currentMusic = MusicManager.getInstance().getMusicConfig().mCurrentMusic;
        if (currentMusic != null) {
            mAudioHelper.registerAudioFocus();
            mPlayer.setPlayWhenReady(true);
            notifyNotification();
        } else {
            Controller.getInstance().getState().badPlay(new PlayerException(PlayerException.ErrorCode.MUSIC_IS_NULL, "Music must not be null"));
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (MusicManager.getInstance().getMusicConfig().mCurrentMusic != null) {
            mAudioHelper.abandonAudioFocus();
            mPlayer.setPlayWhenReady(false);
            notifyNotification();
        }
    }

    /**
     * 跳转进度
     *
     * @param progress 0~100
     */
    public void seekTo(long progress) {
        mPlayer.seekTo(progress);
    }

    //========================================播放相关-end======================================

    public SimpleExoPlayer getExoPlayer() {
        return mPlayer;
    }

    public boolean isPlaying() {
        return mPlayer.getPlayWhenReady();
    }

    public void addListener(ExoPlayer.EventListener listener) {
        mPlayer.addListener(listener);
    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    /** 只在服务第一次创建时调用 */
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        mPlayerEngine = new PlayerEngine(this);
        mPlayer = mPlayerEngine.getPlayer();

        mAudioHelper = new AudioHelper(this);

        mNotificationHelper = new NotificationHelper(this);
    }

    private void notifyNotification() {
        mNotificationHelper.update(isPlaying(), MusicManager.getInstance().getMusicConfig().mCurrentMusic);
    }

    /**
     * 后台强制结束程序,会执行该回调. 因此可以在该生命周期中执行善后工作
     * 但是注意: 某些意外情况,例如在软件管理中强行结束应用, 不会走任何回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        if (mPlayer != null) {
            resetPlayService();
            mPlayer.release();
        }
        mAudioHelper.abandonAudioFocus();
    }

    private void resetPlayService() {
        pause();
    }
}
