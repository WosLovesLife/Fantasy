package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.AnyThread;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.dao.SheetIds;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.baidu.BaiduMusicInfo;
import com.wosloveslife.fantasy.baidu.BaiduSearch;
import com.wosloveslife.fantasy.dao.engine.MusicProvider;
import com.wosloveslife.fantasy.lrc.BLyric;
import com.yesing.blibrary_wos.utils.assist.WLogger;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;

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
//                .observeOn(Schedulers.io())
                .subscribe(new SubscriberAdapter<Context>() {
                    @Override
                    public void onNext(Context context) {
                        super.onNext(context);
                        dispose();
                    }
                });
    }

    @WorkerThread
    private void dispose() {
        loadLastSheet();
    }

    /**
     * 获取上一次关闭前停留的歌单
     */
    @WorkerThread
    private void loadLastSheet() {
        if (checkNeedScan()) {
            mMusicConfig.saveLastSheetId(SheetIds.LOCAL);
            scan();
        } else {
            changeSheet(mMusicConfig.mCurrentSheetId);
        }
    }

    private boolean checkNeedScan() {
        if (TextUtils.isEmpty(mMusicConfig.mCurrentSheetId)) return true;

        return Realm.getDefaultInstance().where(Sheet.class).equalTo(Sheet.ID, SheetIds.LOCAL).findFirst().getSongs().size() == 0;
    }

    // TODO: 17/6/17  remove
    @AnyThread
    private void scan() {
        WLogger.w("scan :  " + Thread.currentThread());
        MusicProvider.scanSysDB(mContext)
                .map(new Func1<List<Audio>, List<Audio>>() {
                    @Override
                    public List<Audio> call(final List<Audio> audios) {
                        MusicProvider.clearSheetEntities(SheetIds.LOCAL).toBlocking().first();
                        if (audios != null) {
                            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    List<Audio> list = realm.copyToRealmOrUpdate(audios);
                                }
                            });
                        }
                        return audios;
                    }
                })
//                .subscribeOn(Schedulers.io())
                .subscribe(new SubscriberAdapter<List<Audio>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        onGotData(null, true);
                    }

                    @Override
                    public void onNext(List<Audio> bMusics) {
                        super.onNext(bMusics);
                        onGotData(bMusics, true);
                    }
                });
    }

    @AnyThread
    private void onGotData(List<Audio> bMusics, boolean fromScan) {
        if (!TextUtils.equals(mMusicConfig.mCurrentSheetId, SheetIds.LOCAL)) {
            return;
        }

        mMusicConfig.mMusicList.clear();
        if (bMusics != null && bMusics.size() > 0) {
            mMusicConfig.mMusicList.addAll(bMusics);
        }

        if (fromScan) {
            EventBus.getDefault().post(new OnScannedMusicEvent(mMusicConfig.mMusicList));
        } else {
            EventBus.getDefault().post(new OnGotMusicEvent(mMusicConfig.mMusicList));
        }
    }

    //=======================================获取音乐-start===========================================
    @AnyThread
    public void scanMusic() {
        scan();
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
    public void changeSheet(String sheetId) {
        MusicProvider.loadMusicBySheet(sheetId).subscribe(new SubscriberAdapter<List<Audio>>() {
            @Override
            public void onNext(List<Audio> audios) {
                super.onNext(audios);
                onGotData(audios, false);
            }
        });
    }

    //==============我的收藏
    @CheckResult
    public Observable<Boolean> addFavor(String audioId) {
        MusicProvider.insertMusics2Sheet(SheetIds.FAVORED, audioId);
        return Observable.just(true);
    }

    @AnyThread
    public Observable<Boolean> removeFavor(String audioId) {
        MusicProvider.removeMusicFromSheet(SheetIds.FAVORED, audioId);
        return Observable.just(true);
    }

    @AnyThread
    public Observable<List<Audio>> getFavored() {
        return MusicProvider.loadMusicBySheet(SheetIds.FAVORED);
    }

    @AnyThread
    public boolean isFavored(Audio audio) {
        if (audio == null) return false;
        for (Sheet sheet : audio.getSongList()) {
            if (TextUtils.equals(sheet.getId(), SheetIds.FAVORED)) {
                return true;
            }
        }
        return false;
    }

    //==============播放记录

    @AnyThread
    public void addRecent(Audio audio) { // TODO: 2017/7/20
//        if (audio == null) return;
//        Audio newRecent = new Audio(audio);
//        newRecent.joinTimestamp = System.currentTimeMillis();
//        EventBus.getDefault().post(new OnMusicChanged(newRecent, SheetIds.RECENT));
//        MusicProvider.addMusic2Sheet(newRecent, mMusicConfig.mSheets.get(SheetIds.FAVORED));
    }

    @AnyThread
    public void removeRecent(Audio audio) { // TODO: 2017/7/20
//        if (audio == null) return;
//        MusicProvider.removeMusicFromSheet(audio, mMusicConfig.mSheets.get(SheetIds.FAVORED));
    }

    @AnyThread
    public Observable<List<Audio>> getRecentMusic() { // TODO: 2017/7/20
        return MusicProvider.loadMusicBySheet(SheetIds.RECENT);
    }

    //=======================================获取歌曲信息===========================================

    /**
     * 获取歌曲封面, 会首先尝试从本地歌曲文件中通过ID3v2来获取封面,如果失败,会尝试从网络自动获取(如果开启了联网)
     *
     * @param audioId    歌曲Id
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    public Observable<Bitmap> getAlbum(final String audioId, final int bitmapSize) {
        return mMusicInfoEngine.getAlbum(audioId, bitmapSize);
    }

    public Observable<BLyric> getLrc(final String audioId) {
        return mMusicInfoEngine.getLrc(audioId);
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
