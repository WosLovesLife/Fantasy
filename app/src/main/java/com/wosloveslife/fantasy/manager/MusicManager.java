package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.bean.BLyric;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.dao.DbHelper;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.presenter.MusicPresenter;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.photo.BitmapUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicManager {
    private static final String KEY_LAST_SHEET = "fantasy.manager.MusicManager.KEY_LAST_SHEET";
    private static final Pattern PATTERN_LRC_INTERVAL = Pattern.compile("\\u005b[0-9]{2}:[0-9]{2}\\u002e[0-9]{2}\\u005d");
    private static MusicManager sMusicManager;

    Context mContext;
    MusicPresenter mPresenter;

    //=============Var
    boolean mScanning;

    //=============Data
    @Deprecated
    List<String> mPinyinIndex;
    List<BMusic> mMusicList;
    private Set<String> mFavoredSheet;
    BMusic mCurrentMusic;
    private String mCurrentSheetOrdinal;

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
        mCurrentSheetOrdinal = SPHelper.getInstance().get(KEY_LAST_SHEET, "0");
        loadLastSheet();

        mFavoredSheet = DbHelper.getMusicHelper().loadFavored("1");
    }

    /**
     * 获取上一次关闭前停留的歌单
     */
    private void loadLastSheet() {
        List<BMusic> bMusics = DbHelper.getMusicHelper().loadSheet(mCurrentSheetOrdinal);
        if (bMusics == null || bMusics.size() == 0) {
            if (TextUtils.equals(mCurrentSheetOrdinal, "0")) {
                scan();
            }
        } else {
            onGotData(bMusics);
        }
    }

    public void saveCurrentSheetOrdinal(String ordinal) {
        mCurrentSheetOrdinal = ordinal;
        SPHelper.getInstance().save(KEY_LAST_SHEET, ordinal);
    }

    public String getCurrentSheetOrdinal() {
        return mCurrentSheetOrdinal;
    }

    private void scan() {
        if (mScanning) return;
        mScanning = true;
        ScanResourceEngine.getMusicFromSystemDao(mContext).subscribe(new SubscriberAdapter<List<BMusic>>() {
            @Override
            public void onError(Throwable e) {
                super.onError(e);
                if (TextUtils.equals(mCurrentSheetOrdinal, "0")) {
                    onGotData(null);
                }
                mScanning = false;
            }

            @Override
            public void onNext(List<BMusic> bMusics) {
                super.onNext(bMusics);
                if (TextUtils.equals(mCurrentSheetOrdinal, "0")) {
                    onGotData(bMusics);
                }
                mScanning = false;

                DbHelper.getMusicHelper().remove("0");
                if (bMusics != null && bMusics.size() > 0) {
                    DbHelper.getMusicHelper().insertOrReplace(bMusics);
                }
            }
        });
    }

    private void onGotData(List<BMusic> bMusics) {
        if (bMusics != mMusicList) {
            mPinyinIndex.clear();
            mMusicList.clear();

            if (bMusics != null && bMusics.size() > 0) {
                mMusicList.addAll(bMusics);
            }
        }

        if (mScanning) {
            EventBus.getDefault().post(new OnScannedMusicEvent(mPinyinIndex, mMusicList));
        } else {
            EventBus.getDefault().post(new OnGotMusicEvent(mPinyinIndex, mMusicList));
        }
    }

    //==============================================================================================
    @Deprecated
    public List<String> getPinyinIndex() {
        return mPinyinIndex;
    }

    public List<BMusic> getMusicList() {
        return mMusicList;
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
        scan();
    }

    //=========================================歌单操作=============================================

    /**
     * 变更当前的播放列表, 通常是用户在一个列表上播放了一首歌才会造成播放列表的切换<br/>
     * 如果只是变更歌曲列表的内容, 应该使用{@link MusicManager#getMusicSheet(String)}方法
     *
     * @param ordinal 歌单序列号
     */
    public void changeSheet(String ordinal) {
        if (TextUtils.equals(ordinal, mCurrentSheetOrdinal)) {
            onGotData(mMusicList);
        } else {
            saveCurrentSheetOrdinal(ordinal);
            onGotData(getMusicSheet(ordinal));
        }
    }

    //==============我的收藏
    public void addFavor(BMusic bMusic) {
        if (bMusic == null) return;
        if (!mFavoredSheet.contains(bMusic.title)) {
            mFavoredSheet.add(bMusic.title);
            EventBus.getDefault().post(new OnFavorite(bMusic, true));
            addMusicBelongTo(bMusic, "1");
        }
    }

    public void removeFavor(BMusic bMusic) {
        if (bMusic == null) return;
        if (mFavoredSheet.contains(bMusic.title)) {
            mFavoredSheet.remove(bMusic.title);
            EventBus.getDefault().post(new OnFavorite(bMusic, false));
            removeMusicBelongFrom(bMusic.path, "1");
        }
    }

    public List<BMusic> getFavored() {
        return getMusicSheet("1");
    }

    //==============播放记录
    public void addRecent(BMusic bMusic) {
        if (bMusic == null) return;
        addMusicBelongTo(bMusic, "2");
    }

    public void removeRecent(BMusic bMusic) {
        if (bMusic == null) return;
        removeMusicBelongFrom(bMusic.path, "2");
    }

    public List<BMusic> getRecentMusic() {
        return getMusicSheet("2");
    }

    //==============通用
    public boolean isFavored(BMusic music) {
        return music != null && mFavoredSheet.contains(music.title);
    }

    public void removeMusicFromCurrentSheet(BMusic bMusic, boolean withImmediate) {
        if (bMusic == null) return;
        DbHelper.getMusicHelper().remove(bMusic);
        if (withImmediate) {
            mMusicList.remove(bMusic);
            /* TODO 通知页面刷新 */
        }
    }

    //==============封装
    private void addMusicBelongTo(BMusic bMusic, String belong) {
        if (bMusic == null) return;
        BMusic newMusic = new BMusic(bMusic);
        newMusic.setBelongTo(belong);
        DbHelper.getMusicHelper().insertOrReplace(newMusic);
    }

    private void removeMusicBelongFrom(String path, String belong) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(belong)) return;
        DbHelper.getMusicHelper().remove(path, belong);
    }

    /**
     * 根据歌单序号获取歌曲列表
     *
     * @param belong 歌单序号
     * @return 歌曲列表
     */
    public List<BMusic> getMusicSheet(String belong) {
        return DbHelper.getMusicHelper().loadSheet(belong);
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

    public Observable<BLyric> getLrc(final BMusic bMusic) {
        String query = bMusic.title + (TextUtils.equals(bMusic.artist, "<unknown>") ? " " : " " + bMusic.artist);
        Observable<BLyric> localOb = Observable.create(new Observable.OnSubscribe<BLyric>() {
            @Override
            public void call(Subscriber<? super BLyric> subscriber) {
                try {
                    Mp3File mp3file = new Mp3File(bMusic.path);
                    if (mp3file.hasId3v2Tag()) {
                        String lyrics = mp3file.getId3v2Tag().getLyrics();
                        if (!TextUtils.isEmpty(lyrics)) {
                            BLyric bLyric = generateLrcData(lyrics);
                            if (bLyric != null) {
                                /* 重点!!! 如果执行了onNext()即表明内容非empty,后面的Observer就不会执行 */
                                subscriber.onNext(bLyric);
                            }
                        }
                    }
                } catch (Throwable e) {
                    Logger.w("从本地歌曲中解析歌词失败,可忽略 error : " + e);
                }
                subscriber.onCompleted();
            }
        });
        Observable<BLyric> netOb = mPresenter.searchLrc(query).map(new Func1<BaiduLrc, BLyric>() {
            @Override
            public BLyric call(BaiduLrc baiduLrc) {
                BLyric lrc = null;
                if (baiduLrc != null) {
                    lrc = generateLrcData(baiduLrc.getLrcContent());
                    saveLrc(bMusic, baiduLrc.getLrcContent());
                }
                return lrc;
            }
        });
        return localOb
                .switchIfEmpty(netOb)
                .subscribeOn(Schedulers.io());
    }

    @WorkerThread
    private void saveLrc(BMusic bMusic, String lrcContent) {
        if (bMusic == null || TextUtils.isEmpty(lrcContent)) return;
        try {
            Mp3File mp3file = new Mp3File(bMusic.path);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                id3v2Tag.setLyrics(lrcContent);
            } else {
                ID3v23Tag id3v23Tag = new ID3v23Tag();
                id3v23Tag.setTitle(bMusic.title);
                id3v23Tag.setArtist(bMusic.artist);
                id3v23Tag.setLyrics(lrcContent);
                mp3file.setId3v2Tag(id3v23Tag);
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }
    }

    public static BLyric generateLrcData(String lrcContent) {
        if (TextUtils.isEmpty(lrcContent)) return null;

        List<BLyric.LyricLine> lrcLines;
        if (PATTERN_LRC_INTERVAL.matcher(lrcContent).find()) {
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
                if (lrcLine.endsWith("\n")) {
                    lrcLine = lrcLine.substring(0, lrcLine.length() - 1);
                }
                WLogger.d("match : LrcLine = " + lrcLine);
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

    public static class OnGotMusicEvent {
        public List<String> mPinyinIndex;
        public List<BMusic> mBMusicList;

        public OnGotMusicEvent(List<String> pinyinIndex, List<BMusic> bMusics) {
            mPinyinIndex = pinyinIndex;
            mBMusicList = bMusics;
        }
    }

    public static class OnScannedMusicEvent extends OnGotMusicEvent {
        public OnScannedMusicEvent(List<String> pinyinIndex, List<BMusic> bMusics) {
            super(pinyinIndex, bMusics);
        }
    }

    public static class OnFavorite {
        public BMusic mMusic;
        public boolean mFavorite;

        public OnFavorite(BMusic music, boolean favorite) {
            mMusic = music;
            mFavorite = favorite;
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
