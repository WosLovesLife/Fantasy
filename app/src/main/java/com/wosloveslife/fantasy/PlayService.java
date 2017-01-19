package com.wosloveslife.fantasy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.interfaces.IPlay;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
    private DefaultBandwidthMeter mBandwidthMeter;
    private DataSource.Factory mDataSourceFactory;
    private ExtractorsFactory mExtractorsFactory;

    //============数据
    BMusic mCurrentMusic;

    //============变量
    public int mPlayOrder;

    List<PlayStateListener> mPlayStateListeners = new ArrayList<>();

    public PlayService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        WLogger.logW("onBind(Intent intent)");
        return new PlayBinder();
    }

    public class PlayBinder extends Binder implements IPlay {
        //========================================播放相关-start====================================
        public void play(BMusic music) {
            PlayService.this.play(music);
        }

        public void togglePlayOrPause() {
            PlayService.this.togglePlayOrPause();
        }

        @Override
        public void play() {
            PlayService.this.play();
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
         *
         * @param progress 0~100
         */
        @Override
        public void setProgress(int progress) {
            PlayService.this.setProgress(progress);
        }

        //========================================播放相关-end======================================

        public SimpleExoPlayer getExoPlayer() {
            return PlayService.this.getExoPlayer();
        }

        public BMusic getCurrentMusic() {
            return PlayService.this.getCurrentMusic();
        }

        public boolean isPlaying() {
            return PlayService.this.isPlaying();
        }

        //========================================监听相关-start====================================
        public boolean addPlayStateListener(PlayStateListener listener) {
            return PlayService.this.addPlayStateListener(listener);
        }

        public boolean removePlayStateListener(PlayStateListener listener) {
            return PlayService.this.removePlayStateListener(listener);
        }
    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    //========================================播放相关-start====================================
    public void play(BMusic music) {
        mCurrentMusic = music;
        if (mCurrentMusic != null) {
            prepare(music.path);
            mPlayer.setPlayWhenReady(true);
            SPHelper.getInstance().save("current_music", new Gson().toJson(mCurrentMusic));
        } else {
            mPlayer.setPlayWhenReady(false);
        }
    }

    public void togglePlayOrPause() {
        if (mPlayer.getPlayWhenReady()) {
            pause();
        } else {
            play();
        }
    }

    public void play() {
        if (mCurrentMusic != null) {
            mPlayer.setPlayWhenReady(true);
        } else {
            next();
        }
    }

    public void pause() {
        if (mCurrentMusic != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    public void next() {
        BMusic next = MusicManager.getInstance().getNext(mCurrentMusic);
            /* 如果下一首没有了,则获取第1首歌曲 */
        if (next == null) {
            next = MusicManager.getInstance().getFirst();
        }

            /* 如果第一首歌也没有了,则说明发生了异常状况 */
        if (next == null) {
            WLogger.logE("next(); 获取下一首歌曲失败");
            //todo 传递异常
            return;
        }

        play(next);
    }

    public void previous() {
        BMusic previous = MusicManager.getInstance().getPrevious(mCurrentMusic);
            /* 如果下一首没有了,则获取第1首歌曲 */
        if (previous == null) {
            previous = MusicManager.getInstance().getLast();
        }

            /* 如果第一首歌也没有了,则说明发生了异常状况 */
        if (previous == null) {
            WLogger.logE("next(); 获取上一首歌曲失败");
        }

        play(previous);
    }

    /**
     * 跳转进度
     *
     * @param progress 0~100
     */
    public void setProgress(int progress) {
        mPlayer.seekTo((long) (progress / 100f * mPlayer.getDuration()));
    }

    //========================================播放相关-end======================================

    public SimpleExoPlayer getExoPlayer() {
        return mPlayer;
    }

    public BMusic getCurrentMusic() {
        return mCurrentMusic;
    }

    public boolean isPlaying() {
        return mPlayer.getPlayWhenReady();
    }

    //========================================监听相关-start====================================
    public boolean addPlayStateListener(PlayStateListener listener) {
        return listener != null && mPlayStateListeners.add(listener);
    }

    public boolean removePlayStateListener(PlayStateListener listener) {
        return listener != null && mPlayStateListeners.remove(listener);
    }

    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    public interface PlayStateListener {
        void onPlay(BMusic music);

        void onPause();
    }

    /** 只在服务第一次创建时调用 */
    @Override
    public void onCreate() {
        super.onCreate();
        WLogger.logW("onCreate()");

        mContext = this;

        initPlayer();

        mPlayer.addListener(new ExoPlayerEventListenerAdapter() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    next();
                    return;
                }

                for (PlayStateListener listener : mPlayStateListeners) {
                    if (playWhenReady) {
                        listener.onPlay(mCurrentMusic);
                    } else {
                        listener.onPause();
                    }
                }
            }
        });

        String currentMusic = SPHelper.getInstance().get("current_music", "");
        if (!TextUtils.isEmpty(currentMusic)) {
            mCurrentMusic = new Gson().fromJson(currentMusic, BMusic.class);
            prepare(mCurrentMusic.path);
        }
    }

    /** 每次调用startService()启用该服务时都被调用 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WLogger.logW("onStartCommand(Intent intent, int flags, int startId); startId = " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 后台强制结束程序,会执行该回调. 因此可以在该生命周期中执行善后工作
     * 但是注意: 某些意外情况,例如在软件管理中强行结束应用, 不会走任何回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        WLogger.logW("onTaskRemoved(Intent rootIntent)");

        mPlayer.release();
    }

    @Override
    public void onDestroy() {
        Toaster.showShort(getApplicationContext(), "onDestroy()");
        super.onDestroy();
        WLogger.logW("onDestroy()");
    }

    //==================================播放逻辑-start==============================================

    private void initPlayer() {
        //==========step1初始操作
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, loadControl);

        //=========step3准备播放
        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "com.wosloveslife.fantasy"), mBandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        mExtractorsFactory = new DefaultExtractorsFactory();
    }

    private void prepare(String path) {
        Uri uri = Uri.parse(path);
        MediaSource videoSource = new ExtractorMediaSource(uri, mDataSourceFactory, mExtractorsFactory, null, null);
        mPlayer.prepare(videoSource);
    }


    //==========================================事件处理============================================
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onGotMusic(MusicManager.OnGotMusicEvent event) {
//        if (event == null || event.mBMusicList == null) return;
//    }

    //==================================记录生命周期-忽略===========================================

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WLogger.logW("onConfigurationChanged(Configuration newConfig)");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        WLogger.logW("onLowMemory()");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        WLogger.logW("onTrimMemory(int level); level = " + level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        WLogger.logW("onUnbind(Intent intent)");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        WLogger.logW("onRebind(Intent intent)");
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
        WLogger.logW("dump(FileDescriptor fd, PrintWriter writer, String[] args)");
    }
}
