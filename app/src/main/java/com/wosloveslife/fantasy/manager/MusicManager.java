package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.arasthel.swissknife.annotations.OnBackground;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.event.RefreshEvent;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicManager {
    private static final MusicManager sMusicManager = new MusicManager();

    Context mContext;
    List<String> mPinyinIndex;
    List<BMusic> mMusicList;

    //=============Var
    boolean mLoading;

    private MusicManager() {
        mPinyinIndex = new ArrayList<>();
        mMusicList = new ArrayList<>();
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();

        dispose();
    }

    public static MusicManager getInstance() {
        return sMusicManager;
    }

    private void dispose() {
        if (mLoading) return;
        mLoading = true;

        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                EventBus.getDefault().post(new RefreshEventM(true));

                mPinyinIndex.clear();
                mMusicList.clear();

                List<BMusic> musicFromSystemDb = getMusicFromSystemDb();
                if (musicFromSystemDb != null && musicFromSystemDb.size() > 0) {
                    mMusicList.addAll(musicFromSystemDb);
                }

                EventBus.getDefault().post(new OnGotMusicEvent(mPinyinIndex, mMusicList));
                EventBus.getDefault().post(new RefreshEventM(false));

                subscriber.onNext("");
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        mLoading = false;
                    }
                });
    }

    @OnBackground
    private List<BMusic> getMusicFromSystemDb() {
        List<BMusic> musicList = new ArrayList<>();
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) return musicList;
        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    BMusic bMusic = new BMusic();

                    //歌曲编号
                    bMusic.id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    //歌曲标题
                    bMusic.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                    bMusic.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                    bMusic.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                    bMusic.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                    bMusic.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                    bMusic.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                    musicList.add(bMusic);

                    cursor.moveToNext();
                }
            }
        } catch (Throwable e) {
            WLogger.logE("从系统数据库读取音乐失败", e);
        } finally {
            cursor.close();
        }

        return musicList;
    }

    //==============================================================================================
    public List<String> getPinyinIndex() {
        if (mLoading) return null;
        return mPinyinIndex;
    }

    public List<BMusic> getMusicList() {
        if (mLoading) return null;
        return mMusicList;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public static class RefreshEventM extends RefreshEvent {
        public RefreshEventM(boolean refreshing) {
            super(refreshing);
        }
    }

    public static class OnGotMusicEvent {
        public List<String> mPinyinIndex;
        public List<BMusic> mBMusicList;

        public OnGotMusicEvent(List<String> pinyinIndex, List<BMusic> bMusics) {
            mPinyinIndex = pinyinIndex;
            mBMusicList = bMusics;
        }
    }
}
