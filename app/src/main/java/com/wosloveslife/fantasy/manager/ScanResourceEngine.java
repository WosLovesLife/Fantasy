package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;

import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.dao.DaoMaster;
import com.wosloveslife.fantasy.dao.DaoSession;
import com.wosloveslife.fantasy.dao.MusicEntityDao;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/2/8.
 */

public class ScanResourceEngine {

    public static void scanMusic() {

    }

    public static Observable<List<BMusic>> getMusicFromSystemDao(final Context context) {
        return Observable.just(context.getApplicationContext()).map(new Func1<Context, List<BMusic>>() {
            @Override
            public List<BMusic> call(Context context1) {
                List<BMusic> musicList = new ArrayList<>();
                Cursor cursor = context1.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                if (cursor == null) return musicList;
                try {
                    if (cursor.moveToFirst()) {
                        long minDuration = CustomConfiguration.getMinDuration() * 1000;
                        while (cursor.moveToNext()) {
                            // 按照过滤配置过滤最小时间
                            long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                            if (duration < minDuration) {
                                continue;
                            }

                            BMusic bMusic = new BMusic();

                            //歌曲标题
                            bMusic.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                            bMusic.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                            //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                            bMusic.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                            bMusic.duration = duration;
                            //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                            bMusic.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                            bMusic.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                            //=============以下字段都不重要.可有可无
                            //年份
                            bMusic.year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
                            //风格,字段被系统hide.可能根本不存在这东西
//                            bMusic.genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                            //轨道
                            bMusic.track = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
                            //是否是乐曲(歌曲)
                            bMusic.isMusic = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)) != 0;
                            //是否是铃声
                            bMusic.isRingtone = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE)) != 0;
                            //是否是提示音
                            bMusic.isAlarm = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM)) != 0;
                            //是否是通知音
                            bMusic.isNotification = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION)) != 0;
                            //是否是博客电台
                            bMusic.isPodcast = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)) != 0;

                            musicList.add(bMusic);
                        }
                    }
                } catch (Throwable e) {
                    Logger.e(e, "从系统数据库读取音乐失败");
                } finally {
                    cursor.close();
                }

                return musicList;
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<BMusic>> getMusicFromDao(Context context) {
        return Observable.just(context.getApplicationContext()).map(new Func1<Context, List<BMusic>>() {
            @Override
            public List<BMusic> call(Context context1) {
                SQLiteDatabase db = new DaoMaster.DevOpenHelper(context1, "blog.db", null).getWritableDatabase();
                DaoMaster daoMaster = new DaoMaster(db);
                DaoSession daoSession = daoMaster.newSession();
                MusicEntityDao dao = daoSession.getMusicEntityDao();
                return dao.queryBuilder().where(MusicEntityDao.Properties.Duration.gt(CustomConfiguration.getMinDuration() * 1000)).build().list();
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 该操作会清空原来的数据, 请谨慎操作
     *
     * @param context
     * @param list
     * @return
     */
    public static Observable<Boolean> saveMusic2Dao(final Context context, final List<BMusic> list) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SQLiteDatabase db = new DaoMaster.DevOpenHelper(context, "blog.db", null).getWritableDatabase();
                DaoMaster daoMaster = new DaoMaster(db);
                DaoSession daoSession = daoMaster.newSession();
                MusicEntityDao dao = daoSession.getMusicEntityDao();
                dao.insertOrReplaceInTx(list);
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }
}
