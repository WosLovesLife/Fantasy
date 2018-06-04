package com.wosloveslife.fantasy.manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.AnyThread
import android.support.annotation.WorkerThread
import android.text.TextUtils
import com.wosloveslife.dao.Audio
import com.wosloveslife.dao.SheetIds
import com.wosloveslife.fantasy.adapter.SubscriberAdapter
import com.wosloveslife.fantasy.baidu.BaiduMusicInfo
import com.wosloveslife.fantasy.baidu.BaiduSearch
import com.wosloveslife.fantasy.dao.MusicInfoProvider
import com.wosloveslife.fantasy.dao.MusicProvider
import com.wosloveslife.fantasy.lrc.BLyric
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by zhangh on 2017/1/2.
 */
class MusicManager private constructor() {

    //=============
    private lateinit var mContext: Context

    //=============Var

    //=============Data
    /** 获取歌曲/歌单的Holder  */
    private lateinit var musicConfig: MusicConfig

    @AnyThread
    fun getMusicConfig(): MusicConfig {
        return musicConfig
    }

    private lateinit var mMusicInfoProvider: MusicInfoProvider

    val favored: Observable<List<Audio>>
        @AnyThread
        get() = MusicProvider.loadMusicBySheet(SheetIds.FAVORED)

    val recentMusic: Observable<List<Audio>>
        @AnyThread
        get() = MusicProvider.loadMusicBySheet(SheetIds.RECENT)

    @AnyThread
    fun init(context: Context) {
        musicConfig = MusicConfig()
        mContext = context.applicationContext
        mMusicInfoProvider = MusicInfoProvider.getInstance()
        mMusicInfoProvider.init(context)
        Observable.just(context)
                .map { `object` ->
                    dispose()
                    `object`
                }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    @WorkerThread
    private fun dispose() {
        loadLastSheet()
    }

    /**
     * 获取上一次关闭前停留的歌单
     */
    @WorkerThread
    private fun loadLastSheet() {
        val sheetId = musicConfig.mCurrentSheetId
        if (TextUtils.isEmpty(sheetId)) {
            musicConfig.saveLastSheetId(SheetIds.LOCAL)
            scan()
        } else {
            changeSheet(sheetId!!)
            if (checkNeedScan()) {
                scan()
            }
        }
    }

    private fun checkNeedScan(): Boolean {
        return musicConfig.mMusicList.size == 0 && TextUtils.equals(musicConfig.mCurrentSheetId, SheetIds.LOCAL)
    }

    // TODO: 17/6/17  remove
    @AnyThread
    private fun scan() {
        MusicProvider.scanSysDB(mContext)
                .map { audios ->
                    MusicProvider.clearSheetEntities(SheetIds.LOCAL).toBlocking().first()
                    MusicProvider.insertMusics(SheetIds.LOCAL, audios).toBlocking().first()
                    audios
                }
                .subscribeOn(Schedulers.io())
                .subscribe(object : SubscriberAdapter<List<Audio>>() {
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (TextUtils.equals(musicConfig.mCurrentSheetId, SheetIds.LOCAL)) {
                            onGotData(null)
                        }
                    }

                    override fun onNext(bMusics: List<Audio>) {
                        super.onNext(bMusics)
                        if (TextUtils.equals(musicConfig.mCurrentSheetId, SheetIds.LOCAL)) {
                            onGotData(bMusics)
                        }
                    }
                })
    }

    @AnyThread
    private fun onGotData(bMusics: List<Audio>?) {
        if (bMusics !== musicConfig.mMusicList) {
            musicConfig.mMusicList.clear()

            if (bMusics != null && bMusics.isNotEmpty()) {
                musicConfig.mMusicList.addAll(bMusics)
            }
        }

        EventBus.getDefault().post(OnScannedMusicEvent(musicConfig.mMusicList))
        EventBus.getDefault().post(OnGotMusicEvent(musicConfig.mMusicList))
    }

