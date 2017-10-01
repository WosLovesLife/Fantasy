package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.dao.SheetIds;
import com.wosloveslife.fantasy.adapter.SubscriberAdapter;
import com.wosloveslife.fantasy.album.AlbumFile;
import com.wosloveslife.fantasy.dao.engine.MusicProvider;
import com.wosloveslife.fantasy.event.Event;
import com.wosloveslife.fantasy.event.RxBus;
import com.wosloveslife.fantasy.lrc.BLyric;

import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by zhangh on 2017/1/2.
 */
public class MusicManager implements IMusicManage{
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
            scan();
        } else {
            loadMusics();
        }
    }

    private boolean checkNeedScan() {
        return Realm.getDefaultInstance().where(Sheet.class).equalTo(Sheet.ID, SheetIds.LOCAL).findFirst().getSongs().size() == 0;
    }

    // TODO: 17/6/17  remove
    @AnyThread
    private void scan() {
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
        mMusicConfig.mMusicList.clear();
        if (bMusics != null && bMusics.size() > 0) {
            mMusicConfig.mMusicList.addAll(bMusics);
        }

        if (fromScan) {
            RxBus.getDefault().post(Event.create(Event.SHEET_SCANNED, mMusicConfig.mMusicList));
        } else {
            RxBus.getDefault().post(Event.create(Event.SHEET_LOADED, mMusicConfig.mMusicList));
        }
    }

    //=======================================获取音乐-start===========================================
    @AnyThread
    public void scanMusic() {
        scan();
    }

    /** 获取歌曲/歌单的Holder */
    @AnyThread
    @NonNull
    public MusicConfig getMusicConfig() {
        return mMusicConfig;
    }

    //=========================================歌单操作=============================================

    public void loadMusics() {
        MusicProvider.loadMusicBySheet(SheetIds.LOCAL).subscribe(new SubscriberAdapter<List<Audio>>() {
            @Override
            public void onNext(List<Audio> audios) {
                super.onNext(audios);
                onGotData(audios, false);
            }
        });
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

    @Override
    public List<Audio> getAudios() {
        return null;
    }

    @Override
    public boolean isFavorite(String audioId) {
        return false;
    }

    @Override
    public AlbumFile getAlbum(String audioId) {
        return null;
    }

    @Override
    public BLyric getLyric(String audioId) {
        return null;
    }
}
