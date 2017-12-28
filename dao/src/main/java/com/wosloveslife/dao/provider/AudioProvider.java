package com.wosloveslife.dao.provider;

import android.support.annotation.AnyThread;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.dao.store.AudioStore;
import com.wosloveslife.dao.store.SheetStore;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by leonard on 17/6/17.
 */

public class AudioProvider {

    /**
     * 根据歌单序号获取歌曲列表
     *
     * @param sheetId 歌单序号
     * @return 歌曲列表
     */
    @CheckResult
    @AnyThread
    public static Observable<List<Audio>> loadMusicBySheet(final String sheetId) {
        return SheetStore.loadById(sheetId).map(new Func1<Sheet, List<Audio>>() {
            @Override
            public List<Audio> call(Sheet sheet) {
                return sheet == null ? new ArrayList<Audio>() : sheet.getSongs();
            }
        });
    }

    @CheckResult
    @AnyThread
    public static Observable<List<Audio>> search(String query, @Nullable String sheetId) {
        return AudioStore.search(query, sheetId);
    }

    @CheckResult
    @AnyThread
    public static Observable<Boolean> clearSheetEntities(@Nullable String sheetId) {
        return SheetStore.clearSheetEntities(sheetId);
    }

    public static void insertMusics2Sheet(@NonNull final String sheetId, @NonNull final List<String> audioIds) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<Audio> audioWhere = realm.where(Audio.class);
                boolean flag = true;
                for (String audioId : audioIds) {
                    if (flag) {
                        flag = false;
                        audioWhere.equalTo(Audio.ID, audioId);
                    } else {
                        audioWhere.or().equalTo(Audio.ID, audioId);
                    }
                }
                Sheet sheet = realm.where(Sheet.class).equalTo(Sheet.ID, sheetId).findFirst();
                if (sheet != null) {
                    sheet.getSongs().addAll(audioWhere.findAll());
                }
            }
        });
    }

    public static void insertMusics2Sheet(@NonNull final String sheetId, @NonNull final String audioId) {
        ArrayList<String> audios = new ArrayList<>();
        audios.add(audioId);
        insertMusics2Sheet(sheetId, audios);
    }

    public static void removeMusicFromSheet(@NonNull final String sheetId, @NonNull final List<String> audioIds) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<Audio> audioWhere = realm.where(Audio.class);
                boolean flag = true;
                for (String audioId : audioIds) {
                    if (flag) {
                        flag = false;
                        audioWhere.equalTo(Audio.ID, audioId);
                    } else {
                        audioWhere.or().equalTo(Audio.ID, audioId);
                    }
                }
                Sheet sheet = realm.where(Sheet.class).equalTo(Sheet.ID, sheetId).findFirst();
                if (sheet != null) {
                    sheet.getSongs().removeAll(audioWhere.findAll());
                }
            }
        });
    }

    public static void removeMusicFromSheet(@NonNull final String sheetId, @NonNull final String audioId) {
        List<String> audios = new ArrayList<>();
        audios.add(audioId);
        removeMusicFromSheet(sheetId, audios);
    }


    public static Observable<Boolean> insertOrUpdateSheet(Sheet sheet) {
        return SheetStore.insertOrReplace(sheet);
    }
}
