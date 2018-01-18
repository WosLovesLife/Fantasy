package com.wosloveslife.fantasy.v2.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.orhanobut.logger.Logger
import com.wosloveslife.dao.Audio
import com.wosloveslife.player.AudioResource
import com.wosloveslife.player.ExoPlayerEventListenerAdapter
import com.wosloveslife.player.IPlayEngine
import com.wosloveslife.player.PlayService
import com.yesing.blibrary_wos.utils.systemUtils.SystemServiceUtils
import java.util.*

/**
 * Created by zhangh on 2017/9/3.
 */

class Controller private constructor() {
    private var mContext: Context? = null
    private var mPlayBinder: PlayService.PlayBinder? = null
    private var mPlayer: IPlayEngine? = null

    companion object {
        val sInstance: Controller by lazy { Controller() }
    }

    fun onAppStop() {
        unbindService()
    }

    fun init(context: Context) {
        mContext = context.applicationContext
        if (SystemServiceUtils.isServiceRunning(context, PlayService::class.java.name)) {
            return
        }
        val intent = Intent(context, PlayService::class.java)
        context.startService(intent)
        bindService()
    }

    private fun bindService() {
        mContext!!.bindService(Intent(mContext, PlayService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        mContext?.unbindService(mServiceConnection)
    }

    val mServiceConnection = object : ServiceConnection {
        /** bindService()方法执行后, 绑定成功时回调  */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Logger.d("连接到播放服务")
            val playBinder = service as PlayService.PlayBinder
            mPlayBinder = playBinder
            mPlayer = playBinder.getPlayEngine()

            initListener()
        }

        /** 和服务断开连接后回调(比如unbindService()方法执行后)  */
        override fun onServiceDisconnected(name: ComponentName) {
            Logger.d("和服务断开连接")
            mPlayer?.release()
        }
    }

    //==============================================================================================

    fun play(audio: Audio) {
        mPlayer?.play(AudioResource(audio.id, audio.title, audio.artist,
                audio.album, Uri.parse(audio.path), audio.duration, audio.size))
    }

    fun pause() {
        mPlayer?.pause()
    }

    fun seekTo(progress: Long) {
        mPlayer?.seekTo(progress)
    }

    //==============================================================================================

    fun isPlaying(): Boolean {
        return mPlayer?.isPlaying() ?: false
    }

    fun getDuration(): Long {
        return mPlayer?.getDuration() ?: 0
    }

    fun getCurrentPosition(): Long {
        return mPlayer?.getCurrentPosition() ?: 0
    }

    fun getBufferedPosition(): Long {
        return mPlayer?.getBufferedPosition() ?: 0
    }

    //==============================================================================================

    fun initListener() {
        mPlayer?.addListener(PlayerEventHandler())
    }

    inner class PlayerEventHandler : ExoPlayerEventListenerAdapter() {
        override fun onPlayerError(error: ExoPlaybackException?) {
            super.onPlayerError(error)
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            super.onTracksChanged(trackGroups, trackSelections)
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            super.onLoadingChanged(isLoading)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            if (playWhenReady) {
                onPlay(Audio())
            } else {
                onPause()
            }
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
            super.onTimelineChanged(timeline, manifest)
        }

        override fun onPositionDiscontinuity() {
            super.onPositionDiscontinuity()
        }
    }

    private val mEventListener: ArrayList<PlayEvent> = ArrayList()

    fun addListener(listener: PlayEvent) {
        mEventListener.add(listener)
    }

    fun removeListener(listener: PlayEvent) {
        mEventListener.remove(listener)
    }

    private fun onPlay(audio: Audio) {
        for (event in mEventListener) {
            event.onPlay(audio)
        }
    }

    private fun onPause() {
        for (event in mEventListener) {
            event.onPause()
        }
    }

    private fun onSeekTo(progress: Long) {
        for (event in mEventListener) {
            event.onSeekTo(progress)
        }
    }
}
