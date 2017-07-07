package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.store.AudioStore;
import com.wosloveslife.fantasy.feature.album.AlbumFile;
import com.wosloveslife.fantasy.baidu.BaiduLrc;
import com.wosloveslife.fantasy.baidu.BaiduMusicInfo;
import com.wosloveslife.fantasy.baidu.BaiduSearch;
import com.wosloveslife.fantasy.feature.lrc.BLyric;
import com.wosloveslife.fantasy.feature.lrc.LrcFile;
import com.wosloveslife.fantasy.feature.lrc.LrcParser;
import com.wosloveslife.fantasy.presenter.MusicPresenter;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.photo.BitmapUtils;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangh on 2017/3/20.
 */

public class MusicInfoEngine {
    private final MusicPresenter mPresenter;
    private final Context mContext;

    public MusicInfoEngine(Context context) {
        mContext = context.getApplicationContext();
        mPresenter = new MusicPresenter(mContext);
    }

    //=======================================获取歌曲信息===========================================

    /**
     * 获取歌曲封面, 会首先尝试从本地歌曲文件中通过ID3v2来获取封面,如果失败,会尝试从网络自动获取(如果开启了联网)
     *
     * @param music      歌曲对象
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    public Observable<Bitmap> getAlbum(final Audio music, final int bitmapSize) {
        final Observable<Bitmap> fileOb = Observable.create(new Observable.OnSubscribe<Bitmap>() {
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

        final Func1<BaiduMusicInfo, Observable<? extends Bitmap>> info2BitmapOb = new Func1<BaiduMusicInfo, Observable<? extends Bitmap>>() {
            @Override
            public Observable<? extends Bitmap> call(BaiduMusicInfo baiduMusicInfo) {
                if (baiduMusicInfo != null) {
                    BaiduMusicInfo.SonginfoBean songinfo = baiduMusicInfo.getSonginfo();
                    if (songinfo != null) {
                        String albumTitle = songinfo.getAlbum_title();
                        String albumAddress = songinfo.getPic_premium();
                        if (TextUtils.isEmpty(albumAddress)) {
                            albumAddress = songinfo.getPic_big();
                            if (TextUtils.isEmpty(albumAddress)) {
                                albumAddress = songinfo.getPic_radio();
                                if (TextUtils.isEmpty(albumAddress)) {
                                    albumAddress = songinfo.getPic_small();
                                }
                            }
                        }

                        if (TextUtils.equals(music.album, albumTitle)) {
                            music.album = albumTitle;
                            AudioStore.insertOrReplace(music).toBlocking().first();
                        }
                        return getAlbumByAddress(albumAddress, music.album, bitmapSize);
                    }
                }
                return null;
            }
        };

        final String query = music.title + (TextUtils.equals(music.artist, "<unknown>") ? " " : " " + music.artist);
        Observable<Bitmap> netOb;
        if (!music.isOnline()) {
            netOb = mPresenter.searchFromBaidu(query)
                    .concatMap(new Func1<BaiduSearch, Observable<BaiduMusicInfo>>() {
                        @Override
                        public Observable<BaiduMusicInfo> call(BaiduSearch baiduSearch) {
                            if (baiduSearch != null) {
                                try {
                                    return mPresenter.getMusicInfo(baiduSearch.getSong().get(0).getSongid());
                                } catch (Throwable e) {
                                    WLogger.w("call : 未搜索到歌曲,可忽略 e = " + e);
                                }
                            }
                            return null;
                        }
                    })
                    .concatMap(info2BitmapOb);
        } else {
            netOb = mPresenter.getMusicInfo(music.id).concatMap(info2BitmapOb);
        }

        return fileOb
                .switchIfEmpty(mp3Ob)
                .switchIfEmpty(netOb)
                .subscribeOn(Schedulers.io());
    }

    private Observable<Bitmap> getAlbumByAddress(final String address, final String album, final int bitmapSize) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                Glide.with(mContext)
                        .load(address)
                        .downloadOnly(new SimpleTarget<File>() {
                            @Override
                            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                                if (resource != null) {
                                    Bitmap bitmap = BitmapUtils.getScaledDrawable(resource.getAbsolutePath(), bitmapSize, bitmapSize, Bitmap.Config.RGB_565);
                                    if (bitmap != null) {
                                        AlbumFile.saveAlbum(mContext, album, bitmap);
                                    }
                                    subscriber.onNext(bitmap);
                                }
                                subscriber.onCompleted();
                            }
                        });
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
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

    public Observable<BLyric> getLrc(final Audio audio) {
        Observable<BLyric> fileOb = Observable.create(new Observable.OnSubscribe<BLyric>() {
            @Override
            public void call(Subscriber<? super BLyric> subscriber) {
                String lrc = LrcFile.getLrc(mContext, audio.title);
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

        String query = audio.title + (TextUtils.equals(audio.artist, "<unknown>") ? " " : " " + audio.artist);
        Observable<BLyric> id3v2Ob = Observable.create(new Observable.OnSubscribe<BLyric>() {
            @Override
            public void call(Subscriber<? super BLyric> subscriber) {
                try {
                    Mp3File mp3file = new Mp3File(audio.path);
                    if (mp3file.hasId3v2Tag()) {
                        String lyrics = mp3file.getId3v2Tag().getLyrics();
                        if (!TextUtils.isEmpty(lyrics)) {
                            BLyric bLyric = LrcParser.parseLrc(lyrics);
                            if (bLyric != null) {
                                LrcFile.saveLrc(mContext, audio.title, lyrics);
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
                        LrcFile.saveLrc(mContext, audio.title, lrc);
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

    public Observable<BaiduSearch> searchMusicByNet(String query) {
        return mPresenter.searchFromBaidu(query);
    }

    public Observable<BaiduMusicInfo> getMusicInfoByNet(String songId) {
        return mPresenter.getMusicInfo(songId);
    }
}
