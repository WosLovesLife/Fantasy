package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.bean.BLyric;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.event.RefreshEvent;
import com.yesing.blibrary_wos.utils.photo.BitmapUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicManager {
    private static final MusicManager sMusicManager = new MusicManager();

    Context mContext;

    //=============Var
    boolean mLoading;

    //=============Data
    List<String> mPinyinIndex;
    List<BMusic> mMusicList;
    BMusic mCurrentMusic;

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

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new SubscriberAdapter<Object>() {
                    @Override
                    public void onNext(Object o) {
                        super.onNext(o);
                        mLoading = false;
                    }
                });
    }

    @WorkerThread
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
            Logger.e(e, "从系统数据库读取音乐失败");
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

    //==============================================================================================
    public int getIndex(BMusic music) {
        return mMusicList.indexOf(music);
    }

    public int getMusicCount() {
        return mMusicList.size();
    }

    @Nullable
    public BMusic getMusic(int position) {
        if (position >= 0 && position < mMusicList.size()) {
            return mMusicList.get(position);
        }
        return null;
    }

    @Nullable
    public BMusic getMusic(String pinyin) {
        if (!TextUtils.isEmpty(pinyin)) {
            int index = mPinyinIndex.indexOf(pinyin);
            return getMusic(index);
        }
        return null;
    }

    public BMusic getFirst() {
        return getMusic(0);
    }

    public BMusic getLast() {
        return getMusic(getMusicCount() - 1);
    }

    @Nullable
    public BMusic getNext(BMusic music) {
        if (music != null) {
            int index = mMusicList.indexOf(music) + 1;
            if (index < mMusicList.size()) {
                return getMusic(index);
            }
            return getNext(music.pinyinIndex);
        }
        return null;
    }

    @Nullable
    public BMusic getNext(String pinyin) {
        if (!TextUtils.isEmpty(pinyin)) {
            int index = mPinyinIndex.indexOf(pinyin);
            return getMusic(index + 1);
        }
        return null;
    }

    @Nullable
    public BMusic getPrevious(BMusic music) {
        if (music != null) {
            int index = mMusicList.indexOf(music) - 1;
            if (index >= 0) {
                return getMusic(index);
            }
            return getPrevious(music.pinyinIndex);
        }
        return null;
    }

    @Nullable
    public BMusic getPrevious(String pinyin) {
        if (!TextUtils.isEmpty(pinyin)) {
            int index = mPinyinIndex.indexOf(pinyin);
            return getMusic(index - 1);
        }
        return null;
    }

    //=======================================工具方法=============================================

    /**
     * 获取歌曲封面, 会首先尝试从本地歌曲文件中通过ID3v2来获取封面,如果失败,会尝试从网络自动获取(如果开启了联网)
     *
     * @param resPath    本地歌曲路径
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    public static Observable<Bitmap> getAlbum(final String resPath, final int bitmapSize) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Logger.d("尝试从本地歌曲文件中解析 时间 = " + System.currentTimeMillis());
                Bitmap bitmap = null;
                try {
                    Mp3File mp3file = new Mp3File(resPath);
                    if (mp3file.hasId3v2Tag()) {
                        Logger.d("开始从ID3v2中解析封面 时间 = " + System.currentTimeMillis());
                        bitmap = getAlbumFromID3v2(mp3file.getId3v2Tag(), bitmapSize);
                    }
                } catch (Throwable e) {
                    Logger.w("从本地歌曲中解析封面失败,可忽略 error : " + e);
                }

                subscriber.onNext(bitmap);
                subscriber.onCompleted();
            }
        })
                .map(new Func1<Bitmap, Bitmap>() {
                    @Override
                    public Bitmap call(Bitmap bitmap) {
                        if (bitmap == null) {
                            /* TODO 联网获取歌曲信息,并存储本地歌曲文件 */
                        }
                        return bitmap;
                    }
                })
                .subscribeOn(Schedulers.computation());
    }

    /**
     * 从歌曲文件的ID3v2字段中读取封面信息并更新封面
     *
     * @param id3v2Tag   ID3v2Tag,通过它来从歌曲文件中读取封面
     * @param bitmapSize 对封面尺寸进行压缩,防止OOM和速度过慢问题.<=0时不压缩
     */
    @WorkerThread
    private static Bitmap getAlbumFromID3v2(final ID3v2 id3v2Tag, final int bitmapSize) {
        Bitmap bitmap = null;
        byte[] image = id3v2Tag.getAlbumImage();
        if (image != null && bitmapSize > 0) {
            /* 通过自定义Option缩减Bitmap生成的时间.以及避免OOM */
            bitmap = BitmapUtils.getScaledDrawable(image, bitmapSize, bitmapSize, Bitmap.Config.RGB_565);
        }
        return bitmap;
    }

    public static BLyric getLrc(final String resPath) {
        BLyric bLyric = null;
        try {
            Mp3File mp3file = new Mp3File(resPath);
            if (mp3file.hasId3v2Tag()) {
                String lyrics = mp3file.getId3v2Tag().getLyrics();
                if (!TextUtils.isEmpty(lyrics)) {
                    bLyric = generateLrcData(lyrics);
                }
            }
        } catch (Throwable e) {
            Logger.w("从本地歌曲中解析歌词失败,可忽略 error : " + e);
        }

        if (bLyric == null) {
            /* TODO 没有内嵌歌词 ,尝试从网络获取,如果开启了网络 */
        }

        return bLyric;
    }

    public static BLyric generateLrcData(String lrcContent) {
        if (TextUtils.isEmpty(lrcContent)) return null;

        lrcContent = lrcContent.replaceAll("\\n", "");
        List<BLyric.LyricLine> lrcLines = new ArrayList<>();
        int startPoint = 0;
        int leftIndex;
        int rightIndex;
        while ((leftIndex = lrcContent.indexOf("[", startPoint)) != -1) {
            rightIndex = lrcContent.indexOf("]", ++startPoint);
            if (rightIndex == -1) continue;
            String time = lrcContent.substring(leftIndex + 1, rightIndex);
            int timestamp = lrcTime2Timestamp(time);

            startPoint = rightIndex + 1;
            if (startPoint >= lrcContent.length()) break;

            int i = lrcContent.indexOf("[", startPoint);
            if (i == -1) {
                i = lrcContent.length();
            }

            String content = lrcContent.substring(startPoint, i);

            lrcLines.add(new BLyric.LyricLine(timestamp, content));
        }
        return new BLyric(lrcLines);
    }

    private static int lrcTime2Timestamp(String time) {
        int minutes = string2Int(time.substring(0, 2));
        int seconds = string2Int(time.substring(3, 5));
        int milliseconds = string2Int(time.substring(6, 8));
        return minutes * 60 * 1000 + seconds * 1000 + milliseconds * 10;
    }

    public static int string2Int(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Throwable e) {
            Logger.w("时间转换错误");
        }
        return 0;
    }


    //========================================事件==================================================
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

    public static class OnPlayStateChangedEvent {
        public boolean mPlay;

        public OnPlayStateChangedEvent(boolean play) {
            mPlay = play;
        }
    }

    public static class OnChangedMusicEvent {
        public BMusic mCurrentMusic;
        public BMusic mPreviousMusic;

        public OnChangedMusicEvent(BMusic previousMusic, BMusic currentMusic) {
            mPreviousMusic = previousMusic;
            mCurrentMusic = currentMusic;
        }
    }
}
