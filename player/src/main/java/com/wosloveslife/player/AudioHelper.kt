package com.wosloveslife.player

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager

/**
 * Created by zhangh on 2017/11/10.
 */
class AudioHelper constructor(
        private val mContext: Context,
        private val mPlayerEngine: PlayerEngine) {

    private var mAudioManager: AudioManager? = null

    private var mSavePlay: Boolean = false

    /**
     * 如果已经获取了焦点再次获取焦点会造成回调失去焦点.因此使用该变量记录当前的焦点状态,
     * 因为焦点适用于整个程序,所以使用静态变量
     */
    private companion object {
        var sHasFocus: Boolean = false
    }

    init {
        mPlayerEngine.addListener(object : ExoPlayerEventListenerAdapter() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playWhenReady) {
                    /* 在这里重置该值, 避免影响后续的播放状态 */
                    mSavePlay = false
                    registerAudioFocus()
                }
            }
        })
    }

    fun registerAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = mContext.getSystemService(AUDIO_SERVICE) as AudioManager
        }

        if (sHasFocus) return

        val result = mAudioManager!!.requestAudioFocus(object : AudioManager.OnAudioFocusChangeListener {

            private var mMAudio: AudioResource? = null

            override fun onAudioFocusChange(focusChange: Int) {
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT // 短暂失去焦点
                    -> {
                        mSavePlay = mPlayerEngine.isPlaying()
                        mMAudio = mPlayerEngine.mAudio
                        if (mPlayerEngine.isPlaying()) {
                            mPlayerEngine.pause()
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS // 失去焦点,
                    -> if (mPlayerEngine.isPlaying()) {
                        mPlayerEngine.pause()
                    }
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT //
                        , AudioManager.AUDIOFOCUS_REQUEST_GRANTED // 获得焦点
                    -> if (mSavePlay) {
                        mPlayerEngine.play(mMAudio!!)
                    }
                }
                sHasFocus = focusChange > 0
            }
        }, 1, AudioManager.AUDIOFOCUS_GAIN)

        sHasFocus = result > 0
    }

    fun abandonAudioFocus() {
        if (mAudioManager != null) {
            mAudioManager!!.abandonAudioFocus(null)
        }
    }
}