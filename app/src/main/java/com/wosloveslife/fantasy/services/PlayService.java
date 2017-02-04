package com.wosloveslife.fantasy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.broadcast.AudioBroadcast;
import com.wosloveslife.fantasy.broadcast.BroadcastManager;
import com.wosloveslife.fantasy.helper.FileDataSourceFactory;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.interfaces.IPlay;
import com.wosloveslife.fantasy.manager.MusicManager;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileDescriptor;
import java.io.IOException;
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
    public DataSource.Factory mDataSourceFactory;
    public ExtractorsFactory mExtractorsFactory;

    //============数据
    BMusic mCurrentMusic;

    //============变量
    public int mPlayOrder;
    private boolean mSaveCache;

    List<PlayStateListener> mPlayStateListeners = new ArrayList<>();

    public PlayService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        WLogger.w("onBind(Intent intent)");
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

    /**
     * 播放给定的某一首歌曲资源<br/>
     * 将歌曲资源序列化存储本地<br/>
     * 如果资源为null,尝试暂停现有播放<br/>
     *
     * @param music 要播放的歌曲资源
     */
    public void play(BMusic music) {
        mCurrentMusic = music;
        if (mCurrentMusic != null) {
            prepare2(music.path);
            mPlayer.setPlayWhenReady(true);
            SPHelper.getInstance().save("current_music", new Gson().toJson(mCurrentMusic));
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
        if (mCurrentMusic != null) {
            mPlayer.setPlayWhenReady(true);
        } else {
            next();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mCurrentMusic != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    /**
     * 播放下一首<br/>
     * 如果下一首没有了,则获取第1首歌曲<br/>
     * 如果第一首歌也没有了,则抛出异常
     */
    public void next() {
        BMusic next = MusicManager.getInstance().getNext(mCurrentMusic);
        if (next == null) {
            next = MusicManager.getInstance().getFirst();
        }

        if (next == null) {
            WLogger.e("next(); 获取下一首歌曲失败");
            //todo 传递异常
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
        BMusic previous = MusicManager.getInstance().getPrevious(mCurrentMusic);
        /* 如果下一首没有了,则获取第1首歌曲 */
        if (previous == null) {
            previous = MusicManager.getInstance().getLast();
        }

        /* 如果第一首歌也没有了,则说明发生了异常状况 */
        if (previous == null) {
            WLogger.e("next(); 获取上一首歌曲失败");
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
        WLogger.w("onCreate()");

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
                        /* 在这里重置该值, 避免影响后续的播放状态 */
                        mSavePlay = false;
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

        EventBus.getDefault().register(this);
        BroadcastManager.getInstance().init(this);
        registerAudioEvent();
    }

    /**
     * 监听声音播放的焦点变化,当有别的应用抢占了焦点时(例如拨打电话).终止播放,并在重新获取焦点后根据之前保存的状态而判断是否继续播放
     */
    private void registerAudioEvent() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.ADJUST_LOWER:
                    case -2:
                        pause();
                        mSavePlay = isPlaying();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (mSavePlay) {
                            /* 在这里重置该值, 避免影响后续的播放状态 */
                            mSavePlay = false;
                            play();
                        }
                        break;
                }
                WLogger.d("requestAudioFocus onAudioFocusChange : [focusChange] = " + focusChange);
            }
        }, 1, AudioManager.AUDIOFOCUS_GAIN);
    }

    boolean mSavePlay;

    /** 每次调用startService()启用该服务时都被调用 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WLogger.w("onStartCommand(Intent intent, int flags, int startId); startId = " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 后台强制结束程序,会执行该回调. 因此可以在该生命周期中执行善后工作
     * 但是注意: 某些意外情况,例如在软件管理中强行结束应用, 不会走任何回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        WLogger.w("onTaskRemoved(Intent rootIntent)");

        mPlayer.release();
    }

    @Override
    public void onDestroy() {
        Toaster.showShort(getApplicationContext(), "onDestroy()");
        super.onDestroy();
        WLogger.w("onDestroy()");
        EventBus.getDefault().unregister(this);
        BroadcastManager.getInstance().unregisterAllBroadcasts();
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
        if (TextUtils.isEmpty(path)) {
            /* todo 没有播放地址,传递错误 */
            Toaster.showShort(this, "找不到播放地址");
            return;
        }

        DataSource.Factory factory = mDataSourceFactory;
        if (mSaveCache && path.startsWith("http")) {
            factory = new CacheDataSourceFactory(new SimpleCache(getExternalFilesDir(Environment.DIRECTORY_MUSIC), new NoOpCacheEvictor()), mDataSourceFactory, 1);
        }
        Uri uri = Uri.parse(path);
        MediaSource videoSource = new ExtractorMediaSource(uri, factory, mExtractorsFactory, new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

            }
        }, new ExtractorMediaSource.EventListener() {
            @Override
            public void onLoadError(IOException error) {

            }
        });
        mPlayer.prepare(videoSource);
    }

    private void prepare2(String path) {
        if (TextUtils.isEmpty(path)) {
            /* todo 没有播放地址,传递错误 */
            Toaster.showShort(this, "找不到播放地址");
            return;
        }

        Uri uri = Uri.parse(path);
        SimpleCache simpleCache = new SimpleCache(getExternalFilesDir(Environment.DIRECTORY_MUSIC), new NoOpCacheEvictor());
        /* 这个文件大小指的是单个缓存文件的尺寸2MB, 例如一首歌10MB,则需要5个缓存文件
         * 同时它也会影响到缓存的时机.
         * Exo会一次性缓存一个缓存文件大小的数据,然后当播放进度接近缓存的末端时开启新的缓存文件
         * 可以参考{@link FileDataSource#write(byte[] buffer, int offset, int length)}
         * 方法和{@link FileDataSource#openNextOutputStream()}方法
         * 当一个文件写满后会开启一个新的文件继续写入*/
        long cacheFileSize = CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE;

        /* 构建带缓存的Source */
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                simpleCache,
                mDataSourceFactory,
                new FileDataSourceFactory(null),
                new com.wosloveslife.fantasy.helper.CacheDataSinkFactory(simpleCache, cacheFileSize, null),
                1,
                new CacheDataSource.EventListener() {
                    @Override
                    public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {
                        Logger.d("cacheSizeBytes = " + cacheSizeBytes + "; cachedBytesRead = " + cachedBytesRead);
                    }
                });

        /* 将带缓存的source作为资源传入,构建普通多媒体播放资源 */
        MediaSource videoSource = new ExtractorMediaSource(uri, cacheDataSourceFactory, mExtractorsFactory, new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

            }
        }, new ExtractorMediaSource.EventListener() {
            @Override
            public void onLoadError(IOException error) {

            }
        });

        mPlayer.prepare(videoSource);
    }

    //==========================================事件处理============================================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBroadcastEvent(AudioBroadcast.AudioNoisyEvent event) {
        pause();
    }

    //==================================记录生命周期-忽略===========================================

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WLogger.w("onConfigurationChanged(Configuration newConfig)");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        WLogger.w("onLowMemory()");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        WLogger.w("onTrimMemory(int level); level = " + level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        WLogger.w("onUnbind(Intent intent)");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        WLogger.w("onRebind(Intent intent)");
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
        WLogger.w("dump(FileDescriptor fd, PrintWriter writer, String[] args)");
    }
}
