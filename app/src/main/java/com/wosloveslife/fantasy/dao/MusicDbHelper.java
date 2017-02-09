package com.wosloveslife.fantasy.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.wosloveslife.fantasy.bean.BMusic;

/**
 * Created by zhangh on 2017/2/10.
 */

public class MusicDbHelper extends DbHelper<MusicEntityDao, BMusic> {
    private static final MusicDbHelper sMusicDbHelper = new MusicDbHelper();

    private MusicDaoMaster mDaoMaster;
    private MusicDaoSession mDaoSession;

    private Context mContext;

    private MusicDbHelper() {
    }

    public void initDb(Context context) {
        mContext = context.getApplicationContext();
        SQLiteDatabase db = new MusicDaoMaster.DevOpenHelper(mContext, "music.db", null).getWritableDatabase();
        mDaoMaster = new MusicDaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
        mDao = mDaoSession.getMusicEntityDao();
    }

    public static MusicDbHelper getInstance() {
        return sMusicDbHelper;
    }
}
