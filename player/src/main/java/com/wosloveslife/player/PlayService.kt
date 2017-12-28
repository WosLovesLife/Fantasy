package com.wosloveslife.player

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * The service for play audio resource
 * Created by zhangh on 2017/11/10.
 */
class PlayService : Service() {

    private var mAudioHelper: AudioHelper? = null
    private var mPlayerEngine: PlayerEngine? = null

    override fun onBind(intent: Intent): IBinder? {
        return PlayBinder()
    }

    inner class PlayBinder : Binder(), IPlayer {
        override fun getPlayEngine(): PlayerEngine {
            return mPlayerEngine!!
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onCreate() { // 只在服务第一次创建时调用
        super.onCreate()

        mPlayerEngine = PlayerEngine(this)
        mAudioHelper = AudioHelper(this, mPlayerEngine!!)
    }

    /**
     * 后台强制结束程序,会执行该回调. 因此可以在该生命周期中执行善后工作
     * 但是注意: 某些意外情况,例如在软件管理中强行结束应用, 不会走任何回调
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        release()
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun release() {
        mPlayerEngine?.release()
        mAudioHelper?.abandonAudioFocus()
    }
}