    //=======================================获取音乐-start===========================================
    @AnyThread
    fun scanMusic() {
        scan()
    }

    @AnyThread
    fun searchMusic(query: String, sheetId: String?): Observable<List<Audio>> {
        return MusicProvider.search(query, sheetId)
    }

    //=========================================歌单操作=============================================

    /**
     * 变更当前的播放列表, 通常是用户在一个列表上播放了一首歌才会造成播放列表的切换<br></br>
     * 如果只是变更歌曲列表的内容, 应该使用[MusicProvider.loadMusicBySheet]方法
     *
     * @param sheetId 歌单序列号
     */
    @AnyThread
    fun changeSheet(sheetId: String): Observable<List<Audio>> {
        return MusicProvider.loadMusicBySheet(sheetId).map { audios ->
            onGotData(audios)
            audios
        }
    }

    //==============我的收藏
    fun addFavor(audio: Audio): Observable<Boolean> {
        return MusicProvider.addMusic2Sheet(audio, musicConfig.mSheets[SheetIds.FAVORED])
    }

    @AnyThread
    fun removeFavor(audio: Audio): Observable<Boolean> {
        return MusicProvider.removeMusicFromSheet(audio, musicConfig.mSheets[SheetIds.FAVORED])
    }

    //==============播放记录

    @AnyThread
    fun addRecent(audio: Audio?) {
        if (audio == null) return
        val newRecent = Audio(audio)
        newRecent.joinTimestamp = System.currentTimeMillis()
        EventBus.getDefault().post(OnMusicChanged(newRecent, SheetIds.RECENT))
        MusicProvider.addMusic2Sheet(newRecent, musicConfig.mSheets[SheetIds.FAVORED])
    }

    @AnyThread
    fun removeRecent(audio: Audio?) {
        if (audio == null) return
        MusicProvider.removeMusicFromSheet(audio, musicConfig.mSheets[SheetIds.FAVORED])
    }

    //==============通用
    @AnyThread
    fun isFavored(audio: Audio?): Boolean {
        val sheet = musicConfig.mSheets[SheetIds.FAVORED] ?: return false
        val songs = sheet.songs
        return audio != null && songs != null && songs.contains(audio)
    }

    //=======================================获取歌曲信息===========================================

    /**
     * 获取歌曲封面, 会首先尝试从本地歌曲文件中通过ID3v2来获取封面,如果失败,会尝试从网络自动获取(如果开启了联网)
     *
     * @param music      歌曲对象
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    fun getAlbum(music: Audio, bitmapSize: Int): Observable<Bitmap> {
        return mMusicInfoProvider.getAlbum(music, bitmapSize)
    }

    fun getLrc(bMusic: Audio): Observable<BLyric> {
        return mMusicInfoProvider.getLrc(bMusic)
    }

    fun searchMusicByNet(query: String): Observable<BaiduSearch> {
        return mMusicInfoProvider.searchMusicByNet(query)
    }

    fun getMusicInfoByNet(songId: String): Observable<BaiduMusicInfo> {
        return mMusicInfoProvider.getMusicInfoByNet(songId)
    }

    //========================================事件==================================================

    open class OnGotMusicEvent(var mBMusicList: List<Audio>)

    class OnScannedMusicEvent(bMusics: List<Audio>) : OnGotMusicEvent(bMusics)

    class OnRemoveMusic(var mMusic: Audio, var mBelongTo: String)

    class OnAddMusic(var mMusic: Audio, var mSheetId: String)

    class OnMusicChanged(var mMusic: Audio, var mSheetId: String)

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sMusicManager: MusicManager? = null

        val instance: MusicManager
            get() {
                if (sMusicManager == null) {
                    synchronized(MusicManager::class.java) {
                        if (sMusicManager == null) {
                            sMusicManager = MusicManager()
                        }
                    }
                }
                return sMusicManager!!
            }
    }
}
