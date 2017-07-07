package com.wosloveslife.fantasy.dao.engine;

import android.content.Context;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.dao.store.AudioStore;
import com.wosloveslife.dao.store.SheetStore;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by leonard on 17/6/17.
 */

public class MusicProvider {

    @AnyThread
    public static Observable<List<Audio>> scanSysDB(Context context) {
        return Observable.just(context.getApplicationContext())
                .map(new Func1<Context, List<Audio>>() {
                    @Override
                    public List<Audio> call(Context context) {
                        return ScanResourceEngine.getMusicFromSystemDao(context);
                    }
                });
    }

    /**
     * 根据歌单序号获取歌曲列表
     *
     * @param sheetId 歌单序号
     * @return 歌曲列表
     */
    @AnyThread
    public static Observable<List<Audio>> loadMusicBySheet(final String sheetId) {
        return SheetStore.loadById(sheetId).map(new Func1<Sheet, List<Audio>>() {
            @Override
            public List<Audio> call(Sheet sheet) {
                return sheet == null ? new ArrayList<Audio>() : sheet.songs;
            }
        });
    }

    @AnyThread
    public static Observable<List<Audio>> search(String query, @Nullable String sheetId) {
        return AudioStore.search(query, sheetId);
    }

    @AnyThread
    public static Observable<Boolean> clearSheetEntities(@Nullable String sheetId) {
        return SheetStore.clearSheetEntities(sheetId);
    }

    public static Observable<Sheet> insertMusics(final String sheetId, final List<Audio> audios) {
        return SheetStore.loadById(sheetId)
                .map(new Func1<Sheet, Sheet>() {
                    @Nullable
                    @Override
                    public Sheet call(Sheet sheet) {
                        if (sheet != null) {
                            sheet.songs = new RealmList<>();
                            sheet.songs.addAll(audios);
                            Boolean success = SheetStore.insertOrReplace(sheet).toBlocking().first();
                            if (!success) {
                                throw new IllegalStateException("存储失败");
                            }
                            return sheet;
                        }
                        return null;
                    }
                });
    }

    public static Observable<Boolean> addMusic2Sheet(Audio audio, Sheet sheet) {
        if (audio == null || sheet == null) return Observable.just(false);
        if (sheet.songs == null) {
            sheet.songs = new RealmList<>();
        } else if (sheet.songs.contains(audio)) {
            return Observable.just(false);
        }

        sheet.songs.add(audio);
        return AudioStore.insertOrReplace(audio);
    }

    public static Observable<Boolean> addMusic2Sheet(final Audio audio, final String sheetId) {
        return SheetStore.loadById(sheetId)
                .flatMap(new Func1<Sheet, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Sheet sheet) {
                        if (sheet != null) {
                            RealmList<Audio> songs = sheet.songs;
                            if (songs != null) {
                                for (Audio song : songs) {
                                    if (TextUtils.equals(song.id, audio.id)) {
                                        songs.remove(song);
                                    }
                                }
                            } else {
                                songs = new RealmList<>();
                            }
                            songs.add(audio);
                            return SheetStore.insertOrReplace(sheet);
                        }
                        return Observable.just(false);
                    }
                });
    }

    public static Observable<Boolean> removeMusicFromSheet(Audio audio, Sheet sheet) {
        if (TextUtils.isEmpty(audio.id) || sheet == null || sheet.songs == null) {
            return Observable.just(false);
        }

        sheet.songs.remove(audio);
        return SheetStore.insertOrReplace(sheet);
    }


    public static Observable<Boolean> insertOrUpdateSheet(Sheet sheet) {
        return SheetStore.insertOrReplace(sheet);
    }
}
