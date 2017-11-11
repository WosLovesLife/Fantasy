package com.wosloveslife.fantasy.v2.player

import com.wosloveslife.dao.Audio
import com.wosloveslife.player.PlayerException

/**
 * Created by zhangh on 2017/11/11.
 */
interface PlayEvent {
    fun onPlay(audio: Audio)
    fun onPause()
    fun onSeekTo(progress: Long)
    fun onStop()
    fun onBuffering(bufferProgress: Long)
    fun onError(e: PlayerException)
}