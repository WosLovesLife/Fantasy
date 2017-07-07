package com.wosloveslife.fantasy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.gson.Gson;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.broadcast.AudioBroadcast;
import com.wosloveslife.fantasy.broadcast.BroadcastManager;
import com.wosloveslife.fantasy.helper.AudioHelper;
import com.wosloveslife.fantasy.helper.NotificationHelper;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.interfaces.IPlay;
import com.wosloveslife.fantasy.manager.SettingConfig;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;

/**
 * Created by zhangh on 2017/1/17.
 */

public class PlayService extends Service {
    //=============常量
    //------循环规则
    /** 列表循环 */
    public static final int PLAY_ORDER_LIST = 0;
    /** 单曲循环 */
    public static final int PLAY_ORDER_CIRCLE = 1;
    /** 列表单次 */
    public static final int PLAY_ORDER_ONCE = 2;

    //=============
    Context mContext;

    //=============ExoPlayer相关
    private SimpleExoPlayer mPlayer;

    private NotificationHelper mNotificationHelper;
    private AudioHelper mAudioHelper;
    private PlayerEngine mPlayerEngine;
    private MusicManager mMusicManager;

    public PlayService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public class PlayBinder extends Binder implements IPlay {
        //========================================播放相关-start====================================
        public void play(Audio audio) {
            if (audio == null) {
                PlayService.this.play();
            } else {
                PlayService.this.play(audio);
            }
        }

        public void togglePlayOrPause() {
            PlayService.this.togglePlayOrPause();
        }

        @Override
        public void pause() {
            PlayService.this.pause();
        }

        @Override
        public void next() {
            PlayService.this.next();
        }

        @Override
        public void previous() {
            PlayService.this.previous();
        }

        /**
         * 跳转进度
         */
        @Override
        public void seekTo(long progress) {
            PlayService.this.setProgress(progress);
        }

        //========================================播放相关-end======================================

        public boolean isPlaying() {
            return PlayService.this.isPlaying();
        }

        @Override
        public int getBufferState() {
            return PlayService.this.getBufferState();
        }

        @Override
        public void addListener(ExoPlayer.EventListener listener) {
            PlayService.this.addListener(listener);
        }

        public long getCurrentPosition() {
            return PlayService.this.getCurrentPosition();
        }

        public long getDuration() {
            return PlayService.this.getDuration();
        }

        public long getBufferedPosition() {
            return PlayService.this.getBufferedPosition();
        }

        public int getPlaybackState() {
            return PlayService.this.getPlaybackState();
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
        mMusicManager.getMusicConfig().mCurrentMusic = audio;
        if (audio != null) {
            /* 这里有一个未知的Bug，调用过ExoPlayer.seekTo()方法后,跳转歌曲的一瞬间进度会闪烁一下,
             * 导致歌词控件同步也会迅速滚动歌词一下, 因此这里在播放一首歌之前先将之前的歌的进度归零 */
            mPlayer.seekTo(0);
            if (mPlayerEngine.prepare(audio.path)) {
                play();
                mMusicManager.addRecent(audio);
                SPHelper.getInstance().save("current_music", new Gson().toJson(audio));
            } else {
                pause();
            }
        } else {
            pause();
        }
    }

    /**
     * 切换播放/暂停两种状态.
     */
    public void togglePlayOrPause() {
        if (mPlayer.getPlayWhenReady()) {
            pause();
        } else {
            play();
        }
    }

    /**
     * 开始播放
     */
    public void play() {
        if (mMusicManager.getMusicConfig().mCurrentMusic != null) {
            /* 如果有未处理的异常,则重置播放并将进度保持一致*/
            if (mEncounteredException != null) {
                mEncounteredException = null;
                long currentPosition = mPlayer.getCurrentPosition();
                mPlayerEngine.prepare(mMusicManager.getMusicConfig().mCurrentMusic.path);
                mPlayer.seekTo(currentPosition);
            }
            mAudioHelper.registerAudioFocus();
            mPlayer.setPlayWhenReady(true);
            notifyNotification();
        } else {
            next();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mMusicManager.getMusicConfig().mCurrentMusic != null) {
            mAudioHelper.abandonAudioFocus();
            mPlayer.setPlayWhenReady(false);
            notifyNotification();
        }
    }

    /**
     * 播放下一首<br/>
     * 如果下一首没有了,则获取第1首歌曲<br/>
     * 如果第一首歌也没有了,则抛出异常
     */
    public void next() {
        Audio next = null;
        switch (SettingConfig.getPlayOrder()) {
            case SettingConfig.PlayOrder.SUCCESSIVE:
                next = mMusicManager.getMusicConfig().getNext(mMusicManager.getMusicConfig().mCurrentMusic);
                if (next == null) {
                    next = mMusicManager.getMusicConfig().getFirst();
                }
                break;
            case SettingConfig.PlayOrder.ONE:
                next = mMusicManager.getMusicConfig().mCurrentMusic;
                break;
            case SettingConfig.PlayOrder.RANDOM:
                int nextInt = new Random().nextInt(mMusicManager.getMusicConfig().getMusicCount());
                WLogger.d("next : nextIndex = " + nextInt);
                next = mMusicManager.getMusicConfig().getMusic(nextInt);
                break;
        }

        if (next == null) {
            pause();
            return;
        }

        play(next);
    }

    /**
     * 播放上一首<br/>
     * 如果上一首没有了,则获取最后一首歌曲<br/>
     * 如果最后一首歌也没有了,则抛出异常
     */
    public void previous() {
        Audio previous = mMusicManager.getMusicConfig().getPrevious(mMusicManager.getMusicConfig().mCurrentMusic);
        /* 如果下一首没有了,则获取第1首歌曲 */
        if (previous == null) {
            previous = mMusicManager.getMusicConfig().getLast();
        }

        /* 如果第一首歌也没有了,则说明发生了异常状况 */
        if (previous == null) {
            pause();
            return;
        }

        play(previous);
    }

    /**
     * 跳转进度
     *
     * @param progress 0~100
     */
    public void setProgress(long progress) {
        mPlayer.seekTo(progress);
    }

    //========================================播放相关-end======================================

    public boolean isPlaying() {
        return mPlayer.getPlayWhenReady();
    }

    public int getBufferState() {
        return 0;
    }

    public void addListener(ExoPlayer.EventListener listener) {
        mPlayer.addListener(listener);
    }

    private long getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    private long getDuration() {
        return mPlayer.getDuration();
    }

    private long getBufferedPosition() {
        return mPlayer.getBufferedPosition();
    }

    private int getPlaybackState() {
        return mPlayer.getPlaybackState();
    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    Throwable mEncounteredException;

    /** 只在服务第一次创建时调用 */
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        mMusicManager = MusicManager.getInstance();

        mPlayerEngine = new PlayerEngine(this);
        mPlayer = mPlayerEngine.getPlayer();

        mAudioHelper = new AudioHelper(this);

        mNotificationHelper = new NotificationHelper(this);

        mPlayer.addListener(new ExoPlayerEventListenerAdapter() {

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                /* 播放中断 TODO ExoPlaybackException cause by HttpDataSourceException
                  * 在这里记录播放状态, 监听网络变化,弹出网络提示, 如果网络恢复并且之前的状态是播放, 则继续播放 */
                super.onPlayerError(error);
                mEncounteredException = error;
                pause();
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if ((playbackState == ExoPlayer.STATE_ENDED || !playWhenReady) && SettingConfig.isCountdown()) {
                    pause();
                    SettingConfig.saveCountdownTime(0);
                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    next();
                }
            }
        });

        String currentMusic = SPHelper.getInstance().get("current_music", "");
        if (!TextUtils.isEmpty(currentMusic)) {
            Audio audio = new Gson().fromJson(currentMusic, Audio.class);
            mMusicManager.getMusicConfig().mCurrentMusic = audio;
            mPlayerEngine.prepare(audio.path);
        }

        EventBus.getDefault().register(this);
        BroadcastManager.getInstance().init(this);
    }

