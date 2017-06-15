package com.wosloveslife.fantasy.services;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

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
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.helper.FileDataSourceFactory;
import com.yesing.blibrary_wos.utils.assist.Toaster;

import java.io.IOException;

import static android.os.Looper.getMainLooper;

/**
 * Created by zhangh on 2017/3/20.
 */

public class PlayerEngine {
    //=============ExoPlayer相关
    private SimpleExoPlayer mPlayer;
    private DefaultBandwidthMeter mBandwidthMeter;
    public DataSource.Factory mDataSourceFactory;
    public ExtractorsFactory mExtractorsFactory;
    private CacheDataSourceFactory mCacheDataSourceFactory;

    private Context mContext;

    public PlayerEngine(Context context){
        mContext = context;

        initPlayer();
    }

    public SimpleExoPlayer getPlayer() {
        return mPlayer;
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

        mCacheDataSourceFactory = generateCacheDataSourceFactory();
    }

    public boolean prepare(String path) {
        if (TextUtils.isEmpty(path)) {
            /* todo 没有播放地址,传递错误 */
            Toaster.showShort("找不到播放地址");
            return false;
        }

        /* 将带缓存的source作为资源传入,构建普通多媒体播放资源 */
        MediaSource source = new ExtractorMediaSource(
                Uri.parse(path),
                path.startsWith("http") ? mCacheDataSourceFactory : mDataSourceFactory,
                mExtractorsFactory,
                new Handler(getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                    }
                },
                new ExtractorMediaSource.EventListener() {
                    @Override
                    public void onLoadError(IOException error) {
                        // 缓存中断 网络错误 TODO HttpDataSource.HttpDataSourceException
                        // 但触发该异常并不代表当前播放被影响. 可能目前还在部分之前缓存的部分.
                        // 最终播放被迫暂停要在 ExoPlayerEventListener.onPlayerError()中回调
                    }
                });

        mPlayer.prepare(source);
        return true;
    }

    private CacheDataSourceFactory generateCacheDataSourceFactory() {
        SimpleCache simpleCache = new SimpleCache(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), new NoOpCacheEvictor());
        /* 这个文件大小指的是单个缓存文件的尺寸2MB, 例如一首歌10MB,则需要5个缓存文件
         * 同时它也会影响到缓存的时机.
         * Exo会一次性缓存一个缓存文件大小的数据,然后当播放进度接近缓存的末端时开启新的缓存文件
         * 可以参考{@link FileDataSource#write(byte[] buffer, int offset, int length)}
         * 方法和{@link FileDataSource#openNextOutputStream()}方法
         * 当一个文件写满后会开启一个新的文件继续写入*/
        long cacheFileSize = CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE;

        /* 构建带缓存的Source */
        return new CacheDataSourceFactory(
                simpleCache,
                mDataSourceFactory,
                new FileDataSourceFactory(mBandwidthMeter),
                new com.wosloveslife.fantasy.helper.CacheDataSinkFactory(simpleCache, cacheFileSize, mBandwidthMeter),
                1,
                new CacheDataSource.EventListener() {
                    @Override
                    public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {
                        Logger.d("cacheSizeBytes = " + cacheSizeBytes + "; cachedBytesRead = " + cachedBytesRead);
                    }
                });
    }
}
