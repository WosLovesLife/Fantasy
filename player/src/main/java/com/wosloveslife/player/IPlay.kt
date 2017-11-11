package com.wosloveslife.player

import android.support.annotation.IntDef

import com.google.android.exoplayer2.ExoPlayer


/**
 * Created by zhangh on 2017/1/2.
 */
interface IPlay {

    @IntDef(PLAY_STATE_IDLE, PLAY_STATE_PLAY, PLAY_STATE_PAUSE, PLAY_STATE_COMPLETE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class PlayState

    companion object {
        const val PLAY_STATE_IDLE = 0L
        const val PLAY_STATE_PLAY = 1L
        const val PLAY_STATE_PAUSE = 2L
        const val PLAY_STATE_COMPLETE = 3L
    }

    fun play(audio: AudioResource)

    fun pause()

    fun seekTo(progress: Long)

    fun isPlaying() : Boolean

    fun addListener(listener: ExoPlayer.EventListener)
}