    private void notifyNotification() {
        mNotificationHelper.update(isPlaying(), mMusicManager.getMusicConfig().mCurrentMusic);
    }

    /** 每次调用startService()启用该服务时都被调用 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getIntExtra("action", -1)) {
            case 0: // 上一曲
                previous();
                break;
            case 1: // 播放/暂停
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
                break;
            case 2: // 下一曲
                next();
                break;
            case 3: // 收藏
                if (mMusicManager.isFavored(mMusicManager.getMusicConfig().mCurrentMusic)) {
                    mMusicManager.removeFavor(mMusicManager.getMusicConfig().mCurrentMusic);
                } else {
                    mMusicManager.addFavor(mMusicManager.getMusicConfig().mCurrentMusic);
                }
                break;
            case 4: // 桌面歌词
                Toaster.showShort("歌词");
                break;
        }

        // 如果设定了定时并且定时到点 就关闭倒计时
        if (SettingConfig.getCountdownTime() > 0 && SettingConfig.isCountdown() && (!SettingConfig.isCloseAfterPlayEnd() || !mPlayer.getPlayWhenReady())) {
            SettingConfig.saveCountdownTime(0);
        }

        return super.onStartCommand(intent, flags, startId);
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
        EventBus.getDefault().unregister(this);
        BroadcastManager.getInstance().unregisterAllBroadcasts();
        if (mPlayer != null) {
            pause();
            mPlayer.release();
        }
        mAudioHelper.abandonAudioFocus();
    }

    //==========================================事件处理============================================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBroadcastEvent(AudioBroadcast.AudioNoisyEvent event) {
        pause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    // TODO: 2017/7/1 如果到了定时时间,就关闭播放
    public void onStopEvent() {
        pause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAddMusic(MusicManager.OnAddMusic event) {
        if (event == null || event.mMusic == null || !mNotificationHelper.isShown()) return;
        if (event.mMusic.equals(mMusicManager.getMusicConfig().mCurrentMusic)) {
            mNotificationHelper.update(isPlaying(), mMusicManager.getMusicConfig().mCurrentMusic);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoveMusic(MusicManager.OnRemoveMusic event) {
        if (event == null || event.mMusic == null || !mNotificationHelper.isShown()) return;
        if (event.mMusic.equals(mMusicManager.getMusicConfig().mCurrentMusic)) {
            mNotificationHelper.update(isPlaying(), mMusicManager.getMusicConfig().mCurrentMusic);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicChanged(MusicManager.OnMusicChanged event) {
        if (event == null || event.mMusic == null || !mNotificationHelper.isShown()) return;
        if (event.mMusic.equals(mMusicManager.getMusicConfig().mCurrentMusic)) {
            mNotificationHelper.update(isPlaying(), mMusicManager.getMusicConfig().mCurrentMusic);
        }
    }
}
