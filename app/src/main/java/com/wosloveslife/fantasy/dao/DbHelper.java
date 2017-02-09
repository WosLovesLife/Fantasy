package com.wosloveslife.fantasy.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by zhangh on 2017/2/10.
 */

final public class DbHelper {
    private DbHelper() {
    }

    public static void init(Context context) {
        SQLiteDatabase db = new DaoMaster.DevOpenHelper(context, "music.db", null).getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();

        MusicDbHelper.getInstance().initDb(daoSession.getMusicEntityDao());
        FolderDbHelper.getInstance().initDb(daoSession.getBFolderDao());
    }

    public static MusicDbHelper getMusicHelper() {
        return MusicDbHelper.getInstance();
    }

    public static FolderDbHelper getFolderHelper() {
        return FolderDbHelper.getInstance();
    }

    //==============================================================================================
}
