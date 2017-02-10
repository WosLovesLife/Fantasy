package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.bean.BLyric;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.dao.DbHelper;
import com.wosloveslife.fantasy.event.RefreshEvent;
import com.wosloveslife.fantasy.presenter.MusicPresenter;
import com.yesing.blibrary_wos.utils.photo.BitmapUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicManager {
    private static final Pattern PATTERN_LRC_INTERVAL = Pattern.compile("\\u005b[0-9]{2}:[0-9]{2}\\u002e[0-9]{2}\\u005d");
    private static MusicManager sMusicManager;

    Context mContext;
    MusicPresenter mPresenter;

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
        mPresenter = new MusicPresenter(mContext);
        dispose();
    }

    public static MusicManager getInstance() {
        if (sMusicManager == null) {
            synchronized (MusicManager.class) {
                if (sMusicManager == null) {
                    sMusicManager = new MusicManager();
                }
            }
        }
        return sMusicManager;
    }

    private void dispose() {
        if (mLoading) return;
        mLoading = true;

        EventBus.getDefault().post(new RefreshEventM(true));
        List<BMusic> bMusics = DbHelper.getMusicHelper().loadEntities();
        if (bMusics == null || bMusics.size() == 0) {
            scan();
        } else {
            onGotData(bMusics);
        }
    }

    private void scan() {
        EventBus.getDefault().post(new RefreshEventM(true));
        ScanResourceEngine.getMusicFromSystemDao(mContext).subscribe(new SubscriberAdapter<List<BMusic>>() {
            @Override
            public void onError(Throwable e) {
                super.onError(e);
                onGotData(null);
            }

            @Override
            public void onNext(List<BMusic> bMusics) {
                super.onNext(bMusics);
                onGotData(bMusics);

                if (bMusics != null && bMusics.size() > 0) {
                    DbHelper.getMusicHelper().clear();
                    DbHelper.getMusicHelper().insertOrReplace(mMusicList);
                }
            }
        });
    }

    private void onGotData(List<BMusic> bMusics) {
        mPinyinIndex.clear();
        mMusicList.clear();

        if (bMusics != null && bMusics.size() > 0) {
            mMusicList.addAll(bMusics);
        }

        EventBus.getDefault().post(new OnGotMusicEvent(mPinyinIndex, mMusicList));
        EventBus.getDefault().post(new RefreshEventM(false));
        mLoading = false;
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
            return getNext(music.titlePinyin);
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
            return getPrevious(music.titlePinyin);
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

    public void scanMusic() {
        if (mLoading) return;
        mLoading = true;
        scan();
    }

    public void addFavor(BMusic bMusic) {
        if (bMusic == null) return;
        BMusic music = mMusicList.get(mMusicList.indexOf(bMusic));
        music.setFavorite(true);
        addMusicBelongTo(music, "1");
    }

    public void removeFavor(BMusic bMusic) {
        if (bMusic == null) return;
        BMusic music = mMusicList.get(mMusicList.indexOf(bMusic));
        music.setFavorite(false);
        removeMusicBelongFrom(music, "1");
    }

    public void addRecent(BMusic bMusic) {
        addMusicBelongTo(bMusic, "2");
    }

    public void removeRecent(BMusic bMusic) {
        removeMusicBelongFrom(bMusic, "2");
    }

    private void addMusicBelongTo(BMusic bMusic, String ordinal) {
        if (bMusic == null) return;
        BMusic music = mMusicList.get(mMusicList.indexOf(bMusic));
        Set<String> belongToSet = music.getBelongToSet();
        belongToSet.add(ordinal);
        music.setBelongToSet(belongToSet);
        DbHelper.getMusicHelper().insertOrReplace(music);
    }

    private void removeMusicBelongFrom(BMusic bMusic, String ordinal) {
        if (bMusic == null || TextUtils.isEmpty(ordinal)) return;
        BMusic music = mMusicList.get(mMusicList.indexOf(bMusic));
        Set<String> belongToSet = music.getBelongToSet();
        belongToSet.remove(ordinal);
        music.setBelongToSet(belongToSet);
        DbHelper.getMusicHelper().insertOrReplace(music);
    }

    public List<BMusic> getFavored() {
        List<BMusic> favoredMusics = new ArrayList<>();
        for (BMusic bMusic : mMusicList) {
            if (bMusic.isFavorite()) {
                favoredMusics.add(bMusic);
            }
        }
        return favoredMusics;
    }

    public List<BMusic> getRecentMusic() {
        return getMusicSheet("2");
    }

    public List<BMusic> getMusicSheet(String ordinal) {
        List<BMusic> musicSheet = new ArrayList<>();
        for (BMusic bMusic : mMusicList) {
            Set<String> toSet = bMusic.getBelongToSet();
            if (toSet.contains(ordinal)) {
                musicSheet.add(bMusic);
            }
        }
        return musicSheet;
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

    public void getLrc(final BMusic bMusic, final Subscriber<BLyric> subscriber) {
        if (subscriber == null) return;
        if (bMusic == null) {
            subscriber.onError(new IllegalArgumentException("BMusic不能为null"));
            subscriber.onCompleted();
            return;
        }

        BLyric bLyric = null;
        try {
            Mp3File mp3file = new Mp3File(bMusic.path);
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
            String query = bMusic.title + (TextUtils.equals(bMusic.artist, "<unknown>") ? "" : bMusic.artist);
            mPresenter.searchLrc(query, AndroidSchedulers.mainThread(), new SubscriberAdapter<BaiduLrc>() {
                @Override
                public void onNext(BaiduLrc baiduLrc) {
                    super.onNext(baiduLrc);
                    BLyric bLyric1 = null;
                    if (baiduLrc != null) {
                        bLyric1 = generateLrcData(baiduLrc.getLrcContent());
                    }
                    subscriber.onNext(bLyric1);
                    subscriber.onCompleted();
                }
            });
        } else {
            subscriber.onNext(bLyric);
            subscriber.onCompleted();
        }
    }

    public static BLyric generateLrcData(String lrcContent) {
        if (TextUtils.isEmpty(lrcContent)) return null;

        List<BLyric.LyricLine> lrcLines;
        if (PATTERN_LRC_INTERVAL.matcher(lrcContent).find()) {
            lrcContent = lrcContent.replaceAll("\\n", "");
            lrcLines = match(lrcContent);
        } else {
            String[] split = lrcContent.split("\r\n");
            lrcLines = new ArrayList<>();
            for (String s : split) {
                lrcLines.add(new BLyric.LyricLine(-1, s));
            }
        }

        return new BLyric(lrcLines);
    }

    private static List<BLyric.LyricLine> match(String content) {
        List<BLyric.LyricLine> lyricLines = new ArrayList<>();
        int start = 0;
        int end;
        String time = null;
        Matcher matcher = PATTERN_LRC_INTERVAL.matcher(content);
        while (matcher.find()) {
            /* 时间字段开始处为上一次遍历的内容的起始处 */
            end = matcher.start();
            if (end > 0) {
                String lrcLine = content.substring(start, end);
                System.out.println("lrcLine = " + lrcLine);
                lyricLines.add(new BLyric.LyricLine(lrcTime2Timestamp(time), lrcLine));
            }
            /* 本次的时间结尾处作为下一次查询的本次内容的起始处 */
            start = matcher.end();
            time = matcher.group();
        }
        return lyricLines;
    }

    private static int lrcTime2Timestamp(String time) {
        if (time == null || time.equals("")) return 0;
        int minutes = string2Int(time.substring(1, 3));
        int seconds = string2Int(time.substring(4, 6));
        int milliseconds = string2Int(time.substring(7, 9));
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
