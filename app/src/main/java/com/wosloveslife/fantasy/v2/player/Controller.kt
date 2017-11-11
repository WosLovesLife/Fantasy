package com.wosloveslife.fantasy.v2.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder

import com.orhanobut.logger.Logger
import com.wosloveslife.dao.Audio
import com.wosloveslife.fantasy.manager.MusicManager
import com.wosloveslife.player.AudioResource
import com.wosloveslife.player.ExoPlayerEventListenerAdapter
import com.wosloveslife.player.PlayService
import com.wosloveslife.player.PlayerException
import com.yesing.blibrary_wos.utils.systemUtils.SystemServiceUtils

/**
 * Created by zhangh on 2017/9/3.
 */

class Controller private constructor() : IPlayer {
    private val mState: State = State()
    private var mContext: Context? = null
    private var mPlayBinder: PlayService.PlayBinder? = null

    companion object {
        val sInstance: Controller by lazy { Controller() }
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

    private val mServiceConnection = object : ServiceConnection {
        /** bindService()方法执行后, 绑定成功时回调  */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Logger.d("连接到播放服务")
            val playBinder = service as PlayService.PlayBinder
            mPlayBinder = playBinder

            mState.setPlayBinder(playBinder)
            mState.addListener(object : ExoPlayerEventListenerAdapter() {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    super.onPlayerStateChanged(playWhenReady, playbackState)

                    if (mState.isPlaying) {
                        val audio = MusicManager.getInstance().musicConfig.mCurrentMusic ?: throw PlayerException(PlayerException.NO_AUDIO)
                        val audioResource = AudioResource(audio.id, audio.title, audio.artist,
                                audio.album, Uri.parse(audio.path), audio.duration, audio.size)
                        playBinder.play(audioResource)
                    } else if (mState.isPlaying) {
                        playBinder.pause()
                    }
                }
            })
        }

        /** 和服务断开连接后回调(比如unbindService()方法执行后)  */
        override fun onServiceDisconnected(name: ComponentName) {
            Logger.d("和服务断开连接")
        }
    }

    fun onAppStop() {
        unbindService()
    }

    override fun play(audio: Audio) {
        mPlayBinder!!.play(AudioResource(audio.id, audio.title, audio.artist,
                audio.album, Uri.parse(audio.path), audio.duration, audio.size))
    }

    override fun pause() {
        mPlayBinder!!.pause()
    }

    override fun seekTo(progress: Long) {
        mPlayBinder!!.seekTo(progress)
    }

    override fun getState(): State {
        return mState
    }
}
