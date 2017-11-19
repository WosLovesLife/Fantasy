package com.wosloveslife.player

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.webkit.URLUtil
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util

/**
 * The Engine for implement Play audio base on ExoPlayer
 * Created by zhangh on 2017/11/10.
 */
class PlayerEngine constructor(private val mContext: Context) : IPlayEngine {

    //=============ExoPlayer相关
    private val mPlayer: SimpleExoPlayer
    private var mBandwidthMeter: DefaultBandwidthMeter
    private var mDataSourceFactory: DataSource.Factory
    private var mExtractorsFactory: ExtractorsFactory
    private var mCacheDataSourceFactory: CacheDataSourceFactory

    var mAudio: AudioResource? = null

    init {
        //==========step1初始操作
        // 1. Create a default TrackSelector
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveVideoTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        // 2. Create a default LoadControl
        val loadControl = DefaultLoadControl()

        // 3. Create the player
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, loadControl)

        //=========step3准备播放
        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = DefaultBandwidthMeter()
        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "com.wosloveslife.fantasy"), mBandwidthMeter)
        // Produces Extractor instances for parsing the media data.
        mExtractorsFactory = DefaultExtractorsFactory()

        mCacheDataSourceFactory = generateCacheDataSourceFactory()
    }

    private fun generateCacheDataSourceFactory(): CacheDataSourceFactory {
        val simpleCache = SimpleCache(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), NoOpCacheEvictor())
        /* 这个文件大小指的是单个缓存文件的尺寸2MB, 例如一首歌10MB,则需要5个缓存文件
         * 同时它也会影响到缓存的时机.
         * Exo会一次性缓存一个缓存文件大小的数据,然后当播放进度接近缓存的末端时开启新的缓存文件
         * 可以参考{@link FileDataSource#write(byte[] buffer, int offset, int length)}
         * 方法和{@link FileDataSource#openNextOutputStream()}方法
         * 当一个文件写满后会开启一个新的文件继续写入*/
        val cacheFileSize = CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE

        /* 构建带缓存的Source */
        return CacheDataSourceFactory(
                simpleCache,
                mDataSourceFactory,
                FileDataSourceFactory(mBandwidthMeter),
                CacheDataSinkFactory(simpleCache, cacheFileSize, mBandwidthMeter),
                1,
                CacheDataSource.EventListener { cacheSizeBytes, cachedBytesRead ->
                    Log.d("FantasyPlayer", "cacheSizeBytes = $cacheSizeBytes; cachedBytesRead = $cachedBytesRead")
                })
    }

    //==================================播放逻辑-start==============================================

    fun getPlayer(): SimpleExoPlayer {
        return mPlayer
    }

    fun prepare(path: Uri): Boolean {
        if (URLUtil.isValidUrl(path.toString())) {
            throw PlayerException(PlayerException.ErrorCode.NO_AUDIO, "Uri invalid")
        }

        /* 将带缓存的source作为资源传入,构建普通多媒体播放资源 */
        val source = ExtractorMediaSource(
                path,
                if (URLUtil.isNetworkUrl(path.toString())) mCacheDataSourceFactory else mDataSourceFactory,
                mExtractorsFactory,
                object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        super.handleMessage(msg)
                    }
                },
                ExtractorMediaSource.EventListener {
                    // 缓存中断 网络错误 TODO HttpDataSource.HttpDataSourceException
                    // 但触发该异常并不代表当前播放被影响. 可能目前还在部分之前缓存的部分.
                    // 最终播放被迫暂停要在 ExoPlayerEventListener.onPlayerError()中回调
                })

        mPlayer.prepare(source)
        return true
    }

    override fun play(audio: AudioResource) {
        mAudio = audio
        /* 这里有一个未知的Bug，调用过ExoPlayer.seekTo()方法后,跳转歌曲的一瞬间进度会闪烁一下,
         * 导致歌词控件同步也会迅速滚动歌词一下, 因此这里在播放一首歌之前先将之前的歌的进度归零 */
//        mPlayer.seekTo(0)
        mPlayer.stop() // TODO 验证这个方法
        if (prepare(audio.path)) {
            mPlayer.playWhenReady = true
        } else {
            pause()
        }
    }

    override fun pause() {
        mPlayer.playWhenReady = false
    }

    override fun seekTo(progress: Long) {
        mPlayer.seekTo(progress)
    }

    override fun isPlaying(): Boolean {
        return mPlayer.playWhenReady
    }

    override fun addListener(listener: ExoPlayer.EventListener) {
        mPlayer.addListener(listener)
    }

    override fun removeListener(listener: ExoPlayer.EventListener) {
        mPlayer.removeListener(listener)
    }

    override fun getDuration(): Long {
        return mPlayer.duration
    }

    override fun getCurrentPosition(): Long {
        return mPlayer.currentPosition
    }

    override fun getBufferedPosition(): Long {
        return mPlayer.bufferedPosition
    }

    override fun release() {
        pause()
        mPlayer.release()
    }
}