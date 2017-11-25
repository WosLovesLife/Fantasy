package com.wosloveslife.fantasy

import android.app.Application
import android.content.Context
import com.facebook.soloader.SoLoader
import com.orhanobut.logger.Logger
import com.wosloveslife.dao.Migration
import com.wosloveslife.fantasy.broadcast.BroadcastManager
import com.wosloveslife.fantasy.helper.SPHelper
import com.wosloveslife.fantasy.manager.MusicManager
import com.wosloveslife.fantasy.manager.SettingConfig
import com.wosloveslife.fantasy.v2.player.Controller
import com.yesing.blibrary_wos.utils.assist.Toaster
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by zhangh on 2017/1/2.
 */
class App : Application() {

    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        initDB()

        initKits()

        initLitho()

        initManager()
    }

    override fun onTerminate() {
        super.onTerminate()
        Controller.sInstance.onAppStop()
        BroadcastManager.getInstance().unregisterAllBroadcasts()
    }

    private fun initDB() {
        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .schemaVersion(Migration.SCHEMA_VERSION)
                .migration(Migration())
                .build())
    }

    private fun initKits() {
        Logger.init("Fantasy")

        Toaster.init(this)
    }

    private fun initLitho() {
        SoLoader.init(this, false)
    }

    private fun initManager() {
        SPHelper.getInstance().init(this)
        SettingConfig.init(this)
        MusicManager.getInstance().init(this)
        Controller.sInstance.init(this)
        BroadcastManager.getInstance().init(this)
    }
}
