package com.wosloveslife.fantasy.v2.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.orhanobut.logger.Logger
import com.wosloveslife.player.AudioResource
import com.wosloveslife.player.IPlayEngine
import com.wosloveslife.player.PlayService
import com.yesing.blibrary_wos.utils.systemUtils.SystemServiceUtils

/**
 * Created by zhangh on 2017/9/3.
 */

class Controller private constructor() {
    private val mState: State = State()
    private var mContext: Context? = null
    private var mPlayBinder: PlayService.PlayBinder? = null
    private var mPlayer: IPlayEngine? = null

    companion object {
        val sInstance: Controller by lazy { Controller() }
    }

    fun init(context: Context) {
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
            mPlayer = playBinder.getPlayEngine()
            mState.setPlayBinder(playBinder.getPlayEngine())
        }

        /** 和服务断开连接后回调(比如unbindService()方法执行后)  */
        override fun onServiceDisconnected(name: ComponentName) {
            Logger.d("和服务断开连接")
            mPlayer?.release()
        }
    }

    fun play(audio: AudioResource) {
        mPlayer?.play(AudioResource(audio.mId, audio.mTitle, audio.artist,
                audio.album, audio.path, audio.duration, audio.size))
    }

    fun pause() {
        mPlayer?.pause()
    }

    fun seekTo(progress: Long) {
        mPlayer?.seekTo(progress)
    }

    fun getState(): State {
        return mState
    }

    fun onAppStop() {
        unbindService()
    }
}
