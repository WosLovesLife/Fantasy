package com.wosloveslife.fantasy.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.album.AlbumFile;
import com.wosloveslife.fantasy.lrc.BLyric;
import com.wosloveslife.fantasy.lrc.LrcFile;
import com.wosloveslife.fantasy.lrc.LrcParser;
import com.wosloveslife.fantasy.presenter.MusicPresenter;
import com.yesing.blibrary_wos.utils.photo.BitmapUtils;

import java.io.File;

import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;

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
     * @param audioId    歌曲Id
     * @param bitmapSize 压缩后的大小,提高速度,避免OOM
     */
    public Observable<Bitmap> getAlbum(final String audioId, final int bitmapSize) {
        final Audio music = Realm.getDefaultInstance().where(Audio.class).equalTo("id", audioId).findFirst().clone();
        final Observable<Bitmap> fileOb = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                File albumFile = AlbumFile.getAlbumFile(mContext, music.getAlbum());
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
                    Mp3File mp3file = new Mp3File(music.getPath());
                    if (mp3file.hasId3v2Tag()) {
                        Bitmap bitmap = getAlbumFromID3v2(mp3file.getId3v2Tag(), bitmapSize);
                        if (bitmap != null) {
                            AlbumFile.saveAlbum(mContext, music.getAlbum(), bitmap);
                            subscriber.onNext(bitmap);
                        }
                    }
                } catch (Throwable e) {
                    Logger.w("从本地歌曲中解析封面失败,可忽略 error : " + e);
                }
                subscriber.onCompleted();
            }
        });
        return fileOb.switchIfEmpty(mp3Ob);
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

    public Observable<BLyric> getLrc(final String audioId) {
        final Audio audio = Realm.getDefaultInstance().where(Audio.class).equalTo("id", audioId).findFirst().clone();
        Observable<BLyric> fileOb = Observable.create(new Observable.OnSubscribe<BLyric>() {
            @Override
            public void call(Subscriber<? super BLyric> subscriber) {
                String lrc = LrcFile.getLrc(mContext, audio.getTitle());
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

        Observable<BLyric> id3v2Ob = Observable.create(new Observable.OnSubscribe<BLyric>() {
            @Override
            public void call(Subscriber<? super BLyric> subscriber) {
                try {
                    Mp3File mp3file = new Mp3File(audio.getPath());
                    if (mp3file.hasId3v2Tag()) {
                        String lyrics = mp3file.getId3v2Tag().getLyrics();
                        if (!TextUtils.isEmpty(lyrics)) {
                            BLyric bLyric = LrcParser.parseLrc(lyrics);
                            if (bLyric != null) {
                                LrcFile.saveLrc(mContext, audio.getTitle(), lyrics);
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

        return fileOb.switchIfEmpty(id3v2Ob);
    }
}
