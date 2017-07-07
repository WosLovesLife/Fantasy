package com.wosloveslife.dao.store;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wosloveslife.dao.Sheet;

import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by leonard on 17/6/17.
 */

public class SheetStore extends BaseStore {
    public static Observable<List<Sheet>> loadAll() {
        return getRealm().map(new Func1<Realm, List<Sheet>>() {
            @Override
            public List<Sheet> call(Realm realm) {
                return realm.where(Sheet.class).findAll();
            }
        });
    }

    public static Observable<Sheet> loadById(final String id) {
        return getRealmTx().map(new Func1<Realm, Sheet>() {
            @Override
            public Sheet call(Realm realm) {
                return realm.where(Sheet.class).equalTo(Sheet.ID, id).findFirst();
            }
        });
    }

    public static Observable<List<Sheet>> loadByTitle(final String title) {
        return getRealm().map(new Func1<Realm, List<Sheet>>() {
            @Override
            public List<Sheet> call(Realm realm) {
                return realm.where(Sheet.class).equalTo(Sheet.TITLE, title).findAll();
            }
        });
    }

    public static Observable<List<Sheet>> loadByType(@Sheet.Type final int type) {
        return getRealm().map(new Func1<Realm, List<Sheet>>() {
            @Override
            public List<Sheet> call(Realm realm) {
                return realm.where(Sheet.class).equalTo(Sheet.TYPE, type).findAll();
            }
        });
    }

    public static Observable<List<Sheet>> loadByTypeAndState(@Sheet.Type final int type, @Sheet.State final int state) {
        return getRealm().map(new Func1<Realm, List<Sheet>>() {
            @Override
            public List<Sheet> call(Realm realm) {
                return realm.where(Sheet.class).equalTo(Sheet.TYPE, type).equalTo(Sheet.STATE, state).findAll();
            }
        });
    }

    public static Observable<Boolean> insertOrReplace(final Sheet entity) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                realm.insertOrUpdate(entity);
                return true;
            }
        });
    }

    public static Observable<Boolean> insertOrReplace(final Collection<Sheet> entities) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                realm.insertOrUpdate(entities);
                return true;
            }
        });
    }

    public static Observable<Boolean> clear(@Nullable final String sheetId) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                return realm.where(Sheet.class).findAll().deleteAllFromRealm();
            }
        });
    }

    /** 删除歌单内的歌曲 */
    public static Observable<Boolean> clearSheetEntities(@Nullable final String sheetId) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                RealmQuery<Sheet> where = realm.where(Sheet.class);
                if (!TextUtils.isEmpty(sheetId)) {
                    where.equalTo(Sheet.ID, sheetId);
                }
                RealmResults<Sheet> sheets = where.findAll();
                for (Sheet sheet : sheets) {
                    sheet.songs.clear();
                }
                return true;
            }
        });
    }

    public static Observable<Boolean> remove(final String id) {
        return getRealm().map(new Func1<Realm, Boolean>() {
            @Override
            public Boolean call(Realm realm) {
                return realm.where(Sheet.class).equalTo(Sheet.ID, id).findAll().deleteAllFromRealm();
            }
        });
    }
}
