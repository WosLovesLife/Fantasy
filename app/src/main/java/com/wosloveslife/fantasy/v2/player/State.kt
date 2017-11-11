package com.wosloveslife.fantasy.v2.player

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.wosloveslife.player.ExoPlayerEventListenerAdapter
import com.wosloveslife.player.PlayService
import com.wosloveslife.player.PlayerException

/**
 * 只负责状态变化的传达和当前状态的获取
 * Created by zhangh on 2017/9/3.
 */
class State {
    companion object State {
        const val STATE_IDLE = 1
        const val STATE_BUFFERING = 2
        const val STATE_READY = 3
        const val STATE_ENDED = 4
    }

    private val mEventListenerBuffers: ArrayList<ExoPlayer.EventListener> = ArrayList()
    private var mPlayer: PlayService.PlayBinder? = null

    var mState: Int = State.STATE_IDLE
    var isPlaying: Boolean = false
    var duration: Long = 0L
    var currentPosition: Long = 0L
    var bufferedPosition: Long = 0L
    var playbackState: Int = STATE_IDLE

    fun setPlayBinder(player: PlayService.PlayBinder) {
        mPlayer = player
        executeAfterBind()
        player.addListener(object : ExoPlayerEventListenerAdapter() {
            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                badPlay(error)
            }
        })
    }

    private fun executeAfterBind() {
        for (listener in mEventListenerBuffers) {
            addListener(listener)
        }
        mEventListenerBuffers.clear()
    }

    fun badPlay(e: ExoPlaybackException) {
        throw PlayerException(PlayerException.ErrorCode.FROM_EXO_PLAYER, e)
    }

    fun addListener(listener: ExoPlayer.EventListener) {
        if (mPlayer == null) {
            mEventListenerBuffers.add(listener)
        } else {
            mPlayer?.addListener(listener)
        }
    }

    fun removeListener(listener: ExoPlayer.EventListener) {
        mEventListenerBuffers.remove(listener)
        mPlayer?.removeListener(listener)
    }
}