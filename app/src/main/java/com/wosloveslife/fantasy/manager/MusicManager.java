package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.github.promeg.pinyinhelper.Pinyin;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.orhanobut.logger.Logger;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.album.AlbumFile;
import com.wosloveslife.fantasy.baidu.BaiduAlbum;
import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.dao.DbHelper;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.lrc.BLyric;
import com.wosloveslife.fantasy.lrc.LrcFile;
import com.wosloveslife.fantasy.lrc.LrcParser;
import com.wosloveslife.fantasy.presenter.MusicPresenter;
import com.yesing.blibrary_wos.utils.photo.BitmapUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicManager {
    private static final String KEY_LAST_SHEET = "fantasy.manager.MusicManager.KEY_LAST_SHEET";
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
        loadLastSheet();

        mFavoredSheet = DbHelper.getMusicHelper().loadFavored("1");
    }

    /**
     * 获取上一次关闭前停留的歌单
     */
    private void loadLastSheet() {
        changeSheet(SPHelper.getInstance().get(KEY_LAST_SHEET, "0"));
        if (mMusicList.size() == 0 && TextUtils.equals(mCurrentSheetOrdinal, "0")) {
            scan();
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
                for (BMusic music : bMusics) {
                    mPinyinIndex.add(Pinyin.toPinyin(music.title, ""));
                }
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

    public List<BMusic> searchMusic(String title) {
        return searchMusic(title, null);
    }

    public List<BMusic> searchMusic(String title, String belongTo) {
        if (TextUtils.isEmpty(title)) {
            return null;
        }
        if (TextUtils.isEmpty(belongTo)) {
            return DbHelper.getMusicHelper().search(title);
        } else {
            return DbHelper.getMusicHelper().search(title, belongTo);
        }
    }

    //=========================================歌单操作=============================================

    /**
     * 变更当前的播放列表, 通常是用户在一个列表上播放了一首歌才会造成播放列表的切换<br/>
     * 如果只是变更歌曲列表的内容, 应该使用{@link MusicManager#getMusicSheet(String)}方法
     *
     * @param ordinal 歌单序列号
     */
    public void changeSheet(String ordinal) {
        saveCurrentSheetOrdinal(ordinal);
        onGotData(getMusicSheet(ordinal));
    }

    //==============我的收藏
    public void addFavor(BMusic bMusic) {
        if (bMusic == null) return;
        if (!mFavoredSheet.contains(bMusic.title)) {
            mFavoredSheet.add(bMusic.title);
            EventBus.getDefault().post(new OnAddMusic(bMusic, "1"));

            BMusic favorMusic = new BMusic(bMusic);
            favorMusic.joinTimestamp = new Date();
            addMusicBelongTo(favorMusic, "1");
        }
    }

    public void removeFavor(BMusic bMusic) {
        if (bMusic == null) return;
        if (mFavoredSheet.contains(bMusic.title)) {
            mFavoredSheet.remove(bMusic.title);
            EventBus.getDefault().post(new OnRemoveMusic(bMusic, "1"));
            removeMusicBelongFrom(bMusic, "1");
        }
    }

    public List<BMusic> getFavored() {
        return getMusicSheet("1");
    }

    //==============播放记录
    public void addRecent(BMusic bMusic) {
        if (bMusic == null) return;
        List<BMusic> bMusics = DbHelper.getMusicHelper().loadEntities(bMusic.path, "2");
        if (bMusics != null && bMusics.size() > 0) {
            removeMusicBelongFrom(bMusic, "2");
        }
        BMusic newRecent = new BMusic(bMusic);
        newRecent.joinTimestamp = new Date();
        EventBus.getDefault().post(new OnMusicChanged(newRecent, "2"));
        addMusicBelongTo(newRecent, "2");
    }

    public void removeRecent(BMusic bMusic) {
        if (bMusic == null) return;
        removeMusicBelongFrom(bMusic, "2");
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

    private void removeMusicBelongFrom(BMusic bMusic, String belong) {
        if (TextUtils.isEmpty(bMusic.path) || TextUtils.isEmpty(belong)) return;
        DbHelper.getMusicHelper().remove(bMusic.path, belong);
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
     * @param music      歌曲对象
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    public Observable<Bitmap> getAlbum(final BMusic music, final int bitmapSize) {

        Observable<Bitmap> fileOb = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                File albumFile = AlbumFile.getAlbumFile(mContext, music.album);
                if (albumFile != null) {
                    Bitmap bitmap = BitmapUtils.getScaledDrawable(albumFile.getAbsolutePath(), bitmapSize, bitmapSize, Bitmap.Config.RGB_565);
                    subscriber.onNext(bitmap);
                }
                subscriber.onCompleted();
            }
        });

        final Observable<Bitmap> mp3Ob = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                try {
                    Mp3File mp3file = new Mp3File(music.path);
                    if (mp3file.hasId3v2Tag()) {
                        Bitmap bitmap = getAlbumFromID3v2(mp3file.getId3v2Tag(), bitmapSize);
                        if (bitmap != null) {
                            AlbumFile.saveAlbum(mContext, music.album, bitmap);
                            subscriber.onNext(bitmap);
                        }
                    }
                } catch (Throwable e) {
                    Logger.w("从本地歌曲中解析封面失败,可忽略 error : " + e);
                }
                subscriber.onCompleted();
            }
        });

        String query = music.album;
        Observable<Bitmap> netOb = mPresenter.searchAlbum(query).map(new Func1<BaiduAlbum, Bitmap>() {
            @Override
            public Bitmap call(BaiduAlbum baiduAlbum) {
                if (baiduAlbum != null) {
                    String artistpic = baiduAlbum.getArtistpic();
                    if (!TextUtils.isEmpty(artistpic)) {
                        artistpic = artistpic.replace("w_40", "w_500");
                        Request request = new Request.Builder()
                                .get()
                                .url(artistpic)
                                .build();
                        try {
                            Response execute = new OkHttpClient.Builder().build().newCall(request).execute();
                            InputStream inputStream = execute.body().byteStream();
                            Bitmap bitmap = BitmapUtils.getScaledDrawable(inputStream, bitmapSize, bitmapSize, Bitmap.Config.RGB_565);
                            AlbumFile.saveAlbum(mContext, music.album, bitmap);
                            return bitmap;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return null;
            }
        });

        return fileOb
                .switchIfEmpty(mp3Ob)
                .switchIfEmpty(netOb)
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
        Observable<BLyric> fileOb = Observable.create(new Observable.OnSubscribe<BLyric>() {
            @Override
            public void call(Subscriber<? super BLyric> subscriber) {
                String lrc = LrcFile.getLrc(mContext, bMusic.title);
                if (!TextUtils.isEmpty(lrc)) {
                    BLyric bLyric = LrcParser.parseLrc(lrc);
                    if (bLyric != null) {
                        /* 重点!!! 如果执行了onNext()即表明内容非empty,后面的Observer就不会执行 */
                        subscriber.onNext(bLyric);
                    }
                }
                subscriber.onCompleted();
            }
        });

        String query = bMusic.title + (TextUtils.equals(bMusic.artist, "<unknown>") ? " " : " " + bMusic.artist);
        Observable<BLyric> id3v2Ob = Observable.create(new Observable.OnSubscribe<BLyric>() {
            @Override
            public void call(Subscriber<? super BLyric> subscriber) {
                try {
                    Mp3File mp3file = new Mp3File(bMusic.path);
                    if (mp3file.hasId3v2Tag()) {
                        String lyrics = mp3file.getId3v2Tag().getLyrics();
                        if (!TextUtils.isEmpty(lyrics)) {
                            BLyric bLyric = LrcParser.parseLrc(lyrics);
                            if (bLyric != null) {
                                LrcFile.saveLrc(mContext, bMusic.title, lyrics);
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
                BLyric bLyric = null;
                if (baiduLrc != null) {
                    String lrc = baiduLrc.getLrcContent();
                    if (!TextUtils.isEmpty(lrc)) {
                        LrcFile.saveLrc(mContext, bMusic.title, lrc);
                        bLyric = LrcParser.parseLrc(lrc);
                    }
                }
                return bLyric;
            }
        });
        return fileOb
                .switchIfEmpty(id3v2Ob)
                .switchIfEmpty(netOb)
                .subscribeOn(Schedulers.io());
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

    public static class OnRemoveMusic {
        public BMusic mMusic;
        public String mBelongTo;

        public OnRemoveMusic(BMusic music, String belongTo) {
            mMusic = music;
            mBelongTo = belongTo;
        }
    }

    public static class OnAddMusic {
        public BMusic mMusic;
        public String mBelongTo;

        public OnAddMusic(BMusic music, String belongTo) {
            mMusic = music;
            mBelongTo = belongTo;
        }
    }

    public static class OnMusicChanged {
        public BMusic mMusic;
        public String mBelongTo;

        public OnMusicChanged(BMusic music, String belongTo) {
            mMusic = music;
            mBelongTo = belongTo;
        }
    }
}
