package com.wosloveslife.fantasy.dao;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.bean.BMusic;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.dao.query.WhereCondition;

/**
 * Created by zhangh on 2017/2/10.
 */

public class MusicDbHelper {
    private static final MusicDbHelper sMusicDbHelper = new MusicDbHelper();

    private MusicEntityDao mDao;

    private MusicDbHelper() {
    }

    void initDb(MusicEntityDao dao) {
        mDao = dao;
    }

    public static MusicDbHelper getInstance() {
        return sMusicDbHelper;
    }

    public List<BMusic> loadAll() {
        return mDao.loadAll();
    }

    public void clear() {
        mDao.deleteAll();
    }

    public void insertOrReplace(BMusic entity) {
        try {
            mDao.insertOrReplace(entity);
        } catch (Throwable e) {
            Logger.e(e, "插入数据失败");
        }
    }

    public void insertOrReplace(List<BMusic> entities) {
        if (entities == null || entities.size() == 0) return;
        try {
            mDao.insertOrReplaceInTx(entities);
        } catch (Throwable e) {
            Logger.e(e, "一次性插入数据失败,尝试单独插入, 跳过错误的数据");
            for (BMusic entity : entities) {
                insertOrReplace(entity);
            }
        }
    }

    public List<BMusic> loadEntitiesBySongId(String songId) {
        return mDao.queryBuilder()
                .where(MusicEntityDao.Properties.SongId.eq(songId))
                .build()
                .list();
    }

    public List<BMusic> loadEntities(String path, String belong) {
        return mDao.queryBuilder()
                .where(MusicEntityDao.Properties.Path.eq(path), MusicEntityDao.Properties.BelongTo.eq(belong))
                .build()
                .list();
    }

    public List<BMusic> loadSheet(String belong) {
        return mDao.queryBuilder()
                .where(MusicEntityDao.Properties.BelongTo.eq(belong))
                .orderDesc(MusicEntityDao.Properties.JoinTimestamp)
                .build()
                .list();
    }

    public void remove(BMusic bMusic) {
        mDao.delete(bMusic);
    }

    public void remove(String belong) {
        mDao.queryBuilder()
                .where(MusicEntityDao.Properties.BelongTo.eq(belong))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public void removeById(String songId, String belong) {
        mDao.queryBuilder()
                .where(MusicEntityDao.Properties.SongId.eq(songId), MusicEntityDao.Properties.BelongTo.eq(belong))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public void removeByPath(String path, String belong) {
        mDao.queryBuilder()
                .where(MusicEntityDao.Properties.Path.eq(path), MusicEntityDao.Properties.BelongTo.eq(belong))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    @NonNull
    public Set<String> loadFavored(String belong) {
        Set<String> favored = new HashSet<>();
        Cursor query = mDao.queryBuilder().where(MusicEntityDao.Properties.BelongTo.eq(belong)).buildCursor().query();
        if (query == null) return favored;
        try {
            if (query.moveToFirst()) {
                int ordinal = MusicEntityDao.Properties.Title.ordinal;
                do {
                    favored.add(query.getString(ordinal));
                } while (query.moveToNext());
            }
        } catch (Throwable e) {
            WLogger.w("loadFavored : 获取收藏列表失败 e = " + e);
        } finally {
            query.close();
        }
        return favored;
    }

    public List<BMusic> search(String title) {
        return mDao.queryBuilder()
                .where(MusicEntityDao.Properties.Title.like("%" + title + "%"))
                .build()
                .list();
    }

    public List<BMusic> search(String query, String belongTo) {
        WhereCondition condition = mDao.queryBuilder().or(
                MusicEntityDao.Properties.Title.like("%" + query + "%"),
                MusicEntityDao.Properties.Artist.like("%" + query + "%"),
                MusicEntityDao.Properties.Album.like("%" + query + "%"));
        return mDao
                .queryBuilder()
                .where(condition, MusicEntityDao.Properties.BelongTo.eq(belongTo))
                .build()
                .list();
    }
}
