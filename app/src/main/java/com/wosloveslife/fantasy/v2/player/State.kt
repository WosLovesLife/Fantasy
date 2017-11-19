package com.wosloveslife.fantasy.v2.player

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.wosloveslife.dao.Audio
import com.wosloveslife.fantasy.manager.MusicManager
import com.wosloveslife.player.ExoPlayerEventListenerAdapter
import com.wosloveslife.player.IPlayEngine
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

    val mEventListener: ArrayList<PlayEvent> = ArrayList()
    private var mPlayer: IPlayEngine? = null

    fun setPlayBinder(player: IPlayEngine) {
        mPlayer = player
        player.addListener(object : ExoPlayerEventListenerAdapter() {
            override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
                super.onTracksChanged(trackGroups, trackSelections)
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                super.onLoadingChanged(isLoading)
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if (playWhenReady) {
                    play(MusicManager.getInstance().musicConfig.mCurrentMusic!!);
                } else {
                    pause()
                }
            }

            override fun onTimelineChanged(timeline: Timeline, manifest: Any) {
                super.onTimelineChanged(timeline, manifest)
            }

            override fun onPositionDiscontinuity() {
                super.onPositionDiscontinuity()
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                throw PlayerException(PlayerException.FROM_EXO_PLAYER, error)
            }
        })
    }

    private fun play(audio: Audio) {
        for (event in mEventListener) {
            event.onPlay(audio)
        }
    }

    private fun pause() {
        for (event in mEventListener) {
            event.onPause()
        }
    }

    private fun seekTo(progress: Long) {
        for (event in mEventListener) {
            event.onSeekTo(progress)
        }
    }

    fun isPlaying(): Boolean {
        return mPlayer?.isPlaying() ?: false
    }

    fun getDuration(): Long {
        return mPlayer?.getDuration() ?: 0
    }

    fun getProgress(duration: Long): Long {
        return mPlayer?.getCurrentPosition() ?: 0
    }

    fun getBufferProgress(duration: Long): Long {
        return mPlayer?.getBufferedPosition() ?: 0
    }
}