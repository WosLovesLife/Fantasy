package com.wosloveslife.fantasy.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.orhanobut.logger.Logger
import com.wosloveslife.dao.Audio
import com.wosloveslife.fantasy.adapter.ExoPlayerEventListenerAdapter
import com.wosloveslife.fantasy.services.PlayService

class PlayerController(context: Context) {
    companion object {
        private var sPlayerController: PlayerController? = null

        fun getInstance(): PlayerController {
            return sPlayerController
                    ?: throw IllegalStateException("You have to invoke init(context) function before get instance")
        }

        fun init(context: Context) {
            if (sPlayerController == null) {
                synchronized(PlayerController::class.java) {
                    if (sPlayerController == null) {
                        sPlayerController = PlayerController(context)
                    }
                }
            }
        }
    }

    init {
        val mServiceConnection: ServiceConnection = object : ServiceConnection {
            /** bindService()方法执行后, 绑定成功时回调  */
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Logger.d("连接到播放服务")
                mPlayBinder = service as PlayService.PlayBinder

                setupPlayer()
            }

            /** 和服务断开连接后回调(比如unbindService()方法执行后)  */
            override fun onServiceDisconnected(name: ComponentName) {
                Logger.d("和服务断开连接")
            }
        }

        val intent = Intent(context, PlayService::class.java)
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private var mPlayBinder: PlayService.PlayBinder? = null

    private var mCurrentMusic: Audio? = null

    val mPlayStateListeners = ArrayList<PlayStateListener>()

    //============定时自动关闭服务
    private var mCountdownUp: Boolean = false
    var mStopPlay: Boolean = false
    private var mCountdownTargetTimestamp: Long = 0
    private var mCloseAfterPlayComplete: Boolean = false

    private fun setupPlayer() {
        mPlayBinder!!.addListener(object : ExoPlayerEventListenerAdapter() {
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
                mPlayStateListeners.map {
                    it.onPlayStateChanged(if (playWhenReady) PlayState.PLAY else PlayState.PAUSE)
                }
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
                super.onTimelineChanged(timeline, manifest)
            }

            override fun onPositionDiscontinuity() {
                super.onPositionDiscontinuity()
            }
        })
    }

    // =========== 公开方法

    fun play(audio: Audio? = null) {
        if (audio != null) {
            mCurrentMusic = audio
            mPlayBinder?.play(audio)
        } else {
            mPlayBinder?.play()
        }
    }

    fun pause() {
        mPlayBinder?.pause()
    }

    fun next() {
    }

    fun previous() {
    }

    fun seekTo(progress: Long) {
        mPlayBinder?.seekTo(progress)
    }

    fun isPlaying(): Boolean {
        return mPlayBinder?.isPlaying ?: false
    }

    fun getPlaybackState(): Int {
        return mPlayBinder?.playbackState ?: ExoPlayer.STATE_IDLE
    }

    fun getDuration(): Long {
        return mPlayBinder?.duration ?: 0
    }

    fun getCurrentPosition(): Long {
        return mPlayBinder?.currentPosition ?: 0
    }

    fun getBufferedPosition(): Long {
        return mPlayBinder?.bufferedPosition ?: 0
    }

    fun getBufferedPercentage(): Int {
        return mPlayBinder?.exoPlayer?.bufferedPercentage ?: 0
    }

    fun addListener(listener: PlayStateListener) {
        mPlayStateListeners.add(listener)
    }

    fun removeListener(listener: PlayStateListener): Boolean {
        return mPlayStateListeners.remove(listener)
    }

    fun stop() {

    }

    fun release() {

    }

    fun setCountdown(pickDate: Long, closeAfterPlayComplete: Boolean) {
        mCountdownUp = pickDate > 0
        mCountdownTargetTimestamp = System.currentTimeMillis() + pickDate
        mCloseAfterPlayComplete = closeAfterPlayComplete
    }

    // ============ 待移除的方法

    fun getCurrentMusic(): Audio? {
        return mCurrentMusic
    }

    /** 如果没有开启计时器或者计时器未完成,则返回false.  */
    fun isCountdown(): Boolean {
        return false
    }

    fun isCloseAfterPlayComplete(): Boolean {
        return mCloseAfterPlayComplete
    }
}

enum class PlayState {
    PLAY, PAUSE, STOP, RELEASE
}

interface PlayStateListener {
    fun onPlayStateChanged(state: PlayState)

    fun onSeekTo()

    fun onBuffering()
}