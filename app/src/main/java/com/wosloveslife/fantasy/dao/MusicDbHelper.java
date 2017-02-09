package com.wosloveslife.fantasy.dao;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.bean.BMusic;

import java.util.List;

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

    private void insertOrReplace(BMusic entity) {
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

    public List<BMusic> loadEntities() {
        return mDao.loadAll();
    }

    public void clear() {
        mDao.deleteAll();
    }
}
