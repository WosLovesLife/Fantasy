package com.wosloveslife.fantasy.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wosloveslife.fantasy.bean.BFolder;
import com.wosloveslife.fantasy.dao.folder.FolderDao;
import com.wosloveslife.fantasy.dao.folder.FolderDaoMaster;
import com.wosloveslife.fantasy.dao.folder.FolderDaoSession;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhangh on 2017/2/10.
 */

public class FolderDbHelper extends DbHelper<FolderDao, BFolder> {
    private static final FolderDbHelper sFolderDbHelper = new FolderDbHelper();

    private FolderDaoMaster mDaoMaster;
    private FolderDaoSession mDaoSession;

    private FolderDbHelper() {
    }

    public void initDb(Context context) {
        SQLiteDatabase db = new FolderDaoMaster.DevOpenHelper(context, "folder.db", null).getWritableDatabase();
        mDaoMaster = new FolderDaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
        mDao = mDaoSession.getBFolderDao();
    }

    public static FolderDbHelper getInstance() {
        return sFolderDbHelper;
    }

    public Set<String> getFilteredFolder() {
        Set<String> fileFilter = new HashSet<>();
        Cursor query = mDao.queryBuilder().where(FolderDao.Properties.IsFiltered.eq(true)).buildCursor().query();
        if (query == null) return null;
        try {
            int ordinal = FolderDao.Properties.FilePath.ordinal;
            if (query.moveToFirst()) {
                do {
                    fileFilter.add(query.getString(ordinal));
                } while (query.moveToNext());
            }
        } catch (Throwable e) {
            WLogger.w("call : 查询文件夹列表失败 ");
        } finally {
            query.close();
        }
        return fileFilter;
    }
}
