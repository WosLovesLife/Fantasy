package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.dao.SheetIds;
import com.wosloveslife.dao.store.SheetStore;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.baidu.BaiduMusicInfo;
import com.wosloveslife.fantasy.baidu.BaiduSearch;
import com.wosloveslife.fantasy.dao.engine.MusicProvider;
import com.wosloveslife.fantasy.feature.lrc.BLyric;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicManager {
    private static MusicManager sMusicManager;

    //=============
    Context mContext;

    //=============Var

    //=============Data
    private MusicConfig mMusicConfig;
    private MusicInfoEngine mMusicInfoEngine;


    private MusicManager() {
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

    @AnyThread
    public void init(Context context) {
        mMusicConfig = new MusicConfig();
        mContext = context.getApplicationContext();
        mMusicInfoEngine = new MusicInfoEngine(context);
        Observable.just(context)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object object) {
                        dispose();
                        return object;
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @WorkerThread
    private void dispose() {
        checkIfHasNoDefSheet();

        loadLastSheet();
    }

    @WorkerThread
    private void checkIfHasNoDefSheet() {
        boolean needCreate = SheetStore.loadById(SheetIds.LOCAL).toBlocking().first() == null;
        needCreate = needCreate || SheetStore.loadById(SheetIds.FAVORED).toBlocking().first() == null;
        needCreate = needCreate || SheetStore.loadById(SheetIds.RECENT).toBlocking().first() == null;
        if (!needCreate) return;

        long timestamp = System.currentTimeMillis();
        Sheet localSheet = new Sheet(SheetIds.LOCAL, "本地音乐", "Def", "Local", "def", null, timestamp, timestamp, Sheet.TYPE_DEF, Sheet.STATE_NORMAL, null);
        Sheet favoriteSheet = new Sheet(SheetIds.FAVORED, "我的收藏", "Def", "My favorite", "def", null, timestamp + 1, timestamp + 1, Sheet.TYPE_DEF, Sheet.STATE_NORMAL, null);
        Sheet recentSheet = new Sheet(SheetIds.RECENT, "最近播放", "Def", "Recent", "def", null, timestamp + 2, timestamp + 2, Sheet.TYPE_DEF, Sheet.STATE_NORMAL, null);
        List<Sheet> sheets = new ArrayList<>();
        sheets.add(localSheet);
        sheets.add(favoriteSheet);
        sheets.add(recentSheet);
        SheetStore.insertOrReplace(sheets).toBlocking().first();
    }

    /**
     * 获取上一次关闭前停留的歌单
     */
    @WorkerThread
    private void loadLastSheet() {
        Log.w("Leonard", "loadLastSheet: " + System.currentTimeMillis());

        String sheetId = mMusicConfig.mCurrentSheetId;
        if (TextUtils.isEmpty(sheetId)) {
            mMusicConfig.saveLastSheetId(SheetIds.LOCAL);
            scan();
        } else {
            changeSheet(sheetId);
        }
    }

    @AnyThread
    private boolean checkNeedScan() {
        return mMusicConfig.mMusicList.size() == 0 && TextUtils.equals(mMusicConfig.mCurrentSheetId, SheetIds.LOCAL);
    }

    private boolean mAutoScanned;

    // TODO: 17/6/17  remove
    @WorkerThread
    private void scan() {
        if (mAutoScanned) {
            return;
        }
        mAutoScanned = true;
        MusicProvider.scanSysDB(mContext)
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends List<Audio>>>() {
                    @Override
                    public Observable<? extends List<Audio>> call(Throwable throwable) {
                        return Observable.just(null);
                    }
                })
                .subscribe(new SubscriberAdapter<List<Audio>>() {
                    @Override
                    public void onNext(List<Audio> bMusics) {
                        super.onNext(bMusics);
                        if (TextUtils.equals(mMusicConfig.mCurrentSheetId, SheetIds.LOCAL)) {
                            onGotData(bMusics);
                            EventBus.getDefault().post(new OnScannedMusicEvent(mMusicConfig.mMusicList));
                        }
                    }
                });
    }

    // TODO: 2017/6/24 歌曲列表的组织形式需要改变
    @AnyThread
    private void onGotData(List<Audio> bMusics) {
        if (bMusics != mMusicConfig.mMusicList) {
            mMusicConfig.mMusicList.clear();

            if (bMusics != null && bMusics.size() > 0) {
                mMusicConfig.mMusicList.addAll(bMusics);
            }
        }
    }

    //=======================================获取音乐-start===========================================
    @AnyThread
    public void scanMusic() {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @AnyThread
    public Observable<List<Audio>> searchMusic(String query, @Nullable String sheetId) {
        if (TextUtils.isEmpty(query)) {
            return Observable.empty();
        }
        return MusicProvider.search(query, sheetId);
    }

    /** 获取歌曲/歌单的Holder */
    @AnyThread
    @NonNull
    public MusicConfig getMusicConfig() {
        return mMusicConfig;
    }

    //=========================================歌单操作=============================================

    /**
     * 变更当前的播放列表, 通常是用户在一个列表上播放了一首歌才会造成播放列表的切换<br/>
     * 如果只是变更歌曲列表的内容, 应该使用{@link MusicProvider#loadMusicBySheet(String)}方法
     *
     * @param sheetId 歌单序列号
     */
    @AnyThread
    public void changeSheet(final String sheetId) {
        mMusicConfig.mCurrentSheetId = sheetId;
        MusicProvider.loadMusicBySheet(sheetId)
                .subscribeOn(Schedulers.io())
                .subscribe(new SubscriberAdapter<List<Audio>>() {
                    @Override
                    public void onNext(List<Audio> audios) {
                        super.onNext(audios);
                        if (!mAutoScanned && TextUtils.equals(sheetId, SheetIds.LOCAL) && (audios == null || audios.size() == 0)) {
                            scan();
                        } else {
                            onGotData(audios);
                            EventBus.getDefault().post(new OnGotMusicEvent(mMusicConfig.mMusicList));
                        }
                    }
                });
    }

    //==============我的收藏
    public Observable<Boolean> addFavor(Audio audio) {
        return MusicProvider.addMusic2Sheet(audio, mMusicConfig.mSheets.get(SheetIds.FAVORED));
    }

    @AnyThread
    public Observable<Boolean> removeFavor(Audio audio) {
        return MusicProvider.removeMusicFromSheet(audio, mMusicConfig.mSheets.get(SheetIds.FAVORED));
    }

    @AnyThread
    public Observable<List<Audio>> getFavored() {
        return MusicProvider.loadMusicBySheet(SheetIds.FAVORED);
    }

    //==============播放记录

    @AnyThread
    public void addRecent(Audio audio) {
        if (audio == null) return;
        Audio newRecent = new Audio(audio);
        newRecent.joinTimestamp = System.currentTimeMillis();
        EventBus.getDefault().post(new OnMusicChanged(newRecent, SheetIds.RECENT));
        MusicProvider.addMusic2Sheet(newRecent, mMusicConfig.mSheets.get(SheetIds.FAVORED));
    }

    @AnyThread
    public void removeRecent(Audio audio) {
        if (audio == null) return;
        MusicProvider.removeMusicFromSheet(audio, mMusicConfig.mSheets.get(SheetIds.FAVORED));
    }

    @AnyThread
    public Observable<List<Audio>> getRecentMusic() {
        return MusicProvider.loadMusicBySheet(SheetIds.RECENT);
    }

    //==============通用
    @AnyThread
    public boolean isFavored(Audio audio) {
        Sheet sheet = mMusicConfig.mSheets.get(SheetIds.FAVORED);
        if (sheet == null) {
            return false;
        }
        RealmList<Audio> songs = sheet.songs;
        return audio != null && songs != null && songs.contains(audio);
    }

    //=======================================获取歌曲信息===========================================

    /**
     * 获取歌曲封面, 会首先尝试从本地歌曲文件中通过ID3v2来获取封面,如果失败,会尝试从网络自动获取(如果开启了联网)
     *
     * @param music      歌曲对象
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    public Observable<Bitmap> getAlbum(final Audio music, final int bitmapSize) {
        return mMusicInfoEngine.getAlbum(music, bitmapSize);
    }

    public Observable<BLyric> getLrc(final Audio bMusic) {
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
        public List<Audio> mBMusicList;

        public OnGotMusicEvent(List<Audio> bMusics) {
            mBMusicList = bMusics;
        }
    }

    public static class OnScannedMusicEvent extends OnGotMusicEvent {
        public OnScannedMusicEvent(List<Audio> bMusics) {
            super(bMusics);
        }
    }

    public static class OnRemoveMusic {
        public Audio mMusic;
        public String mBelongTo;

        public OnRemoveMusic(Audio music, String belongTo) {
            mMusic = music;
            mBelongTo = belongTo;
        }
    }

    public static class OnAddMusic {
        public Audio mMusic;
        public String mSheetId;

        public OnAddMusic(Audio music, String sheetId) {
            mMusic = music;
            mSheetId = sheetId;
        }
    }

    public static class OnMusicChanged {
        public Audio mMusic;
        public String mSheetId;

        public OnMusicChanged(Audio music, String sheetId) {
            mMusic = music;
            mSheetId = sheetId;
        }
    }
}
