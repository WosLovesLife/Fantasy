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

import com.google.android.exoplayer2.DefaultLoadControl;
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
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.interfaces.IPlay;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import java.io.FileDescriptor;
import java.io.PrintWriter;

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
            mCurrentMusic = music;
            prepare(music.path);
        }

        public void togglePlayOrPause() {
            if (mPlayer.getPlayWhenReady()) {
                pause();
            } else {
                play();
            }
        }

        @Override
        public void play() {
            if (mCurrentMusic != null) {
                mPlayer.setPlayWhenReady(true);
            } else {
                next();
            }
        }

        @Override
        public void pause() {
            if (mCurrentMusic != null) {
                mPlayer.setPlayWhenReady(false);
            }
        }

        @Override
        public void next() {
            BMusic next = MusicManager.getInstance().getNext(mCurrentMusic);
            /* 如果下一首没有了,则获取第1首歌曲 */
            if (next == null) {
                next = MusicManager.getInstance().getFirst();
            }

            /* 如果第一首歌也没有了,则说明发生了异常状况 */
            if (next == null) {
                WLogger.logE("next(); 获取下一首歌曲失败");
            }

            play(next);
        }

        @Override
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
        @Override
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
    }


    /** 只在服务第一次创建时调用 */
    @Override
    public void onCreate() {
        super.onCreate();
        WLogger.logW("onCreate()");

        mContext = this;

        initPlayer();
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
        mPlayer.setPlayWhenReady(true);
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
