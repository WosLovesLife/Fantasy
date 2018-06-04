package com.wosloveslife.fantasy.dao

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.WorkerThread
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.Mp3File
import com.orhanobut.logger.Logger
import com.wosloveslife.dao.Audio
import com.wosloveslife.dao.store.AudioStore
import com.wosloveslife.fantasy.album.AlbumFile
import com.wosloveslife.fantasy.baidu.BaiduMusicInfo
import com.wosloveslife.fantasy.baidu.BaiduSearch
import com.wosloveslife.fantasy.lrc.BLyric
import com.wosloveslife.fantasy.lrc.LrcFile
import com.wosloveslife.fantasy.lrc.LrcParser
import com.wosloveslife.fantasy.presenter.MusicPresenter
import com.yesing.blibrary_wos.utils.assist.WLogger
import com.yesing.blibrary_wos.utils.photo.BitmapUtils
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.schedulers.Schedulers
import java.io.File

/**
 * Created by zhangh on 2017/3/20.
 */

class MusicInfoProvider private constructor() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sMusicInfoProvider: MusicInfoProvider? = null

        fun getInstance(): MusicInfoProvider {
            if (sMusicInfoProvider == null) {
                synchronized(MusicInfoProvider::class) {
                    if (sMusicInfoProvider == null) {
                        sMusicInfoProvider = MusicInfoProvider()
                    }
                }
            }
            return sMusicInfoProvider!!
        }
    }

    private lateinit var mContext: Context
    private lateinit var mPresenter: MusicPresenter


    fun init(context: Context) {
        mContext = context.applicationContext
        mPresenter = MusicPresenter(mContext)
    }

    //=======================================获取歌曲信息===========================================

    /**
     * 获取歌曲封面, 会首先尝试从本地歌曲文件中通过ID3v2来获取封面,如果失败,会尝试从网络自动获取(如果开启了联网)
     *
     * @param music      歌曲对象
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    fun getAlbum(music: Audio, bitmapSize: Int): Observable<Bitmap> {
        val fileOb = Observable.create(Observable.OnSubscribe<Bitmap> { subscriber ->
            val albumFile = AlbumFile.getAlbumFile(mContext, music.album)
            if (albumFile != null) {
                val bitmap = BitmapUtils.getScaledDrawable(albumFile.absolutePath, bitmapSize.toFloat(), bitmapSize.toFloat(), Bitmap.Config.RGB_565)
                subscriber.onNext(bitmap)
            }
            subscriber.onCompleted()
        })

        val mp3Ob = Observable.create(Observable.OnSubscribe<Bitmap> { subscriber ->
            try {
                val mp3file = Mp3File(music.path)
                if (mp3file.hasId3v2Tag()) {
                    val bitmap = getAlbumFromID3v2(mp3file.id3v2Tag, bitmapSize)
                    if (bitmap != null) {
                        AlbumFile.saveAlbum(mContext, music.album, bitmap)
                        subscriber.onNext(bitmap)
                    }
                }
            } catch (e: Throwable) {
                Logger.w("从本地歌曲中解析封面失败,可忽略 error : $e")
            }

            subscriber.onCompleted()
        })

        val info2BitmapOb = Func1<BaiduMusicInfo, Observable<out Bitmap>> { baiduMusicInfo ->
            if (baiduMusicInfo != null) {
                val songinfo = baiduMusicInfo.songinfo
                if (songinfo != null) {
                    val albumTitle = songinfo.album_title
                    var albumAddress = songinfo.pic_premium
                    if (TextUtils.isEmpty(albumAddress)) {
                        albumAddress = songinfo.pic_big
                        if (TextUtils.isEmpty(albumAddress)) {
                            albumAddress = songinfo.pic_radio
                            if (TextUtils.isEmpty(albumAddress)) {
                                albumAddress = songinfo.pic_small
                            }
                        }
                    }

                    if (TextUtils.equals(music.album, albumTitle)) {
                        music.album = albumTitle
                        AudioStore.insertOrReplace(music).toBlocking().first()
                    }
                    return@Func1 getAlbumByAddress(albumAddress, music.album, bitmapSize)
                }
            }
            null
        }

        val query = music.title!! + if (TextUtils.equals(music.artist, "<unknown>")) " " else " " + music.artist!!
        val netOb: Observable<Bitmap>
        if (!music.isOnline) {
            netOb = mPresenter.searchFromBaidu(query)
                    .concatMap(Func1<BaiduSearch, Observable<BaiduMusicInfo>> { baiduSearch ->
                        if (baiduSearch != null) {
                            try {
                                return@Func1 mPresenter.getMusicInfo(baiduSearch.song[0].songid)
                            } catch (e: Throwable) {
                                WLogger.w("call : 未搜索到歌曲,可忽略 e = $e")
                            }

                        }
                        null
                    })
                    .concatMap(info2BitmapOb)
        } else {
            netOb = mPresenter.getMusicInfo(music.id).concatMap(info2BitmapOb)
        }

        return fileOb
                .switchIfEmpty(mp3Ob)
                .switchIfEmpty(netOb)
                .subscribeOn(Schedulers.io())
    }

    private fun getAlbumByAddress(address: String, album: String?, bitmapSize: Int): Observable<Bitmap> {
        return Observable.create(Observable.OnSubscribe<Bitmap> { subscriber ->
            Glide.with(mContext)
                    .load(address)
                    .downloadOnly(object : SimpleTarget<File>() {
                        override fun onResourceReady(resource: File?, glideAnimation: GlideAnimation<in File>) {
                            if (resource != null) {
                                val bitmap = BitmapUtils.getScaledDrawable(resource.absolutePath, bitmapSize.toFloat(), bitmapSize.toFloat(), Bitmap.Config.RGB_565)
                                if (bitmap != null) {
                                    AlbumFile.saveAlbum(mContext, album, bitmap)
                                }
                                subscriber.onNext(bitmap)
                            }
                            subscriber.onCompleted()
                        }
                    })
        }).subscribeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 从歌曲文件的ID3v2字段中读取封面信息并更新封面
     *
     * @param id3v2Tag   ID3v2Tag,通过它来从歌曲文件中读取封面
     * @param bitmapSize 对封面尺寸进行压缩,防止OOM和速度过慢问题.<=0时不压缩
     */
    @WorkerThread
    private fun getAlbumFromID3v2(id3v2Tag: ID3v2, bitmapSize: Int): Bitmap? {
        var bitmap: Bitmap? = null
        val image = id3v2Tag.albumImage
        if (image != null && bitmapSize > 0) {
            /* 通过自定义Option缩减Bitmap生成的时间.以及避免OOM */
            bitmap = BitmapUtils.getScaledDrawable(image, bitmapSize.toFloat(), bitmapSize.toFloat(), Bitmap.Config.RGB_565)
        }
        return bitmap
    }

    fun getLrc(audio: Audio): Observable<BLyric> {
        val fileOb = Observable.create(Observable.OnSubscribe<BLyric> { subscriber ->
            val lrc = LrcFile.getLrc(mContext, audio.title)
            if (!TextUtils.isEmpty(lrc)) {
                val bLyric = LrcParser.parseLrc(lrc)
                if (bLyric != null) {
                    /* 重点!!! 如果执行了onNext()即表明内容非empty,后面的Observer就不会执行 */
                    subscriber.onNext(bLyric)
                }
            }
            subscriber.onCompleted()
        })

        val query = audio.title!! + if (TextUtils.equals(audio.artist, "<unknown>")) " " else " " + audio.artist!!
        val id3v2Ob = Observable.create(Observable.OnSubscribe<BLyric> { subscriber ->
            try {
                val mp3file = Mp3File(audio.path)
                if (mp3file.hasId3v2Tag()) {
                    val lyrics = mp3file.id3v2Tag.lyrics
                    if (!TextUtils.isEmpty(lyrics)) {
                        val bLyric = LrcParser.parseLrc(lyrics)
                        if (bLyric != null) {
                            LrcFile.saveLrc(mContext, audio.title, lyrics)
                            subscriber.onNext(bLyric)
                        }
                    }
                }
            } catch (e: Throwable) {
                Logger.w("从本地歌曲中解析歌词失败,可忽略 error : $e")
            }

            subscriber.onCompleted()
        })

        val netOb = mPresenter.searchLrc(query).map { baiduLrc ->
            var bLyric: BLyric? = null
            if (baiduLrc != null) {
                val lrc = baiduLrc.lrcContent
                if (!TextUtils.isEmpty(lrc)) {
                    LrcFile.saveLrc(mContext, audio.title, lrc)
                    bLyric = LrcParser.parseLrc(lrc)
                }
            }
            bLyric
        }
        return fileOb
                .switchIfEmpty(id3v2Ob)
                .switchIfEmpty(netOb)
                .subscribeOn(Schedulers.io())
    }

    fun searchMusicByNet(query: String): Observable<BaiduSearch> {
        return mPresenter.searchFromBaidu(query)
    }

    fun getMusicInfoByNet(songId: String): Observable<BaiduMusicInfo> {
        return mPresenter.getMusicInfo(songId)
    }
}
