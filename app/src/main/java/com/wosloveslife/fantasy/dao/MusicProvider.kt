package com.wosloveslife.fantasy.dao

import android.content.Context
import android.support.annotation.AnyThread
import android.text.TextUtils

import com.wosloveslife.dao.Audio
import com.wosloveslife.dao.Sheet
import com.wosloveslife.dao.store.AudioStore
import com.wosloveslife.dao.store.SheetStore
import com.wosloveslife.fantasy.dao.engine.ScanResourceEngine

import io.realm.RealmList
import rx.Observable
import rx.functions.Func1

/**
 * Created by leonard on 17/6/17.
 */

object MusicProvider {

    @AnyThread
    fun scanSysDB(context: Context): Observable<List<Audio>> {
        return Observable.just(context.applicationContext)
                .map { c -> ScanResourceEngine.getMusicFromSystemDao(c) }
    }

    /**
     * 根据歌单序号获取歌曲列表
     *
     * @param sheetId 歌单序号
     * @return 歌曲列表
     */
    @AnyThread
    fun loadMusicBySheet(sheetId: String): Observable<List<Audio>> {
        return AudioStore.loadBySheetId(sheetId)
    }

    @AnyThread
    fun search(query: String, sheetId: String?): Observable<List<Audio>> {
        return AudioStore.search(query, sheetId)
    }

    @AnyThread
    fun clearSheetEntities(sheetId: String?): Observable<Boolean> {
        return SheetStore.clearSheetEntities(sheetId)
    }

    fun insertMusics(sheetId: String, audios: List<Audio>): Observable<Sheet> {
        return SheetStore.loadById(sheetId)
                .map(Func1 { sheets ->
                    if (sheets != null && sheets.size > 0) {
                        val sheet = sheets[0]
                        sheet.songs = RealmList()
                        sheet.songs!!.addAll(audios)
                        val success = SheetStore.insertOrReplace(sheet).toBlocking().first()
                        if (!success) {
                            throw IllegalStateException("存储失败")
                        }
                        return@Func1 sheet
                    }
                    null
                })
    }

    fun addMusic2Sheet(audio: Audio?, sheet: Sheet?): Observable<Boolean> {
        if (audio == null || sheet == null) return Observable.just(false)
        if (sheet.songs == null) {
            sheet.songs = RealmList()
        } else if (sheet.songs!!.contains(audio)) {
            return Observable.just(false)
        }

        sheet.songs!!.add(audio)
        return AudioStore.insertOrReplace(audio)
    }

    fun removeMusicFromSheet(audio: Audio, sheet: Sheet?): Observable<Boolean> {
        if (TextUtils.isEmpty(audio.id) || sheet == null || sheet.songs == null) {
            return Observable.just(false)
        }

        sheet.songs!!.remove(audio)
        return SheetStore.insertOrReplace(sheet)
    }


    fun insertOrUpdateSheet(sheet: Sheet): Observable<Boolean> {
        return SheetStore.insertOrReplace(sheet)
    }
}
