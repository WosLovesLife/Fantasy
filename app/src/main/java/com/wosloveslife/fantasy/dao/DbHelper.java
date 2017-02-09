package com.wosloveslife.fantasy.dao;

import android.content.Context;

import com.orhanobut.logger.Logger;

import java.util.List;

import de.greenrobot.dao.AbstractDao;

/**
 * Created by zhangh on 2017/2/10.
 */

public class DbHelper<Dao extends AbstractDao, Data extends Object> {
    Dao mDao;

    public static void init(Context context) {
        MusicDbHelper.getInstance().initDb(context.getApplicationContext());
        FolderDbHelper.getInstance().initDb(context.getApplicationContext());
    }

    public static MusicDbHelper getMusicHelper() {
        return MusicDbHelper.getInstance();
    }

    public static FolderDbHelper getFolderHelper() {
        return FolderDbHelper.getInstance();
    }

    //==============================================================================================

    public void insertOrReplace(Data entity) {
        try {
            mDao.insertOrReplace(entity);
        } catch (Throwable e) {
            Logger.e(e, "插入数据失败");
        }
    }

    public void insertOrReplace(List<Data> entities) {
        if (entities == null || entities.size() == 0) return;
        try {
            mDao.insertOrReplaceInTx(entities);
        } catch (Throwable e) {
            Logger.e(e, "一次性插入数据失败,尝试单独插入, 跳过错误的数据");
            for (Data entity : entities) {
                insertOrReplace(entity);
            }
        }
    }

    public List<Data> loadEntities() {
        return mDao.loadAll();
    }

    public void clear() {
        mDao.deleteAll();
    }
}
