package com.wosloveslife.fantasy.v2.player

import com.wosloveslife.dao.Audio
import com.wosloveslife.player.PlayerException

/**
 * Created by zhangh on 2017/12/24.
 */
open class PlayEventAdapter :PlayEvent {
    override fun onPlay(audio: Audio) {
    }

    override fun onPause() {
    }

    override fun onSeekTo(progress: Long) {
    }

    override fun onStop() {
    }

    override fun onBuffering(bufferProgress: Long) {
    }

    override fun onError(e: PlayerException) {
    }
}