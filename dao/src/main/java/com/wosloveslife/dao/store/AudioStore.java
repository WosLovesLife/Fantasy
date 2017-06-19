package com.wosloveslife.dao.store;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;
import rx.Observable;
import rx.functions.Func1;

import static com.wosloveslife.dao.store.BaseStore.getRealm;

/**
 * Created by leonard on 17/6/17.
 */

public class AudioStore {
    public static Observable<List<Audio>> loadAll() {
        return getRealm().map(new Func1<Realm, List<Audio>>() {
            @Override
            public List<Audio> call(Realm realm) {
                return realm.where(Audio.class).findAllSorted(Audio.JOIN_TIMESTAMP, Sort.DESCENDING);
            }
        });
    }

    public static Observable<List<Audio>> loadBySongId(final String songId) {
        return getRealm().map(new Func1<Realm, List<Audio>>() {
            @Override
            public List<Audio> call(Realm realm) {
                return realm.where(Audio.class).equalTo(Audio.ID, songId).findAll();
            }
        });
    }

    public static Observable<List<Audio>> loadBySheetId(final String sheetId) {
        return getRealm().map(new Func1<Realm, List<Audio>>() {
            @Override
            public List<Audio> call(Realm realm) {
                return realm.where(Audio.class)
                        .equalTo(Sheet.class.getName() + "." + Sheet.ID, sheetId)
                        .findAllSorted(Audio.JOIN_TIMESTAMP, Sort.DESCENDING);
            }
        });
    }

    public static Observable<Boolean> clear() {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                return realm.where(Audio.class).findAll().deleteAllFromRealm();
            }
        });
    }

    public static Observable<Boolean> insertOrReplace(final Audio entity) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                realm.insertOrUpdate(entity);
                return true;
            }
        });
    }

    public static Observable<Boolean> insertOrReplace(final List<Audio> entities) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                realm.insertOrUpdate(entities);
                return true;
            }
        });
    }

    public static Observable<Boolean> remove(final String id) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                return realm.where(Audio.class).equalTo(Audio.ID, id).findAll().deleteAllFromRealm();
            }
        });
    }

    public static Observable<Boolean> removeByPath(final String path) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                return realm.where(Audio.class).equalTo(Audio.PATH, path).findAll().deleteAllFromRealm();
            }
        });
    }

    public static Observable<List<Audio>> search(final String query, @Nullable final String songId) {
        return getRealm().map(new Func1<Realm, List<Audio>>() {
            @Override
            public List<Audio> call(Realm realm) {
                RealmQuery<Audio> where = realm.where(Audio.class);
                if (!TextUtils.isEmpty(songId)) {
                    where.equalTo(Sheet.class.getName() + "." + Sheet.ID, songId);
                }
                return where
                        .beginGroup()
                        .like(Audio.TITLE, query)
                        .or()
                        .like(Audio.ARTIST, query)
                        .or()
                        .like(Audio.ALBUM, query)
                        .or()
                        .like(Audio.TITLE_PINYIN, query)
                        .or()
                        .like(Audio.ARTIST_PINYIN, query)
                        .or()
                        .like(Audio.ALBUM_PINYIN, query)
                        .endGroup()
                        .findAll();
            }
        });
    }
}
