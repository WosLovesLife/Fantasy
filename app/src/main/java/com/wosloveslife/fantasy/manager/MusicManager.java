package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.promeg.pinyinhelper.Pinyin;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.baidu.BaiduMusicInfo;
import com.wosloveslife.fantasy.baidu.BaiduSearch;
import com.wosloveslife.fantasy.bean.BMusic;
import com.wosloveslife.fantasy.dao.DbHelper;
import com.wosloveslife.fantasy.helper.SPHelper;
import com.wosloveslife.fantasy.lrc.BLyric;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import rx.Observable;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicManager {
    private static final String KEY_LAST_SHEET = "fantasy.manager.MusicManager.KEY_LAST_SHEET";
    private static MusicManager sMusicManager;

    Context mContext;

    //=============Var
    boolean mScanning;

    //=============Data
    @Deprecated
    List<String> mPinyinIndex;
    List<BMusic> mMusicList;
    private Set<String> mFavoredSheet;
    BMusic mCurrentMusic;
    private String mCurrentSheetOrdinal;
    private MusicInfoEngine mMusicInfoEngine;

    private MusicManager() {
        mPinyinIndex = new ArrayList<>();
        mMusicList = new ArrayList<>();
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mMusicInfoEngine = new MusicInfoEngine(context);
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
        if (TextUtils.isEmpty(bMusic.songId) || TextUtils.isEmpty(belong)) return;
        DbHelper.getMusicHelper().removeById(bMusic.songId, belong);
//        /* TODO 临时办法,如果一首歌曲的路径包含该关键字说明是网络歌曲,则其path路径是随机的,因此通过模糊方法将含有该songId的歌曲删掉 */
//        if (bMusic.path.contains(".mp3?")) {
//            String substring = bMusic.path.substring(0, bMusic.path.indexOf(".mp3?xcode"));
//            DbHelper.getMusicHelper().removeVague(substring, belong);
//        }
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

    //=======================================获取歌曲信息===========================================

    /**
     * 获取歌曲封面, 会首先尝试从本地歌曲文件中通过ID3v2来获取封面,如果失败,会尝试从网络自动获取(如果开启了联网)
     *
     * @param music      歌曲对象
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    public Observable<Bitmap> getAlbum(final BMusic music, final int bitmapSize) {
        return mMusicInfoEngine.getAlbum(music, bitmapSize);
    }

    public Observable<BLyric> getLrc(final BMusic bMusic) {
        return mMusicInfoEngine.getLrc(bMusic);
    }

    public Observable<BaiduSearch> searchMusicByNet(String query) {
        return mMusicInfoEngine.searchMusicByNet(query);
    }

    public Observable<BaiduMusicInfo> getMusicInfoByNet(String songId) {
        return mMusicInfoEngine.getMusicInfoByNet(songId);
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
